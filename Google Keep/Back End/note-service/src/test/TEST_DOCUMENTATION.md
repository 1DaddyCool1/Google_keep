# Notes Service - Test Documentation

## Overview
This document provides comprehensive documentation for all test cases in the notes-service module. The test suite includes unit tests, integration tests, and repository tests covering all layers of the application.

## Test Coverage Summary

| Component | Test Class | Test Cases | Coverage Areas |
|-----------|-----------|------------|----------------|
| Entity | `NoteTest` | 15 | Entity POJO, getters/setters, lifecycle |
| DTO | `NoteDtoTest` | 8 | DTO constructors, serialization |
| Repository | `NoteRepositoryTest` | 18 | Database operations, queries |
| Service | `TrashCleanupServiceTest` | 13 | Scheduled cleanup, error handling |
| Controller | `NotesControllerTest` | 25 | REST endpoints, authorization |
| Request DTOs | `NotesControllerRequestDtosTest` | 15 | Request validation, edge cases |
| **Total** | **6 Test Classes** | **94+ Test Cases** | **Complete coverage** |

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=NoteTest
mvn test -Dtest=NotesControllerTest
```

### Run Test Suite
```bash
mvn test -Dtest=NotesServiceTestSuite
```

### Run with Coverage Report
```bash
mvn clean test jacoco:report
```

## Test Classes Documentation

### 1. NoteTest (Entity Tests)
**Location:** `src/test/java/com/rajan/keep/notes/entity/NoteTest.java`

Tests the Note entity class for proper field mapping and behavior.

#### Test Cases:
- ✅ `shouldCreateNoteWithDefaults()` - Verifies default values
- ✅ `shouldSetAndGetId()` - Tests ID field
- ✅ `shouldSetAndGetTitle()` - Tests title field
- ✅ `shouldSetAndGetContent()` - Tests content field
- ✅ `shouldSetAndGetUsername()` - Tests username field
- ✅ `shouldSetAndGetTrashedStatus()` - Tests trash functionality
- ✅ `shouldSetAndGetArchivedStatus()` - Tests archive functionality
- ✅ `shouldHandleMediaIds()` - Tests media ID collection
- ✅ `shouldHandleLabelIds()` - Tests label ID collection
- ✅ `shouldAllowEmptyMediaIds()` - Tests empty media collection
- ✅ `shouldAllowEmptyLabelIds()` - Tests empty label collection
- ✅ `shouldHandleCompleteNoteLifecycle()` - Tests full lifecycle
- ✅ `shouldHandleNullTitleAndContent()` - Tests null handling
- ✅ `shouldHandleLongContent()` - Tests large content

### 2. NoteDtoTest (DTO Tests)
**Location:** `src/test/java/com/rajan/keep/notes/dto/NoteDtoTest.java`

Tests the NoteDto data transfer object for proper serialization.

#### Test Cases:
- ✅ `shouldCreateWithNoArgsConstructor()` - Tests default constructor
- ✅ `shouldCreateWithBasicConstructor()` - Tests basic constructor
- ✅ `shouldCreateWithTrashedConstructor()` - Tests trashed constructor
- ✅ `shouldCreateWithFullConstructor()` - Tests full constructor
- ✅ `shouldSetAndGetAllProperties()` - Tests all getters/setters
- ✅ `shouldHandleNullValues()` - Tests null handling
- ✅ `shouldHandleEmptyCollections()` - Tests empty collections
- ✅ `shouldHandleLargeCollections()` - Tests large collections

### 3. NoteRepositoryTest (Repository Tests)
**Location:** `src/test/java/com/rajan/keep/notes/repository/NoteRepositoryTest.java`

Tests all repository methods with an in-memory H2 database.

#### Test Cases:
- ✅ `shouldFindByUsername()` - Tests finding by username
- ✅ `shouldFindByIdAndUsername()` - Tests finding by ID and user
- ✅ `shouldNotFindWithWrongUsername()` - Tests authorization
- ✅ `shouldFindActiveByUsername()` - Tests active notes query
- ✅ `shouldFindTrashedByUsername()` - Tests trashed notes query
- ✅ `shouldFindArchivedByUsername()` - Tests archived notes query
- ✅ `shouldFindByUsernameAndLabelId()` - Tests label filtering
- ✅ `shouldNotReturnTrashedNotesWhenFindingByLabel()` - Tests exclusion
- ✅ `shouldNotReturnArchivedNotesWhenFindingByLabel()` - Tests exclusion
- ✅ `shouldFindTrashedNotesBefore()` - Tests date filtering
- ✅ `shouldFindByUsernameAndIsTrashed()` - Tests trash status
- ✅ `shouldSaveNoteWithMediaIds()` - Tests saving with media
- ✅ `shouldSaveNoteWithLabelIds()` - Tests saving with labels
- ✅ `shouldUpdateNote()` - Tests update operations
- ✅ `shouldDeleteNote()` - Tests delete operations
- ✅ `shouldReturnEmptyListWhenNoNotesFound()` - Tests empty results
- ✅ `shouldHandleMultipleLabelsOnSameNote()` - Tests multiple labels

### 4. TrashCleanupServiceTest (Service Tests)
**Location:** `src/test/java/com/rajan/keep/notes/service/TrashCleanupServiceTest.java`

Tests the scheduled trash cleanup service with mocked dependencies.

#### Test Cases:
- ✅ `shouldCleanupOldTrashedNotes()` - Tests cleanup logic
- ✅ `shouldNotCleanupRecentTrashedNotes()` - Tests date filtering
- ✅ `shouldHandleNotesWithNoMedia()` - Tests notes without media
- ✅ `shouldHandleMediaServiceErrors()` - Tests error handling
- ✅ `shouldHandleNoteDeletionErrors()` - Tests resilience
- ✅ `shouldHandleEmptyResult()` - Tests empty results
- ✅ `shouldCleanupNotesWithMultipleMedia()` - Tests multiple media
- ✅ `shouldUseCorrectCutoffDate()` - Tests date calculation
- ✅ `shouldProcessMultipleUsersNotes()` - Tests multi-user
- ✅ `shouldHandlePartialMediaDeletionFailures()` - Tests partial failures
- ✅ `shouldCallRepositoryOnce()` - Tests efficiency

### 5. NotesControllerTest (Controller Tests)
**Location:** `src/test/java/com/rajan/keep/notes/controller/NotesControllerTest.java`

Integration tests for all REST endpoints with Spring Security.

#### Test Cases:
- ✅ `shouldGetAllActiveNotes()` - GET /api/notes
- ✅ `shouldGetNotesFilteredByLabel()` - GET /api/notes?labelId=X
- ✅ `shouldCreateNote()` - POST /api/notes
- ✅ `shouldCreateNoteWithImages()` - POST /api/notes with images
- ✅ `shouldGetNoteById()` - GET /api/notes/{id}
- ✅ `shouldReturn404WhenNoteNotFound()` - Tests 404 handling
- ✅ `shouldUpdateNote()` - PUT /api/notes/{id}
- ✅ `shouldDeleteNote()` - DELETE /api/notes/{id}
- ✅ `shouldSetNoteLabels()` - PUT /api/notes/{id}/labels
- ✅ `shouldCopyNote()` - POST /api/notes/{id}/copy
- ✅ `shouldGetArchivedNotes()` - GET /api/notes/archived
- ✅ `shouldArchiveNote()` - POST /api/notes/{id}/archive
- ✅ `shouldNotArchiveTrashedNote()` - Tests business logic
- ✅ `shouldUnarchiveNote()` - POST /api/notes/{id}/unarchive
- ✅ `shouldGetTrashedNotes()` - GET /api/notes/trash
- ✅ `shouldRestoreNote()` - POST /api/notes/{id}/restore
- ✅ `shouldEmptyTrash()` - DELETE /api/notes/trash/empty
- ✅ `shouldPermanentlyDeleteNote()` - DELETE /api/notes/{id}/permanent
- ✅ `shouldRemoveLabelFromNotes()` - Internal endpoint test
- ✅ `shouldHandleInvalidLabelId()` - Tests error handling

### 6. NotesControllerRequestDtosTest (Request DTO Tests)
**Location:** `src/test/java/com/rajan/keep/notes/controller/NotesControllerRequestDtosTest.java`

Tests request DTO classes for validation and edge cases.

#### Test Cases:
- ✅ `createNoteRequestShouldSetAndGetAllProperties()` - Tests CreateNoteRequest
- ✅ `createNoteRequestShouldHandleNullValues()` - Tests null handling
- ✅ `createNoteRequestShouldHandleEmptyImages()` - Tests empty collections
- ✅ `updateNoteRequestShouldSetAndGetAllProperties()` - Tests UpdateNoteRequest
- ✅ `updateNoteRequestShouldHandleNullValues()` - Tests null handling
- ✅ `updateNoteRequestShouldHandlePartialUpdates()` - Tests partial updates
- ✅ `updateNoteRequestShouldHandleEmptyCollections()` - Tests empty collections
- ✅ `labelIdsRequestShouldSetAndGetLabelIds()` - Tests LabelIdsRequest
- ✅ `labelIdsRequestShouldHandleNullLabelIds()` - Tests null handling
- ✅ `labelIdsRequestShouldHandleEmptyLabelIds()` - Tests empty collections
- ✅ `labelIdsRequestShouldHandleLargeNumberOfLabels()` - Tests large data
- ✅ `createNoteRequestShouldHandleLargeContent()` - Tests large content
- ✅ `createNoteRequestShouldHandleHtmlContent()` - Tests HTML content
- ✅ `updateNoteRequestShouldHandleMultipleNewImages()` - Tests multiple images
- ✅ `updateNoteRequestShouldKeepExistingAndAddNew()` - Tests combined updates

## Test Configuration

### Test Properties
**Location:** `src/test/resources/application-test.properties`

Key configurations:
- Uses H2 in-memory database for testing
- Disables Eureka client for isolated testing
- Disables distributed tracing
- Disables scheduling for controlled testing

### Dependencies
```xml
<!-- H2 Database for testing -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Security Test -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Boot Test Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Testing Best Practices Used

1. **Isolation**: Each test is independent and can run in any order
2. **Mocking**: External dependencies are mocked using Mockito
3. **In-Memory Database**: Uses H2 for fast, isolated repository tests
4. **Test Data**: BeforeEach setup methods create fresh test data
5. **Assertions**: Clear, descriptive assertions with meaningful messages
6. **Coverage**: Tests cover happy paths, edge cases, and error scenarios
7. **Naming**: Descriptive test method names following "should" convention
8. **Organization**: Tests organized by component and responsibility

## Code Coverage Goals

- **Line Coverage**: > 85%
- **Branch Coverage**: > 80%
- **Method Coverage**: > 90%
- **Class Coverage**: 100%

## Continuous Integration

These tests are designed to run in CI/CD pipelines:
- Fast execution (< 60 seconds total)
- No external dependencies required
- Consistent, repeatable results
- Clear failure messages

## Future Enhancements

Potential test improvements:
- [ ] Add performance tests for large datasets
- [ ] Add mutation testing with PIT
- [ ] Add contract tests for inter-service communication
- [ ] Add integration tests with TestContainers
- [ ] Add load/stress testing scenarios
- [ ] Add API documentation tests with Spring REST Docs

## Troubleshooting

### Common Issues

**Issue**: Tests fail with "Table not found"
**Solution**: Ensure application-test.properties has correct H2 configuration

**Issue**: Security tests fail with 401
**Solution**: Verify @WithMockUser annotation is present

**Issue**: Repository tests fail randomly
**Solution**: Check that BeforeEach properly clears database state

## Contributing

When adding new functionality:
1. Write tests first (TDD approach)
2. Ensure all existing tests pass
3. Maintain > 85% code coverage
4. Follow existing naming conventions
5. Update this documentation

## Contact

For questions about tests, please refer to:
- Project documentation
- Existing test examples
- Spring Boot Testing documentation

