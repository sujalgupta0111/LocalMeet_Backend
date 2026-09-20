# User Entity - Production-Level Implementation Guide

## Overview
The User entity has been updated to follow Spring Boot/JPA production-level best practices with UUID-based ID generation and JPA auditing capabilities.

## Implementation Details

### 1. **UUID-Based ID Generation**
```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
@Column(columnDefinition = "VARCHAR(36)")
private UUID id;
```

**Why UUID?**
- Globally unique identifiers across distributed systems
- No sequential guessing attacks
- Better for horizontal scaling and microservices
- Suitable for production environments

**Database Mapping:**
- Stored as VARCHAR(36) to accommodate UUID string format
- Automatically generated when entity is persisted

---

### 2. **JPA Auditing Configuration**

#### Entity-Level Configuration
```java
@EntityListeners(AuditingEntityListener.class)  // Enables auditing for this entity
public class User {
    @CreatedDate   // Auto-populated on creation
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate  // Auto-updated on every modification
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

#### Application-Level Configuration
**File:** `JpaAuditingConfig.java`
```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
    
    @Bean(name = "auditorAware")
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("SYSTEM");
    }
}
```

**Features:**
- `@CreatedDate`: Automatically set when record is first created
- `@LastModifiedDate`: Automatically updated on every modification
- `updatable = false` on createdAt: Prevents accidental updates
- Uses `LocalDateTime` for better Java temporal handling

---

### 3. **Field-Level Constraints**

#### Mandatory Fields
| Field | Constraint | Reason |
|-------|-----------|--------|
| firstName | `nullable = false` | User's first name is compulsory |
| email | `unique = true`, `nullable = false` | Email must be unique and required |
| password | `nullable = false` | Password is required |
| role | `nullable = false` | User role must be defined |
| status | `nullable = false` | User status must be defined |
| createdAt | `nullable = false`, `updatable = false` | Immutable creation timestamp |
| updatedAt | `nullable = false` | Always tracked modification time |
| isDeleted | `nullable = false` | Soft delete indicator |

#### Optional Fields
| Field | Constraint | Reason |
|-------|-----------|--------|
| lastName | `nullable = true` | Optional middle/last name |

#### Column Optimization
```java
@Column(nullable = false, length = 100)
private String firstName;  // 100 chars max

@Column(unique = true, nullable = false, length = 255)
private String email;      // 255 chars for email RFC compliance

@Column(nullable = false, length = 50)
private String role;       // Limited to role values
```

---

### 4. **Data Type Changes**

| Field | Old Type | New Type | Reason |
|-------|----------|----------|--------|
| id | Long | UUID | Better for distributed systems |
| createdAt | String | LocalDateTime | Native Java-8+ temporal API |
| updatedAt | String | LocalDateTime | Better query and comparison support |
| isDeleted | Boolean | Boolean | Fixed typo (was "booela") |

---

### 5. **Configuration Files**

#### application.properties
Critical settings for UUID and JPA:
```properties
spring.jpa.properties.hibernate.id.new_generator_mappings=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

---

## How Auditing Works

### On Entity Creation
```java
User user = new User();
user.setFirstName("John");
user.setLastName("Doe");
user.setEmail("john@example.com");
// Don't set createdAt or updatedAt - they're automatic!

userRepository.save(user);
// createdAt is automatically set to current timestamp
// updatedAt is automatically set to current timestamp
// id is automatically generated as UUID
```

### On Entity Update
```java
user.setStatus("ACTIVE");
userRepository.save(user);
// updatedAt is automatically updated to current timestamp
// createdAt remains unchanged (updatable = false)
```

---

## Database Schema

The generated table will look like:
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT false NOT NULL
);
```

---

## Best Practices Implemented

✅ **UUID Generation**
- Distributed ID generation without central authority
- Prevents sequential ID prediction attacks

✅ **Automatic Auditing**
- No manual timestamp management required
- Consistent audit trail across all entities
- Updatable/Immutable field control

✅ **Column Constraints**
- Proper length specifications prevent data truncation
- Uniqueness constraints prevent duplicates
- Nullable constraints enforce data integrity

✅ **LocalDateTime**
- Better temporal handling than String
- Database-agnostic timezone handling
- Improved query performance

✅ **Soft Deletes**
- `isDeleted` field for logical deletion
- Preserves data for audit/compliance

✅ **Entity Listeners**
- Centralized auditing logic
- Reusable `AuditingEntityListener`
- No boilerplate in entity class

✅ **Lombok Integration**
- Auto-generated getters/setters
- Reduces boilerplate code
- `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`

---

## Usage Examples

### Create a User
```java
User user = new User();
user.setFirstName("John");
user.setEmail("john@example.com");
user.setPassword("encrypted_password");
user.setRole("USER");
user.setStatus("ACTIVE");

User savedUser = userRepository.save(user);
// savedUser.getId() -> UUID value
// savedUser.getCreatedAt() -> Current timestamp
// savedUser.getUpdatedAt() -> Current timestamp
```

### Update a User
```java
User user = userRepository.findById(userId).orElseThrow();
user.setStatus("INACTIVE");

User updatedUser = userRepository.save(user);
// updatedUser.getUpdatedAt() -> New timestamp
// updatedUser.getCreatedAt() -> Original creation time (unchanged)
```

### Query by CreatedDate
```java
List<User> recentUsers = userRepository.findByCreatedAtAfter(
    LocalDateTime.now().minusDays(7)
);
```

---

## Dependencies Required

Already included in pom.xml:
- ✓ `spring-boot-starter-data-jpa`
- ✓ `lombok`
- ✓ `jakarta.persistence-api` (via spring-boot-starter-data-jpa)

---

## Configuration Checklist

- [x] User entity with UUID ID
- [x] JPA auditing enabled globally
- [x] AuditingEntityListener registered
- [x] @CreatedDate and @LastModifiedDate annotations
- [x] firstName: mandatory (not nullable)
- [x] lastName: optional (nullable)
- [x] LocalDateTime for temporal fields
- [x] application.properties configured
- [x] JpaAuditingConfig created

---

## Future Enhancements

1. **Auditor Tracking**: Extend `AuditorAware` to track which user made changes
   ```java
   return () -> Optional.of(SecurityContextHolder.getContext()
       .getAuthentication().getName());
   ```

2. **Validation Annotations**: Add Bootstrap Validation
   ```java
   @NotBlank(message = "First name cannot be empty")
   private String firstName;
   ```

3. **Temporal Auditing**: Use `@CreatedBy`, `@LastModifiedBy` for user tracking
   ```java
   @CreatedBy
   private String createdBy;
   
   @LastModifiedBy
   private String modifiedBy;
   ```

4. **Enums for Constraints**: Use enums for role and status
   ```java
   @Enumerated(EnumType.STRING)
   private UserRole role;
   ```

---

## Troubleshooting

### Issue: createdAt/updatedAt not being set
**Solution:** Ensure `@EnableJpaAuditing` is present in configuration class

### Issue: UUID not generating automatically
**Solution:** Verify `GenerationType.UUID` and `new_generator_mappings=true` property

### Issue: DateTimeFormatter errors
**Solution:** Use `LocalDateTime` instead of `String` for temporal fields

---

**Last Updated:** August 21, 2026
**Status:** Production Ready ✓
