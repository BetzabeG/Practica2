package com.universidad.service;

import com.universidad.dto.MateriaDTO;
import java.util.List;

/**
 * Interfaz que define los servicios para la gestión de materias.
 */
public interface IMateriaService {
    /**
     * Recupera todas las materias del sistema.
     * @return Lista de todas las materias convertidas a DTO
     */
    List<MateriaDTO> obtenerTodasLasMaterias();

    /**
     * Busca una materia por su identificador único.
     * @param id El identificador de la materia
     * @return DTO con la información de la materia
     */
    MateriaDTO obtenerMateriaPorId(Long id);

    /**
     * Busca una materia por su código único.
     * @param codigoUnico El código único de la materia
     * @return DTO con la información de la materia
     */
    MateriaDTO obtenerMateriaPorCodigoUnico(String codigoUnico);

    /**
     * Crea una nueva materia en el sistema.
     * @param materiaDTO Datos de la nueva materia
     * @return DTO con la información de la materia creada
     */
    MateriaDTO crearMateria(MateriaDTO materiaDTO);

    /**
     * Actualiza los datos de una materia existente.
     * @param id Identificador de la materia a actualizar
     * @param materiaDTO Nuevos datos de la materia
     * @return DTO con la información de la materia actualizada
     */
    MateriaDTO actualizarMateria(Long id, MateriaDTO materiaDTO);

    /**
     * Elimina una materia del sistema.
     * @param id Identificador de la materia a eliminar
     */
    void eliminarMateria(Long id);

    /**
     * Asigna un docente a una materia.
     * @param materiaId ID de la materia
     * @param docenteId ID del docente a asignar
     * @return DTO con la información actualizada de la materia
     */
    MateriaDTO asignarDocente(Long materiaId, Long docenteId);

    /**
     * Elimina la asignación de un docente a una materia.
     * @param materiaId ID de la materia
     * @return DTO con la información actualizada de la materia
     */
    MateriaDTO eliminarAsignacionDocente(Long materiaId);

    /**
     * Recupera todas las materias asignadas a un docente.
     * @param docenteId ID del docente
     * @return Lista de materias asignadas al docente
     */
    List<MateriaDTO> obtenerMateriasPorDocente(Long docenteId);

    /**
     * Verifica si una materia existe por su ID.
     * @param id El identificador de la materia
     * @return true si existe, false si no
     */
    boolean existeMateriaPorId(Long id);

    /**
     * Verifica si una materia existe por su código único.
     * @param codigoUnico El código único de la materia
     * @return true si existe, false si no
     */
    boolean existeMateriaPorCodigoUnico(String codigoUnico);

    /**
     * Agrega un prerrequisito a una materia.
     * @param materiaId ID de la materia principal
     * @param prerequisitoId ID de la materia a agregar como prerrequisito
     * @return DTO con la información actualizada de la materia
     */
    MateriaDTO agregarPrerequisito(Long materiaId, Long prerequisitoId);

    /**
     * Elimina un prerrequisito de una materia.
     * @param materiaId ID de la materia principal
     * @param prerequisitoId ID del prerrequisito a eliminar
     * @return DTO con la información actualizada de la materia
     */
    MateriaDTO eliminarPrerequisito(Long materiaId, Long prerequisitoId);
}