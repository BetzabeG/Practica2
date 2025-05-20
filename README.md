# Sistema de Registro Universitario

Este proyecto implementa un sistema completo de gestión académica para universidades, con funcionalidades de registro de estudiantes, administración de materias e inscripciones a cursos.

---

## Características principales

- Gestión de estudiantes: Registro y administración de información estudiantil.  
- Catálogo de materias: Gestión de asignaturas con prerrequisitos académicos.  
- Sistema de inscripciones: Control automatizado del proceso de inscripción.  
- Validación de prerrequisitos: Verificación automática de requisitos académicos previos.  
- Control de períodos: Organización de inscripciones por período académico.  
- Estados de inscripción: Seguimiento del progreso (pendiente, aprobada, reprobada).  
- Control de cupos: Límite de estudiantes por materia.  
- Restricciones de inscripción: Límite de materias por estudiante en cada período.  

---

## Tecnologías utilizadas

- Backend: Spring Boot, Java 17  
- Persistencia: JPA / Hibernate  
- Base de datos: MySQL / H2  
- Arquitectura: MVC (Modelo-Vista-Controlador)  
- Control de versiones: Git  
- Gestión de dependencias: Maven  
- Logs: SLF4J con Logback  
- Seguridad: Spring Security (autenticación y autorización)  
- Control de concurrencia: Optimistic locking con `@Version`  

---

## Estructura del proyecto
```bash
src/
├── main/
│ ├── java/com/universidad/
│ │ ├── controller/
│ │ ├── dto/
│ │ ├── model/
│ │ ├── repository/
│ │ ├── service/
│ │ │ └── impl/
│ │ ├── exception/
│ │ ├── util/
│ │ └── config/
│ └── resources/
│ ├── application.properties
│ └── data.sql
└── test/
```

---

## API REST

El sistema expone endpoints RESTful para:

- `/api/estudiantes`: Gestión de estudiantes  
- `/api/materias`: Gestión de materias  
- `/api/inscripciones`: Gestión de inscripciones  

---

## Modelo de datos

- Estudiante: Datos personales y académicos  
- Materia: Información académica, prerrequisitos y cupos  
- Inscripción: Relación estudiante-materia, estado, calificación y período  

---

## Mejoras implementadas

- Corrección en la verificación de prerrequisitos (uso correcto de ID en lugar de `Class`).  
- Implementación de optimistic locking con `@Version` para manejo de concurrencia.  
- Gestión adecuada de transacciones para operaciones críticas.  
- Carga `EAGER` para relaciones críticas, evitando errores de `LazyInitializationException`.  
- Logging detallado para seguimiento de operaciones.  

---

## Documentación adicional

Para más información sobre el uso de la API, consulta la documentación Swagger disponible en:

[http://localhost:8090/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## Instalación y configuración

```bash
# Clonar el repositorio
git clone https://github.com/BetzabeG/Practica2.git

# Navegar al directorio del proyecto
cd Practica2

# Compilar el proyecto con Maven
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
