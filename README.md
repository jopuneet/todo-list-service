# Todo List Service

A resilient backend service for managing a simple to-do list, built with Spring Boot and Java 21.

## Service Description

This service provides a RESTful API for managing todo items with the following features:

- **CRUD Operations**: Create, read, update todo items
- **Status Management**: Mark items as "done" or "not done"
- **Automatic Past Due Detection**: Items automatically transition to "past due" status when their due date passes
- **Immutability for Past Due Items**: Once an item is past due, it cannot be modified

### Assumptions

1. The scheduler runs every minute to check for past due items
2. Past due status is also checked on-demand when retrieving items via API
3. All timestamps are stored in the server's local timezone
4. No authentication/authorization is required (as per spec)

## Tech Stack

- **Runtime**: Java 21 (Eclipse Temurin)
- **Framework**: Spring Boot 3.2.0
- **Database**: H2 In-Memory Database
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, Spring Test
- **Containerization**: Docker

### Key Libraries

- Spring Web (REST API)
- Spring Data JPA (Database access)
- Spring Validation (Request validation)
- SpringDoc OpenAPI (Swagger UI & API documentation)
- Lombok (Boilerplate reduction)
- H2 Database (In-memory persistence)

## Project Structure

```
src/
├── main/
│   ├── java/com/tradebytes/todo/
│   │   ├── config/         # Configuration (OpenAPI)
│   │   ├── controller/     # REST Controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA Entities
│   │   ├── exception/      # Custom Exceptions & Handlers
│   │   ├── mapper/         # Entity-DTO Mappers
│   │   ├── repository/     # JPA Repositories
│   │   ├── scheduler/      # Scheduled Tasks
│   │   └── service/        # Business Logic
│   └── resources/
│       ├── api.yml         # OpenAPI Specification
│       └── application.properties
└── test/
    └── java/com/tradebytes/todo/
        ├── controller/     # Controller Tests
        ├── integration/    # Integration Tests
        └── service/        # Service Unit Tests
```

## How-To Guide

### Prerequisites

- Java 21 or higher
- Maven 3.8+ (or use the included Maven wrapper)
- Docker and Docker Compose (for containerized deployment)

### Build the Service

```bash
# Using Makefile
make build

# Or using Maven directly
./mvnw package -DskipTests
```

### Run the Automatic Tests

```bash
# Using Makefile
make test

# Or using Maven directly
./mvnw test
```

### Run the Service Locally

#### Option 1: Using Maven (Development)

```bash
# Using Makefile
make run

# Or using Maven directly
./mvnw spring-boot:run
```

#### Option 2: Using Docker (Production-like)

```bash
# Build and run with Docker
make start

# Or step by step:
make docker-build
make docker-run

# View logs
make docker-logs

# Stop the service
make docker-stop
```

The service will be available at `http://localhost:8080`

## API Documentation (Swagger)

Once the service is running, you can access the interactive API documentation:

| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |
| OpenAPI YAML | http://localhost:8080/api-docs.yaml |
| Static OpenAPI Spec | `src/main/resources/api.yml` |

The Swagger UI provides an interactive interface to explore and test all API endpoints.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/todos` | Create a new todo item |
| GET | `/api/todos/{id}` | Get a todo item by ID |
| GET | `/api/todos` | Get all "not done" items |
| GET | `/api/todos?all=true` | Get all items (any status) |
| PATCH | `/api/todos/{id}/description` | Update item description |
| PATCH | `/api/todos/{id}/status` | Update item status (done/not done) |

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

#### Get All Todo Items

```bash
# Get only "not done" items
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

#### Update Status (Mark as Done)

```bash
curl -X PATCH http://localhost:8080/api/todos/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "done"}'
```

#### Update Status (Mark as Not Done)

```bash
curl -X PATCH http://localhost:8080/api/todos/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "not done"}'
```

## Response Format

### Success Response

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

### Error Response

```json
{
  "timestamp": "2026-01-11T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Todo item not found with id: 999",
  "path": "/api/todos/999"
}
```

## Status Values

- `not done` - Item is pending
- `done` - Item has been completed
- `past due` - Item's due date has passed and it was not completed (immutable, set automatically)

**Note:** Only `done` and `not done` can be set via API. The `past due` status is automatically assigned by the system when an item's due date passes.

## H2 Console

For development purposes, the H2 console is available at:

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:tododb`
- Username: `sa`
- Password: (empty)

## Make Commands Reference

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
