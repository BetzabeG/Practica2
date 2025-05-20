package com.universidad.dto;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MateriaDTO implements Serializable {

    private Long id;

    @NotBlank(message = "El nombre de la materia es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombreMateria;

    @NotBlank(message = "El código único es obligatorio")
    @Pattern(regexp = "^[A-Z]{3}\\d{3}$", message = "El código debe tener formato de 3 letras mayúsculas seguidas de 3 números")
    private String codigoUnico;

    @NotNull(message = "Los créditos son obligatorios")
    @Min(value = 1, message = "La materia debe tener al menos 1 crédito")
    @Max(value = 10, message = "La materia no puede tener más de 10 créditos")
    private Integer creditos;

    @NotNull(message = "El ID del docente es obligatorio")
    private Long docenteId;

    @Size(max = 100, message = "El nombre del docente no puede exceder los 100 caracteres")
    private String nombreDocente; // Para mostrar en la UI

    @NotNull(message = "La lista de prerequisitos no puede ser nula")
    private List<Long> prerequisitos;

    @NotNull(message = "La lista de materias que dependen de esta no puede ser nula")
    private List<Long> esPrerequisitoDe;
}