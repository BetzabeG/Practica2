package com.universidad.service.impl;

import com.universidad.dto.MateriaDTO;
import com.universidad.exception.RecursoNoDisponibleException;
import com.universidad.model.Docente;
import com.universidad.model.Materia;
import com.universidad.repository.DocenteRepository;
import com.universidad.repository.MateriaRepository;
import com.universidad.service.IMateriaService;
import com.universidad.validation.MateriaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MateriaServiceImpl implements IMateriaService {

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private MateriaValidator materiaValidator;

    // Método utilitario para mapear Materia a MateriaDTO
    private MateriaDTO mapToDTO(Materia materia) {
        if (materia == null) return null;

        MateriaDTO dto = MateriaDTO.builder()
                .id(materia.getId())
                .nombreMateria(materia.getNombreMateria())
                .codigoUnico(materia.getCodigoUnico())
                .creditos(materia.getCreditos())
                .prerequisitos(materia.getPrerequisitos() != null ?
                    materia.getPrerequisitos().stream().map(Materia::getId).collect(Collectors.toList()) : new ArrayList<>())
                .esPrerequisitoDe(materia.getEsPrerequisitoDe() != null ?
                    materia.getEsPrerequisitoDe().stream().map(Materia::getId).collect(Collectors.toList()) : new ArrayList<>())
                .build();

        // Agregar información del docente si existe
        if (materia.getDocente() != null) {
            dto.setDocenteId(materia.getDocente().getId());
            dto.setNombreDocente(materia.getDocente().getNombre() + " " + materia.getDocente().getApellido());
        }

        return dto;
    }

    // Método para mapear DTO a Entidad
    private Materia mapToEntity(MateriaDTO dto, boolean isUpdate) {
        Materia materia;

        if (isUpdate && dto.getId() != null) {
            materia = materiaRepository.findById(dto.getId())
                .orElseThrow(() -> new RecursoNoDisponibleException("Materia no encontrada con ID: " + dto.getId()));
        } else {
            materia = new Materia();
        }

        materia.setNombreMateria(dto.getNombreMateria());
        materia.setCodigoUnico(dto.getCodigoUnico());
        materia.setCreditos(dto.getCreditos());

        return materia;
    }

    @Override
    @Cacheable(value = "materias")
    @Transactional(readOnly = true)
    public List<MateriaDTO> obtenerTodasLasMaterias() {
        return materiaRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "materia", key = "#id")
    @Transactional(readOnly = true)
    public MateriaDTO obtenerMateriaPorId(Long id) {
        return materiaRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RecursoNoDisponibleException("Materia no encontrada con ID: " + id));
    }

    @Override
    @Cacheable(value = "materia", key = "#codigoUnico")
    @Transactional(readOnly = true)
    public MateriaDTO obtenerMateriaPorCodigoUnico(String codigoUnico) {
        Materia materia = materiaRepository.findOptionalByCodigoUnico(codigoUnico)
                .orElseThrow(() -> new RecursoNoDisponibleException("Materia no encontrada con código: " + codigoUnico));
        return mapToDTO(materia);
    }

    @Override
    @CachePut(value = "materia", key = "#result.id")
    @CacheEvict(value = "materias", allEntries = true)
    @Transactional
    public MateriaDTO crearMateria(MateriaDTO materiaDTO) {
        // Validar datos
        materiaValidator.validacionCompletaMateria(materiaDTO);

        // Verificar si ya existe el código
        if (materiaRepository.existsByCodigoUnico(materiaDTO.getCodigoUnico())) {
            throw new IllegalArgumentException("Ya existe una materia con el código: " + materiaDTO.getCodigoUnico());
        }

        // Mapear y guardar
        Materia materia = mapToEntity(materiaDTO, false);
        Materia savedMateria = materiaRepository.save(materia);

        // Procesar docente si existe
        if (materiaDTO.getDocenteId() != null) {
            asignarDocente(savedMateria.getId(), materiaDTO.getDocenteId());
            // Refrescar después de asignar docente
            savedMateria = materiaRepository.findById(savedMateria.getId()).orElseThrow();
        }

        // Procesar prerequisitos si existen
        if (materiaDTO.getPrerequisitos() != null && !materiaDTO.getPrerequisitos().isEmpty()) {
            procesarPrerequisitos(savedMateria, materiaDTO.getPrerequisitos());
            // Refrescar después de asignar prerequisitos
            savedMateria = materiaRepository.findById(savedMateria.getId()).orElseThrow();
        }

        return mapToDTO(savedMateria);
    }

    @Override
    @CachePut(value = "materia", key = "#id")
    @CacheEvict(value = "materias", allEntries = true)
    @Transactional
    public MateriaDTO actualizarMateria(Long id, MateriaDTO materiaDTO) {
        // Validar que la materia existe
        Materia materiaExistente = materiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoDisponibleException("Materia no encontrada con ID: " + id));

        // Validar datos
        materiaValidator.validacionCompletaMateria(materiaDTO);

        // Verificar si el código ya existe en otra materia
        if (!materiaExistente.getCodigoUnico().equals(materiaDTO.getCodigoUnico()) &&
                materiaRepository.existsByCodigoUnico(materiaDTO.getCodigoUnico())) {
            throw new IllegalArgumentException("Ya existe otra materia con el código: " + materiaDTO.getCodigoUnico());
        }

        // Actualizar campos básicos
        materiaExistente.setNombreMateria(materiaDTO.getNombreMateria());
        materiaExistente.setCodigoUnico(materiaDTO.getCodigoUnico());
        materiaExistente.setCreditos(materiaDTO.getCreditos());

        // Actualizar docente si cambió
        if (materiaDTO.getDocenteId() != null) {
            if (materiaExistente.getDocente() == null ||
                !materiaExistente.getDocente().getId().equals(materiaDTO.getDocenteId())) {
                asignarDocente(id, materiaDTO.getDocenteId());
            }
        } else if (materiaExistente.getDocente() != null) {
            // Eliminar asignación de docente si ahora es nulo
            eliminarAsignacionDocente(id);
        }

        // Guardar cambios básicos
        Materia updatedMateria = materiaRepository.save(materiaExistente);

        // Actualizar prerrequisitos si están presentes
        if (materiaDTO.getPrerequisitos() != null) {
            procesarPrerequisitos(updatedMateria, materiaDTO.getPrerequisitos());
        }

        // Recuperar la materia actualizada con todas sus relaciones
        updatedMateria = materiaRepository.findById(id).orElseThrow();
        return mapToDTO(updatedMateria);
    }

    @Override
    @CacheEvict(value = {"materia", "materias"}, allEntries = true)
    @Transactional
    public void eliminarMateria(Long id) {
        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoDisponibleException("Materia no encontrada con ID: " + id));

        // Verificar si es prerrequisito de otras materias
        if (materia.getEsPrerequisitoDe() != null && !materia.getEsPrerequisitoDe().isEmpty()) {
            throw new IllegalStateException(
                "No se puede eliminar la materia porque es prerrequisito de otras materias");
        }

        // Eliminar referencias de prerrequisitos
        if (materia.getPrerequisitos() != null && !materia.getPrerequisitos().isEmpty()) {
            materia.getPrerequisitos().clear();
            materiaRepository.save(materia);
        }

        materiaRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CachePut(value = "materia", key = "#materiaId")
    @CacheEvict(value = "materias", allEntries = true)
    public MateriaDTO asignarDocente(Long materiaId, Long docenteId) {
        // Validar que ambos existan
        Materia materia = materiaRepository.findById(materiaId)
            .orElseThrow(() -> new RecursoNoDisponibleException("Materia no encontrada: " + materiaId));

        materiaValidator.validarAsignacionDocente(docenteId);
        materiaValidator.validarCargaDocente(docenteId);

        Docente docente = docenteRepository.findById(docenteId)
            .orElseThrow(() -> new RecursoNoDisponibleException("Docente no encontrado: " + docenteId));

        // Asignar docente
        materia.setDocente(docente);
        Materia materiaActualizada = materiaRepository.save(materia);

        return mapToDTO(materiaActualizada);
    }

    @Override
    @Transactional
    @CachePut(value = "materia", key = "#materiaId")
    @CacheEvict(value = "materias", allEntries = true)
    public MateriaDTO eliminarAsignacionDocente(Long materiaId) {
        Materia materia = materiaRepository.findById(materiaId)
            .orElseThrow(() -> new RecursoNoDisponibleException("Materia no encontrada: " + materiaId));

        materia.setDocente(null);
        Materia materiaActualizada = materiaRepository.save(materia);

        return mapToDTO(materiaActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "materiasPorDocente", key = "#docenteId")
    public List<MateriaDTO> obtenerMateriasPorDocente(Long docenteId) {
        // Verificar si el docente existe
        if (!docenteRepository.existsById(docenteId)) {
            throw new RecursoNoDisponibleException("Docente no encontrado: " + docenteId);
        }

        List<Materia> materias = materiaRepository.findByDocenteId(docenteId);
        return materias.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeMateriaPorId(Long id) {
        return materiaRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeMateriaPorCodigoUnico(String codigoUnico) {
        return materiaRepository.existsByCodigoUnico(codigoUnico);
    }

    @Override
    @Transactional
    public MateriaDTO agregarPrerequisito(Long materiaId, Long prerequisitoId) {
        // Verificar que ambas materias existan
        Materia materia = materiaRepository.findById(materiaId)
            .orElseThrow(() -> new RecursoNoDisponibleException("Materia no encontrada: " + materiaId));

        Materia prerequisito = materiaRepository.findById(prerequisitoId)
            .orElseThrow(() -> new RecursoNoDisponibleException("Prerequisito no encontrado: " + prerequisitoId));

        // Validaciones
        if (materiaId.equals(prerequisitoId)) {
            throw new IllegalArgumentException("Una materia no puede ser prerequisito de sí misma");
        }

        // Verificar ciclos
        if (materia.getPrerequisitos().contains(prerequisito)) {
            throw new IllegalArgumentException("Esta materia ya tiene este prerequisito");
        }

        // Agregar prerrequisito
        materia.getPrerequisitos().add(prerequisito);
        prerequisito.getEsPrerequisitoDe().add(materia);

        materiaRepository.save(materia);
        materiaRepository.save(prerequisito);

        return mapToDTO(materia);
    }

    @Override
    @Transactional
    public MateriaDTO eliminarPrerequisito(Long materiaId, Long prerequisitoId) {
        Materia materia = materiaRepository.findById(materiaId)
            .orElseThrow(() -> new RecursoNoDisponibleException("Materia no encontrada: " + materiaId));

        Materia prerequisito = materiaRepository.findById(prerequisitoId)
            .orElseThrow(() -> new RecursoNoDisponibleException("Prerequisito no encontrado: " + prerequisitoId));

        // Eliminar la relación
        materia.getPrerequisitos().remove(prerequisito);
        prerequisito.getEsPrerequisitoDe().remove(materia);

        materiaRepository.save(materia);
        materiaRepository.save(prerequisito);

        return mapToDTO(materia);
    }

    // Método privado para procesar los prerrequisitos
    private void procesarPrerequisitos(Materia materia, List<Long> prerequisitosIds) {
        if (materia.getPrerequisitos() == null) {
            materia.setPrerequisitos(new ArrayList<>());
        } else {
            materia.getPrerequisitos().clear();
        }

        if (prerequisitosIds == null || prerequisitosIds.isEmpty()) {
            materiaRepository.save(materia);
            return;
        }

        for (Long prereqId : prerequisitosIds) {
            // Evitar auto-referencia
            if (prereqId.equals(materia.getId())) {
                throw new IllegalArgumentException("Una materia no puede ser prerrequisito de sí misma");
            }

            Materia prerequisito = materiaRepository.findById(prereqId)
                .orElseThrow(() -> new RecursoNoDisponibleException("Prerrequisito no encontrado: " + prereqId));

            materia.getPrerequisitos().add(prerequisito);

            if (prerequisito.getEsPrerequisitoDe() == null) {
                prerequisito.setEsPrerequisitoDe(new ArrayList<>());
            }

            if (!prerequisito.getEsPrerequisitoDe().contains(materia)) {
                prerequisito.getEsPrerequisitoDe().add(materia);
                materiaRepository.save(prerequisito);
            }
        }

        materiaRepository.save(materia);
    }
}