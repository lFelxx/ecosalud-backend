# Ecosalud — Backend

API REST que potencia la plataforma **Ecosalud**, un centro de salud integral que ofrece terapias alternativas, gestión de citas médicas y administración de usuarios. Desarrollada con Spring Boot 3.4.5, Spring Security 6 y PostgreSQL.

---

## Descripción del proyecto

Ecosalud Backend provee todos los servicios de negocio que consume el frontend React:

- **Autenticación y autorización** basada en JWT con roles diferenciados (Paciente, Editor, Admin).
- **Gestión de usuarios**: registro, consulta y administración de cuentas.
- **Gestión de citas**: creación, consulta y actualización de estado de citas médicas.
- **Gestión de servicios**: catálogo de terapias disponibles en el centro.
- **Seguridad stateless**: cada solicitud se valida mediante token JWT sin sesiones en servidor.
- **CORS configurado** para integración con el frontend desplegado en Vercel.

---

## Arquitectura general

El backend implementa una **arquitectura en capas** estándar de Spring Boot:

```
ecosalud-backend/
└── src/main/java/com/demo/ecosalud/
    ├── config/             # Configuración de seguridad y filtros
    │   ├── SecurityConfig.java     # Cadena de filtros, CORS, rutas públicas
    │   └── JwtFilter.java          # Interceptor de validación JWT
    ├── controller/         # Endpoints REST (capa de presentación)
    │   ├── AuthController.java     # POST /auth/login, /auth/register
    │   └── UserController.java     # GET, POST /api/users
    ├── service/            # Lógica de negocio (interfaces + implementaciones)
    │   ├── AuthService.java
    │   ├── UserService.java
    │   └── impl/
    │       ├── AuthServiceImpl.java
    │       ├── UserServiceImpl.java
    │       ├── UserDetailsImpl.java
    │       └── UserDetailsServiceImpl.java
    ├── repository/         # Acceso a datos con Spring Data JPA
    │   ├── UserRepository.java
    │   ├── AppointmentRepository.java
    │   └── ServiceRepository.java
    ├── model/
    │   ├── entities/       # Entidades JPA mapeadas a PostgreSQL
    │   │   ├── User.java
    │   │   ├── Appointment.java
    │   │   └── Service.java
    │   └── dto/            # Objetos de transferencia de datos
    │       ├── UserDTO.java
    │       ├── LoginRequestDTO.java
    │       └── LoginResponseDTO.java
    ├── mapper/             # Conversión entidad ↔ DTO
    │   └── UserMapper.java
    ├── enums/              # Tipos enumerados del dominio
    │   ├── RolUser.java           # PATIENT, EDITOR, ADMIN
    │   ├── UserStatus.java        # ACTIVE, INACTIVE
    │   └── AppointmentSatus.java  # PENDIENTE, CONFIRMADA, CANCELADA
    ├── exception/          # Manejo centralizado de errores
    │   └── ResourceNotFoundException.java
    └── util/
        └── JwtUtils.java   # Generación y validación de tokens JWT
```

### Flujo de una solicitud autenticada

```
Cliente → JwtFilter (valida token) → SecurityConfig (autoriza ruta)
       → Controller (valida DTO) → Service (lógica de negocio)
       → Repository (JPA/Hibernate) → PostgreSQL
```

---

## Tecnologías utilizadas

| Categoría        | Tecnología                        | Versión  |
|------------------|-----------------------------------|----------|
| Lenguaje         | Java                              | 21       |
| Framework        | Spring Boot                       | 3.4.5    |
| Seguridad        | Spring Security                   | 6.x      |
| Autenticación    | JWT (JJWT)                        | 0.11.5   |
| Persistencia     | Spring Data JPA / Hibernate       | 6.x      |
| Base de datos    | PostgreSQL                        | 15+      |
| Pool de conexiones | HikariCP                        | (incluido)|
| Reducción boilerplate | Lombok                       | último   |
| Validación       | Jakarta Validation (Bean Validation) | 3.x   |
| Construcción     | Apache Maven                      | 3.9+     |
| Despliegue       | Railway / Render / cualquier PaaS | —        |

---

## Requisitos previos

- **Java 21** — [descargar JDK](https://adoptium.net/)
- **Maven 3.9+** — [descargar](https://maven.apache.org/download.cgi)
- **PostgreSQL 15+** — [descargar](https://www.postgresql.org/download/)
- Cliente REST para pruebas: Postman, Insomnia o `curl`

---

## Instalación

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-org/ecosalud-backend.git
cd ecosalud-backend

# 2. Crear la base de datos en PostgreSQL
psql -U postgres -c "CREATE DATABASE ecosalud;"

# 3. Configurar las variables de entorno (ver sección siguiente)

# 4. Compilar y descargar dependencias
mvn clean install -DskipTests
```

---

## Ejecución local

```bash
# Ejecutar la aplicación con Maven
mvn spring-boot:run
```

La API estará disponible en: **http://localhost:8080**

```bash
# Compilar el JAR ejecutable
mvn clean package -DskipTests

# Ejecutar el JAR directamente
java -jar target/ecosalud-backend-1.0.0.jar
```

---

## Variables de entorno

La aplicación lee su configuración desde variables de entorno. Si no se definen, usa los valores por defecto indicados:

| Variable            | Descripción                                         | Valor por defecto                       |
|---------------------|-----------------------------------------------------|-----------------------------------------|
| `DATABASE_URL`      | URL JDBC de conexión a PostgreSQL                   | `jdbc:postgresql://localhost:5432/ecosalud` |
| `DATABASE_USERNAME` | Usuario de la base de datos                         | `postgres`                              |
| `DATABASE_PASSWORD` | Contraseña de la base de datos                      | *(sin valor — requerida en producción)* |
| `DDL_AUTO`          | Estrategia DDL de Hibernate (`update`, `validate`)  | `update`                                |
| `SHOW_SQL`          | Mostrar SQL generado en los logs                    | `false`                                 |
| `PORT`              | Puerto en que escucha el servidor                   | `8080`                                  |
| `FRONTEND_URL`      | Origen permitido en CORS                            | `http://localhost:5173`                 |
| `JWT_SECRET`        | Clave secreta para firmar los tokens JWT (Base64)   | *(clave de desarrollo — cambiar en prod)*|
| `JWT_EXPIRATION`    | Duración del token JWT en milisegundos              | `86400000` (24 horas)                   |

### Configuración rápida para desarrollo local

Crear un archivo `.env` en la raíz o exportar las variables antes de ejecutar:

```bash
# Windows (PowerShell)
$env:DATABASE_PASSWORD="tu_password_postgres"
$env:FRONTEND_URL="http://localhost:5173"
mvn spring-boot:run

# Linux / macOS
export DATABASE_PASSWORD="tu_password_postgres"
export FRONTEND_URL="http://localhost:5173"
mvn spring-boot:run
```

---

## Endpoints principales

| Método | Ruta                   | Descripción                          | Autenticación |
|--------|------------------------|--------------------------------------|---------------|
| POST   | `/auth/register`       | Registro de nuevo usuario            | Pública       |
| POST   | `/auth/login`          | Inicio de sesión — retorna JWT       | Pública       |
| GET    | `/api/users/{id}`      | Consultar usuario por ID             | JWT requerido |
| POST   | `/api/users`           | Crear usuario (uso interno/admin)    | JWT requerido |

> La documentación completa de endpoints se puede explorar con Swagger si se agrega la dependencia `springdoc-openapi`.

---

## Estructura de ramas

| Rama      | Propósito                                            |
|-----------|------------------------------------------------------|
| `main`    | Producción — solo versiones revisadas y aprobadas    |
| `develop` | Desarrollo activo — integración de nuevas funciones  |

---

## Autores

Proyecto académico desarrollado para la asignatura **Proyecto de Software** — Universidad IBERO.
