# Todo List Service

A resilient backend service for managing a simple to-do list, built with Spring Boot and Java 21.

## Service Description

This service provides a RESTful API for managing todo items with the following features:

- **CRUD Operations**: Create, read, update todo items
- **Status Management**: Mark items as "done" or "not done"
- **Automatic Past Due Detection**: Items automatically transition to "past due" status when their due date passes
- **Immutability for Past Due Items**: Once an item is past due, it cannot be modified

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
- Lombok (Boilerplate reduction)
- H2 Database (In-memory persistence)

## How-To Guide

### Prerequisites

- Java 21 or higher
- Maven 3.8+ (or use the included Maven wrapper)
- Docker and Docker Compose (for containerized deployment)
