.PHONY: help build test run clean docker-build docker-run docker-stop docker-logs

# Default target
help:
	@echo "Todo List Service - Available Commands"
	@echo "======================================="
	@echo ""
	@echo "Local Development:"
	@echo "  make build        - Build the application (skip tests)"
	@echo "  make build-full   - Build the application (with tests)"
	@echo "  make test         - Run all tests"
	@echo "  make run          - Run the application locally"
	@echo "  make clean        - Clean build artifacts"
	@echo ""
	@echo "Docker:"
	@echo "  make docker-build - Build Docker image"
	@echo "  make docker-run   - Run the application in Docker"
	@echo "  make docker-stop  - Stop Docker containers"
	@echo "  make docker-logs  - View Docker container logs"
	@echo "  make docker-clean - Remove Docker containers and images"
	@echo ""
	@echo "Utilities:"
	@echo "  make deps         - Download dependencies"
	@echo "  make lint         - Run code style checks"

# ===================
# Local Development
# ===================

# Build the application (skip tests for faster builds)
build:
	./mvnw package -DskipTests -B

# Build with tests
build-full:
	./mvnw package -B

# Run all tests
test:
	./mvnw test -B

# Run the application locally
run:
	./mvnw spring-boot:run

# Clean build artifacts
clean:
	./mvnw clean

# Download dependencies
deps:
	./mvnw dependency:go-offline -B

# Run code style checks (if checkstyle is configured)
lint:
	./mvnw checkstyle:check || echo "Checkstyle not configured"

# ===================
# Docker Commands
# ===================

# Build Docker image
docker-build:
	docker build -t todo-service:latest .

# Run the application in Docker using docker-compose
docker-run:
	docker-compose up -d

# Run in foreground (useful for debugging)
docker-run-fg:
	docker-compose up

# Stop Docker containers
docker-stop:
	docker-compose down

# View Docker container logs
docker-logs:
	docker-compose logs -f

# Remove Docker containers and images
docker-clean:
	docker-compose down --rmi all --volumes --remove-orphans

# Rebuild and run Docker
docker-rebuild: docker-stop docker-build docker-run

# ===================
# Combined Commands
# ===================

# Full build and test
all: clean build-full

# Quick start with Docker
start: docker-build docker-run
	@echo ""
	@echo "Todo Service is starting..."
	@echo "API will be available at: http://localhost:8080/api/todos"
	@echo "H2 Console available at: http://localhost:8080/h2-console"
	@echo ""
	@echo "Use 'make docker-logs' to view logs"
	@echo "Use 'make docker-stop' to stop the service"
