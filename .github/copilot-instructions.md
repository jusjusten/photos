# Copilot Instructions for Photos Management System

## Project Overview
A Java photo management application with user accounts, albums, photo organization, and flexible tag-based searching. The system uses serialization for persistent storage and supports both admin and regular user roles.

## Architecture & Data Flow

### Core Components
- **DataManager** (singleton pattern): Central hub for all data persistence and login logic
  - Manages Admin and User object persistence in `data/admin.dat` and `data/users/{username}.dat`
  - Handles login/logout and maintains current user state
  - Initializes data directories on first run
  
- **User**: Individual user account with albums and photos
  - Contains `List<Album>` for organization
  - Maintains `Map<String, Photo>` in-memory cache (transient) keyed by file path
  - Stores `Map<String, Set<String>> tagTypes` to define tag names and their cardinality ("single" or "multiple")
  - Default tag types: "location" (single), "person" (multiple), "event" (single)

- **Album**: Container for photos with automatic date range tracking
  - Updates date range in `updateDateRange()` when photos added/removed
  - Uses case-insensitive name comparison for equality

- **Photo**: Represents a single image file with metadata
  - Stores file path as unique identifier (used in equals/hashCode)
  - Lazy-loads JavaFX Image objects via reflection (handles non-JavaFX environments)
  - Extracts date from file's last modified timestamp
  - Manages `List<Tag>` for flexible metadata

- **Admin**: Manages user accounts and system-level operations
  - Immutable users: "admin" (system) and "stock" (always present)
  - Case-insensitive username handling
  - Deletes user data files on user removal

- **Tag**: Key-value pair with case-insensitive name/value matching
  - Implements custom `equals()` and `hashCode()` for case-insensitive comparison
  - Used for photo tagging and search

- **TagCriteria**: Encodes search logic for photos
  - Supports: single tag, AND (conjunctive), OR (disjunctive) operations
  - Used by `User.searchByTags(TagCriteria)`

## Key Patterns & Conventions

### Serialization
- All data model classes implement `Serializable`
- Use `transient` keyword for in-memory caches (e.g., `Photo.thumbnail`, `User.allPhotos`)
- Custom `writeObject()` and `readObject()` methods restore transient state
- File storage: `data/admin.dat` and `data/users/{username}.dat`

### Null Safety
- Constructor parameters null-check with defaults:
  ```java
  this.name = name != null ? name.toLowerCase() : "";
  ```
- All getter methods return defensive copies (`new ArrayList<>()`, `new HashMap<>()`)

### Case-Insensitivity
- Username, album name, and tag name comparisons use `.equalsIgnoreCase()`
- Tag names always stored lowercase in constructor
- Reflects real-world need for case-insensitive searches (e.g., "John" vs "john")

### Defensive Copying
- Methods returning collections always wrap with `new ArrayList<>()` or `new HashSet<>()`
- Prevents external modification of internal state

## Workflow Patterns

### Adding a Photo
1. `User.addPhoto(File, String albumName)` checks `allPhotos` cache first
2. Creates `Photo` on first encounter (extracts date, initializes tags)
3. Stores in cache for subsequent album additions
4. Album updates its date range automatically

### Searching Photos
- **By date**: `User.searchByDateRange(Date start, Date end)` (inclusive on both bounds)
- **By tags**: `User.searchByTags(TagCriteria criteria)` → `TagCriteria.matches(Photo)`
- Tag matching is case-insensitive via `Tag.equals()`

### Admin & User Management
- Admin user has no `User` object (`currentUser == null` indicates admin login)
- Stock user always exists and is immutable
- Regular user creation: `Admin.createUser()` → creates User → `User.saveUserData()`

### Persistence Flow
```
login(username)
  ↓
DataManager.login() → Admin.userExists() check → User.loadUserData()
  ↓
Operations on currentUser
  ↓
DataManager.saveCurrentUser() or User.saveUserData()
```

## Testing Considerations
- **Serialization round-trips**: Ensure transient fields restore correctly in `readObject()`
- **Photo cache**: Verify same `File` produces same `Photo` object instance
- **Tag matching**: Test case-insensitivity (e.g., "John" matches "john")
- **Date boundaries**: Test inclusive range matching in `searchByDateRange()`
- **Album date range**: Verify updates correctly when photos added/removed
- **Admin restrictions**: Prevent "admin" and "stock" user manipulation

## Common Tasks

### Modifying User/Photo Data
1. Make changes to `currentUser` or User instance
2. Call `User.saveUserData()` or `DataManager.saveCurrentUser()` to persist

### Adding New Tag Types
User has `addTagType(String tagName, boolean isSingleValue)` to define cardinality—use this before adding tags of that type.

### JavaFX Compatibility
`Photo.loadImages()` uses reflection to load `javafx.scene.image.Image`. Gracefully falls back to file paths if JavaFX unavailable. Transient fields restore on deserialization.
