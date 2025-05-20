package com.universidad.controller;

import com.universidad.model.Materia;
import com.universidad.service.IMateriaService;
import jakarta.transaction.Transactional;
import com.universidad.dto.MateriaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/materias")
public class MateriaController {

    private final IMateriaService materiaService;
    private static final Logger logger = LoggerFactory.getLogger(MateriaController.class);

    @Autowired
    public MateriaController(IMateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    public ResponseEntity<List<MateriaDTO>> obtenerTodasLasMaterias() {
        long inicio = System.currentTimeMillis();
        logger.info("[MATERIA] Inicio obtenerTodasLasMaterias: {}", inicio);
        List<MateriaDTO> result = materiaService.obtenerTodasLasMaterias();
        long fin = System.currentTimeMillis();
        logger.info("[MATERIA] Fin obtenerTodasLasMaterias: {} (Duracion: {} ms)", fin, (fin-inicio));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MateriaDTO> obtenerMateriaPorId(@PathVariable Long id) {
        long inicio = System.currentTimeMillis();
        logger.info("[MATERIA] Inicio obtenerMateriaPorId: {}", inicio);
        try {
            MateriaDTO materia = materiaService.obtenerMateriaPorId(id);
            long fin = System.currentTimeMillis();
            logger.info("[MATERIA] Fin obtenerMateriaPorId: {} (Duracion: {} ms)", fin, (fin-inicio));
            return ResponseEntity.ok(materia);
        } catch (Exception e) {
            logger.error("[MATERIA] Error en obtenerMateriaPorId: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/codigo/{codigoUnico}")
    public ResponseEntity<MateriaDTO> obtenerMateriaPorCodigoUnico(@PathVariable String codigoUnico) {
        try {
            MateriaDTO materia = materiaService.obtenerMateriaPorCodigoUnico(codigoUnico);
            return ResponseEntity.ok(materia);
        } catch (Exception e) {
            logger.error("[MATERIA] Error en obtenerMateriaPorCodigoUnico: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> crearMateria(@RequestBody MateriaDTO materia) {
        try {
            MateriaDTO nueva = materiaService.crearMateria(materia);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
        } catch (IllegalArgumentException e) {
            logger.error("[MATERIA] Error al crear materia: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("[MATERIA] Error al crear materia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear la materia: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarMateria(@PathVariable Long id, @RequestBody MateriaDTO materia) {
        try {
            MateriaDTO actualizadaDTO = materiaService.actualizarMateria(id, materia);
            return ResponseEntity.ok(actualizadaDTO);
        } catch (IllegalArgumentException e) {
            logger.error("[MATERIA] Error al actualizar materia: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("[MATERIA] Error al actualizar materia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar la materia: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMateria(@PathVariable Long id) {
        try {
            materiaService.eliminarMateria(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            // Error específico cuando la materia es prerrequisito de otras
            logger.error("[MATERIA] Error al eliminar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("[MATERIA] Error al eliminar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la materia: " + e.getMessage());
        }
    }

    @GetMapping("/formaria-circulo/{materiaId}/{prerequisitoId}")
    @Transactional
    public ResponseEntity<Boolean> formariaCirculo(@PathVariable Long materiaId, @PathVariable Long prerequisitoId) {
        try {
            MateriaDTO materiaDTO = materiaService.obtenerMateriaPorId(materiaId);
            Materia materia = new Materia(materiaDTO.getId(), materiaDTO.getNombreMateria(), materiaDTO.getCodigoUnico());
            boolean circulo = materia.formariaCirculo(prerequisitoId);
            if (circulo) {
                return ResponseEntity.badRequest().body(circulo);
            }
            return ResponseEntity.ok(circulo);
        } catch (Exception e) {
            logger.error("[MATERIA] Error en formariaCirculo: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // NUEVOS ENDPOINTS PARA GESTIÓN DE DOCENTES

    @PostMapping("/{materiaId}/docentes/{docenteId}")
    public ResponseEntity<?> asignarDocente(@PathVariable Long materiaId, @PathVariable Long docenteId) {
        try {
            MateriaDTO materia = materiaService.asignarDocente(materiaId, docenteId);
            return ResponseEntity.ok(materia);
        } catch (IllegalArgumentException e) {
            logger.error("[MATERIA] Error al asignar docente: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("[MATERIA] Error al asignar docente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al asignar docente: " + e.getMessage());
        }
    }

    @DeleteMapping("/{materiaId}/docentes")
    public ResponseEntity<?> desasignarDocente(@PathVariable Long materiaId) {
        try {
            MateriaDTO materia = materiaService.eliminarAsignacionDocente(materiaId);
            return ResponseEntity.ok(materia);
        } catch (Exception e) {
            logger.error("[MATERIA] Error al desasignar docente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al desasignar docente: " + e.getMessage());
        }
    }

    @GetMapping("/docentes/{docenteId}")
    public ResponseEntity<List<MateriaDTO>> obtenerMateriasPorDocente(@PathVariable Long docenteId) {
        try {
            List<MateriaDTO> materias = materiaService.obtenerMateriasPorDocente(docenteId);
            return ResponseEntity.ok(materias);
        } catch (Exception e) {
            logger.error("[MATERIA] Error al obtener materias por docente: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ENDPOINTS PARA GESTIÓN DE PREREQUISITOS

    @PostMapping("/{materiaId}/prerequisitos/{prerequisitoId}")
    public ResponseEntity<?> agregarPrerequisito(@PathVariable Long materiaId, @PathVariable Long prerequisitoId) {
        try {
            MateriaDTO materia = materiaService.agregarPrerequisito(materiaId, prerequisitoId);
            return ResponseEntity.ok(materia);
        } catch (IllegalArgumentException e) {
            logger.error("[MATERIA] Error al agregar prerequisito: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("[MATERIA] Error al agregar prerequisito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al agregar prerequisito: " + e.getMessage());
        }
    }

    @DeleteMapping("/{materiaId}/prerequisitos/{prerequisitoId}")
    public ResponseEntity<?> eliminarPrerequisito(@PathVariable Long materiaId, @PathVariable Long prerequisitoId) {
        try {
            MateriaDTO materia = materiaService.eliminarPrerequisito(materiaId, prerequisitoId);
            return ResponseEntity.ok(materia);
        } catch (Exception e) {
            logger.error("[MATERIA] Error al eliminar prerequisito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar prerequisito: " + e.getMessage());
        }
    }
}