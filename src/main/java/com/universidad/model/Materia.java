package com.universidad.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "materia")
public class Materia implements Serializable {

    private static final long serialVersionUID = 1L;

    public Materia(Long id, String nombreMateria, String codigoUnico) {
        this.id = id;
        this.nombreMateria = nombreMateria;
        this.codigoUnico = codigoUnico;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_materia")
    private Long id;

    @NotBlank(message = "El nombre de la materia es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(name = "nombre_materia", nullable = false, length = 100)
    private String nombreMateria;

    @NotBlank(message = "El código único es obligatorio")
    @Pattern(regexp = "^[A-Z]{3}\\d{3}$", message = "El código debe tener formato de 3 letras mayúsculas seguidas de 3 números")
    @Column(name = "codigo_unico", nullable = false, unique = true)
    private String codigoUnico;

    @NotNull(message = "Los créditos son obligatorios")
    @Min(value = 1, message = "La materia debe tener al menos 1 crédito")
    @Max(value = 10, message = "La materia no puede tener más de 10 créditos")
    @Column(name = "creditos", nullable = false)
    private Integer creditos;

    @Version
    private Long version;

    @ManyToMany
    @JoinTable(
        name = "materia_prerequisito",
        joinColumns = @JoinColumn(name = "id_materia"),
        inverseJoinColumns = @JoinColumn(name = "id_prerequisito")
    )
    private List<Materia> prerequisitos;

    // Para asignacion de docentes
    @ManyToOne
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @ManyToMany(mappedBy = "prerequisitos")
    private List<Materia> esPrerequisitoDe;

    public boolean formariaCirculo(Long prerequisitoId) {
        return formariaCirculoRecursivo(this.getId(), prerequisitoId, new java.util.HashSet<>());
    }

    private boolean formariaCirculoRecursivo(Long objetivoId, Long actualId, java.util.Set<Long> visitados) {
        if (objetivoId == null || actualId == null) return false;
        if (objetivoId.equals(actualId)) return true;
        if (!visitados.add(actualId)) return false;
        if (this.getPrerequisitos() == null) return false;

        for (Materia prereq : this.getPrerequisitos()) {
            if (prereq != null && prereq.getId() != null && prereq.getId().equals(actualId)) {
                if (prereq.getPrerequisitos() != null) {
                    for (Materia subPrereq : prereq.getPrerequisitos()) {
                        if (formariaCirculoRecursivo(objetivoId, subPrereq.getId(), visitados)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}