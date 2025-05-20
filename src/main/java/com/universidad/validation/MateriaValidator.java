package com.universidad.validation;

import org.springframework.stereotype.Component;
import com.universidad.dto.MateriaDTO;
import com.universidad.repository.MateriaRepository;
import com.universidad.repository.DocenteRepository;

@Component
public class MateriaValidator {

    private final MateriaRepository materiaRepository;
    private final DocenteRepository docenteRepository;

    public MateriaValidator(MateriaRepository materiaRepository, DocenteRepository docenteRepository) {
        this.materiaRepository = materiaRepository;
        this.docenteRepository = docenteRepository;
    }

    public void validarCodigoUnico(String codigo) {
        if (materiaRepository.existsByCodigoUnico(codigo)) {
            throw new IllegalArgumentException("Ya existe una materia con este código único");
        }
    }

    public void validarFormatoCodigoUnico(String codigo) {
        if (!codigo.matches("^[A-Z]{3}\\d{3}$")) {
            throw new IllegalArgumentException("El código debe tener formato de 3 letras mayúsculas seguidas de 3 números");
        }
    }

    public void validarCreditos(Integer creditos) {
        if (creditos == null || creditos < 1 || creditos > 10) {
            throw new IllegalArgumentException("Los créditos deben estar entre 1 y 10");
        }
    }

    public void validarAsignacionDocente(Long docenteId) {
        if (!docenteRepository.existsById(docenteId)) {
            throw new IllegalArgumentException("El docente no existe en la base de datos");
        }
    }

    public void validarCargaDocente(Long docenteId) {
        // Verificar si el docente ya tiene demasiadas materias asignadas
        long materiasAsignadas = materiaRepository.countByDocenteId(docenteId);
        if (materiasAsignadas >= 4) {
            throw new IllegalArgumentException("El docente ya tiene el máximo de 4 materias asignadas");
        }
    }

    public void validacionCompletaMateria(MateriaDTO materia) {
        validarFormatoCodigoUnico(materia.getCodigoUnico());
        validarCreditos(materia.getCreditos());
        // Otras validaciones
    }
}