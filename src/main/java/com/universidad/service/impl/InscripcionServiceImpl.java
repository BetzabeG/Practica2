package com.universidad.service.impl;

import com.universidad.dto.InscripcionDTO;
import com.universidad.dto.MateriaDTO;
import com.universidad.model.EstadoInscripcion;
import com.universidad.model.Inscripcion;
import com.universidad.model.Materia;
import com.universidad.model.Estudiante;
import com.universidad.repository.InscripcionRepository;
import com.universidad.repository.MateriaRepository;
import com.universidad.repository.EstudianteRepository;
import com.universidad.service.IInscripcionService;
import com.universidad.service.IMateriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class InscripcionServiceImpl implements IInscripcionService {

    private static final Logger logger = LoggerFactory.getLogger(InscripcionServiceImpl.class);
    private static final int LIMITE_INSCRIPCIONES_DEFAULT = 7; // Máximo de materias por periodo

    private final InscripcionRepository inscripcionRepository;
    private final EstudianteRepository usuarioRepository;
    private final MateriaRepository materiaRepository;
    private final IMateriaService materiaService;

    @Autowired
    public InscripcionServiceImpl(
            InscripcionRepository inscripcionRepository,
            EstudianteRepository usuarioRepository,
            MateriaRepository materiaRepository,
            IMateriaService materiaService) {
        this.inscripcionRepository = inscripcionRepository;
        this.usuarioRepository = usuarioRepository;
        this.materiaRepository = materiaRepository;
        this.materiaService = materiaService;
    }

    @Override
    @Transactional
    public InscripcionDTO crearInscripcion(Long estudianteId, Long materiaId, String periodo) {
        logger.info("[INSCRIPCION] Iniciando creación de inscripción para estudiante {} en materia {}", estudianteId, materiaId);

        // Verificar si el estudiante existe
        EstudianteRepository estudianteRepository;
        Estudiante estudiante = usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));



        // Verificar si la materia existe
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new IllegalArgumentException("Materia no encontrada"));

        // Verificar si ya existe la inscripción
        if (inscripcionRepository.existsByEstudianteIdAndMateriaIdAndPeriodo(estudianteId, materiaId, periodo)) {
            throw new IllegalArgumentException("Ya existe una inscripción para esta materia en este período");
        }

        // Verificar prerrequisitos
        if (!verificarPrerequisitos(estudianteId, materiaId)) {
            throw new IllegalArgumentException("El estudiante no cumple con los prerrequisitos necesarios para esta materia");
        }

        // Verificar cupo disponible (implementación pendiente)
        if (!verificarCupoDisponible(materiaId)) {
            throw new IllegalArgumentException("No hay cupos disponibles para esta materia");
        }

        // Verificar límite de inscripciones
        if (!verificarLimiteInscripciones(estudianteId, periodo, LIMITE_INSCRIPCIONES_DEFAULT)) {
            throw new IllegalArgumentException("El estudiante ha alcanzado el límite de inscripciones para este período");
        }

        // Crear y guardar la inscripción
        Inscripcion inscripcion = new Inscripcion(estudiante, materia, periodo);
        inscripcion = inscripcionRepository.save(inscripcion);

        logger.info("[INSCRIPCION] Inscripción creada exitosamente con ID: {}", inscripcion.getId());

        return convertirADTO(inscripcion);
    }

    @Override
    public InscripcionDTO obtenerInscripcionPorId(Long id) {
        logger.info("[INSCRIPCION] Buscando inscripción con ID: {}", id);
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));
        return convertirADTO(inscripcion);
    }

    @Override
    public List<InscripcionDTO> obtenerTodasLasInscripciones() {
        logger.info("[INSCRIPCION] Obteniendo todas las inscripciones");
        return inscripcionRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InscripcionDTO> obtenerInscripcionesPorEstudiante(Long estudianteId) {
        logger.info("[INSCRIPCION] Obteniendo inscripciones para estudiante ID: {}", estudianteId);
        return inscripcionRepository.findByEstudianteId(estudianteId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InscripcionDTO> obtenerInscripcionesPorMateria(Long materiaId) {
        logger.info("[INSCRIPCION] Obteniendo inscripciones para materia ID: {}", materiaId);
        return inscripcionRepository.findByMateriaId(materiaId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InscripcionDTO> obtenerInscripcionesPorEstudianteYPeriodo(Long estudianteId, String periodo) {
        logger.info("[INSCRIPCION] Obteniendo inscripciones para estudiante ID: {} en periodo: {}", estudianteId, periodo);
        return inscripcionRepository.findByEstudianteIdAndPeriodo(estudianteId, periodo).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InscripcionDTO actualizarEstadoInscripcion(Long inscripcionId, EstadoInscripcion nuevoEstado) {
        logger.info("[INSCRIPCION] Actualizando estado de inscripción ID: {} a: {}", inscripcionId, nuevoEstado);
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        inscripcion.setEstado(nuevoEstado);
        inscripcion = inscripcionRepository.save(inscripcion);

        return convertirADTO(inscripcion);
    }

    @Override
    @Transactional
    public void eliminarInscripcion(Long inscripcionId) {
        logger.info("[INSCRIPCION] Eliminando inscripción ID: {}", inscripcionId);
        if (!inscripcionRepository.existsById(inscripcionId)) {
            throw new IllegalArgumentException("Inscripción no encontrada");
        }
        inscripcionRepository.deleteById(inscripcionId);
    }
    
    @Override
    public boolean verificarPrerequisitos(Long estudianteId, Long materiaId) {
        logger.info("[INSCRIPCION] Verificando prerrequisitos para estudiante {} en materia {}", estudianteId, materiaId);

        // Obtener la materia y sus prerrequisitos
        MateriaDTO materia = materiaService.obtenerMateriaPorId(materiaId);
        if (materia.getPrerequisitos() == null || materia.getPrerequisitos().isEmpty()) {
            return true; // No tiene prerrequisitos
        }

        // Obtener las materias aprobadas por el estudiante (suponiendo que solo las APROBADAS cuentan)
        List<Inscripcion> inscripcionesAprobadas = inscripcionRepository.findByEstudianteId(estudianteId).stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.APROBADA)
                .collect(Collectors.toList());

        List<Long> materiasAprobadasIds = inscripcionesAprobadas.stream()
                .map(i -> i.getMateria().getId())
                .collect(Collectors.toList());

        // Verificar si todas las materias prerrequisito están en las aprobadas
        return materia.getPrerequisitos().stream()
                .allMatch(prerequisito -> materiasAprobadasIds.contains(prerequisito.getClass()));
    }

    @Override
    public boolean verificarCupoDisponible(Long materiaId) {
        return true;
    }

    @Override
    public boolean verificarLimiteInscripciones(Long estudianteId, String periodo, int limiteMaximo) {
        int cantidadInscripciones = inscripcionRepository.countInscripcionesPorEstudianteYPeriodo(estudianteId, periodo);
        return cantidadInscripciones < limiteMaximo;
    }

    // Método auxiliar para convertir entidad a DTO
    private InscripcionDTO convertirADTO(Inscripcion inscripcion) {
        InscripcionDTO dto = new InscripcionDTO();
        dto.setId(inscripcion.getId());
        dto.setEstudianteId(inscripcion.getEstudiante().getId());
        dto.setNombreEstudiante(inscripcion.getEstudiante().getNombre() + " " + inscripcion.getEstudiante().getApellido());
        dto.setMateriaId(inscripcion.getMateria().getId());
        dto.setNombreMateria(inscripcion.getMateria().getNombreMateria());
        dto.setCodigoMateria(inscripcion.getMateria().getCodigoUnico());
        dto.setFechaInscripcion(inscripcion.getFechaInscripcion());
        dto.setPeriodo(inscripcion.getPeriodo());
        dto.setEstado(inscripcion.getEstado());
        return dto;
    }
}