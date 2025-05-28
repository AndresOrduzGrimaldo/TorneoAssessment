# TorneoAssessment - Sistema de Gestión de Torneos E-Sport

## Descripción del Proyecto

Sistema integral para la gestión de torneos de videojuegos (E-Sport) que permite:

- ✅ Venta de tickets virtuales con QR/código único
- ✅ Creación de torneos gratuitos y de pago con límites de participantes
- ✅ Integración con plataformas de transmisión (Twitch, Discord, Zoom, Meet)
- ✅ Cálculo automático de comisiones por servicio
- ✅ Sistema de alertas y notificaciones
- ✅ Dashboard de monitoreo con métricas de audiencia y ganancias
- ✅ Disponibilidad 24x7 con tolerancia a fallos

## Arquitectura de Alto Nivel

### Stack Tecnológico

#### Backend
- **Framework**: Spring Boot 3.x
- **Programación Reactiva**: Spring WebFlux
- **Seguridad**: Spring Security (OAuth2/JWT)
- **Persistencia**: Spring Data JPA + Hibernate
- **Base de Datos**: PostgreSQL
- **Mensajería**: RabbitMQ/Apache Kafka
- **API Documentation**: OpenAPI 3.0 + Swagger

#### Testing & Quality
- **Unit Testing**: JUnit 5 + Mockito
- **Integration Testing**: Spring Test + TestContainers
- **API Testing**: REST Assured
- **Code Coverage**: JaCoCo (mínimo 80%)

#### Infrastructure
- **Containerization**: Docker + Docker Compose
- **IaC**: Terraform (opcional)
- **Monitoring**: Prometheus + Grafana
- **CI/CD**: GitHub Actions

### Arquitectura de Microservicios

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │   Load Balancer │    │   Reverse Proxy │
│   (Rate Limit)  │    │                 │    │     (nginx)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
    ┌────────────────────────────┼────────────────────────────┐
    │                            │                            │
┌───▼────┐  ┌──────────┐  ┌─────▼─────┐  ┌──────────┐  ┌────▼────┐
│Tournament│  │ Ticket   │  │  User     │  │Category  │  │  Game   │
│Service   │  │ Service  │  │  Service  │  │Service   │  │ Service │
└─────┬────┘  └─────┬────┘  └─────┬─────┘  └─────┬────┘  └────┬────┘
      │             │             │              │            │
      └─────────────┼─────────────┼──────────────┼────────────┘
                    │             │              │
              ┌─────▼─────┐  ┌────▼────┐  ┌─────▼─────┐
              │ Message   │  │Database │  │  Cache    │
              │ Broker    │  │(PostgreSQL)│  │ (Redis) │
              │(RabbitMQ) │  └─────────┘  └───────────┘
              └───────────┘
```

### Modelo de Dominio (DDD)

#### Agregados Principales

1. **Tournament Aggregate**
   - Tournament (Root)
   - TournamentStage
   - TournamentParticipant

2. **Ticket Aggregate**
   - Ticket (Root)
   - TicketSale
   - Commission

3. **User Aggregate**
   - User (Root)
   - UserProfile
   - UserRole

4. **Category Aggregate**
   - Category (Root)

5. **Game Aggregate**
   - Game (Root)

### Patrones de Diseño Implementados

- **Domain Driven Design (DDD)**: Separación clara entre dominio, aplicación e infraestructura
- **CQRS**: Separación de comandos y consultas
- **Event Sourcing**: Para auditoría y trazabilidad
- **Circuit Breaker**: Para resiliencia (Resilience4j)
- **Repository Pattern**: Abstracción de acceso a datos
- **Factory Pattern**: Creación de objetos complejos
- **Observer Pattern**: Sistema de notificaciones

## Funcionalidades Principales

### 1. Gestión de Categorías
- CRUD completo de categorías de torneo
- Validaciones de negocio
- Soft delete

### 2. Gestión de Tipos de Juego
- CRUD de juegos con validaciones
- Control de cantidad de jugadores

### 3. Sistema de Tickets
- Generación de QR/códigos únicos (ZXing)
- Cálculo automático de comisiones
- Estados de ticket: RESERVED, PAID, USED, EXPIRED

### 4. Gestión de Torneos
- Torneos gratuitos y de pago
- Límites por organizador (máx. 2 eventos gratuitos)
- Límites por usuario (1 vista gratuita)
- Estados: DRAFT, PUBLISHED, IN_PROGRESS, FINISHED, CANCELLED

### 5. Sistema de Notificaciones
- Eventos asincrónicos via RabbitMQ
- Email notifications
- WebSocket para tiempo real
- Push notifications

### 6. Dashboard y Métricas
- Métricas de audiencia en tiempo real
- Reportes financieros
- Analytics de participación

### 7. Seguridad
- OAuth2 + JWT
- Roles: ADMIN, ORGANIZER, USER
- Rate limiting
- CORS configuration

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/
│   │   └── com/esport/torneo/
│   │       ├── TorneoApplication.java
│   │       ├── config/           # Configuraciones
│   │       ├── domain/           # Entidades y lógica de negocio
│   │       │   ├── tournament/
│   │       │   ├── ticket/
│   │       │   ├── user/
│   │       │   ├── category/
│   │       │   └── game/
│   │       ├── application/      # Servicios de aplicación
│   │       ├── infrastructure/   # Persistencia, messaging, etc.
│   │       └── presentation/     # Controllers, DTOs
│   └── resources/
│       ├── application.yml
│       ├── db/migration/        # Flyway migrations
│       └── static/
├── test/
│   ├── java/
│   └── resources/
├── docs/                        # Documentación técnica
├── docker/                      # Docker configurations
└── terraform/                   # Infrastructure as Code
```

## Requisitos No Funcionales

### Resiliencia
- Circuit Breaker pattern con Resilience4j
- Timeout y retry strategies
- Graceful degradation
- Health checks

### Escalabilidad
- Arquitectura de microservicios
- Cache distribuido (Redis)
- Database sharding (futuro)
- Horizontal scaling

### Mantenibilidad
- Clean Code principles
- SOLID principles
- Comprehensive documentation
- Code coverage > 80%

### Testeabilidad
- Unit tests con JUnit 5
- Integration tests con TestContainers
- Contract testing
- Performance testing

### Eficiencia
- Índices optimizados en BD
- Query optimization
- Connection pooling
- Async processing

### Seguridad
- HTTPS obligatorio
- JWT token validation
- Input sanitization
- SQL injection prevention
- Rate limiting

## Instalación y Configuración

### Prerrequisitos
- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 15+

### Configuración Local

1. Clonar el repositorio:
```bash
git clone https://github.com/tu-usuario/TorneoAssessment.git
cd TorneoAssessment
```

2. Levantar servicios con Docker:
```bash
docker-compose up -d
```

3. Ejecutar migraciones:
```bash
mvn flyway:migrate
```

4. Ejecutar aplicación:
```bash
mvn spring-boot:run
```

### URLs Importantes
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Grafana: http://localhost:3000
- RabbitMQ Management: http://localhost:15672

## Testing

### Ejecutar Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Coverage report
mvn jacoco:report
```

### Estrategia de Testing
- **Unit Tests**: Lógica de negocio y servicios
- **Integration Tests**: APIs y persistencia
- **Contract Tests**: Contratos entre servicios
- **Performance Tests**: Carga y stress

## Monitoreo y Observabilidad

### Métricas
- Application metrics (Micrometer)
- Business metrics (custom)
- Infrastructure metrics (Prometheus)

### Logging
- Structured logging (JSON)
- Correlation IDs
- Log levels configurables

### Tracing
- Distributed tracing (Sleuth)
- Request correlation

## Deployment

### Ambientes
- **Local**: Docker Compose
- **Development**: Kubernetes cluster
- **Production**: Cloud provider (AWS/GCP/Azure)

### CI/CD Pipeline
1. Code commit
2. Automated tests
3. Security scanning
4. Build Docker image
5. Deploy to staging
6. Automated acceptance tests
7. Deploy to production

## Contribución

### Convenciones de Commits
```
<tipo>(<alcance>): <descripción>

Tipos: feat, fix, docs, style, refactor, test, chore
Ejemplos:
- feat(tournament): agregar validación de fechas
- fix(ticket): corregir generación de QR
- test(api): agregar tests para POST /tournaments
```

### Code Review Checklist
- [ ] Tests unitarios incluidos
- [ ] Documentación actualizada
- [ ] Principios SOLID aplicados
- [ ] Manejo de errores implementado
- [ ] Logging apropiado
- [ ] Seguridad validada

## Roadmap

### Fase 1 (MVP) ✅
- [x] Arquitectura base
- [x] Entidades principales
- [x] APIs básicas
- [x] Autenticación

### Fase 2 (Core Features) 🚧
- [ ] Sistema de tickets completo
- [ ] Notificaciones en tiempo real
- [ ] Dashboard básico

### Fase 3 (Advanced Features) 📋
- [ ] Analytics avanzados
- [ ] Integración con streams
- [ ] Mobile API

### Fase 4 (Scale) 🚀
- [ ] Microservices architecture
- [ ] Multi-tenancy
- [ ] Global distribution

## Contacto y Soporte

- **Desarrollador**: Andrés Orduz Grimaldo
- **Email**: desarrollo@torneoesport.com
- **Documentación**: `/docs`
- **Issues**: GitHub Issues

---

**Versión**: 1.0.0  
**Última actualización**: 2024  
**Licencia**: MIT 