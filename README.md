# DevForge — Backend

Spring Boot backend for **DevForge**, an AI-powered app builder: users create projects,
chat with an LLM, and the model generates/edits the project's source files, which are
stored per-project and streamed back to the client.

## Tech stack

| Area | Choice |
| --- | --- |
| Language / build | Java 21, Gradle (wrapper included) |
| Framework | Spring Boot 4.1.1 (Web MVC + Reactor for SSE streaming) |
| Persistence | Spring Data JPA + PostgreSQL (`ddl-auto: update`) |
| Security | Spring Security, JWT access tokens + rotating refresh tokens (jjwt) |
| AI | Spring AI 2.0 (OpenAI chat model, tool calling, custom advisor) |
| Media | ImageKit (user profile images) |
| Billing | Stripe (gateway integration pending) |
| Mapping / boilerplate | MapStruct, Lombok |
| Config | `application.yaml` + `.env` loaded via java-dotenv |

## Project layout

```
src/main/java/com/devforge/
├── ai/            # Spring AI layer
│   ├── advisor/   #   file-tree context advisor
│   ├── prompt/    #   system prompt, runtime manifest, LLM response parser
│   └── tools/     #   tool callbacks the model can invoke (read_files)
├── config/        # @ConfigurationProperties + @Configuration beans
├── controller/    # REST endpoints
├── dto/           # Request/response records grouped by domain
├── entity/        # JPA entities + enums
├── exception/     # Domain exceptions + GlobalExceptionHandler
├── mapper/        # MapStruct mappers (shared MapperConfiguration)
├── repository/    # Spring Data repositories
├── scheduling/    # Background maintenance tasks
├── security/      # SecurityConfig, JWT, cookies, principal
│   └── access/    #   ProjectAccessGuard (membership + permission checks)
├── service/       # Service interfaces
│   └── impl/      #   implementations
├── storage/       # ProjectFileStorage seam + database-backed implementation
└── validation/    # Custom constraints and path normalisation
```

## Domain model

`User`, `Project`, `ProjectMember` (+ role/permission enums), `ProjectFile`,
`ProjectFileContent`, `Preview`, `ChatSession`, `ChatMessage`, `ChatEvent`, `Plan`,
`Subscription`, `Payment`, `UsageRecord`, `WebhookEvent`, `RefreshToken`.

### Access control

Project access is membership-based. `ProjectRole` (`OWNER`, `EDITOR`, `VIEWER`) maps to a
set of `ProjectPermission` values, and `ProjectAccessGuard` resolves the caller's
membership and asserts the required permission. A caller with no accepted membership gets
`404`, not `403`, so project existence is not leaked.

An invitation is a `ProjectMember` row with `acceptedAt` still null. Access queries ignore
unaccepted rows, so a pending invite grants nothing until the invitee accepts it.

### File storage

Generated project files are metadata (`ProjectFile`) plus bytes behind the
`ProjectFileStorage` interface. The shipped implementation keeps content in Postgres
(`ProjectFileContent`); swapping in S3/MinIO means adding one implementation, and nothing
in the service layer changes.

Every path passes through `ProjectFilePaths.normalize`, which rejects `..`, leading
slashes, backslashes and control characters, so LLM-supplied paths cannot escape their
project.

## Getting started

### Prerequisites
- JDK 21
- A reachable PostgreSQL database
- An OpenAI API key

### Configuration

Create a `.env` file in the project root (it is git-ignored; values are read into system
properties on startup by `EnvConfig`):

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/devforge?user=postgres&password=postgres

JWT_ACCESS_SECRET=<base64 secret, at least 32 chars>
JWT_ACCESS_EXPIRATION=900000

JWT_REFRESH_SECRET=<base64 secret, at least 32 chars>
JWT_REFRESH_EXPIRATION=604800000

IMAGEKIT_PUBLIC_KEY=<key>
IMAGEKIT_PRIVATE_KEY=<key>
IMAGEKIT_URL_ENDPOINT=https://ik.imagekit.io/<id>

OPENAI_API_KEY=<key>

STRIPE_API_SECRET=<key>
STRIPE_WEBHOOK_SECRET=<key>
```

`application.yaml` must wire the OpenAI key through, or the context will not start:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```

Everything else has a default and only needs to be set to override it:

```yaml
spring:
  jpa:
    open-in-view: false      # recommended: keeps lazy loading out of the view layer

security:
  cookie:
    refresh-token-name: devforge_refresh_token
    secure: true
    same-site: Lax           # Strict | Lax | None
    path: /api/auth
  cors:
    allowed-origins:
      - http://localhost:5173
      - http://localhost:5174

app:
  project-template:
    name: react-starter      # a directory under resources/project-templates
  billing:
    free-plan-name: Free
    free-max-projects: 3
    free-max-tokens-per-day: 50000
    free-max-previews: 1
```

### Run

```bash
./gradlew bootRun     # start the API on http://localhost:8080
./gradlew build       # compile + test + package
```

## API

All routes are under `/api` and require a `Bearer` access token, except the public ones
listed in `SecurityConfig` (`/api/auth/**`, `/api/webhooks/**`, `/actuator/health`).
Every response is wrapped in the `ApiResponse<T>` envelope, and errors are normalised by
`GlobalExceptionHandler`.

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
| GET | `` | Current user profile |
| PATCH | `` | Update profile fields |
| PUT | `/image` | Upload profile image (multipart, max 5 MB) |
| DELETE | `/image` | Remove profile image |

### Projects — `/api/projects`
| Method | Path | Description |
| --- | --- | --- |
| GET | `` | Projects the caller is a member of, with their role |
| POST | `` | Create a project, seeded from the starter template |
| GET | `/{projectId}` | One project |
| PATCH | `/{projectId}` | Partial update |
| DELETE | `/{projectId}` | Soft delete |

### Files — `/api/projects/{projectId}/files`
| Method | Path | Description |
| --- | --- | --- |
| GET | `` | File tree (metadata only) |
| GET | `/content?path=` | One file's content |
| PUT | `` | Create or replace a file |
| DELETE | `?path=` | Delete a file |

### Members — `/api/projects/{projectId}/members`
| Method | Path | Description |
| --- | --- | --- |
| GET | `` | Members and pending invitations |
| POST | `` | Invite an existing user by email |
| PATCH | `/{userId}` | Change a member's role |
| DELETE | `/{userId}` | Remove a member |

### Invitations — `/api/invitations`
| Method | Path | Description |
| --- | --- | --- |
| GET | `` | The caller's pending invitations |
| POST | `/{projectId}/accept` | Accept an invitation |
| DELETE | `/{projectId}` | Decline an invitation |

### Chat — `/api/chat`
| Method | Path | Description |
| --- | --- | --- |
| POST | `/stream` | Send a message, stream the response as SSE |
| GET | `/projects/{projectId}` | Messages of the caller's most recent session |
| GET | `/projects/{projectId}/sessions` | The caller's sessions for a project |
| POST | `/projects/{projectId}/sessions` | Start a session |
| GET | `/projects/{projectId}/sessions/{sessionId}/messages` | Messages in a session |
| DELETE | `/projects/{projectId}/sessions/{sessionId}` | Soft delete a session |

### Billing and usage
| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/plans` | Active plans |
| GET | `/api/plans/{planId}` | One plan |
| GET | `/api/subscriptions/me` | Current subscription, or null on the free tier |
| GET | `/api/subscriptions/me/limits` | Effective plan limits |
| GET | `/api/payments` | Payment history (paged) |
| GET | `/api/usage/today` | Today's token and message usage against the limit |

## Status

Auth, users, projects, members and invitations, project files, the project template,
chat sessions and history, the Spring AI generation pipeline, usage metering and the
billing read side are implemented and wired.

Not yet built:

- **Stripe gateway.** The schema, statuses and service methods are Stripe-shaped
  (`stripeSubscriptionId`, `stripeCustomerId`, `stripePriceId`, `stripePaymentIntentId`),
  and `WebhookEvent` is the idempotency table. What is missing is the SDK dependency,
  the checkout endpoint and the webhook controller that calls the existing
  `SubscriptionService` lifecycle methods. `/api/webhooks/**` is already public in
  `SecurityConfig`.
- **Preview.** `Preview` and `PreviewStatus` describe the intended schema; running a
  preview needs container infrastructure that is not part of this service.
