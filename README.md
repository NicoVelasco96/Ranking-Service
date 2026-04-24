# 🏅 Ranking Service

Escoge tu idioma / Choose your language:

<details open>
<summary><b>🇪🇸 Español</b></summary>

## Resumen

**Ranking Service** gestiona el sistema de ranking ELO de los jugadores, el leaderboard global y las estadísticas de torneos. Escucha eventos de RabbitMQ publicados por el tournament-service para actualizar el ELO automáticamente después de cada partido y registrar las victorias en torneos. El leaderboard se cachea en Redis para minimizar consultas a la base de datos.

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 4.0.5 |
| Base de Datos | PostgreSQL (vía Railway) |
| ORM | Hibernate / Spring Data JPA |
| Cache | Redis (vía Redis Cloud) |
| Mensajería | RabbitMQ (vía CloudAMQP) |
| Seguridad | Spring Security + JWT |
| Documentación | SpringDoc OpenAPI (Swagger UI) |
| Build Tool | Gradle |
| Utilidades | Lombok, Slf4j |

---

## Arquitectura

```
RabbitMQ
    │
    ├── match.finished ──► TournamentEventListener ──► EloService
    │                                │
    └── tournament.finished ─────────┘
                                     │
                              IPlayerRankingService
                              PlayerRankingService
                                     │
                              IPlayerRankingRepository
                                     │
                                PostgreSQL
                                     │
                              RedisTemplate
                                     │
                                   Redis
                                (leaderboard cache)
```

### Sistema ELO

El `EloService` implementa el algoritmo ELO estándar con las siguientes características:

- Rating inicial: 1000 puntos
- Factor K: 32 (ajuste máximo por partida)
- Rating mínimo: 500 puntos (no puede bajar de ese límite)
- El leaderboard se invalida del cache cada vez que se actualiza el ELO

---

## Primeros Pasos

### Requisitos

- Java 17+
- Gradle
- Base de datos PostgreSQL
- Instancia de Redis
- Instancia de RabbitMQ (local o en la nube)

### Ejecución local

1. Clonar el repositorio:
```bash
git clone https://github.com/NicoVelasco96/Ranking-Service.git
cd Ranking-Service
```

2. Configurar las variables de entorno (ver sección correspondiente).

3. Ejecutar la aplicación:
```bash
./gradlew bootRun
```

4. Acceder a Swagger UI en:
```
http://localhost:8083/api/docs
```

---

## Variables de Entorno

| Variable | Descripción | Ejemplo |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://host:port/db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | `tupassword` |
| `REDIS_HOST` | Host de Redis | `redis-18384.c77.eu-west-1-1.ec2.cloud.redislabs.com` |
| `REDIS_PORT` | Puerto de Redis | `18384` |
| `REDIS_PASSWORD` | Contraseña de Redis | `tupassword` |
| `RABBITMQ_HOST` | Host de RabbitMQ | `stingray.rmq.cloudamqp.com` |
| `RABBITMQ_PORT` | Puerto de RabbitMQ (5671 para SSL) | `5671` |
| `RABBITMQ_USERNAME` | Usuario de RabbitMQ | `fnzrdgdf` |
| `RABBITMQ_PASSWORD` | Contraseña de RabbitMQ | `tupassword` |
| `RABBITMQ_VHOST` | Virtual host de RabbitMQ | `fnzrdgdf` |
| `RABBITMQ_SSL` | Habilitar SSL para RabbitMQ | `true` |
| `JWT_SECRET` | Clave secreta para validar tokens JWT | `tu_secret` |

> ⚠️ Nunca subas credenciales reales al repositorio. Siempre usá variables de entorno.

---

## Endpoints

URL Base: `http://localhost:8083`

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/rankings/leaderboard` | Obtener leaderboard global ordenado por ELO |
| `GET` | `/api/rankings/players/{playerId}` | Obtener ranking de un jugador específico |
| `POST` | `/api/rankings/players/{playerId}` | Registrar un jugador en el sistema de ranking |
| `POST` | `/api/rankings/elo` | Actualizar ELO manualmente |

### Ejemplo: Respuesta del Leaderboard

```json
GET /api/rankings/leaderboard
[
  {
    "playerId": 3,
    "eloRating": 1048,
    "wins": 3,
    "losses": 1,
    "tournamentsPlayed": 2,
    "tournamentsWon": 1,
    "rank": 1,
    "updatedAt": "2026-04-23T19:12:47"
  }
]
```

### Ejemplo: Actualizar ELO

```json
POST /api/rankings/elo
{
  "winnerId": 3,
  "loserId": 4
}
```

---

## Flujo de Actualización de Ranking

```
tournament-service reporta resultado de partido
        │
        ▼
RabbitMQ publica evento MATCH_FINISHED
        │
        ▼
TournamentEventListener recibe el evento
        │
        ▼
EloService calcula nuevos ratings
        │
        ▼
PlayerRankingService actualiza DB
        │
        ▼
Cache de Redis invalidado
        │
        ▼
Próxima consulta al leaderboard reconstruye el cache
```

---

## Estructura del Proyecto

```
src/main/java/com/tournament/ranking/
├── config/
│   ├── AppConfig.java                  # Configuración OpenAPI y ObjectMapper
│   ├── RabbitMQConfig.java             # Exchange, colas y bindings
│   └── RedisConfig.java                # Configuración RedisTemplate
├── controller/
│   └── RankingController.java          # Endpoints REST
├── dto/
│   └── RankingDTO.java                 # DTOs de request/response
├── messaging/
│   └── TournamentEventListener.java    # Listener de eventos RabbitMQ
├── model/
│   └── PlayerRanking.java              # Entidad ranking
├── repository/
│   └── IPlayerRankingRepository.java
├── security/
│   ├── JWTService.java                 # Validación de tokens JWT
│   └── SecurityConfig.java             # Configuración Spring Security
├── service/
│   ├── EloService.java                 # Algoritmo ELO
│   ├── IEloService.java
│   ├── IPlayerRankingService.java
│   └── PlayerRankingService.java       # Lógica de negocio
└── RankingServiceApplication.java
```

</details>

---

<details>
<summary><b>🇺🇸 English</b></summary>

## Overview

**Ranking Service** manages the ELO ranking system for players, the global leaderboard, and tournament statistics. It listens to RabbitMQ events published by the tournament-service to automatically update ELO after each match and record tournament wins. The leaderboard is cached in Redis to minimize database queries.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 |
| Database | PostgreSQL (via Railway) |
| ORM | Hibernate / Spring Data JPA |
| Cache | Redis (via Redis Cloud) |
| Messaging | RabbitMQ (via CloudAMQP) |
| Security | Spring Security + JWT |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Build Tool | Gradle |
| Utilities | Lombok, Slf4j |

---

## Architecture

```
RabbitMQ
    │
    ├── match.finished ──► TournamentEventListener ──► EloService
    │                                │
    └── tournament.finished ─────────┘
                                     │
                              IPlayerRankingService
                              PlayerRankingService
                                     │
                              IPlayerRankingRepository
                                     │
                                PostgreSQL
                                     │
                              RedisTemplate
                                     │
                                   Redis
                                (leaderboard cache)
```

### ELO System

The `EloService` implements the standard ELO algorithm with the following characteristics:

- Initial rating: 1000 points
- K-factor: 32 (maximum adjustment per match)
- Minimum rating: 500 points (floor to prevent negative ratings)
- Leaderboard cache is invalidated on every ELO update

---

## Getting Started

### Prerequisites

- Java 17+
- Gradle
- PostgreSQL database
- Redis instance
- RabbitMQ instance (local or cloud)

### Run locally

1. Clone the repository:
```bash
git clone https://github.com/NicoVelasco96/Ranking-Service.git
cd Ranking-Service
```

2. Set the required environment variables (see below).

3. Run the application:
```bash
./gradlew bootRun
```

4. Access Swagger UI at:
```
http://localhost:8083/api/docs
```

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://host:port/db` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `yourpassword` |
| `REDIS_HOST` | Redis host | `redis-18384.c77.eu-west-1-1.ec2.cloud.redislabs.com` |
| `REDIS_PORT` | Redis port | `18384` |
| `REDIS_PASSWORD` | Redis password | `yourpassword` |
| `RABBITMQ_HOST` | RabbitMQ host | `stingray.rmq.cloudamqp.com` |
| `RABBITMQ_PORT` | RabbitMQ port (5671 for SSL) | `5671` |
| `RABBITMQ_USERNAME` | RabbitMQ username | `fnzrdgdf` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `yourpassword` |
| `RABBITMQ_VHOST` | RabbitMQ virtual host | `fnzrdgdf` |
| `RABBITMQ_SSL` | Enable SSL for RabbitMQ | `true` |
| `JWT_SECRET` | Secret key for JWT validation | `your_secret` |

> ⚠️ Never commit credentials to the repository. Always use environment variables.

---

## API Endpoints

Base URL: `http://localhost:8083`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/rankings/leaderboard` | Get global leaderboard sorted by ELO |
| `GET` | `/api/rankings/players/{playerId}` | Get ranking for a specific player |
| `POST` | `/api/rankings/players/{playerId}` | Register a player in the ranking system |
| `POST` | `/api/rankings/elo` | Manually update ELO ratings |

### Example: Leaderboard Response

```json
GET /api/rankings/leaderboard
[
  {
    "playerId": 3,
    "eloRating": 1048,
    "wins": 3,
    "losses": 1,
    "tournamentsPlayed": 2,
    "tournamentsWon": 1,
    "rank": 1,
    "updatedAt": "2026-04-23T19:12:47"
  }
]
```

### Example: Update ELO

```json
POST /api/rankings/elo
{
  "winnerId": 3,
  "loserId": 4
}
```

---

## Ranking Update Flow

```
tournament-service reports match result
        │
        ▼
RabbitMQ publishes MATCH_FINISHED event
        │
        ▼
TournamentEventListener receives event
        │
        ▼
EloService calculates new ratings
        │
        ▼
PlayerRankingService updates DB
        │
        ▼
Redis cache invalidated
        │
        ▼
Next leaderboard query rebuilds cache
```

---

## Project Structure

```
src/main/java/com/tournament/ranking/
├── config/
│   ├── AppConfig.java                  # OpenAPI & ObjectMapper config
│   ├── RabbitMQConfig.java             # Exchange, queues and bindings
│   └── RedisConfig.java                # RedisTemplate configuration
├── controller/
│   └── RankingController.java          # REST endpoints
├── dto/
│   └── RankingDTO.java                 # Request/Response DTOs
├── messaging/
│   └── TournamentEventListener.java    # RabbitMQ event listener
├── model/
│   └── PlayerRanking.java              # Ranking entity
├── repository/
│   └── IPlayerRankingRepository.java
├── security/
│   ├── JWTService.java                 # JWT token validation
│   └── SecurityConfig.java             # Spring Security configuration
├── service/
│   ├── EloService.java                 # ELO algorithm
│   ├── IEloService.java
│   ├── IPlayerRankingService.java
│   └── PlayerRankingService.java       # Business logic
└── RankingServiceApplication.java
```

</details>

---

## 📜 Licencia / License

Este proyecto es parte de un portafolio personal. / This project is part of a personal portfolio.
