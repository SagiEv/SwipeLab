# SwipeLab Backend - Package Hierarchy

This document provides a comprehensive overview of the package structure for the SwipeLab backend system, including package purposes, contents, and dependencies.

---

## Table of Contents

1. [Package Overview](#package-overview)
2. [Visual Package Hierarchy](#visual-package-hierarchy)
3. [Detailed Package Descriptions](#detailed-package-descriptions)
4. [Package Dependencies](#package-dependencies)
5. [Layered Architecture](#layered-architecture)

---

## Package Overview

**Base Package**: `com.swipelab`

The SwipeLab backend follows a **layered architecture** with clear separation of concerns:

| Layer | Packages | Purpose |
|-------|----------|---------|
| **Presentation** | `controller` | REST API endpoints |
| **Application** | `service`, `dto` | Business logic and data transfer |
| **Domain** | `model`, `mapper` | Core business entities and mappings |
| **Data Access** | `repository` | Database interactions |
| **Infrastructure** | `config`, `security`, `exception`, `util` | Cross-cutting concerns |

**Total Packages**: 11 main packages + 10 sub-packages = **21 packages**

---

## Visual Package Hierarchy

```
com.swipelab
├── SwipeLabApplication.java (main entry point)
│
├── 📦 config (6 files)
│   ├── AsyncConfig
│   ├── JwtConfig
│   ├── OAuth2Config
│   ├── RestClientConfig
│   ├── SecurityBeansConfig
│   └── SecurityConfig
│
├── 📦 controller (12 files)
│   ├── AdminDashboardController
│   ├── AuthController
│   ├── ClassificationController
│   ├── ExportController
│   ├── GoldImageController
│   ├── HealthController
│   ├── ImageController
│   ├── LabelController
│   ├── LeaderboardController
│   ├── TaskController
│   ├── UserController
│   └── UserDashboardController
│
├── 📦 dto (39 files)
│   ├── 📁 request (15 files)
│   │   ├── ClassificationRequest
│   │   ├── CreateRecipientGroupRequest
│   │   ├── CreateTaskRequest
│   │   ├── EmailVerificationRequest
│   │   ├── ForgotPasswordRequest
│   │   ├── GoldImageRequest
│   │   ├── ImageUploadRequest
│   │   ├── LoginRequest
│   │   ├── ReferenceImageRequest
│   │   ├── RegisterRequest
│   │   ├── ResetPasswordRequest
│   │   ├── TargetSpeciesRequest
│   │   ├── UpdateProfileRequest
│   │   ├── UpdateRecipientGroupRequest
│   │   └── UpdateTaskRequest
│   │
│   └── 📁 response (24 files)
│       ├── AuthResponse
│       ├── ClassificationResponse
│       ├── DashboardStatsResponse
│       ├── ErrorResponse
│       ├── GoldImageResponse
│       ├── ImageBatchResponse
│       ├── ImageResponse
│       ├── LeaderboardResponse
│       ├── MyTaskListResponse
│       ├── PlayTaskResponse
│       ├── RecipientGroupListResponse
│       ├── RecipientGroupResponse
│       ├── ReferenceImageResponse
│       ├── StatsResponse
│       ├── TargetSpeciesResponse
│       ├── TaskAnalyticsResponse
│       ├── TaskListResponse
│       ├── TaskProgressResponse
│       ├── TaskResponse
│       └── UserProfileResponse
│       └── (and more...)
│
├── 📦 exception (7 files)
│   ├── GlobalExceptionHandler
│   ├── InvalidCredentialsException
│   ├── InvalidTokenException
│   ├── ResourceAlreadyExistsException
│   ├── ResourceNotFoundException
│   ├── UnauthorizedException
│   └── (and more...)
│
├── 📦 mapper (2 files)
│   ├── AuthMapper
│   └── TaskMapper
│
├── 📦 model (16 files)
│   ├── 📁 entity (11 files)
│   │   ├── Badge
│   │   ├── Classification
│   │   ├── GoldImage
│   │   ├── Image
│   │   ├── Label
│   │   ├── Leaderboard
│   │   ├── RecipientGroup
│   │   ├── Task
│   │   ├── User
│   │   ├── UserBadge
│   │   └── UserStats
│   │
│   └── 📁 enums (5 files)
│       ├── AuthProvider
│       ├── BadgeType
│       ├── ClassificationDecision
│       ├── TaskStatus
│       └── UserRole
│
├── 📦 repository (10 files)
│   ├── BadgeRepository
│   ├── ClassificationRepository
│   ├── GoldImageRepository
│   ├── ImageRepository
│   ├── LabelRepository
│   ├── LeaderboardRepository
│   ├── RecipientGroupRepository
│   ├── TaskRepository
│   ├── UserBadgeRepository
│   └── UserRepository
│
├── 📦 security (8 files)
│   ├── CustomOAuth2UserService
│   ├── CustomUserDetailsService
│   ├── JwtAuthenticationFilter
│   ├── JwtTokenProvider
│   ├── OAuth2AuthenticationFailureHandler
│   ├── OAuth2AuthenticationSuccessHandler
│   ├── OAuth2SuccessHandler
│   └── 📁 enums (1 file)
│       └── OAuth2Provider
│
├── 📦 service (27 files)
│   ├── AdminDashboardService
│   ├── ClassificationService
│   ├── GoldImageService
│   ├── ImageService
│   ├── LabelService
│   ├── TaskService
│   ├── UserDashboardService
│   │
│   ├── 📁 analytics (3 files)
│   │   ├── AnalyticsService
│   │   ├── ExportService
│   │   └── StatisticsService
│   │
│   ├── 📁 auth (4 files)
│   │   ├── AuthenticationService
│   │   ├── EmailService
│   │   ├── JwtService
│   │   └── OAuth2Service
│   │
│   ├── 📁 classification (4 files)
│   │   ├── ConsensusService
│   │   ├── FraudDetectionService
│   │   ├── TaskDistributionService
│   │   └── ValidationService
│   │
│   ├── 📁 gamification (4 files)
│   │   ├── BadgeService
│   │   ├── LeaderboardService
│   │   ├── PointsService
│   │   └── StreakService
│   │
│   ├── 📁 integration (2 files)
│   │   ├── ImageFetchService
│   │   └── StardbiApiService
│   │
│   └── 📁 user (3 files)
│       ├── CredibilityService
│       ├── ProfileService
│       └── UserService
│
└── 📦 util (4 files)
    ├── DateUtils
    ├── StringUtils
    └── (and more...)
```

---

## Detailed Package Descriptions

### 1. Root Package: `com.swipelab`

**Purpose**: Application entry point

**Contents**:
- `SwipeLabApplication.java` - Spring Boot main application class

**Key Annotations**: `@SpringBootApplication`

---

### 2. `com.swipelab.config`

**Purpose**: Application configuration and setup

**Type**: Infrastructure Layer

**Contents** (6 files):
- `AsyncConfig` - Async task execution configuration
- `JwtConfig` - JWT token configuration properties
- `OAuth2Config` - OAuth 2.0 configuration
- `RestClientConfig` - REST client configuration
- `SecurityBeansConfig` - Security beans (password encoder, etc.)
- `SecurityConfig` - Main Spring Security configuration

**Key Annotations**: `@Configuration`, `@EnableWebSecurity`, `@EnableAsync`

**Dependencies**: 
- → `security` package (security beans)
- → Spring Security framework

---

### 3. `com.swipelab.controller`

**Purpose**: REST API endpoints (Presentation Layer)

**Type**: Presentation Layer

**Contents** (12 files):
- `AdminDashboardController` - Admin dashboard endpoints
- `AuthController` - Authentication endpoints (login, register, OAuth)
- `ClassificationController` - Image classification endpoints
- `ExportController` - Data export endpoints
- `GoldImageController` - Gold standard image management
- `HealthController` - Health check endpoints
- `ImageController` - Image management
- `LabelController` - Label/species management
- `LeaderboardController` - Leaderboard endpoints
- `TaskController` - Task/project management
- `UserController` - User profile management
- `UserDashboardController` - User dashboard data

**Key Annotations**: `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`

**Dependencies**:
- → `service` (business logic)
- → `dto.request` (input DTOs)
- → `dto.response` (output DTOs)
- → `security` (authentication)

**Pattern**: Controllers are thin - they delegate to services for business logic

---

### 4. `com.swipelab.dto`

**Purpose**: Data Transfer Objects for API communication

**Type**: Application Layer

**Sub-packages**:

#### 4.1. `com.swipelab.dto.request` (15 files)

**Purpose**: Request payload objects for API endpoints

**Examples**:
- `LoginRequest` - Login credentials
- `RegisterRequest` - User registration data
- `ClassificationRequest` - Image classification submission
- `CreateTaskRequest` - New task creation
- `UpdateProfileRequest` - Profile updates

**Pattern**: Immutable DTOs with validation annotations

#### 4.2. `com.swipelab.dto.response` (24 files)

**Purpose**: Response payload objects returned by API

**Examples**:
- `AuthResponse` - Authentication tokens
- `TaskResponse` - Task details
- `ClassificationResponse` - Classification result
- `DashboardStatsResponse` - Dashboard statistics
- `LeaderboardResponse` - Leaderboard entries

**Pattern**: Read-only DTOs, often converted from entities using mappers

**Dependencies**:
- Uses entities from `model.entity` (converted via mappers)

---

### 5. `com.swipelab.exception`

**Purpose**: Custom exceptions and global exception handling

**Type**: Infrastructure Layer

**Contents** (7 files):
- `GlobalExceptionHandler` - Centralized exception handling (`@ControllerAdvice`)
- `ResourceNotFoundException` - 404 errors
- `ResourceAlreadyExistsException` - Conflict errors
- `InvalidCredentialsException` - Authentication errors
- `InvalidTokenException` - Token validation errors
- `UnauthorizedException` - Authorization errors

**Key Annotations**: `@ControllerAdvice`, `@ExceptionHandler`

**Pattern**: Custom runtime exceptions extending `RuntimeException`

---

### 6. `com.swipelab.mapper`

**Purpose**: Entity ↔ DTO conversion logic

**Type**: Application Layer

**Contents** (2 files):
- `AuthMapper` - User/Auth DTOs ↔ User entity
- `TaskMapper` - Task DTOs ↔ Task entity

**Key Annotations**: `@Mapper` (MapStruct)

**Dependencies**:
- → `model.entity` (entities)
- → `dto.request`, `dto.response` (DTOs)

**Pattern**: Uses MapStruct for automatic mapping

---

### 7. `com.swipelab.model`

**Purpose**: Core domain model (entities and enums)

**Type**: Domain Layer

**Sub-packages**:

#### 7.1. `com.swipelab.model.entity` (11 files)

**Purpose**: JPA entity classes representing database tables

**Contents**:
- `User` - User accounts
- `Task` - Classification tasks/projects
- `Image` - Images to classify
- `Classification` - User classification decisions
- `Label` - Classification labels/species
- `GoldImage` - Gold standard test images
- `Badge` - Achievement badges
- `UserBadge` - User-badge junction table
- `Leaderboard` - Leaderboard entries
- `RecipientGroup` - User groups
- `UserStats` - User statistics (placeholder)

**Key Annotations**: `@Entity`, `@Table`, `@Id`, `@ManyToOne`, `@OneToMany`, `@ManyToMany`

**Pattern**: Rich domain entities with Lombok annotations (`@Data`, `@Builder`)

#### 7.2. `com.swipelab.model.enums` (5 files)

**Purpose**: Enumeration types for domain concepts

**Contents**:
- `UserRole` - USER, ADMIN, RESEARCHER
- `TaskStatus` - DRAFT, ACTIVE, PAUSED, COMPLETED, ARCHIVED
- `AuthProvider` - LOCAL, GOOGLE
- `BadgeType` - Badge categories
- `ClassificationDecision` - Classification decision types

**Pattern**: Simple Java enums, mapped to database via `@Enumerated(EnumType.STRING)`

---

### 8. `com.swipelab.repository`

**Purpose**: Data access layer (persistence)

**Type**: Data Access Layer

**Contents** (10 files):
- `UserRepository`
- `TaskRepository`
- `ImageRepository`
- `ClassificationRepository`
- `LabelRepository`
- `BadgeRepository`
- `UserBadgeRepository`
- `GoldImageRepository`
- `LeaderboardRepository`
- `RecipientGroupRepository`

**Key Interface**: Extends `JpaRepository<Entity, ID>`

**Pattern**: Spring Data JPA repositories with custom query methods

**Dependencies**:
- → `model.entity` (entities)

**Custom Methods**: Uses `@Query` annotations for complex queries

---

### 9. `com.swipelab.security`

**Purpose**: Authentication and authorization infrastructure

**Type**: Infrastructure Layer

**Contents** (8 files):
- `CustomUserDetailsService` - Loads user details for authentication
- `CustomOAuth2UserService` - OAuth 2.0 user service
- `JwtTokenProvider` - JWT token generation and validation
- `JwtAuthenticationFilter` - JWT filter for requests
- `OAuth2AuthenticationSuccessHandler` - OAuth success handling
- `OAuth2AuthenticationFailureHandler` - OAuth failure handling
- `OAuth2SuccessHandler` - (placeholder)

**Sub-package**: `security.enums`
- `OAuth2Provider` - OAuth provider enumeration

**Key Annotations**: `@Component`, `@Service`

**Dependencies**:
- → `model.entity.User`
- → `repository.UserRepository`
- → Spring Security framework

---

### 10. `com.swipelab.service`

**Purpose**: Business logic and application services

**Type**: Application Layer

**Contents** (7 top-level + 17 sub-package files = 24 total):

#### Top-Level Services:
- `AdminDashboardService` - Admin dashboard data aggregation
- `ClassificationService` - Image classification workflow
- `GoldImageService` - Gold image management
- `ImageService` - Image CRUD operations
- `LabelService` - Label management
- `TaskService` - Task lifecycle management
- `UserDashboardService` - User dashboard data

#### Sub-packages:

**10.1. `service.analytics` (3 files)**
- `AnalyticsService` - Analytics computation
- `ExportService` - Data export (CSV, JSON)
- `StatisticsService` - Statistical analysis

**10.2. `service.auth` (4 files)**
- `AuthenticationService` - Login, registration, token management
- `EmailService` - Email notifications (verification, password reset)
- `JwtService` - JWT operations
- `OAuth2Service` - OAuth 2.0 integration

**10.3. `service.classification` (4 files)**
- `ConsensusService` - Consensus calculation
- `FraudDetectionService` - Fraud/anomaly detection
- `TaskDistributionService` - Distributes images to users
- `ValidationService` - Classification validation

**10.4. `service.gamification` (4 files)**
- `BadgeService` - Badge awarding logic
- `LeaderboardService` - Leaderboard generation
- `PointsService` - Points calculation
- `StreakService` - Streak tracking

**10.5. `service.integration` (2 files)**
- `ImageFetchService` - Fetches images from external sources
- `StardbiApiService` - Integration with Stardbi API

**10.6. `service.user` (3 files)**
- `CredibilityService` - User credibility scoring
- `ProfileService` - User profile management
- `UserService` - User operations

**Key Annotations**: `@Service`, `@Transactional`

**Dependencies**:
- → `repository` (data access)
- → `model.entity` (entities)
- → Other services (composition)

**Pattern**: Service layer implements business logic, orchestrates repositories

---

### 11. `com.swipelab.util`

**Purpose**: Utility classes and helper methods

**Type**: Infrastructure Layer

**Contents** (4 files):
- `DateUtils` - Date/time utilities
- `StringUtils` - String manipulation
- Additional utility classes

**Pattern**: Static utility methods

**Dependencies**: None (utilities should be independent)

---

## Package Dependencies

### Dependency Flow (Top to Bottom)

```
┌─────────────────────────────────────────────────────┐
│              controller (REST API)                  │
│         Depends on: service, dto, security          │
└──────────────────┬──────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────┐
│          service (Business Logic)                   │
│   Depends on: repository, model, mapper, util       │
└──────────────────┬──────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────┐
│       repository (Data Access)                      │
│           Depends on: model.entity                  │
└──────────────────┬──────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────┐
│           model (Domain Entities)                   │
│              No dependencies                        │
└─────────────────────────────────────────────────────┘
```

### Cross-Cutting Concerns

These packages are used across multiple layers:

```
config ─────→ All layers (Spring configuration)
security ───→ controller, service (authentication/authorization)
exception ──→ controller, service (error handling)
dto ────────→ controller, service, mapper (data transfer)
mapper ─────→ controller, service (entity ↔ DTO conversion)
util ───────→ service, controller (utilities)
```

### Detailed Dependency Graph

| Package | Dependencies |
|---------|-------------|
| `controller` | → service, dto, security, exception |
| `service` | → repository, model, dto, mapper, security, util, other services |
| `repository` | → model.entity |
| `model` | → (none - pure domain) |
| `dto` | → (minimal - may reference enums) |
| `mapper` | → model, dto |
| `security` | → model.entity, repository, config |
| `config` | → security, service |
| `exception` | → dto.response (ErrorResponse) |
| `util` | → (none - independent utilities) |

---

## Layered Architecture

### Architecture Diagram

```
┌────────────────────────────────────────────────────────────┐
│                    Presentation Layer                      │
│                      (controller)                          │
│  - REST API endpoints                                      │
│  - Request/Response handling                               │
│  - Input validation                                        │
└──────────────────────┬─────────────────────────────────────┘
                       │
                       ↓
┌────────────────────────────────────────────────────────────┐
│                   Application Layer                        │
│            (service, dto, mapper)                          │
│  - Business logic                                          │
│  - Transaction management                                  │
│  - DTO ↔ Entity conversion                                │
│  - Service orchestration                                   │
└──────────────────────┬─────────────────────────────────────┘
                       │
                       ↓
┌────────────────────────────────────────────────────────────┐
│                    Domain Layer                            │
│                  (model)                                   │
│  - Business entities                                       │
│  - Domain logic                                            │
│  - Value objects                                           │
│  - Enumerations                                            │
└──────────────────────┬─────────────────────────────────────┘
                       │
                       ↓
┌────────────────────────────────────────────────────────────┐
│                 Data Access Layer                          │
│                   (repository)                             │
│  - Database operations                                     │
│  - Query methods                                           │
│  - JPA repositories                                        │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│              Infrastructure (Cross-Cutting)                │
│      (config, security, exception, util)                   │
│  - Security & Authentication                               │
│  - Exception handling                                      │
│  - Configuration                                           │
│  - Utilities                                               │
└────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### **Presentation Layer** (`controller`)
- ✅ Handle HTTP requests/responses
- ✅ Validate input
- ✅ Map DTOs
- ❌ NO business logic
- ❌ NO direct database access

#### **Application Layer** (`service`, `dto`, `mapper`)
- ✅ Implement business logic
- ✅ Coordinate transactions
- ✅ Orchestrate multiple repositories
- ✅ Convert entities ↔ DTOs
- ❌ NO HTTP concerns
- ❌ NO direct database queries

#### **Domain Layer** (`model`)
- ✅ Define business entities
- ✅ Contain domain logic (if any)
- ✅ Enforce business rules
- ❌ NO framework dependencies (pure POJOs)
- ❌ NO persistence logic

#### **Data Access Layer** (`repository`)
- ✅ Database CRUD operations
- ✅ Custom queries
- ✅ Data persistence
- ❌ NO business logic
- ❌ NO transaction management (handled by service)

#### **Infrastructure** (`config`, `security`, `exception`, `util`)
- ✅ Security configuration
- ✅ Global exception handling
- ✅ Application configuration
- ✅ Reusable utilities

---

## Package Design Principles

### 1. **Separation of Concerns**
Each package has a single, well-defined responsibility.

### 2. **Dependency Rule**
Dependencies flow **inward** toward the domain:
- Controllers depend on services (not vice versa)
- Services depend on repositories (not vice versa)
- Repositories depend on entities (not vice versa)

### 3. **Acyclic Dependencies**
No circular dependencies between packages.

### 4. **Common Closure Principle**
Classes that change together are packaged together:
- Auth-related services in `service.auth`
- Gamification services in `service.gamification`

### 5. **Interface Segregation**
- Repositories are interfaces (Spring Data JPA)
- Services implement specific interfaces when needed

---

## Package Metrics

| Package | Files | Purpose | Complexity |
|---------|-------|---------|-----------|
| `config` | 6 | Infrastructure setup | Low |
| `controller` | 12 | API endpoints | Low-Medium |
| `dto` | 39 | Data transfer | Low |
| `exception` | 7 | Error handling | Low |
| `mapper` | 2 | Entity-DTO conversion | Low |
| `model.entity` | 11 | Domain entities | Medium |
| `model.enums` | 5 | Enumerations | Low |
| `repository` | 10 | Data access | Low |
| `security` | 8 | Auth/Security | Medium-High |
| `service` (all) | 24 | Business logic | High |
| `util` | 4 | Utilities | Low |
| **TOTAL** | **128** | **Full Backend** | **Medium** |

---

## Best Practices

### ✅ DO:
- Keep controllers thin - delegate to services
- Use DTOs for API communication (never expose entities)
- Use mappers for entity ↔ DTO conversion
- Apply `@Transactional` at service layer
- Group related services in sub-packages
- Use Spring Data JPA for repositories

### ❌ DON'T:
- Put business logic in controllers
- Return entities directly from controllers
- Create circular dependencies between packages
- Bypass service layer from controllers
- Mix infrastructure code with domain logic

---

## Summary

The SwipeLab backend follows a **clean, layered architecture** with:
- **Clear separation of concerns** across 11 main packages
- **Logical sub-packaging** for better organization (e.g., `service.auth`, `service.gamification`)
- **Unidirectional dependencies** flowing from presentation → application → domain → data access
- **Cross-cutting infrastructure** for security, configuration, and error handling

This structure promotes **maintainability**, **testability**, and **scalability** for the SwipeLab platform.

---

**Total Lines of Code**: ~15,000+ lines
**Total Classes/Interfaces**: 128 files
**Architecture Pattern**: Layered Architecture + Domain-Driven Design (DDD) principles
