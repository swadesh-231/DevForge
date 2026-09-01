# DevForge — Backend

Spring Boot backend for **DevForge**, an AI-powered app builder: users create projects,
chat with an LLM, and the model generates/edits the project's source files, which are
stored per-project and streamed back to the client.

## Tech stack

| Area | Choice |
| --- | --- |
| Language / build | Java 21, Gradle (wrapper included) |
| Framework | Spring Boot 4.1.1 (Web MVC + WebFlux for streaming) |
| Persistence | Spring Data JPA + PostgreSQL (`ddl-auto: update`) |
| Security | Spring Security, JWT access tokens + rotating refresh tokens (jjwt) |
| AI | Spring AI (OpenAI chat model, tool calling, custom advisors) |
| Media | ImageKit (user profile images) |
| Mapping / boilerplate | MapStruct, Lombok |
| Config | `application.yaml` + `.env` loaded via java-dotenv |

## Project layout

```
src/main/java/com/devforge/
├── ai/            # Spring AI layer: prompt templates, response parser,
│                  # file-tree advisor, code-generation tools (read/write files)
├── config/        # @ConfigurationProperties: JWT, cookies, CORS, ImageKit, .env loader
├── controller/    # REST endpoints (auth, users, projects)
├── dto/           # Request/response records grouped by domain
│                  # (auth, user, project, member, file, chat, billing, preview, common)
├── entity/        # JPA entities + enums
├── exception/     # Domain exceptions + GlobalExceptionHandler
├── mapper/        # MapStruct mappers
├── repository/    # Spring Data repositories
├── security/      # SecurityConfig, JWT filter/service, cookie handling, principal
├── service/       # Service interfaces
│   └── impl/      # Implementations
└── validation/    # Custom constraints (e.g. @SafePath for file paths)
```

## Domain model

`User`, `Project`, `ProjectMember` (+ role/permission enums), `ProjectFile`, `Preview`,
`ChatSession`, `ChatMessage`, `ChatEvent`, `Plan`, `Subscription`, `Payment`,
`UsageRecord`, `WebhookEvent`, `RefreshToken`.

## Getting started

### Prerequisites
- JDK 21
- A reachable PostgreSQL database

### Configuration

Create a `.env` file in the project root (it is git-ignored; values are read into system
properties on startup by `EnvConfig`):

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/devforge?user=postgres&password=postgres

JWT_ACCESS_SECRET=<base64 secret>
JWT_ACCESS_EXPIRATION=<millis, e.g. 900000>

JWT_REFRESH_SECRET=<base64 secret>
JWT_REFRESH_EXPIRATION=<millis, e.g. 604800000>

IMAGEKIT_PUBLIC_KEY=<key>
IMAGEKIT_PRIVATE_KEY=<key>
IMAGEKIT_URL_ENDPOINT=https://ik.imagekit.io/<id>
```

Optional overrides (defaults shown) in `application.yaml`:

```yaml
security:
  cookie:
    refresh-token-name: devforge_refresh_token
    secure: true
    same-site: Lax          # Strict | Lax | None
    path: /api/auth
  cors:
    allowed-origins:
      - http://localhost:5173
      - http://localhost:5174
```

### Run

```bash
./gradlew bootRun     # start the API on http://localhost:8080
./gradlew test        # run tests
./gradlew build       # compile + test + package
```

## API

All routes are under `/api` and require a `Bearer` access token, except the public ones
listed in `SecurityConfig` (`/api/auth/**`, `/api/webhooks/**`, `/actuator/health`).

### Auth — `/api/auth`
| Method | Path | Description |
| --- | --- | --- |
| POST | `/register` | Create an account, returns access token + sets refresh cookie |
| POST | `/login` | Authenticate, returns access token + sets refresh cookie |
| POST | `/refresh` | Rotate the refresh cookie, issue a new access token |
| POST | `/logout` | Revoke the refresh token and clear the cookie |

### Users — `/api/users/me`
| Method | Path | Description |
| --- | --- | --- |
| GET | `/` | Current user profile |
| PATCH | `/` | Update profile fields |
| PUT | `/image` | Upload profile image (multipart, max 5 MB) |
| DELETE | `/image` | Remove profile image |

Responses are wrapped in a common `ApiResponse<T>` envelope; errors are normalised by
`GlobalExceptionHandler`.

## Status

Auth, user profiles, security wiring, the entity/DTO layer and the ImageKit integration
are in place. The project/file/chat/billing service layer and the Spring AI generation
pipeline are still being built out — `ProjectController` is a stub, and the OpenAI
credentials (`spring.ai.openai.api-key`) are not wired into configuration yet.
