# Google Keep Clone Workspace

This workspace contains two subprojects:
- **`frontend/`**: Angular (latest) app - Google Keep clone UI
- **`backend/`**: Spring Boot (Java 21) REST API

## ✅ What's Implemented

### Frontend (Angular)
✅ **Top Bar** with:
- Menu toggle button (3-dash hamburger icon)
- Google Keep logo and title
- User account icon placeholder (login/logout to be implemented)

✅ **Left Sidebar** with:
- Toggle functionality (expand/collapse)
- 5 menu items with Material icons:
  - Notes (lightbulb)
  - Reminders (notification bell)
  - Edit Labels (edit icon)
  - Archive (archive icon)
  - Trash (delete icon)
- Active route highlighting
- Smooth animations

✅ **Layout & Styling**:
- Google Keep-inspired yellow theme
- Material Design components
- Responsive sidebar (280px expanded, 80px collapsed)
- Clean white background with proper spacing

### Backend (Spring Boot)
- Basic REST API setup
- `NotesController` with GET/POST endpoints at `/api/notes`
- CORS configured for `http://localhost:4200`
- Server running on port 8080

## Prerequisites
- Node.js 18+ and npm
- Java 21 (JDK)
- Maven (uses wrapper `mvnw`)

## 🚀 Run Instructions (PowerShell)

### Frontend
```powershell
cd frontend
npm install  # Only needed once
npm run start
```
Frontend will be available at **`http://localhost:4200`**

### Backend
**Note**: Requires Java 21 on PATH. If build fails, set JAVA_HOME:
```powershell
# Set JAVA_HOME to your JDK 21 installation
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

# Build and run
cd backend
./mvnw spring-boot:run
```
Backend API will run on **`http://localhost:8080`**

## 📁 Project Structure

### Frontend
```
frontend/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── sidebar/      # Left navigation menu
│   │   │   └── top-bar/      # Header with logo
│   │   ├── pages/
│   │   │   └── notes/        # Notes page (placeholder)
│   │   ├── app.ts            # Main app component
│   │   └── app.routes.ts     # Routing configuration
│   └── styles.scss           # Global styles (Google Keep theme)
```

### Backend
```
backend/
└── src/main/java/com/example/keep/
    ├── BackendApplication.java
    └── NotesController.java  # REST endpoints
```

## 🎯 Next Steps
- [ ] Implement note creation and display
- [ ] Add note editing and deletion
- [ ] Implement reminders functionality
- [ ] Add label management
- [ ] Implement archive and trash
- [ ] Add authentication (login/logout)
- [ ] Connect frontend to backend API
