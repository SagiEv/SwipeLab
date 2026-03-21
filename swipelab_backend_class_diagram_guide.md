# SwipeLab Backend - Class Diagram Guide for Draw.io

This document provides comprehensive guidelines for creating a UML class diagram for the SwipeLab backend system. It includes general UML conventions and a complete listing of all backend classes with their attributes, methods, and relationships.

---

## Part 1: General UML Class Diagram Guidelines

### 1.1 Class Notation

A UML class is represented as a rectangle divided into three compartments:

```
┌─────────────────────┐
│    ClassName        │  ← Class Name (bold, centered)
├─────────────────────┤
│ - field1: Type      │  ← Attributes/Fields
│ + field2: Type      │
├─────────────────────┤
│ + method1(): Type   │  ← Methods/Operations
│ - method2(): Type   │
└─────────────────────┘
```

### 1.2 Visibility Modifiers

- `+` Public
- `-` Private
- `#` Protected
- `~` Package/Default

### 1.3 Relationship Types

#### Association
- **Notation**: Solid line
- **Meaning**: One class uses or interacts with another
- **Example**: User → Classification

#### Aggregation
- **Notation**: Hollow diamond at the container end
- **Meaning**: "has-a" relationship; parts can exist independently
- **Example**: Task ◇→ Label (targetSpecies)

#### Composition
- **Notation**: Filled diamond at the container end
- **Meaning**: Strong "has-a" relationship; parts cannot exist without container
- **Example**: Task ◆→ Image

#### Inheritance/Generalization
- **Notation**: Hollow triangle arrow pointing to parent
- **Meaning**: "is-a" relationship
- **Example**: (Not heavily used in this project)

#### Dependency
- **Notation**: Dashed arrow
- **Meaning**: One class depends on another (often temporary)
- **Example**: Service classes → Entity classes

#### Realization/Implementation
- **Notation**: Dashed line with hollow triangle
- **Meaning**: Interface implementation
- **Example**: Repository interfaces

### 1.4 Multiplicity (Cardinality)

Place multiplicity indicators near the association ends:

- `1` - Exactly one
- `0..1` - Zero or one
- `*` or `0..*` - Zero or more
- `1..*` - One or more
- `n` - Exactly n instances

**Example**:
```
User 1 ────────── * Classification
     (one user has many classifications)
```

### 1.5 Design Tips for Draw.io

1. **Layout**: Arrange classes logically
   - Core entities in the center
   - Related classes grouped together
   - Services/repositories in separate layers

2. **Color Coding** (recommended):
   - **Entities**: Light blue
   - **Enumerations**: Light yellow
   - **Services**: Light green
   - **Repositories**: Light orange
   - **DTOs**: Light gray (if included)

3. **Simplification**: For large diagrams, consider:
   - Showing only key attributes and methods
   - Creating multiple diagrams for different subsystems
   - Using packages to group related classes

4. **Notes**: Add notes for important design decisions or constraints

---

## Part 2: Backend Classes Listing

### 2.1 Core Entity Classes

#### **User**
**Location**: `com.swipelab.model.entity.User`

**Purpose**: Represents a user account in the system with authentication, profile, and gamification data.

**Key Attributes**:
- `- username: String` (PK, unique, not blank)
- `- email: String` (unique, not blank)
- `- passwordHash: String` (nullable for OAuth users)
- `- emailVerified: Boolean` (default: false)
- `- provider: AuthProvider` (enum: LOCAL, GOOGLE)
- `- providerId: String` (external OAuth ID)
- `- role: UserRole` (enum: USER, ADMIN, RESEARCHER)
- `- refreshTokenHash: String`
- `- resetPasswordToken: String`
- `- resetTokenExpiry: LocalDateTime`
- `- emailVerificationToken: String`
- `- verificationTokenExpiry: LocalDateTime`
- `- displayName: String`
- `- profileImageUrl: String`
- `- credibilityScore: Double` (default: 0.0)
- `- agreementWithExperts: Double` (default: 0.0, range: -1 to 1)
- `- majorityAgreementScore: Double` (default: 0.0, range: 0 to 1)
- `- totalClassifications: Integer` (default: 0)
- `- correctGoldClassifications: Integer` (default: 0)
- `- totalGoldClassifications: Integer` (default: 0)
- `- points: Long` (default: 0)
- `- currentStreak: Integer` (default: 0)
- `- lastStreakUpdate: LocalDateTime`
- `- badges: Set<Badge>` (Many-to-Many)
- `- createdAt: LocalDateTime`
- `- updatedAt: LocalDateTime`
- `- lastLogin: LocalDateTime`
- `- active: Boolean` (default: true)
- `- accountLocked: Boolean` (default: false)
- `- isFlagged: Boolean` (default: false)

**Important Methods**: None (entity class)

**Relationships**:
- `User` ──(1)*── `Classification` (One user has many classifications)
- `User` ──(1)*── `Task` (One user creates many tasks via createdBy)
- `User` ──(*)*─ `Badge` (Many-to-Many through user_badges table)
- `User` ──(1)*── `Leaderboard` (One user has many leaderboard entries)
- `User` ──(*)*─ `RecipientGroup` (Many-to-Many; users belong to groups)

---

#### **Task**
**Location**: `com.swipelab.model.entity.Task`

**Purpose**: Represents a classification task/project with configuration and lifecycle management.

**Key Attributes**:
- `- id: Long` (PK, auto-generated)
- `- title: String` (not null)
- `- description: String`
- `- deadline: LocalDateTime`
- `- minClassificationsPerImage: Integer` (default: 3)
- `- consensusThreshold: Double` (default: 80.0)
- `- experiments: List<Long>` (stored in task_experiments table)
- `- recipientGroups: List<Long>` (stored in task_recipient_groups table)
- `- targetSpecies: List<Label>` (Many-to-Many)
- `- createdBy: User` (Many-to-One, lazy)
- `- images: List<Image>` (One-to-Many, cascade all, orphan removal)
- `- status: TaskStatus` (enum: DRAFT, ACTIVE, PAUSED, COMPLETED, ARCHIVED)
- `- createdAt: LocalDateTime`
- `- updatedAt: LocalDateTime`

**Important Methods**:
- `+ activate(): void` (state transition)
- `+ pause(): void` (state transition)
- `+ archive(): void` (state transition)
- `+ isActive(): boolean`
- `+ isArchived(): boolean`

**Relationships**:
- `Task` ──(1)*── `Image` (Composition: One task contains many images)
- `Task` ──(*)1── `User` (Many tasks created by one user)
- `Task` ──(*)*─ `Label` (Many-to-Many for targetSpecies)

---

#### **Image**
**Location**: `com.swipelab.model.entity.Image`

**Purpose**: Represents an image to be classified within a task.

**Key Attributes**:
- `- id: Long` (PK, auto-generated)
- `- imageUrl: String` (not null)
- `- thumbnailUrl: String`
- `- caption: String`
- `- experimentId: Long`
- `- priority: Integer` (default: 0)
- `- isGoldStandard: Boolean` (default: false)
- `- correctLabel: Label` (Many-to-One, lazy; for gold standard images)
- `- task: Task` (Many-to-One, lazy, not null)
- `- createdAt: LocalDateTime`

**Important Methods**: None (entity class)

**Relationships**:
- `Image` ──(*)1── `Task` (Many images belong to one task)
- `Image` ──(1)*── `Classification` (One image has many classifications)
- `Image` ──(*)0..1── `Label` (Many images may reference one correct label for gold standard)
- `Image` ──(1)0..1── `GoldImage` (One-to-One; image may have gold image metadata)

---

#### **Classification**
**Location**: `com.swipelab.model.entity.Classification`

**Purpose**: Represents a user's classification decision for an image.

**Key Attributes**:
- `- id: Long` (PK, auto-generated)
- `- user: User` (Many-to-One, lazy, not null)
- `- image: Image` (Many-to-One, lazy, not null)
- `- label: Label` (Many-to-One, lazy, not null)
- `- createdAt: LocalDateTime`

**Important Methods**: None (entity class)

**Relationships**:
- `Classification` ──(*)1── `User` (Many classifications by one user)
- `Classification` ──(*)1── `Image` (Many classifications of one image)
- `Classification` ──(*)1── `Label` (Many classifications use one label)

---

#### **Label**
**Location**: `com.swipelab.model.entity.Label`

**Purpose**: Represents a classification label/category (e.g., species name).

**Key Attributes**:
- `- id: Long` (PK, auto-generated)
- `- name: String` (not null, unique)
- `- commonName: String`
- `- description: String`

**Important Methods**: None (entity class)

**Relationships**:
- `Label` ──(1)*── `Classification` (One label used in many classifications)
- `Label` ──(*)*─ `Task` (Many-to-Many; labels are target species for tasks)
- `Label` ──(1)*── `Image` (One label can be correct answer for many gold standard images)

---

#### **GoldImage**
**Location**: `com.swipelab.model.entity.GoldImage`

**Purpose**: Holds metadata for gold standard (test) images with known correct answers.

**Key Attributes**:
- `- id: Long` (PK, auto-generated)
- `- image: Image` (One-to-One, lazy, not null, unique)
- `- difficultyLevel: String` (default: "MEDIUM")
- `- explanation: String` (text)
- `- createdAt: LocalDateTime`

**Important Methods**: None (entity class)

**Relationships**:
- `GoldImage` ──(1)1── `Image` (One-to-One relationship)

---

#### **Badge**
**Location**: `com.swipelab.model.entity.Badge`

**Purpose**: Represents an achievement badge in the gamification system.

**Key Attributes**:
- `- id: Long` (PK, auto-generated)
- `- name: String` (not null, unique)
- `- description: String`
- `- iconUrl: String`
- `- criteriaJson: String` (text; stores badge earning criteria)
- `- createdAt: LocalDateTime`

**Important Methods**: None (entity class)

**Relationships**:
- `Badge` ──(*)*─ `User` (Many-to-Many through user_badges table)
- `Badge` ──(1)*── `UserBadge` (One badge awarded many times)

---

#### **UserBadge**
**Location**: `com.swipelab.model.entity.UserBadge`

**Purpose**: Junction table entity tracking when badges were awarded to users.

**Key Attributes**:
- `- id: Long` (PK, auto-generated)
- `- user: User` (Many-to-One, lazy, not null)
- `- badge: Badge` (Many-to-One, lazy, not null)
- `- awardedAt: LocalDateTime`

**Important Methods**: None (entity class)

**Relationships**:
- `UserBadge` ──(*)1── `User` (Many awards to one user)
- `UserBadge` ──(*)1── `Badge` (Many awards of one badge)

**Unique Constraint**: (user_id, badge_id)

---

#### **Leaderboard**
**Location**: `com.swipelab.model.entity.Leaderboard`

**Purpose**: Represents leaderboard entries for different time periods.

**Key Attributes**:
- `- id: Long` (PK, auto-generated)
- `- period: String` (not null; values: WEEKLY, MONTHLY, ALL_TIME)
- `- user: User` (Many-to-One, lazy, not null)
- `- score: Double` (not null)
- `- rank: Integer` (not null)
- `- generatedAt: LocalDateTime`

**Important Methods**: None (entity class)

**Relationships**:
- `Leaderboard` ──(*)1── `User` (Many leaderboard entries for one user)

**Index**: (period, rank)

---

#### **RecipientGroup**
**Location**: `com.swipelab.model.entity.RecipientGroup`

**Purpose**: Represents a group of users who can access specific tasks.

**Key Attributes**:
- `- id: Long` (PK, auto-generated)
- `- name: String` (not null, unique)
- `- users: Set<User>` (Many-to-Many)
- `- createdAt: LocalDateTime`
- `- updatedAt: LocalDateTime`

**Important Methods**:
- `+ getUserCount(): int`
- `+ addUser(User user): void`
- `+ removeUser(User user): void`
- `+ addUsers(Set<User> usersToAdd): void`
- `+ removeUsers(Set<User> usersToRemove): void`

**Relationships**:
- `RecipientGroup` ──(*)*─ `User` (Many-to-Many through recipient_group_users table)

---

#### **UserStats**
**Location**: `com.swipelab.model.entity.UserStats`

**Purpose**: Placeholder class (currently empty in implementation).

**Note**: This class exists but has no fields or methods yet. You may exclude it from the diagram or show it as an empty placeholder.

---

### 2.2 Enumeration Classes

#### **UserRole**
**Location**: `com.swipelab.model.enums.UserRole`

**Values**:
- `USER`
- `ADMIN`
- `RESEARCHER`

---

#### **TaskStatus**
**Location**: `com.swipelab.model.enums.TaskStatus`

**Values**:
- `DRAFT`
- `ACTIVE`
- `PAUSED`
- `COMPLETED`
- `ARCHIVED`

**State Transitions**:
- DRAFT → ACTIVE
- PAUSED → ACTIVE
- ACTIVE → PAUSED
- ACTIVE → ARCHIVED
- PAUSED → ARCHIVED

---

#### **AuthProvider**
**Location**: `com.swipelab.model.enums.AuthProvider`

**Values**:
- `LOCAL`
- `GOOGLE`

---

#### **BadgeType**
**Location**: `com.swipelab.model.enums.BadgeType`

**Note**: Exists in the codebase but implementation details would need to be checked.

---

#### **ClassificationDecision**
**Location**: `com.swipelab.model.enums.ClassificationDecision`

**Note**: Exists in the codebase but implementation details would need to be checked.

---

### 2.3 Service Layer (Optional - for comprehensive diagram)

If you want to include services in your diagram to show the application architecture:

#### Key Service Classes

1. **ClassificationService**
   - `+ submitClassification(String username, ClassificationRequest): ClassificationResponse`
   - Dependencies: ClassificationRepository, ImageRepository, LabelRepository, UserRepository, BadgeService, PointsService, StreakService, FraudDetectionService, CredibilityService

2. **TaskService**
   - `+ createTask(String username, CreateTaskRequest): TaskResponse`
   - `+ getAllTasks(): List<TaskResponse>`
   - `+ getActiveTasks(): List<TaskResponse>`
   - `+ getTaskById(Long id): TaskResponse`
   - Dependencies: TaskRepository, UserRepository

3. **UserService**
   - `+ getUserProfile(String username): UserProfileResponse`
   - `+ getCurrentUserProfile(): UserProfileResponse`
   - `+ updateUserProfile(UpdateProfileRequest): UserProfileResponse`
   - Dependencies: UserRepository, AuthMapper

4. **GoldImageService**
   - Manages gold standard images

5. **BadgeService**
   - `+ checkForBadges(User): void`

6. **PointsService**
   - `+ calculateAndAddPoints(User, int basePoints): void`

7. **StreakService**
   - `+ updateStreak(User): void`

8. **CredibilityService**
   - `+ updateUserCredibility(String username, Long imageId): void`

9. **FraudDetectionService**
   - `+ analyzeClassification(User, Long responseTimeMs): void`

10. **LeaderboardService**
    - Manages leaderboard generation and retrieval

> [!TIP]
> **For Draw.io**: Show services as classes with stereotypes `<<Service>>` and only include their public methods. Use dependency arrows (dashed) from services to entities and repositories.

---

### 2.4 Repository Layer (Optional)

If including repository interfaces:

1. **UserRepository** (extends JpaRepository)
2. **TaskRepository**
3. **ImageRepository**
4. **ClassificationRepository**
5. **LabelRepository**
6. **BadgeRepository**
7. **UserBadgeRepository**
8. **GoldImageRepository**
9. **LeaderboardRepository**
10. **RecipientGroupRepository**

> [!TIP]
> **For Draw.io**: Show repositories as interfaces with stereotype `<<Repository>>`. Use realization arrows from repository interfaces to JpaRepository interface.

---

## Part 3: Recommended Diagram Organization

### 3.1 Primary Class Diagram (Core Domain)

**Include**:
- All entity classes (User, Task, Image, Classification, Label, GoldImage, Badge, Leaderboard, RecipientGroup, UserBadge)
- All enumerations (UserRole, TaskStatus, AuthProvider)
- All relationships with proper cardinality

**Exclude**:
- Services and repositories (too cluttered)
- DTOs and request/response classes

### 3.2 Service Layer Diagram (Optional)

**Include**:
- Service classes with key methods
- Dependencies between services
- Dependencies from services to entities (dashed arrows)

### 3.3 Complete Architecture Diagram (Optional)

**Include**:
- Entities (center)
- Services (left layer)
- Repositories (right layer)
- Controllers (top layer)
- Show layered architecture with dependencies

---

## Part 4: Key Relationships Summary

| From | Relationship Type | To | Cardinality | Description |
|------|------------------|-----|-------------|-------------|
| User | Association | Classification | 1 to * | User has many classifications |
| User | Association | Task | 1 to * | User creates many tasks |
| User | Association | Badge | * to * | Many-to-many through user_badges |
| User | Association | Leaderboard | 1 to * | User has many leaderboard entries |
| User | Association | RecipientGroup | * to * | Many-to-many through recipient_group_users |
| Task | Composition | Image | 1 to * | Task contains many images |
| Task | Association | User | * to 1 | Many tasks by one user (createdBy) |
| Task | Aggregation | Label | * to * | Many-to-many (targetSpecies) |
| Image | Association | Task | * to 1 | Many images in one task |
| Image | Association | Classification | 1 to * | One image has many classifications |
| Image | Association | Label | * to 0..1 | Image may have one correct label |
| Image | Association | GoldImage | 1 to 0..1 | One-to-one (optional) |
| Classification | Association | User | * to 1 | Many classifications by one user |
| Classification | Association | Image | * to 1 | Many classifications of one image |
| Classification | Association | Label | * to 1 | Classifications reference one label |
| GoldImage | Association | Image | 1 to 1 | One-to-one relationship |
| Badge | Association | User | * to * | Many-to-many through user_badges |
| UserBadge | Association | User | * to 1 | Junction table |
| UserBadge | Association | Badge | * to 1 | Junction table |
| Leaderboard | Association | User | * to 1 | Many entries for one user |
| RecipientGroup | Association | User | * to * | Many-to-many |

---

## Part 5: Notes and Constraints

> [!IMPORTANT]
> **Key Design Patterns**:
> 1. **State Pattern**: Task uses state machine with methods `activate()`, `pause()`, `archive()`
> 2. **Builder Pattern**: All entities use Lombok @Builder annotation
> 3. **Repository Pattern**: Data access through JPA repositories
> 4. **Service Layer**: Business logic separated from entities

> [!NOTE]
> **Important Constraints**:
> - User.username is the primary key (not email)
> - Classification has unique constraint: one user can classify each image only once
> - Badge names must be unique
> - RecipientGroup names must be unique
> - UserBadge has unique constraint on (user_id, badge_id)

> [!WARNING]
> **Cascade Operations**:
> - Task → Image: CascadeType.ALL with orphan removal (deleting task deletes all images)
> - Be aware of lazy loading: Most relationships use FetchType.LAZY

---

## Part 6: Draw.io Specific Tips

### Creating the Diagram

1. **Start with Entity Classes**: Create rectangles for each entity class
2. **Add Enumerations**: Use smaller rectangles with `<<enumeration>>` stereotype
3. **Define Attributes**: List key fields with visibility and type
4. **Add Methods**: Include important business methods (like Task state transitions)
5. **Draw Relationships**: Connect classes with appropriate lines and arrows
6. **Add Cardinality**: Label relationship ends with multiplicity
7. **Use Colors**: Apply consistent color scheme for entity types
8. **Add Notes**: Include important constraints or design decisions
9. **Layout**: Arrange for readability - minimize crossing lines

### Recommended Draw.io Settings

- **Grid**: Enable for alignment
- **Connectors**: Use orthogonal connectors for cleaner look
- **Auto-layout**: Consider using after initial placement
- **Layers**: Use layers to separate entities, services, repositories

### Drawing Relationships in Draw.io

- **Association**: Use "Arrow" connector
- **Composition**: Use "Filled Diamond" connector
- **Aggregation**: Use "Hollow Diamond" connector
- **Inheritance**: Use "Hollow Triangle" connector
- **Dependency**: Use dashed line with arrow

---

Good luck with your class diagram! Start with the core entities and their relationships, then expand to include services and repositories if needed.
