package com.demo.ecosalud.multitenancy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio que crea y migra el schema de un nuevo tenant en PostgreSQL.
 *
 * <p>Se invoca al registrar una nueva clínica en la plataforma.
 * Crea el schema {@code tenant_{slug}} y todas las tablas necesarias
 * para operar de forma autónoma.</p>
 *
 * <p>Tablas creadas por tenant:</p>
 * <ul>
 *   <li>{@code users}          — Pacientes y especialistas de la clínica</li>
 *   <li>{@code specialists}    — Perfil clínico del especialista</li>
 *   <li>{@code services}       — Terapias y servicios ofrecidos</li>
 *   <li>{@code appointments}   — Citas agendadas</li>
 *   <li>{@code therapy_plans}  — Planes de terapia multi-sesión</li>
 *   <li>{@code health_records} — Historia clínica (Res. 1995/1999)</li>
 *   <li>{@code consents}       — Consentimientos informados digitales</li>
 * </ul>
 *
 * <p>TODO Fase 3 — Historial clínico con Gobernación:</p>
 * <p>Cuando se integre con SISPRO (Res. 2654/2019), esta clase deberá
 * agregar las tablas de interoperabilidad HL7 FHIR al schema del tenant.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaInitializationService {

    private final DataSource dataSource;

    /**
     * Crea el schema PostgreSQL para el tenant y todas sus tablas.
     *
     * @param schemaName nombre del schema (ej. {@code tenant_fisiosalud})
     * @throws RuntimeException si la creación del schema falla
     */
    public void initializeSchema(String schemaName) {
        log.info("Inicializando schema para tenant: {}", schemaName);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Crear el schema
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);

            // 2. Crear todas las tablas del tenant
            createTables(stmt, schemaName);

            // 3. Insertar datos iniciales del especialista (placeholder)
            insertInitialData(stmt, schemaName);

            log.info("Schema '{}' inicializado correctamente.", schemaName);
        } catch (SQLException e) {
            log.error("Error al inicializar schema '{}': {}", schemaName, e.getMessage());
            throw new RuntimeException("No se pudo crear el schema del tenant: " + schemaName, e);
        }
    }

    /**
     * Aplica migraciones a un schema de tenant ya existente.
     * Crea tablas que falten (usa IF NOT EXISTS — idempotente).
     * Útil para tenants creados antes de añadir nuevas tablas al schema base.
     *
     * @param schemaName schema existente (ej. {@code tenant_dra_angelica_camacho})
     */
    public void migrateSchema(String schemaName) {
        log.info("[Migration] Aplicando migración al schema: {}", schemaName);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            createTables(stmt, schemaName);
            log.info("[Migration] Schema '{}' actualizado correctamente.", schemaName);
        } catch (SQLException e) {
            log.error("[Migration] Error al migrar schema '{}': {}", schemaName, e.getMessage());
            // No lanza excepción — la migración es best-effort
        }
    }

    /** Elimina el schema completo de un tenant. Solo super-admin puede invocar esto. */
    public void dropSchema(String schemaName) {
        log.warn("Eliminando schema de tenant: {}", schemaName);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
            log.info("Schema '{}' eliminado.", schemaName);
        } catch (SQLException e) {
            log.error("Error al eliminar schema '{}': {}", schemaName, e.getMessage());
            throw new RuntimeException("No se pudo eliminar el schema: " + schemaName, e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────

    private void createTables(Statement stmt, String s) throws SQLException {

        // ── Usuarios (pacientes + especialistas de la clínica) ───────────────
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS %s.users (
                id              BIGSERIAL PRIMARY KEY,
                name            VARCHAR(255) NOT NULL,
                email           VARCHAR(255) NOT NULL UNIQUE,
                password        VARCHAR(255) NOT NULL,
                role            VARCHAR(50)  NOT NULL DEFAULT 'PATIENT',
                status          VARCHAR(50)  NOT NULL DEFAULT 'ACTIVO',
                phone           VARCHAR(50),
                document_type   VARCHAR(20),
                document_number VARCHAR(50),
                birth_date      DATE,
                gender          VARCHAR(20),
                address         TEXT,
                city            VARCHAR(100),
                created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                updated_at      TIMESTAMP
            )
            """.formatted(s));

        // ── Especialistas (perfil clínico extendido) ─────────────────────────
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS %s.specialists (
                id                  BIGSERIAL PRIMARY KEY,
                user_id             BIGINT NOT NULL REFERENCES %s.users(id) ON DELETE CASCADE,
                specialty           VARCHAR(255),
                registration_number VARCHAR(100),
                bio                 TEXT,
                photo_url           TEXT,
                university          VARCHAR(255),
                badge               VARCHAR(100),
                active              BOOLEAN NOT NULL DEFAULT TRUE,
                created_at          TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """.formatted(s, s));

        // ── Servicios / terapias ─────────────────────────────────────────────
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS %s.services (
                id               BIGSERIAL PRIMARY KEY,
                name             VARCHAR(255) NOT NULL,
                category         VARCHAR(100),
                description      TEXT,
                benefits         TEXT,
                duration_minutes INTEGER,
                price_cop        NUMERIC(12,2),
                image_url        TEXT,
                active           BOOLEAN NOT NULL DEFAULT TRUE,
                created_at       TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """.formatted(s));

        // ── Citas ────────────────────────────────────────────────────────────
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS %s.appointments (
                id                  BIGSERIAL PRIMARY KEY,
                patient_id          BIGINT NOT NULL REFERENCES %s.users(id),
                specialist_id       BIGINT REFERENCES %s.specialists(id),
                service_id          BIGINT REFERENCES %s.services(id),
                appointment_date    DATE NOT NULL,
                appointment_time    TIME NOT NULL,
                status              VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE',
                notes               TEXT,
                cancellation_reason TEXT,
                created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
                updated_at          TIMESTAMP
            )
            """.formatted(s, s, s, s));

        // ── Planes de terapia (multi-sesión) ─────────────────────────────────
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS %s.therapy_plans (
                id                  BIGSERIAL PRIMARY KEY,
                patient_id          BIGINT NOT NULL REFERENCES %s.users(id),
                specialist_id       BIGINT REFERENCES %s.specialists(id),
                service_id          BIGINT REFERENCES %s.services(id),
                total_sessions      INTEGER NOT NULL,
                completed_sessions  INTEGER NOT NULL DEFAULT 0,
                start_date          DATE NOT NULL,
                status              VARCHAR(50) NOT NULL DEFAULT 'ACTIVO',
                notes               TEXT,
                created_at          TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """.formatted(s, s, s, s));

        // ── Historia clínica (Res. 1995/1999 — base) ─────────────────────────
        // TODO Fase 3: ampliar para cumplir Res. 2654/2019 e integración SISPRO
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS %s.health_records (
                id                      BIGSERIAL PRIMARY KEY,
                patient_id              BIGINT NOT NULL REFERENCES %s.users(id),
                appointment_id          BIGINT REFERENCES %s.appointments(id),
                specialist_id           BIGINT REFERENCES %s.specialists(id),
                reason_for_consultation TEXT,
                current_illness         TEXT,
                physical_exam           TEXT,
                diagnosis               TEXT,
                treatment_plan          TEXT,
                observations            TEXT,
                next_appointment        DATE,
                is_locked               BOOLEAN NOT NULL DEFAULT FALSE,
                hash_integrity          VARCHAR(64),
                created_by              BIGINT,
                created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
                updated_at              TIMESTAMP
            )
            """.formatted(s, s, s, s));

        // ── Consentimientos informados digitales ─────────────────────────────
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS %s.consents (
                id                BIGSERIAL PRIMARY KEY,
                patient_id        BIGINT NOT NULL REFERENCES %s.users(id),
                service_id        BIGINT REFERENCES %s.services(id),
                consent_text      TEXT NOT NULL,
                digital_signature TEXT,
                ip_address        VARCHAR(45),
                signed_at         TIMESTAMP,
                created_at        TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """.formatted(s, s, s));

        // ── Publicaciones / blog de la clínica ───────────────────────────────
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS %s.posts (
                id           BIGSERIAL PRIMARY KEY,
                title        VARCHAR(500) NOT NULL,
                slug         VARCHAR(500) NOT NULL UNIQUE,
                excerpt      TEXT,
                content      TEXT,
                image_url    TEXT,
                status       VARCHAR(20) NOT NULL DEFAULT 'draft',
                tags         TEXT,
                author_id    BIGINT REFERENCES %s.users(id),
                author_name  VARCHAR(255),
                published_at TIMESTAMP,
                created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                updated_at   TIMESTAMP
            )
            """.formatted(s, s));

        log.debug("Tablas creadas en schema '{}'", s);
    }

    private void insertInitialData(Statement stmt, String s) throws SQLException {
        // Inserta el usuario administrador placeholder (se actualiza en onboarding)
        stmt.execute("""
            INSERT INTO %s.users (name, email, password, role, status)
            VALUES ('Administrador', 'admin@clinica.ecosalud', 'PENDING_SETUP', 'ADMIN', 'ACTIVO')
            ON CONFLICT (email) DO NOTHING
            """.formatted(s));
    }
}
