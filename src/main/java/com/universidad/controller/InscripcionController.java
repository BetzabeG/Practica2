package com.universidad.controller;

import com.universidad.dto.InscripcionDTO;
import com.universidad.model.EstadoInscripcion;
import com.universidad.service.IInscripcionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private static final Logger logger = LoggerFactory.getLogger(InscripcionController.class);
    private final IInscripcionService inscripcionService;

    @Autowired
    public InscripcionController(IInscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InscripcionDTO>> obtenerTodasLasInscripciones() {
        logger.info("[INSCRIPCION] Solicitud para obtener todas las inscripciones");
        List<InscripcionDTO> inscripciones = inscripcionService.obtenerTodasLasInscripciones();
        return ResponseEntity.ok(inscripciones);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCENTE') or (hasRole('ESTUDIANTE') and @inscripcionSeguridad.esInscripcionDeEstudiante(#id, authentication.principal.id))")
    public ResponseEntity<?> obtenerInscripcionPorId(@PathVariable Long id) {
        logger.info("[INSCRIPCION] Solicitud para obtener inscripción por ID: {}", id);
        try {
            InscripcionDTO inscripcion = inscripcionService.obtenerInscripcionPorId(id);
            return ResponseEntity.ok(inscripcion);
        } catch (IllegalArgumentException e) {
            logger.error("[INSCRIPCION] Error al obtener inscripción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("[INSCRIPCION] Error al obtener inscripción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener la inscripción");
        }
    }

    @GetMapping("/estudiante/{estudianteId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ESTUDIANTE') and #estudianteId == authentication.principal.id)")
    public ResponseEntity<List<InscripcionDTO>> obtenerInscripcionesPorEstudiante(@PathVariable Long estudianteId) {
        logger.info("[INSCRIPCION] Solicitud para obtener inscripciones de estudiante ID: {}", estudianteId);
        List<InscripcionDTO> inscripciones = inscripcionService.obtenerInscripcionesPorEstudiante(estudianteId);
        return ResponseEntity.ok(inscripciones);
    }

    @GetMapping("/materia/{materiaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCENTE')")
    public ResponseEntity<List<InscripcionDTO>> obtenerInscripcionesPorMateria(@PathVariable Long materiaId) {
        logger.info("[INSCRIPCION] Solicitud para obtener inscripciones de materia ID: {}", materiaId);
        List<InscripcionDTO> inscripciones = inscripcionService.obtenerInscripcionesPorMateria(materiaId);
        return ResponseEntity.ok(inscripciones);
    }

    @GetMapping("/estudiante/{estudianteId}/periodo/{periodo}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ESTUDIANTE') and #estudianteId == authentication.principal.id)")
    public ResponseEntity<List<InscripcionDTO>> obtenerInscripcionesPorEstudianteYPeriodo(
            @PathVariable Long estudianteId,
            @PathVariable String periodo) {
        logger.info("[INSCRIPCION] Solicitud para obtener inscripciones de estudiante ID: {} en periodo: {}", estudianteId, periodo);
        List<InscripcionDTO> inscripciones = inscripcionService.obtenerInscripcionesPorEstudianteYPeriodo(estudianteId, periodo);
        return ResponseEntity.ok(inscripciones);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ESTUDIANTE')")
    public ResponseEntity<?> crearInscripcion(@RequestBody Map<String, Object> datos) {
        try {
            Long estudianteId;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean esAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            // Si es estudiante, usar su propio ID
            if (!esAdmin) {
                // Obtener ID del estudiante autenticado
                estudianteId = Long.parseLong(auth.getName()); // Ajustar según implementación de seguridad
            } else {
                // Si es admin, usar el ID proporcionado
                estudianteId = Long.parseLong(datos.get("estudianteId").toString());
            }

            Long materiaId = Long.parseLong(datos.get("materiaId").toString());
            String periodo = datos.get("periodo").toString();

            logger.info("[INSCRIPCION] Creando inscripción para estudiante: {}, materia: {}, periodo: {}",
                    estudianteId, materiaId, periodo);

            InscripcionDTO inscripcion = inscripcionService.crearInscripcion(estudianteId, materiaId, periodo);
            return ResponseEntity.status(HttpStatus.CREATED).body(inscripcion);
        } catch (IllegalArgumentException e) {
            logger.error("[INSCRIPCION] Error al crear inscripción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("[INSCRIPCION] Error al crear inscripción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la inscripción: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCENTE')")
    public ResponseEntity<?> actualizarEstadoInscripcion(
            @PathVariable Long id,
            @RequestBody Map<String, String> datos) {
        try {
            EstadoInscripcion nuevoEstado = EstadoInscripcion.valueOf(datos.get("estado"));
            logger.info("[INSCRIPCION] Actualizando estado de inscripción ID: {} a: {}", id, nuevoEstado);

            InscripcionDTO inscripcion = inscripcionService.actualizarEstadoInscripcion(id, nuevoEstado);
            return ResponseEntity.ok(inscripcion);
        } catch (IllegalArgumentException e) {
            logger.error("[INSCRIPCION] Error al actualizar estado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("[INSCRIPCION] Error al actualizar estado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el estado de la inscripción: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ESTUDIANTE') and @inscripcionSeguridad.esInscripcionDeEstudiante(#id, authentication.principal.id))")
    public ResponseEntity<?> eliminarInscripcion(@PathVariable Long id) {
        try {
            logger.info("[INSCRIPCION] Eliminando inscripción ID: {}", id);
            inscripcionService.eliminarInscripcion(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("[INSCRIPCION] Error al eliminar inscripción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("[INSCRIPCION] Error al eliminar inscripción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la inscripción: " + e.getMessage());
        }
    }
}