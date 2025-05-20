package com.universidad.service;

import com.universidad.dto.InscripcionDTO;
import com.universidad.model.EstadoInscripcion;

import java.util.List;

public interface IInscripcionService {

    // Métodos básicos CRUD
    InscripcionDTO crearInscripcion(Long estudianteId, Long materiaId, String periodo);

    InscripcionDTO obtenerInscripcionPorId(Long id);

    List<InscripcionDTO> obtenerTodasLasInscripciones();

    List<InscripcionDTO> obtenerInscripcionesPorEstudiante(Long estudianteId);

    List<InscripcionDTO> obtenerInscripcionesPorMateria(Long materiaId);

    List<InscripcionDTO> obtenerInscripcionesPorEstudianteYPeriodo(Long estudianteId, String periodo);

    InscripcionDTO actualizarEstadoInscripcion(Long inscripcionId, EstadoInscripcion nuevoEstado);

    void eliminarInscripcion(Long inscripcionId);

    // Métodos de validación
    boolean verificarPrerequisitos(Long estudianteId, Long materiaId);

    boolean verificarCupoDisponible(Long materiaId);

    boolean verificarLimiteInscripciones(Long estudianteId, String periodo, int limiteMaximo);
}