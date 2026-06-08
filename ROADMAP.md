# 🌿 Ecosalud Market — Roadmap y Estado del Proyecto

> Generado: 2026-06-06 | Arquitecto: Claude Sonnet 4.6

---

## ✅ Estado actual — Lo que funciona HOY

### Backend (Spring Boot 3.4.5 · PostgreSQL · Multi-tenant)

| Módulo | Estado | Notas |
|--------|--------|-------|
| Multi-tenancy (schema-per-tenant) | ✅ Producción | TenantFilter + TenantContext + search_path |
| Migración automática de schemas | ✅ Producción | `migrateSchema()` en FounderTenantsSeeder |
| JWT Auth | ✅ Producción | Stateless, BCrypt, roles ADMIN/EDITOR/USER |
| Onboarding público (`POST /api/onboarding`) | ✅ Nuevo | Crea tenant + user + envía email + JWT auto-login |
| Recuperación de contraseña (`POST /api/auth/forgot-password`) | ✅ Nuevo | Genera password temporal + envía email |
| Gestión de servicios | ✅ Producción | CRUD completo |
| Blog / Posts | ✅ Producción | Draft/published, imágenes |
| Especialistas | ✅ Producción | Perfil, credenciales, foto |
| Citas | ✅ Producción | CRUD, estados (PENDIENTE/CONFIRMADA/COMPLETADA/CANCELADA) |
| Historia clínica (HCE) | ✅ Producción | Res. 1995/1999, bloqueo de registros, hash SHA-256 |
| Planes de terapia | ✅ Producción | Seguimiento de sesiones |
| Gestión de usuarios | ✅ Producción | Roles, estados, cambio de contraseña |
| Media upload | ✅ Producción | Local disk, UUID, anti path-traversal, cache 1hr |
| Super-admin API | ✅ Producción | CRUD tenants, stats, reset password, delete |
| Email transaccional (Resend) | ✅ Listo | Necesita `RESEND_API_KEY` en prod |

### Frontend (React + MUI v7 + TypeScript + Vite)

| Página | Estado | Notas |
|--------|--------|-------|
| Landing pública | ✅ Producción | SEO dinámico por tenant |
| Onboarding (registro clínica) | ✅ Mejorado | Split layout, plan features, success con próximos pasos |
| Pricing | ✅ | Planes Starter / Pro |
| Login | ✅ | JWT, auto-redirect |
| Registro paciente | ✅ | |
| Dashboard paciente | ✅ | |
| Agendar cita | ✅ | |
| Ver citas | ✅ | |
| Servicios (pública) | ✅ | Filtros, modal de detalle, SEO |
| Blog / Publicaciones (pública) | ✅ | SEO por artículo |
| Perfil especialista (pública) | ✅ | |
| Panel Admin — Dashboard | ✅ | Métricas, stats |
| Panel Admin — Servicios | ✅ | CRUD, imagen |
| Panel Admin — Blog | ✅ | Editor + CRUD |
| Panel Admin — Media | ✅ | Upload real al servidor |
| Panel Admin — Usuarios | ✅ | Roles, estados |
| Panel Admin — Especialista | ✅ | Edición perfil + foto |
| Panel Admin — Citas | ✅ | Gestión con estados |
| Panel Admin — Historia Clínica | ✅ | HCE completa, bloqueo |
| Panel Admin — Planes de Terapia | ✅ | |
| Super-Admin | ✅ Mejorado | Stats, edición, password reset, delete con drawer |
| Super-Admin — Nueva Clínica | ✅ Nuevo | Formulario completo con todas las opciones |

---

## 🏗️ Arquitectura actual

```
Cliente (React SPA)
    │ HTTPS / JWT
    ▼
Spring Boot API  ──→  PostgreSQL (public schema: tenants, users)
    │                           └─→ tenant_XXX schema (por clínica)
    ├─ TenantFilter (X-Tenant-ID / subdomain → TenantContext)
    ├─ JwtFilter (Bearer token → UserDetails)
    ├─ SecurityConfig (routes: public vs protected)
    └─ Email: Resend API (RESEND_API_KEY)
         └─ Upload: local disk (uploads/{tenantSchema}/{uuid}.ext)
```

**Modelo de planes:** STARTER → PRO → CLINIC → FOUNDER  
**Facturación:** BillingStatus (ACTIVE / EXEMPT / SUSPENDED / OVERDUE)  
**Multi-sede:** no implementado — cada clínica es un slug/schema independiente

---

## 🔮 Fase 2 — Próximas 4-8 semanas (Alta prioridad)

### 2.1 Notificaciones por email reales (Backend listo, falta integración)
- [ ] Llamar `emailService.sendAppointmentConfirmation()` desde `AppointmentServiceImpl` al confirmar una cita
- [ ] Enviar `sendAppointmentReminder()` vía job programado 24h antes (Spring `@Scheduled`)
- [ ] Enviar `sendAppointmentCancellation()` al cancelar
- **Impacto:** Retención de pacientes, reducción de no-shows

### 2.2 Pagos — Stripe / Wompi
- [ ] Integrar Stripe (o Wompi para Colombia) para cobrar suscripciones
- [ ] Webhook para actualizar `BillingStatus` automáticamente
- [ ] Portal de facturación: facturas PDF, historial de pagos
- **Impacto:** Monetización del SaaS

### 2.3 Dashboard de paciente mejorado
- [ ] Ver historial de citas propias
- [ ] Descargar/ver historia clínica personal
- [ ] Ver planes de terapia activos y progreso
- [ ] Notificaciones in-app de próximas citas
- **Impacto:** Retención y satisfacción del paciente

### 2.4 Panel admin — Módulo de reportes
- [ ] Citas por período (gráfico de barras)
- [ ] Ingresos estimados por servicio
- [ ] Pacientes nuevos vs recurrentes
- [ ] Export a CSV/PDF
- **Impacto:** Decisiones basadas en datos para los médicos

### 2.5 Dominio personalizado (infraestructura)
- [ ] Guía CNAME en panel admin
- [ ] Verificación de dominio desde el backend (`GET /.well-known/ecosalud-verify`)
- [ ] Nginx / Traefik reverse proxy en producción
- **Impacto:** Diferenciador clave para plan PRO/CLINIC

---

## 🚀 Fase 3 — 2-3 meses (Escalado y normativa)

### 3.1 Historia clínica — Interoperabilidad HL7 FHIR (Res. 2654/2019)
- [ ] Exportar HCE en formato FHIR JSON
- [ ] Integración con SISPRO (MinSalud Colombia)
- [ ] Firma digital de documentos clínicos
- **Impacto:** Cumplimiento normativo para clínicas medianas/grandes

### 3.2 Multi-especialista real (Agenda por especialista)
- [ ] Cada especialista con su propia agenda configurable (horarios, descansos)
- [ ] Citas asignadas a especialista específico
- [ ] Vista de calendario compartido en admin
- **Impacto:** Clínicas con múltiples profesionales

### 3.3 Portal del paciente autónomo (App móvil opcional)
- [ ] App React Native o PWA para pacientes
- [ ] Push notifications (citas, recordatorios)
- [ ] Telemedicina básica (videollamada vía Daily.co o Jitsi)
- **Impacto:** Experiencia premium para el paciente

### 3.4 Marketplace de servicios
- [ ] Directorio público de clínicas Ecosalud (SEO nacional)
- [ ] Búsqueda por especialidad y ciudad
- [ ] Reseñas verificadas de pacientes
- **Impacto:** Adquisición orgánica de nuevas clínicas

### 3.5 API pública para integraciones
- [ ] REST API documentada con OpenAPI/Swagger
- [ ] Webhooks para terceros (Zapier, CRM, Google Calendar)
- [ ] SDK básico para desarrolladores
- **Impacto:** Ecosistema y retención enterprise

---

## ⚠️ Deuda técnica identificada

| Item | Prioridad | Descripción |
|------|-----------|-------------|
| `RESEND_API_KEY` no configurada | Alta | Emails en modo dev (solo logs). Configurar para producción. |
| `UPLOAD_DIR` solo local | Alta | En producción mover a S3 o bucket equivalente |
| Feature flags no implementados | Media | `SubscriptionPlan` define límites pero no hay enforcement real |
| Tests unitarios | Media | Sin cobertura de tests automatizados |
| Liquibase/Flyway | Media | Migraciones manuales vía `SchemaInitializationService` — fragile a largo plazo |
| HTTPS / SSL local | Baja | Desarrollo en HTTP, producción requiere cert |
| Logs estructurados | Baja | Usar MDC con tenantId para trazabilidad |

---

## 📊 Métricas de calidad actuales

- **Endpoints backend:** ~45 endpoints REST documentados
- **Páginas frontend:** 25 páginas/vistas
- **Archivos TS/TSX activos:** 46
- **Archivos Java activos:** 87 (0 archivos muertos tras limpieza)
- **Multi-tenant:** ✅ Completamente aislado por schema
- **Seguridad:** JWT stateless + BCrypt + CORS configurado
- **SEO:** ✅ Dinámico por tenant en todas las páginas públicas

---

## 💡 Recomendaciones inmediatas (esta semana)

1. **Configurar `RESEND_API_KEY`** — el servicio está listo, solo falta la variable de entorno
2. **Configurar `UPLOAD_DIR`** en producción (o migrar a S3)
3. **Test del flujo completo de onboarding** — el endpoint `/api/onboarding` es nuevo
4. **Migrar plan `ENTERPRISE`→`CLINIC`** en cualquier dato existente en BD
5. **Deploy backend** con los nuevos endpoints antes de publicar la landing

---

*Generado automáticamente por Claude Sonnet 4.6 para el proyecto Ecosalud Market.*
