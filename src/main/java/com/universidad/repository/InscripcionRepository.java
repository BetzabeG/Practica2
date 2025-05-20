package com.universidad.repository;

import com.universidad.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    List<Inscripcion> findByEstudianteId(Long estudianteId);

    List<Inscripcion> findByMateriaId(Long materiaId);

    List<Inscripcion> findByEstudianteIdAndPeriodo(Long estudianteId, String periodo);

    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.estudiante.id = :estudianteId AND i.periodo = :periodo")
    int countInscripcionesPorEstudianteYPeriodo(@Param("estudianteId") Long estudianteId, @Param("periodo") String periodo);

    Optional<Inscripcion> findByEstudianteIdAndMateriaIdAndPeriodo(Long estudianteId, Long materiaId, String periodo);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Inscripcion i " +
           "WHERE i.estudiante.id = :estudianteId AND i.materia.id = :materiaId AND i.periodo = :periodo")
    boolean existsByEstudianteIdAndMateriaIdAndPeriodo(@Param("estudianteId") Long estudianteId,
                                                     @Param("materiaId") Long materiaId,
                                                     @Param("periodo") String periodo);
}