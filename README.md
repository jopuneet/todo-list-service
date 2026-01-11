# Todo List Service

A resilient backend service for managing a simple to-do list, built with Spring Boot and Java 21.

---

## Quick Start

```bash
# Clone and run with Docker (recommended)
make start

# Or run locally with Maven
./mvnw spring-boot:run
```

Service available at: `http://localhost:8080`
API Documentation: `http://localhost:8080/swagger-ui.html`

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [API Reference](#api-reference)
- [Status Lifecycle](#status-lifecycle)
- [How-To Guide](#how-to-guide)
- [Testing](#testing)
- [Development](#development)

---

## Overview

This service provides a RESTful API for managing todo items with:

| Feature | Description |
|---------|-------------|
| CRUD Operations | Create, read, update todo items |
| Status Management | Mark items as "done" or "not done" |
| Auto Past Due | Items automatically become "past due" when due date passes |
| Immutability | Past due items cannot be modified |

### Assumptions

1. Scheduler runs every minute to check for past due items
2. Past due status is also checked on-demand when retrieving items
3. Timestamps stored in server's local timezone
4. No authentication required (as per spec)

---

## Architecture

### System Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Todo List Service                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐                 │
│  │   Client     │────▶│  Controller  │────▶│   Service    │                 │
│  │  (REST API)  │◀────│    Layer     │◀────│    Layer     │                 │
│  └──────────────┘     └──────────────┘     └──────┬───────┘                 │
│                                                   │                         │
│                       ┌──────────────┐            │                         │
│                       │  Scheduler   │────────────┤                         │
│                       │ (Every 1min) │            │                         │
│                       └──────────────┘            ▼                         │
│                                            ┌──────────────┐                 │
│                                            │  Repository  │                 │
│                                            │    Layer     │                 │
│                                            └──────┬───────┘                 │
│                                                   │                         │
│                                                   ▼                         │
│                                            ┌──────────────┐                 │
│                                            │ H2 Database  │                 │
│                                            │ (In-Memory)  │                 │
│                                            └──────────────┘                 │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                Components                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                         Controller Layer                           │     │
│  │  ┌──────────────────┐  ┌─────────────────┐  ┌──────────────────┐   │     │
│  │  │  TodoController  │  │ GlobalException │  │   OpenAPI Docs   │   │     │
│  │  │  (REST Endpoints)│  │    Handler      │  │   (Swagger UI)   │   │     │
│  │  └──────────────────┘  └─────────────────┘  └──────────────────┘   │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                    │                                        │
│                                    ▼                                        │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                          Service Layer                             │     │
│  │  ┌──────────────────┐  ┌─────────────────┐  ┌──────────────────┐   │     │
│  │  │  TodoServiceImpl │  │  TodoMapper     │  │ PastDueScheduler │   │     │
│  │  │ (Business Logic) │  │ (DTO ↔ Entity)  │  │  (Cron Job)      │   │     │
│  │  └──────────────────┘  └─────────────────┘  └──────────────────┘   │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                    │                                        │
│                                    ▼                                        │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │                        Repository Layer                            │     │
│  │  ┌──────────────────┐  ┌─────────────────┐  ┌──────────────────┐   │     │
│  │  │  TodoRepository  │  │   TodoItem      │  │   TodoStatus     │   │     │
│  │  │  (Spring Data)   │  │   (Entity)      │  │   (Enum)         │   │     │
│  │  └──────────────────┘  └─────────────────┘  └──────────────────┘   │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Project Structure

```
src/
├── main/
│   ├── java/com/tradebytes/todo/
│   │   ├── config/         # OpenAPI/Swagger configuration
│   │   ├── controller/     # REST endpoints
│   │   ├── dto/            # Request/Response objects
│   │   ├── entity/         # JPA entities (TodoItem, TodoStatus)
│   │   ├── exception/      # Custom exceptions & handlers
│   │   ├── mapper/         # Entity ↔ DTO conversion
│   │   ├── repository/     # Database access layer
│   │   ├── scheduler/      # Background jobs
│   │   └── service/        # Business logic
│   └── resources/
│       ├── api.yml         # OpenAPI specification
│       └── application.properties
└── test/
    └── java/com/tradebytes/todo/
        ├── controller/     # Controller unit tests
        ├── integration/    # End-to-end tests
        └── service/        # Service unit tests
```

---

## Tech Stack

| Category | Technology |
|----------|------------|
| Runtime | Java 21 (Eclipse Temurin) |
| Framework | Spring Boot 3.2.0 |
| Database | H2 In-Memory |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito, Spring Test |
| Containerization | Docker |
| API Docs | SpringDoc OpenAPI (Swagger) |

---

## API Reference

### Endpoints Overview

```
BASE URL: http://localhost:8080/api/todos

┌────────┬─────────────────────────┬────────────────────────────────────────┐
│ Method │ Endpoint                │ Description                            │
├────────┼─────────────────────────┼────────────────────────────────────────┤
│ POST   │ /api/todos              │ Create a new todo item                 │
│ GET    │ /api/todos/{id}         │ Get a todo item by ID                  │
│ GET    │ /api/todos              │ Get all "not done" items               │
│ GET    │ /api/todos?all=true     │ Get all items (any status)             │
│ PATCH  │ /api/todos/{id}/description │ Update item description            │
│ PATCH  │ /api/todos/{id}/status  │ Update status (done/not done)          │
└────────┴─────────────────────────┴────────────────────────────────────────┘
```

### Request/Response Flow

```
                         CREATE TODO
┌────────┐  POST /api/todos   ┌────────────┐  201 Created  ┌────────┐
│ Client │ ─────────────────▶ │   Server   │ ────────────▶ │ Client │
└────────┘  {description,     └────────────┘  {id, desc,   └────────┘
             due_datetime}                     status...}


                         GET TODO
┌────────┐  GET /api/todos/1  ┌────────────┐   200 OK      ┌────────┐
│ Client │ ─────────────────▶ │   Server   │ ────────────▶ │ Client │
└────────┘                    └────────────┘  {todo item}  └────────┘


                       UPDATE STATUS
┌────────┐  PATCH ../status   ┌────────────┐   200 OK      ┌────────┐
│ Client │ ─────────────────▶ │   Server   │ ────────────▶ │ Client │
└────────┘  {status: "done"}  └────────────┘  {updated}    └────────┘

                                   │
                                   ▼ (if past due)

┌────────┐  PATCH ../status   ┌────────────┐ 409 Conflict  ┌────────┐
│ Client │ ─────────────────▶ │   Server   │ ────────────▶ │ Client │
└────────┘  {status: "done"}  └────────────┘  {error msg}  └────────┘
```

### Example Requests

#### Create a Todo Item

```bash
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Complete the coding challenge",
    "due_datetime": "2026-01-15T18:00:00"
  }'
```

**Response (201 Created):**
```json
{
  "id": 1,
  "description": "Complete the coding challenge",
  "status": "not done",
  "creation_datetime": "2026-01-11T10:30:00",
  "due_datetime": "2026-01-15T18:00:00",
  "done_datetime": null
}
```

#### Get All Todo Items

```bash
# Get only "not done" items (default)
curl http://localhost:8080/api/todos

# Get all items regardless of status
curl "http://localhost:8080/api/todos?all=true"
```

#### Update Description

```bash
curl -X PATCH http://localhost:8080/api/todos/1/description \
  -H "Content-Type: application/json" \
  -d '{"description": "Updated task description"}'
```

#### Update Status

```bash
# Mark as done
curl -X PATCH http://localhost:8080/api/todos/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "done"}'

# Mark as not done
curl -X PATCH http://localhost:8080/api/todos/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "not done"}'
```

### Error Responses

| Status Code | Meaning | Example Scenario |
|-------------|---------|------------------|
| 400 | Bad Request | Invalid input / validation error |
| 404 | Not Found | Todo item doesn't exist |
| 409 | Conflict | Trying to modify a "past due" item |

**Error Response Format:**
```json
{
  "timestamp": "2026-01-11T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Todo item not found with id: 999",
  "path": "/api/todos/999"
}
```

---

## Status Lifecycle

### Status Values

| Status | Description | Mutable? |
|--------|-------------|----------|
| `not done` | Item is pending | Yes |
| `done` | Item completed | Yes |
| `past due` | Due date passed (auto-set) | No |

### State Diagram

```
                    ┌─────────────────────────────────────────────────────┐
                    │              TODO STATUS LIFECYCLE                  │
                    └─────────────────────────────────────────────────────┘

                                     [Create Todo]
                                          │
                                          ▼
                                 ┌───────────────┐
                       ┌─────────│   NOT DONE    │─────────┐
                       │         └───────────────┘         │
                       │                 │                 │
                       │                 │                 │
              [Mark as done]    [Due date passes]   [Mark as done]
                       │         (Auto/Scheduler)          │
                       │                 │                 │
                       ▼                 ▼                 │
               ┌───────────────┐ ┌───────────────┐         │
               │     DONE      │ │   PAST DUE    │◀────────┘
               └───────────────┘ └───────────────┘   (if due date
                       │                 │            already passed)
                       │                 │
              [Mark as not done]    [IMMUTABLE]
                       │            No changes
                       │             allowed
                       ▼
               ┌───────────────┐
               │   NOT DONE    │
               └───────────────┘
```

### Past Due Detection

The service uses a **dual mechanism** to detect past due items:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        PAST DUE DETECTION                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   1. SCHEDULED (Background)              2. ON-DEMAND (API Request)         │
│   ┌─────────────────────────┐            ┌─────────────────────────┐        │
│   │                         │            │                         │        │
│   │    Every 1 Minute       │            │   GET /api/todos/{id}   │        │
│   │         │               │            │   GET /api/todos        │        │
│   │         ▼               │            │         │               │        │
│   │  ┌─────────────┐        │            │         ▼               │        │
│   │  │  Scheduler  │        │            │  ┌─────────────┐        │        │
│   │  │   checks    │        │            │  │   Service   │        │        │
│   │  │  database   │        │            │  │   checks    │        │        │
│   │  └──────┬──────┘        │            │  │   status    │        │        │
│   │         │               │            │  └──────┬──────┘        │        │
│   │         ▼               │            │         │               │        │
│   │  Update all overdue     │            │         ▼               │        │
│   │  items to PAST_DUE      │            │ Do not Update if overdue│        │
│   │                         │            │  before returning       │        │
│   └─────────────────────────┘            └─────────────────────────┘        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## How-To Guide

### Prerequisites

- Java 21 or higher
- Maven 3.8+ (or use included wrapper)
- Docker & Docker Compose (for containerized deployment)

### Build the Service

```bash
# Using Makefile (recommended)
make build

# Using Maven directly
./mvnw package -DskipTests
```

### Run the Service

#### Option 1: Docker (Recommended)

```bash
# One command to build and run
make start

# Or step by step:
make docker-build    # Build image
make docker-run      # Start container
make docker-logs     # View logs
make docker-stop     # Stop container
```

#### Option 2: Maven (Development)

```bash
make run
# or
./mvnw spring-boot:run
```

Service will be available at `http://localhost:8080`

---

## Testing

### Test Coverage

```
┌────────────────────────────────────────────────────────────────┐
│                    TEST SUITE (67 Tests)                       │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  Unit Tests                    Integration Tests               │
│  ┌──────────────────────┐     ┌──────────────────────┐         │
│  │ TodoServiceImplTest  │     │ TodoIntegrationTest  │         │
│  │ (13 tests)           │     │ (6 tests)            │         │
│  │                      │     │                      │         │
│  │ • CRUD operations    │     │ • Full API flow      │         │
│  │ • Status transitions │     │ • Error handling     │         │
│  │ • Past due logic     │     │ • Validation         │         │
│  └──────────────────────┘     └──────────────────────┘         │
│                                                                │
│  Controller Tests              Scheduler Tests                 │
│  ┌──────────────────────┐     ┌──────────────────────┐         │
│  │ TodoControllerTest   │     │SchedulerIntegration  │         │
│  │ (14 tests)           │     │ (11 tests)           │         │
│  │                      │     │                      │         │
│  │ • Endpoint mapping   │     │ • Auto past due      │         │
│  │ • Request validation │     │ • Idempotency        │         │
│  │ • Response format    │     │ • On-demand checks   │         │
│  └──────────────────────┘     └──────────────────────┘         │
│                                                                │
│  Entity & Mapper Tests                                         │
│  ┌──────────────────────┐     ┌──────────────────────┐         │
│  │ TodoItemTest         │     │ TodoMapperTest       │         │
│  │ (16 tests)           │     │ (6 tests)            │         │
│  │                      │     │                      │         │
│  │ • Lifecycle hooks    │     │ • Entity ↔ DTO       │         │
│  │ • Effective status   │     │ • Response mapping   │         │
│  │ • Past due detection │     │                      │         │
│  └──────────────────────┘     └──────────────────────┘         │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

### Run Specific Tests

```bash
# Run all tests
make test

# Run specific test class
./mvnw test -Dtest=TodoServiceImplTest

# Run integration tests only
./mvnw test -Dtest=*IntegrationTest
```

---

## Development

### API Documentation

| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |
| OpenAPI YAML | http://localhost:8080/api-docs.yaml |

### H2 Console (Development Only)

| Property | Value |
|----------|-------|
| URL | http://localhost:8080/h2-console |
| JDBC URL | `jdbc:h2:mem:tododb` |
| Username | `sa` |
| Password | *(empty)* |

### Makefile Commands

| Command | Description |
|---------|-------------|
| `make help` | Show all available commands |
| `make build` | Build the application (skip tests) |
| `make build-full` | Build with tests |
| `make test` | Run all tests |
| `make run` | Run locally with Maven |
| `make clean` | Clean build artifacts |
| `make docker-build` | Build Docker image |
| `make docker-run` | Run in Docker |
| `make docker-stop` | Stop Docker containers |
| `make docker-logs` | View container logs |
| `make start` | Build and run with Docker |

---

## Data Model

### TodoItem Entity

```
┌─────────────────────────────────────────────────────────────┐
│                        TodoItem                             │
├─────────────────────────────────────────────────────────────┤
│  id                 : Long (PK, Auto-generated)             │
│  description        : String (Required)                     │
│  status             : TodoStatus (NOT_DONE|DONE|PAST_DUE)   │
│  creation_datetime  : LocalDateTime (Auto-set, Immutable)   │
│  due_datetime       : LocalDateTime (Required)              │
│  done_datetime      : LocalDateTime (Nullable)              │
└─────────────────────────────────────────────────────────────┘
```

---

## License

MIT License. See `LICENSE` file for details.
