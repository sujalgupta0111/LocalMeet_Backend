# Developer Quick Start - Role Enum & User Entity

## 🚀 Start Here (5-Minute Setup)

### What You Have
✅ Role enum with 3 roles (USER, ADMIN, MANAGER)
✅ User entity with proper JPA mapping
✅ UUID-based ID generation
✅ Automatic audit timestamps
✅ Complete documentation

### Quick Test

```java
// 1. Create a user with role
User user = new User();
user.setFirstName("John");
user.setEmail("john@example.com");
user.setPassword("encrypted_password");
user.setRole(Role.ADMIN);           // ✓ Type-safe
user.setStatus("ACTIVE");

// 2. Save to database
User savedUser = userRepository.save(user);
System.out.println(savedUser.getId());       // UUID generated
System.out.println(savedUser.getCreatedAt()); // Auto-set timestamp

// 3. Query by role
List<User> admins = userRepository.findByRole(Role.ADMIN);

// 4. Check permissions
if (user.getRole().isAdmin()) {
    System.out.println("Has admin access");
}
```

---

## 📋 10-Minute Integration Checklist

- [ ] **View Role.java** - Understand the 3 roles
- [ ] **View User.java** - See the @Enumerated(EnumType.STRING) mapping
- [ ] **Verify Compilation** - Run `mvn clean compile`
- [ ] **Create UserRepository** - Add JPA repository interface
- [ ] **Create REST Controller** - Basic CRUD endpoints
- [ ] **Test Endpoint** - POST /users with role field
- [ ] **Check Database** - Verify role stored as 'USER', 'ADMIN', 'MANAGER'
- [ ] **View Audit Timestamps** - Verify createdAt/updatedAt auto-populated
- [ ] **Run Permission Check** - Test isAdmin() and isManager() methods
- [ ] **Review Documentation** - Understand the STRING vs ORDINAL decision

---

## 🔧 Common Development Tasks

### Task 1: Create User with Role

```java
@PostMapping("/users")
public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
    User user = new User();
    user.setFirstName(request.getFirstName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    
    // Convert string to enum
    Role role = Role.fromString(request.getRole());  // "admin" -> Role.ADMIN
    user.setRole(role);
    
    user.setStatus("ACTIVE");
    
    User saved = userRepository.save(user);
    return ResponseEntity.status(201).body(saved);
}
```

---

### Task 2: Query Users by Role

```java
// Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByRole(Role role);
    List<User> findByRoleAndStatusAndIsDeletedFalse(Role role, String status);
}

// Service
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public List<User> getAllAdmins() {
        return userRepository.findByRole(Role.ADMIN);
    }
    
    public List<User> getActiveManagers() {
        return userRepository.findByRoleAndStatusAndIsDeletedFalse(
            Role.MANAGER, 
            "ACTIVE"
        );
    }
}
```

---

### Task 3: Authorize Based on Role

```java
@RestController
@RequestMapping("/api")
public class AdminController {
    
    @GetMapping("/admin-panel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminPanel> getAdminPanel() {
        return ResponseEntity.ok(new AdminPanel());
    }
    
    @PostMapping("/users/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> promoteToManager(@PathVariable UUID id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(Role.MANAGER);
        return ResponseEntity.ok(userRepository.save(user));
    }
}
```

---

### Task 4: Validate Role Input

```java
@PostMapping("/users")
public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
    // Validate role
    try {
        Role role = Role.fromString(request.getRole());
        
        User user = new User();
        user.setRole(role);
        // ... rest of setup
        
        return ResponseEntity.ok(userRepository.save(user));
        
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Invalid role",
            "message", "Role must be one of: USER, ADMIN, MANAGER",
            "received", request.getRole()
        ));
    }
}
```

---

### Task 5: Log Role Changes

```java
@Service
public class AuditService {
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    public void logRoleChange(User user, Role oldRole, Role newRole) {
        AuditLog log = new AuditLog();
        log.setUserId(user.getId());
        log.setAction("ROLE_CHANGE");
        log.setDetails(String.format(
            "Role changed from %s to %s",
            oldRole.getDisplayName(),
            newRole.getDisplayName()
        ));
        log.setTimestamp(LocalDateTime.now());
        
        auditLogRepository.save(log);
    }
}
```

---

## 🧪 Testing Examples

### Unit Test

```java
@Test
public void testRoleConversion() {
    // Test uppercase
    Role role = Role.fromString("ADMIN");
    assertEquals(Role.ADMIN, role);
    
    // Test lowercase
    Role role2 = Role.fromString("admin");
    assertEquals(Role.ADMIN, role2);
    
    // Test invalid
    assertThrows(IllegalArgumentException.class, () -> {
        Role.fromString("invalid");
    });
}

@Test
public void testRolePermissions() {
    assertTrue(Role.ADMIN.isAdmin());
    assertTrue(Role.ADMIN.isManager());
    assertFalse(Role.USER.isManager());
    assertFalse(Role.USER.isAdmin());
}
```

---

### Integration Test

```java
@SpringBootTest
public class UserRoleIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void testSaveUserWithRole() {
        User user = new User();
        user.setFirstName("Admin");
        user.setEmail("admin@test.com");
        user.setPassword("hashed");
        user.setRole(Role.ADMIN);
        user.setStatus("ACTIVE");
        
        User saved = userRepository.save(user);
        assertNotNull(saved.getId());
        assertEquals(Role.ADMIN, saved.getRole());
    }
    
    @Test
    public void testFindByRole() {
        User admin = createUser("admin@test.com", Role.ADMIN);
        User user = createUser("user@test.com", Role.USER);
        
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        
        assertTrue(admins.stream()
            .anyMatch(u -> u.getEmail().equals("admin@test.com")));
        assertTrue(admins.stream()
            .noneMatch(u -> u.getEmail().equals("user@test.com")));
    }
    
    private User createUser(String email, Role role) {
        User user = new User();
        user.setFirstName("Test");
        user.setEmail(email);
        user.setPassword("hashed");
        user.setRole(role);
        user.setStatus("ACTIVE");
        return userRepository.save(user);
    }
}
```

---

## 📊 Database Query Examples

### View All Roles

```sql
SELECT DISTINCT role FROM users;

Output:
admin
manager
user
```

---

### Count Users per Role

```sql
SELECT role, COUNT(*) as count 
FROM users 
WHERE is_deleted = false 
GROUP BY role
ORDER BY count DESC;

Output:
role    | count
--------|-------
user    | 1050
manager | 75
admin   | 15
```

---

### Find Admins and Their Creation Dates

```sql
SELECT id, email, role, created_at 
FROM users 
WHERE role = 'ADMIN'
ORDER BY created_at DESC;

Output:
id                                   | email           | role  | created_at
-------------------------------------|-----------------|-------|---------------------
550e8400-e29b-41d4-a716-446655440000 | admin1@test.com | ADMIN | 2026-08-15 10:30:00
550e8400-e29b-41d4-a716-446655440001 | admin2@test.com | ADMIN | 2026-08-16 14:22:00
```

---

### Promote User to Manager

```sql
UPDATE users 
SET role = 'MANAGER', updated_at = NOW() 
WHERE id = '550e8400-e29b-41d4-a716-446655440000';

-- Verify the change
SELECT id, email, role, updated_at 
FROM users 
WHERE id = '550e8400-e29b-41d4-a716-446655440000';
```

---

## 🔗 Code References

### Role.java Methods

```java
// Check admin
role.isAdmin()           // boolean

// Check manager or admin
role.isManager()         // boolean

// Get display name
role.getDisplayName()    // "Administrator"

// Convert from string (strict)
Role.fromString("admin") // Throws on invalid

// Convert from string (safe)
Role.fromStringOrNull("admin") // Returns null on invalid

// Get database value
role.getDbValue()        // "admin"

// String representation
role.toString()          // "ADMIN"
```

---

### User.java Fields

```java
UUID id                          // Auto-generated
String firstName                 // Required
String lastName                  // Optional
String email                     // Required, unique
String password                  // Required
Role role                        // Required, enum
String status                    // Required
LocalDateTime createdAt          // Auto-set
LocalDateTime updatedAt          // Auto-updated
Boolean isDeleted                // Default: false
```

---

## ⚠️ Common Mistakes to Avoid

### ❌ DON'T: Use String for roles

```java
user.setRole("ADMIN");  // ❌ Wrong type, not type-safe
```

### ✅ DO: Use Role enum

```java
user.setRole(Role.ADMIN);  // ✓ Correct type-safe approach
```

---

### ❌ DON'T: Hardcode role value comparison

```java
if (user.getRole().toString().equals("ADMIN")) {  // ❌ Fragile
```

### ✅ DO: Use enum methods

```java
if (user.getRole().isAdmin()) {  // ✓ Clean and safe
```

---

### ❌ DON'T: Assume case sensitivity

```java
Role role = Role.fromString("Admin");  // Might fail
```

### ✅ DO: Let the method handle it

```java
Role role = Role.fromString("admin");  // ✓ Works (case-insensitive)
```

---

### ❌ DON'T: Ignore string conversion errors

```java
Role role = Role.fromString(userInput);  // May throw exception
```

### ✅ DO: Handle conversion safely

```java
try {
    Role role = Role.fromString(userInput);
} catch (IllegalArgumentException e) {
    // Handle error gracefully
}
```

---

## 📚 Documentation Quick Links

```
Start Here:
→ 00_INDEX_AND_OVERVIEW.md

Need Examples:
→ ROLE_ENUM_EXAMPLES.md

Want Details:
→ ROLE_ENUM_DOCUMENTATION.md
→ USER_ENTITY_DOCUMENTATION.md

Curious About STRING:
→ STRING_VS_ORDINAL.md

Quick Overview:
→ IMPLEMENTATION_SUMMARY.md
```

---

## 🎯 Typical Workflow

### 1. User Signup
```
User enters role preference → Convert string to Role enum → Create User → Save with enum → Database stores 'ADMIN'/'USER'/'MANAGER'
```

### 2. User Query
```
Query database for 'ADMIN' → ORM converts 'ADMIN' string to Role.ADMIN → Java object has Role.ADMIN enum → Use isAdmin() method
```

### 3. Permission Check
```
Get user object → Call user.getRole().isAdmin() → Boolean result → Grant/deny access
```

### 4. Role Update
```
Load user → Set user.setRole(Role.MANAGER) → Save user → Database updates to 'MANAGER' → updatedAt auto-updated
```

---

## ✨ Pro Tips

### Tip 1: Use Display Names in UI
```java
userResponse.setRoleDisplay(user.getRole().getDisplayName());
// Shows "Administrator" instead of "ADMIN" in frontend
```

---

### Tip 2: Check Multiple Roles
```java
if (user.getRole().isManager()) {  // Checks both MANAGER and ADMIN
    // Show manager dashboard
}
```

---

### Tip 3: Query Active Users by Role
```java
List<User> activeAdmins = userRepository
    .findByRoleAndStatusAndIsDeletedFalse(Role.ADMIN, "ACTIVE");
```

---

### Tip 4: Batch Promote Users
```java
List<User> users = userRepository.findByRole(Role.USER);
users.forEach(u -> u.setRole(Role.MANAGER));
userRepository.saveAll(users);  // Bulk update, all updatedAt auto-updated
```

---

### Tip 5: Order Users by Recent Updates
```java
List<User> recent = userRepository.findAll(
    Sort.by(Sort.Direction.DESC, "updatedAt")
);
```

---

## 🚀 Ready to Go!

You now have:
✅ Type-safe role management
✅ Production-ready code
✅ Complete documentation
✅ Example usage patterns
✅ Best practices implemented
✅ Security considerations addressed

**Start building! 🎉**

---

**Last Updated:** August 21, 2026
**Quality:** Production Ready
**Status:** Ready to Use ✅

