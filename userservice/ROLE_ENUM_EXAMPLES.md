# Role Enum - Quick Reference & Examples

## Quick Summary

| Component | Details |
|-----------|---------|
| **Enum Class** | `com.users.enums.Role` |
| **Database Mapping** | `@Enumerated(EnumType.STRING)` |
| **Roles Defined** | USER, ADMIN, MANAGER |
| **Storage Format** | VARCHAR(50) in database |
| **Database Values** | 'USER', 'ADMIN', 'MANAGER' |

---

## Usage Patterns

### 1️⃣ Create User with Role

```java
User user = new User();
user.setFirstName("John");
user.setEmail("john@example.com");
user.setPassword("encrypted_password");
user.setRole(Role.USER);        // ✓ Type-safe
user.setStatus("ACTIVE");

userRepository.save(user);
// Database: role = 'USER'
```

---

### 2️⃣ Convert String to Role

```java
// Method 1: Strict conversion (throws exception on invalid)
Role role = Role.fromString("admin");       // ✓ Returns Role.ADMIN
Role role = Role.fromString("user");        // ✓ Returns Role.USER
Role role = Role.fromString("ADMIN");       // ✓ Case-insensitive

// Method 2: Safe conversion (returns null on invalid)
Role role = Role.fromStringOrNull("admin");     // ✓ Returns Role.ADMIN
Role role = Role.fromStringOrNull("invalid");   // ✓ Returns null (no exception)
```

---

### 3️⃣ Query by Role

```java
// Spring Data JPA
List<User> admins = userRepository.findByRole(Role.ADMIN);
List<User> managers = userRepository.findByRole(Role.MANAGER);

// With additional conditions
List<User> activeAdmins = userRepository
    .findByRoleAndStatusAndIsDeletedFalse(Role.ADMIN, "ACTIVE");
```

---

### 4️⃣ Check User Permissions

```java
User currentUser = userRepository.findById(userId).orElseThrow();

// Check admin
if (currentUser.getRole().isAdmin()) {
    // Grant admin panel access
}

// Check manager or higher
if (currentUser.getRole().isManager()) {
    // Grant manager panel access
}

// Get display name for UI
String roleDisplay = currentUser.getRole().getDisplayName();
// Returns: "Administrator", "Standard User", or "Manager"
```

---

### 5️⃣ Spring Security Integration

```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/admin/**").hasRole("ADMIN")
            .antMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
            .anyRequest().authenticated()
            .and()
            .formLogin();
        return http.build();
    }
}

// In Controller
@RestController
@RequestMapping("/api")
public class UserController {
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<User>> listAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

### 6️⃣ REST API Request/Response

**Create User Request:**
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane@example.com",
  "password": "password123",
  "role": "MANAGER",
  "status": "ACTIVE"
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane@example.com",
  "role": "MANAGER",
  "status": "ACTIVE",
  "createdAt": "2026-08-21T10:30:00",
  "updatedAt": "2026-08-21T10:30:00",
  "isDeleted": false
}
```

---

### 7️⃣ Update User Role

```java
@PutMapping("/users/{id}/role")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<User> updateUserRole(
    @PathVariable UUID id,
    @RequestBody UpdateRoleRequest request) {
    
    User user = userRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
    // Convert string to enum
    Role newRole = Role.fromString(request.getRole());
    user.setRole(newRole);
    
    User updated = userRepository.save(user);
    // updatedAt automatically updated by JPA auditing
    
    return ResponseEntity.ok(updated);
}

class UpdateRoleRequest {
    public String role;  // "USER", "ADMIN", or "MANAGER"
}
```

---

### 8️⃣ Database Query Examples

**Find all admins:**
```sql
SELECT * FROM users WHERE role = 'ADMIN';
```

**Count users by role:**
```sql
SELECT role, COUNT(*) as count 
FROM users 
WHERE is_deleted = false 
GROUP BY role;

-- Output:
-- role   | count
-- USER   | 150
-- ADMIN  | 5
-- MANAGER| 20
```

**Update user role:**
```sql
UPDATE users 
SET role = 'MANAGER', updated_at = NOW() 
WHERE id = '550e8400-e29b-41d4-a716-446655440000';
```

**Audit trail:**
```sql
SELECT id, email, role, created_at, updated_at 
FROM users 
ORDER BY updated_at DESC 
LIMIT 10;
```

---

### 9️⃣ Error Handling

```java
@PostMapping("/users")
public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
    try {
        // Validate and convert role
        Role role = Role.fromString(request.getRole());
        
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setEmail(request.getEmail());
        user.setRole(role);
        
        User saved = userRepository.save(user);
        return ResponseEntity.status(201).body(saved);
        
    } catch (IllegalArgumentException e) {
        Map<String, String> error = Map.of(
            "error", "Invalid role",
            "message", "Role must be one of: USER, ADMIN, MANAGER",
            "received", request.getRole()
        );
        return ResponseEntity.badRequest().body(error);
    }
}
```

---

### 🔟 Batch Operations

```java
@PostMapping("/users/batch/promote-to-manager")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> promoteUsers(@RequestBody List<UUID> userIds) {
    List<User> users = userRepository.findAllById(userIds);
    
    users.forEach(user -> user.setRole(Role.MANAGER));
    userRepository.saveAll(users);
    
    return ResponseEntity.ok(Map.of(
        "message", "Successfully promoted users",
        "count", users.size()
    ));
}
```

---

## Database Schema

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
    is_deleted BOOLEAN DEFAULT false NOT NULL,
    
    -- Constraint to ensure only valid roles are stored
    CONSTRAINT chk_role CHECK (role IN ('USER', 'ADMIN', 'MANAGER'))
);

-- Create index on role for efficient queries
CREATE INDEX idx_users_role ON users(role);
```

---

## Sample Data

```sql
INSERT INTO users (id, first_name, email, password, role, status, created_at, updated_at, is_deleted) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'John', 'john@example.com', 'hash1', 'USER', 'ACTIVE', NOW(), NOW(), false),
('550e8400-e29b-41d4-a716-446655440001', 'Jane', 'jane@example.com', 'hash2', 'MANAGER', 'ACTIVE', NOW(), NOW(), false),
('550e8400-e29b-41d4-a716-446655440002', 'Admin', 'admin@example.com', 'hash3', 'ADMIN', 'ACTIVE', NOW(), NOW(), false);
```

---

## STRING vs ORDINAL - Why STRING? ✓

**ORDINAL Problem:**
```
Original Order:  USER(0), ADMIN(1), MANAGER(2)
New Order:       SUPERUSER(0), USER(1), ADMIN(2), MANAGER(3)
❌ All existing data is corrupted!
```

**STRING Solution:**
```
'USER', 'ADMIN', 'MANAGER' always stay the same
✓ Adding 'SUPERUSER' doesn't affect existing data
```

**Database Readability:**
```
STRING:   SELECT * FROM users WHERE role = 'ADMIN';     ✓ Clear
ORDINAL:  SELECT * FROM users WHERE role = 1;           ✗ Unclear
```

**Compliance/Audit:**
```
STRING:   Changed role from USER to ADMIN              ✓ Readable
ORDINAL:  Changed role from 0 to 1                     ✗ Not readable
```

---

## Files Involved

```
src/main/java/com/users/
├── enums/
│   └── Role.java                          ← Role enum definition
├── entity/
│   └── User.java                          ← Updated with Role field
├── repository/
│   └── UserRepository.java                ← JPA queries (if created)
├── controller/
│   └── UserController.java                ← REST endpoints (if created)
└── config/
    └── JpaAuditingConfig.java             ← JPA auditing config
```

---

## Common Mistakes to Avoid

### ❌ Don't do this:

```java
// ❌ Using String instead of Role enum
user.setRole("ADMIN");  // Wrong - not type-safe

// ❌ Hardcoding role checks
if (user.getRole().equals("ADMIN")) {  // Wrong - fragile string comparison

// ❌ Using ORDINAL mapping
@Enumerated(EnumType.ORDINAL)  // ❌ WRONG!
```

### ✅ Do this instead:

```java
// ✓ Use enum type
user.setRole(Role.ADMIN);  // Correct - type-safe

// ✓ Use enum method
if (user.getRole().isAdmin()) {  // Correct - safe and readable

// ✓ Use STRING mapping
@Enumerated(EnumType.STRING)  // ✓ CORRECT!
private Role role;
```

---

## References

- **Role Enum:** `com.users.enums.Role`
- **User Entity:** `com.users.entity.User`
- **Documentation:** `ROLE_ENUM_DOCUMENTATION.md`
- **Configuration:** `JpaAuditingConfig.java`

---

**Status:** Production Ready ✓
**Last Updated:** August 21, 2026
