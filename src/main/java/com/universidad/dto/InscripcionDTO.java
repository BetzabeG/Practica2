package com.universidad.dto;
import com.universidad.model.EstadoInscripcion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionDTO implements Serializable {
    private Long id;
    private Long estudianteId;
    private String nombreEstudiante;
    private Long materiaId;
    private String nombreMateria;
    private String codigoMateria;
    private LocalDateTime fechaInscripcion;
    private String periodo;
    private EstadoInscripcion estado;
}