# Google Keep Frontend - Development Log
**Last Updated:** December 16, 2025

## Overview
This document outlines the complete development journey from initial project setup through advanced features, including technical implementations, challenges faced, and solutions implemented for the Google Keep-like notes application frontend.

---

## Phase 0: Project Setup & Authentication System

### 0.1 Initial Angular Project Setup

#### Technical Stack
- **Framework:** Angular 16+ (Standalone Components)
- **UI Library:** Angular Material
- **State Management:** Angular Signals
- **HTTP Client:** Angular HttpClient with RxJS
- **Styling:** SCSS
- **Routing:** Angular Router

#### Project Structure
```
src/app/
├── components/
│   ├── auth-dialog/
│   ├── sidebar/
│   ├── top-bar/
│   └── ...
├── pages/
│   ├── notes/
│   └── ...
├── services/
│   ├── auth.ts
│   ├── notes.ts
│   └── labels.ts
└── environments/
```

### 0.2 Authentication Service Implementation

#### Technical Implementation
**File Created:** `src/app/services/auth.ts`

**Features:**
- User authentication state management
- BehaviorSubject for reactive user state
- Login/Logout functionality
- Token management (if applicable)
- Current user observable stream

```typescript
export interface User {
  id: string;
  email: string;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  get isAuthenticated(): boolean {
    return !!this.currentUserSubject.value;
  }

  login(email: string, password: string): Observable<User> {
    return this.http.post<User>(`${apiUrl}/auth/login`, { email, password });
  }

  signup(email: string, password: string, name: string): Observable<User> {
    return this.http.post<User>(`${apiUrl}/auth/signup`, { email, password, name });
  }

  logout(): void {
    this.currentUserSubject.next(null);
  }
}
```

**Backend API Contracts:**
1. **POST /api/auth/login**
   - Request: `{ email: string, password: string }`
   - Response: `User` object with authentication token
   
2. **POST /api/auth/signup**
   - Request: `{ email: string, password: string, name: string }`
   - Response: `User` object with authentication token

### 0.3 Login/Signup Dialog UI

#### Technical Implementation
**Files Created:**
- `src/app/components/auth-dialog/auth-dialog.ts`
- `src/app/components/auth-dialog/auth-dialog.html`
- `src/app/components/auth-dialog/auth-dialog.scss`

**Features:**
- Material Dialog component
- Tab-based interface (Login/Signup)
- Form validation with Angular Forms
- Email and password input fields
- Error message display
- Responsive design

**UI Components:**
- MatDialog for modal
- MatFormField for inputs
- MatButton for actions
- MatTabs for switching between login/signup

```html
<mat-dialog-content>
  <mat-tab-group>
    <mat-tab label="Login">
      <form [formGroup]="loginForm" (ngSubmit)="onLogin()">
        <mat-form-field>
          <input matInput type="email" formControlName="email" placeholder="Email">
        </mat-form-field>
        <mat-form-field>
          <input matInput type="password" formControlName="password" placeholder="Password">
        </mat-form-field>
        <button mat-raised-button color="primary" type="submit">Login</button>
      </form>
    </mat-tab>
    <mat-tab label="Sign Up">
      <!-- Similar structure for signup -->
    </mat-tab>
  </mat-tab-group>
</mat-dialog-content>
```

### 0.4 Top Bar with Authentication Controls

#### Technical Implementation
**Files Created:**
- `src/app/components/top-bar/top-bar.ts`
- `src/app/components/top-bar/top-bar.html`
- `src/app/components/top-bar/top-bar.scss`

**Features:**
- Application logo/title
- Search bar (placeholder)
- User profile menu
- Login/Logout buttons
- Responsive toolbar

```typescript
export class TopBar {
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  
  currentUser$ = this.authService.currentUser$;
  
  openAuthDialog() {
    this.dialog.open(AuthDialogComponent, {
      width: '400px',
      disableClose: false
    });
  }
  
  logout() {
    this.authService.logout();
  }
}
```

### 0.5 Sidebar Navigation

#### Technical Implementation
**Files Created:**
- `src/app/components/sidebar/sidebar.ts`
- `src/app/components/sidebar/sidebar.html`
- `src/app/components/sidebar/sidebar.scss`

**Features:**
- Collapsible/expandable sidebar
- Navigation menu items (Notes, Reminders, Archive, Trash)
- Material List component
- Icons for each menu item
- Active route highlighting

```typescript
export class Sidebar {
  isExpanded = signal(true);
  
  menuItems = [
    { name: 'Notes', icon: 'lightbulb_outline', route: '/notes' },
    { name: 'Reminders', icon: 'notifications_none', route: '/reminders' },
    { name: 'Archive', icon: 'archive', route: '/archive' },
    { name: 'Trash', icon: 'delete', route: '/trash' }
  ];
  
  toggleSidebar() {
    this.isExpanded.set(!this.isExpanded());
  }
}
```

---

## Phase 1: Basic Notes Screen & CRUD Operations

### 1.1 Notes Service Foundation

#### Technical Implementation
**File Created:** `src/app/services/notes.ts`

**Interfaces:**
```typescript
export interface NoteImage {
  id: string;
  imagePath: string;
}

export interface Note {
  id: string;
  title: string;
  content: string;
  images?: NoteImage[];
  labels?: Label[];
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateNoteRequest {
  title: string;
  content: string;
  images?: string[]; // base64 strings
}
```

**Service Methods:**
```typescript
@Injectable({ providedIn: 'root' })
export class NotesService {
  private apiUrl = `${environment.apiBaseUrl}/api/notes`;
  
  getAllNotes(): Observable<Note[]> {
    return this.http.get<Note[]>(this.apiUrl);
  }
  
  createNote(note: CreateNoteRequest): Observable<Note> {
    return this.http.post<Note>(this.apiUrl, note);
  }
  
  updateNote(note: any): Observable<Note> {
    return this.http.put<Note>(`${this.apiUrl}/${note.id}`, note);
  }
  
  getImage(imageId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/images/${imageId}`, {
      responseType: 'blob'
    });
  }
}
```

**Backend API Contracts:**
1. **GET /api/notes** - Get all notes for authenticated user
2. **POST /api/notes** - Create new note
   - Request: `CreateNoteRequest` with base64 images
   - Response: Created `Note` object
3. **PUT /api/notes/{noteId}** - Update existing note
4. **GET /api/notes/images/{imageId}** - Get image blob

### 1.2 Notes Page Component

#### Technical Implementation
**Files Created:**
- `src/app/pages/notes/notes.ts`
- `src/app/pages/notes/notes.html`
- `src/app/pages/notes/notes.scss`

**Features:**
- Display notes in a responsive grid
- Loading state management
- Empty state message
- Image loading with blob URLs
- Safe URL sanitization

```typescript
export class Notes implements OnInit {
  private notesService = inject(NotesService);
  private authService = inject(AuthService);
  private sanitizer = inject(DomSanitizer);
  
  notes = signal<Note[]>([]);
  isLoading = signal(true);
  loadedImageUrls = new Map<string, SafeUrl>();
  
  ngOnInit() {
    if (this.authService.isAuthenticated) {
      this.loadNotes();
    }
  }
  
  loadNotes() {
    this.isLoading.set(true);
    this.notesService.getAllNotes().subscribe({
      next: (notes) => {
        this.notes.set(notes);
        this.loadImagesForNotes(notes);
        this.isLoading.set(false);
      }
    });
  }
  
  loadImage(imageId: string) {
    this.notesService.getImage(imageId).subscribe({
      next: (blob) => {
        const objectUrl = URL.createObjectURL(blob);
        const safeUrl = this.sanitizer.bypassSecurityTrustUrl(objectUrl);
        this.loadedImageUrls.set(imageId, safeUrl);
      }
    });
  }
}
```

### 1.3 Create Note Functionality

#### Technical Implementation
**Features:**
- Collapsed placeholder "Take a note..."
- Expanded card with title and content fields
- Rich text editing with contenteditable
- Image upload with preview
- Base64 image conversion
- Auto-save on blur
- Close button for manual save

**UI Components:**
```html
<!-- Collapsed State -->
<div class="add-note-collapsed" (click)="startAddingNote()">
  <span>Take a note...</span>
  <button mat-icon-button>
    <mat-icon>add</mat-icon>
  </button>
</div>

<!-- Expanded State -->
<mat-card class="add-note-card" (focusout)="onAddCardBlur($event)">
  <mat-form-field>
    <input matInput [(ngModel)]="newNote.title" placeholder="Title">
  </mat-form-field>
  
  <div 
    contenteditable="true"
    dir="ltr"
    (input)="onContentInput($event)"
    (focus)="onContentFocus($event)">
  </div>
  
  <!-- Image preview -->
  <div class="image-preview-container">
    <img [src]="url" *ngFor="let url of imagePreviewUrls()">
  </div>
  
  <mat-card-actions>
    <button mat-icon-button (click)="fileInput.click()">
      <mat-icon>image</mat-icon>
    </button>
    <button mat-button (click)="saveNote()">Close</button>
  </mat-card-actions>
</mat-card>
```

**Image Handling:**
```typescript
onImageSelect(event: Event) {
  const input = event.target as HTMLInputElement;
  if (input.files) {
    Array.from(input.files).forEach(file => {
      const reader = new FileReader();
      reader.onload = (e) => {
        const base64String = e.target?.result as string;
        this.newNote.images.push(base64String);
        const safeUrl = this.sanitizer.bypassSecurityTrustUrl(base64String);
        this.imagePreviewUrls.update(urls => [...urls, safeUrl]);
      };
      reader.readAsDataURL(file);
    });
  }
}
```

### 1.4 Rich Text Formatting

#### Technical Implementation
**Features:**
- Formatting toolbar with Material Menu
- Text styles: H1, H2, Normal, Bold, Italic, Underline
- Clear formatting option
- execCommand API usage

```typescript
applyFormat(format: string) {
  switch (format) {
    case 'h1':
      document.execCommand('formatBlock', false, '<h1>');
      break;
    case 'h2':
      document.execCommand('formatBlock', false, '<h2>');
      break;
    case 'bold':
      document.execCommand('bold', false);
      break;
    case 'italic':
      document.execCommand('italic', false);
      break;
    case 'underline':
      document.execCommand('underline', false);
      break;
  }
}

clearFormatting() {
  document.execCommand('removeFormat', false);
  document.execCommand('formatBlock', false, '<p>');
}
```

### 1.5 Notes Grid Display

#### Technical Implementation
**Features:**
- Responsive CSS Grid layout
- Material Card for each note
- Title, content, and images display
- HTML content rendering with [innerHTML]
- Loading placeholders for images

**CSS Grid:**
```scss
.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  padding: 16px;
}

.note-card {
  cursor: pointer;
  transition: box-shadow 0.2s;
  
  &:hover {
    box-shadow: 0 4px 8px rgba(0,0,0,0.2);
  }
}
```

### 1.6 Authentication-Gated Note Loading

#### Requirement
Only load notes when user is authenticated; defer label API calls until login.

#### Implementation
```typescript
ngOnInit() {
  // Only load if already authenticated
  if (this.authService.isAuthenticated) {
    this.loadNotes();
  } else {
    this.isLoading.set(false);
  }
  
  // Subscribe to auth changes
  this.authService.currentUser$.subscribe(user => {
    if (user) {
      this.loadNotes();
    } else {
      this.notes.set([]);
    }
  });
}
```

---

## Phase 2: Core Note Management & Editing

### 2.1 Edit Note Dialog Implementation

#### Requirement
Implement a dialog to edit existing notes with support for title, content, images, and labels.

#### Technical Implementation
**Files Created:**
- `src/app/components/edit-note-dialog/edit-note-dialog.ts`
- `src/app/components/edit-note-dialog/edit-note-dialog.html`
- `src/app/components/edit-note-dialog/edit-note-dialog.scss`

**Features:**
- Material Dialog with custom styling
- Contenteditable div for rich text content
- Image display and management
- Label assignment integration
- Auto-save on close/backdrop click

**Backend API Contract:**
- **Endpoint:** `PUT /api/notes/{noteId}`
- **Request:** Note object with updated fields
- **Response:** Updated `Note` object

### 2.2 Text Direction Fix

#### Issue Encountered
New text in the edit dialog was appearing in right-to-left (RTL) direction instead of left-to-right (LTR).

#### Fix Applied
**File:** `src/app/components/edit-note-dialog/edit-note-dialog.ts`

```typescript
onContentFocus(event: Event) {
  const element = event.target as HTMLElement;
  element.setAttribute('dir', 'ltr');
}
```

**HTML:**
```html
<div 
  class="note-content-editable"
  contenteditable="true"
  dir="ltr"
  (focus)="onContentFocus($event)">
</div>
```

### 2.3 Formatting Menu Enhancement

#### Issue
Formatting options were not clearly visible in the edit dialog, unlike the new note creation dialog.

#### Solution
- Unified formatting UI across both dialogs
- Used Material Menu with clear icons and labels
- Added formatting options: H1, H2, Normal, Bold, Italic, Underline, Clear Formatting

**Implementation:**
```typescript
applyFormat(format: string) {
  switch (format) {
    case 'h1':
      document.execCommand('formatBlock', false, '<h1>');
      break;
    case 'bold':
      document.execCommand('bold', false);
      break;
    // ... other formats
  }
}
```

### 2.4 Click-to-Edit Note Cards

#### Requirement
Clicking on a note card should open the edit dialog.

#### Implementation
**File:** `src/app/pages/notes/notes.html`

```html
<mat-card class="note-card" (click)="editNote(note)">
  <!-- Note content -->
</mat-card>
```

**File:** `src/app/pages/notes/notes.ts`

```typescript
editNote(note: Note) {
  const dialogRef = this.dialog.open(EditNoteDialogComponent, {
    data: { note, imageUrls: this.loadedImageUrls },
    disableClose: false,
    panelClass: 'edit-note-dialog-container'
  });

  dialogRef.afterClosed().subscribe(updatedNote => {
    if (updatedNote) {
      this.notes.update(notes => 
        notes.map(n => n.id === updatedNote.id ? updatedNote : n)
      );
    }
  });
}
```

### 2.5 Image Display in Edit Dialog

#### Requirement
Show existing images in the edit dialog, not just when creating notes.

#### Implementation
- Passed `loadedImageUrls` map to the dialog via data property
- Display images with remove functionality
- Support adding new images while editing

### 2.6 Auto-Save on Close/Backdrop Click

#### Requirement
Save note changes when clicking the Close button or clicking outside the dialog (backdrop).

#### Implementation
**File:** `src/app/components/edit-note-dialog/edit-note-dialog.ts`

```typescript
@HostListener('document:click', ['$event'])
onDocumentClick(event: MouseEvent) {
  const target = event.target as HTMLElement;
  if (target.classList.contains('cdk-overlay-backdrop')) {
    this.saveAndClose();
  }
}

saveAndClose() {
  // Save note via API
  this.notesService.updateNote(this.noteData).subscribe({
    next: (updatedNote) => {
      this.dialogRef.close(updatedNote);
    }
  });
}
```

---

## Phase 3: Label Management System

### 3.1 Add Label to Note Dialog

#### Requirement
Implement dialog to add/remove labels from notes with checkbox selection.

#### Technical Implementation
**Files Created:**
- `src/app/components/add-label-to-note-dialog/add-label-to-note-dialog.ts`
- `src/app/components/add-label-to-note-dialog/add-label-to-note-dialog.html`
- `src/app/components/add-label-to-note-dialog/add-label-to-note-dialog.scss`

**Features:**
- Display all available labels with checkboxes
- Pre-select labels already assigned to the note
- Two-way binding for label selection
- Save only checked labels on dialog close

**Backend API Contract:**
- **Endpoint:** `PUT /api/notes/{noteId}/labels`
- **Request:** `{ labelIds: string[] }`
- **Response:** Updated `Note` object with labels array

### 3.2 Label Pre-Selection Issue

#### Issue Encountered
The dialog was not showing already assigned labels as checked when opening the dialog.

#### Root Cause
Type mismatch between label IDs - note.labels had string IDs while checkboxes expected numbers.

#### Fix Applied
```typescript
// Convert label IDs to strings for consistent comparison
const selectedIds = this.note.labels?.map(l => String(l.id)) || [];
this.selectedLabelIds.set(selectedIds);

// In HTML template
[checked]="selectedLabelIds().includes(String(label.id))"
```

### 3.3 Label Chips on Note Cards

#### Requirement
Display label names as chips on note cards in the notes grid.

#### Implementation
**File:** `src/app/pages/notes/notes.html`

```html
@if (note.labels && note.labels.length > 0) {
  <div class="note-labels">
    @for (label of note.labels; track label.id) {
      <span class="note-label-chip">{{ label.name }}</span>
    }
  </div>
}
```

**CSS Styling:**
```scss
.note-labels {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 8px;
}

.note-label-chip {
  background-color: rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  padding: 4px 12px;
  font-size: 12px;
}
```

### 3.4 CSS Overlap Fix

#### Issue
Label chips and action buttons were overlapping on note cards.

#### Solution
- Adjusted layout to use flexbox with proper spacing
- Ensured labels section has dedicated space
- Added margin-bottom to labels container

### 3.5 Edit Labels Dialog

#### Requirement
Implement dialog to manage all labels (create, rename, delete).

#### Technical Implementation
**Files Created:**
- `src/app/components/edit-labels-dialog/edit-labels-dialog.ts`
- `src/app/components/edit-labels-dialog/edit-labels-dialog.html`
- `src/app/components/edit-labels-dialog/edit-labels-dialog.scss`

**Features:**
- List all existing labels
- Create new labels
- Rename existing labels
- Delete labels with confirmation

**Backend API Contracts:**
1. **GET /api/labels** - Get all labels
2. **POST /api/labels** - Create label
   - Request: `{ name: string }`
   - Response: New `Label` object
3. **PUT /api/labels/{labelId}** - Update label
   - Request: `{ name: string }`
   - Response: Updated `Label` object
4. **DELETE /api/labels/{labelId}** - Delete label
   - Response: 204 No Content

### 3.6 Confirmation Dialog for Delete

#### Requirement
Show confirmation dialog when deleting a label with a specific warning message.

#### Technical Implementation
**File Created:** `src/app/components/confirm-dialog/confirm-dialog.ts`

**Usage:**
```typescript
deleteLabel(labelId: string, labelName: string) {
  const dialogRef = this.dialog.open(ConfirmDialog, {
    data: {
      title: 'Delete label',
      message: `This will remove "${labelName}" label from all the Notes.`
    }
  });

  dialogRef.afterClosed().subscribe(confirmed => {
    if (confirmed) {
      this.labelsService.deleteLabel(labelId).subscribe();
    }
  });
}
```

### 3.7 Refresh Notes on Label Deletion

#### Requirement
When a label is deleted, refresh all notes to reflect the removed label.

#### Implementation
**File:** `src/app/pages/notes/notes.ts`

```typescript
ngOnInit() {
  let previousLabelCount = 0;
  this.labelsService.labels$.subscribe(labels => {
    const currentLabelCount = labels.length;
    if (previousLabelCount > 0 && currentLabelCount < previousLabelCount) {
      this.loadNotes(); // Refresh notes when labels are deleted
    }
    previousLabelCount = currentLabelCount;
  });
}
```

### 3.8 Dynamic Tooltips for Label Button

#### Requirement
Show "Add a Label" tooltip if note has no labels, otherwise show "Change Labels".

#### Implementation
```html
<button 
  mat-icon-button 
  [matTooltip]="note.labels && note.labels.length > 0 ? 'Change Labels' : 'Add a Label'">
  <mat-icon>label</mat-icon>
</button>
```

---

## Phase 4: Delete Note Functionality

### 4.1 Delete Note Implementation

#### Requirement
Clicking delete button should remove the note and show a confirmation message.

#### Technical Implementation
**File:** `src/app/pages/notes/notes.ts`

```typescript
deleteNote(noteId: string) {
  this.notesService.deleteNote(noteId).subscribe({
    next: () => {
      this.notes.update(notes => notes.filter(note => note.id !== noteId));
      this.snackBar.open('Note deleted', 'Close', { duration: 3000 });
    },
    error: (error) => {
      const errorMessage = error?.error?.message || 'Failed to delete note';
      this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
    }
  });
}
```

**Backend API Contract:**
- **Endpoint:** `DELETE /api/notes/{noteId}`
- **Response:** 204 No Content or success message

---

## Phase 5: Advanced Label Filtering (Current Session)

## 5.1 Label-Based Note Filtering with User-Friendly URLs

### Requirement
Clicking on a label name in the sidebar should display notes associated with that label, with the browser URL showing the label name (`/notes?labelName={labelName}`) while the API uses the label ID internally (`/notes?label={labelId}`).

### Technical Implementation

#### 1.1 Sidebar Navigation Update
**File:** `src/app/components/sidebar/sidebar.ts`

**Changes:**
- Modified `selectLabel()` method to navigate with `labelName` as query parameter instead of `labelId`
- Added label lookup to find the label object and extract its name

```typescript
selectLabel(labelId: string) {
  const label = this.labels().find(l => l.id === labelId);
  if (label) {
    this.router.navigate(['/notes'], { queryParams: { labelName: label.name } });
  }
}
```

#### 1.2 Notes Component Enhancement
**File:** `src/app/pages/notes/notes.ts`

**Changes:**
- Added `selectedLabelName` signal to track the display name
- Updated `ngOnInit()` to read `labelName` from URL query params
- Implemented mapping logic to convert `labelName` to `labelId` for API calls
- Updated `clearLabelFilter()` to clear both signals

```typescript
selectedLabelId = signal<string | null>(null);
selectedLabelName = signal<string | null>(null);

ngOnInit() {
  this.route.queryParams.subscribe(params => {
    const labelName = params['labelName'];
    if (labelName) {
      const labels = this.labelsService.getLabels();
      const label = labels.find(l => l.name === labelName);
      if (label) {
        this.selectedLabelId.set(label.id);
        this.selectedLabelName.set(label.name);
      } else {
        this.selectedLabelId.set(null);
        this.selectedLabelName.set(null);
      }
    } else {
      this.selectedLabelId.set(null);
      this.selectedLabelName.set(null);
    }
    // ... load notes
  });
}
```

#### 1.3 Filter Info Bar Update
**File:** `src/app/pages/notes/notes.html`

**Changes:**
- Updated filter bar to display the actual label name instead of generic text

```html
@if (selectedLabelId()) {
  <div class="filter-info-bar">
    <mat-icon>label</mat-icon>
    <span>{{ selectedLabelName() }}</span>
    <button mat-icon-button (click)="clearLabelFilter()" matTooltip="Clear filter">
      <mat-icon>close</mat-icon>
    </button>
  </div>
}
```

### Result
- **Browser URL:** `/notes?labelName=Work` (user-friendly, readable)
- **API Call:** `/api/notes?labelId={labelId}` (uses ID internally)
- **Display:** Shows actual label name "Work" in filter bar

---

## 5.2 API Endpoint Fix for Label Filtering

### Issue Encountered
The `getNotesByLabel()` method was using a path parameter (`/notes/label/{labelId}`) instead of a query parameter.

### Fix Applied
**File:** `src/app/services/notes.ts`

**Before:**
```typescript
getNotesByLabel(labelId: string): Observable<Note[]> {
  return this.http.get<Note[]>(`${this.apiUrl}/label/${labelId}`);
}
```

**After:**
```typescript
getNotesByLabel(labelId: string): Observable<Note[]> {
  return this.http.get<Note[]>(`${this.apiUrl}?labelId=${labelId}`);
}
```

### Backend API Contract
**Endpoint:** `GET /api/notes?labelId={labelId}`
- **Method:** GET
- **Query Parameter:** `labelId` (string)
- **Response:** Array of `Note` objects matching the label

---

## 5.3 Dynamic Note Removal on Label Change

### Requirement
When viewing notes filtered by a specific label, if a user removes that label from a note (via "Change Labels"), the note should automatically disappear from the filtered view.

### Technical Implementation
**File:** `src/app/pages/notes/notes.ts`

**Changes:**
- Enhanced `addLabelToNote()` method to check if the note still has the filtered label after update
- Added logic to remove the note from the list if the filtered label was removed
- Maintained note update if the filtered label is still present

```typescript
addLabelToNote(note: Note) {
  const dialogRef = this.dialog.open(AddLabelToNoteDialogComponent, {
    data: { note },
    disableClose: false,
    width: '400px'
  });

  dialogRef.afterClosed().subscribe(labelIds => {
    if (labelIds !== undefined) {
      this.notesService.updateNoteLabels(note.id, labelIds).subscribe({
        next: (updatedNote) => {
          const currentLabelId = this.selectedLabelId();
          if (currentLabelId) {
            const noteStillHasLabel = updatedNote.labels?.some(
              label => String(label.id) === String(currentLabelId)
            );
            if (!noteStillHasLabel) {
              // Remove from list
              this.notes.update(notes => notes.filter(n => n.id !== updatedNote.id));
            } else {
              // Update in list
              this.notes.update(notes => 
                notes.map(n => n.id === updatedNote.id ? updatedNote : n)
              );
            }
          } else {
            // Not filtering, just update
            this.notes.update(notes => 
              notes.map(n => n.id === updatedNote.id ? updatedNote : n)
            );
          }
          this.snackBar.open('Labels updated', 'Close', { duration: 2000 });
        },
        error: (error) => {
          const errorMessage = error?.error?.message || 'Failed to update labels';
          this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
        }
      });
    }
  });
}
```

### Issue Faced: Type Mismatch
**Problem:** Notes were being removed even when adding other labels (not removing the filtered one).

**Root Cause:** Type comparison issue between label IDs (string vs number).

**Solution:** Added `String()` conversion on both sides of the comparison:
```typescript
const noteStillHasLabel = updatedNote.labels?.some(
  label => String(label.id) === String(currentLabelId)
);
```

---

## 5.4 Copy Note Functionality

### Requirement
Implement "Make a copy" functionality that creates a duplicate of the current note with all its content, images, and labels.

### Technical Implementation

#### 4.1 Service Layer
**File:** `src/app/services/notes.ts`

**Added Method:**
```typescript
copyNote(noteId: string): Observable<Note> {
  return this.http.post<Note>(`${this.apiUrl}/${noteId}/copy`, {});
}
```

#### 4.2 Component Layer
**File:** `src/app/pages/notes/notes.ts`

**Implementation:**
```typescript
copyNote(note: Note) {
  this.notesService.copyNote(note.id).subscribe({
    next: (copiedNote) => {
      // Add the copied note to the top of the list
      this.notes.update(notes => [copiedNote, ...notes]);
      
      // Load images for the copied note
      if (copiedNote.images && copiedNote.images.length > 0) {
        copiedNote.images.forEach(image => {
          this.loadImage(image.id);
        });
      }
      
      this.snackBar.open('Copied Note Created', 'Close', { duration: 2000 });
    },
    error: (error) => {
      const errorMessage = error?.error?.message || 'Failed to copy note';
      this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
    }
  });
}
```

### User Feedback Enhancement
**Requirement:** Show a user-friendly alert when a note is copied.

**Implementation:** Updated success message from "Note copied" to "Copied Note Created" with a Close button, displayed at the bottom of the screen for 2 seconds.

### Backend API Contract
**Endpoint:** `POST /api/notes/{noteId}/copy`
- **Method:** POST
- **Path Parameter:** `noteId` (string)
- **Request Body:** Empty `{}`
- **Response:** New `Note` object with:
  - New unique ID
  - Same title, content, images, and labels as original
  - Current timestamps (createdAt, updatedAt)
  - Duplicated image records (new IDs pointing to copied/shared image files)

---

## Summary of Files Modified

### Component Files
1. **src/app/components/sidebar/sidebar.ts**
   - Updated label navigation to use labelName in URL

2. **src/app/pages/notes/notes.ts**
   - Added selectedLabelName signal
   - Implemented labelName to labelId mapping
   - Enhanced label change handling for filtered views
   - Implemented copy note functionality

3. **src/app/pages/notes/notes.html**
   - Updated filter info bar to display label name

### Service Files
1. **src/app/services/notes.ts**
   - Fixed getNotesByLabel to use query parameter
   - Added copyNote method

---

## Backend API Requirements Summary

### Existing APIs Used
1. **GET /api/notes?labelId={labelId}**
   - Filters notes by label ID
   - Returns array of Note objects

2. **PUT /api/notes/{noteId}/labels**
   - Updates labels for a note
   - Request: `{ labelIds: string[] }`
   - Response: Updated Note object

### New API Required
1. **POST /api/notes/{noteId}/copy**
   - Creates a duplicate of the specified note
   - Request: Empty body
   - Response: New Note object with duplicated content

---

## Key Technical Decisions

### 1. URL Design Pattern
- **Decision:** Show human-readable label names in URL while using IDs for API calls
- **Rationale:** Improves user experience with readable URLs while maintaining efficient database queries

### 2. Type Safety in Comparisons
- **Decision:** Use `String()` conversion for all ID comparisons
- **Rationale:** Prevents type mismatch issues between string and number IDs from different sources

### 3. Reactive State Management
- **Decision:** Use Angular signals for state management
- **Rationale:** Provides reactive updates with better performance and simpler syntax

### 4. User Feedback
- **Decision:** Use MatSnackBar for all user notifications
- **Rationale:** Consistent, non-intrusive feedback aligned with Material Design principles

---

## Testing Considerations

### Scenarios to Test
1. **Label Filtering:**
   - Click label in sidebar → Verify URL shows labelName
   - Verify only notes with that label are displayed
   - Clear filter → Verify all notes are shown

2. **Label Change in Filtered View:**
   - Filter by label A
   - Remove label A from a note → Verify note disappears
   - Add label B to a note → Verify note remains visible

3. **Copy Note:**
   - Click copy icon → Verify new note appears at top
   - Verify all content, images, and labels are duplicated
   - Verify success message appears

4. **Edge Cases:**
   - Filter by non-existent label name → Verify graceful handling
   - Copy note with no images → Verify no errors
   - Change labels when not filtering → Verify note updates normally

---

## Future Enhancements

### Potential Improvements
1. **URL Encoding:** Handle special characters in label names
2. **Loading States:** Add loading indicators for copy operation
3. **Batch Operations:** Support copying multiple notes at once
4. **Undo Support:** Add ability to undo note copy
5. **Search Integration:** Combine label filtering with text search

---

## Conclusion
Today's development successfully implemented advanced label-based filtering with user-friendly URLs, dynamic note management, and copy functionality. All features maintain data consistency and provide clear user feedback through Material Design components.
