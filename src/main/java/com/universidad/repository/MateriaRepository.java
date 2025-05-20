package com.universidad.repository;

import com.universidad.model.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Long> {
    // Método existente
    Materia findByCodigoUnico(String codigoUnico);

    // Posiblemente deberías manejar el caso null aquí también
    Optional<Materia> findOptionalByCodigoUnico(String codigoUnico);

    // Verificar si existe una materia con este código
    boolean existsByCodigoUnico(String codigoUnico);

    @Lock(LockModeType.PESSIMISTIC_WRITE) // Bloqueo pesimista para evitar condiciones de carrera
    Optional<Materia> findById(Long id);
    /*
    // Métodos adicionales útiles
    @Query("SELECT m FROM Materia m WHERE m.creditos >= :minCreditos")
    List<Materia> findByMinimumCredits(int minCreditos);

    // Buscar materias sin prerequisitos (útil para primer semestre)
    @Query("SELECT m FROM Materia m WHERE m.prerequisitos IS EMPTY OR m.prerequisitos IS NULL")
    List<Materia> findMateriasWithoutPrerequisites();*/

    List<Materia> findByDocenteId(Long docenteId);

    long countByDocenteId(Long docenteId);
/*
    @Query("SELECT m FROM Materia m WHERE m.docente IS NULL")
    List<Materia> findMateriasNoAsignadas();*/
}