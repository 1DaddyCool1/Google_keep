# Google Keep Project

## Project Overview
This project is a clone of Google Keep, a note-taking service that allows users to create, manage, and organize their notes and lists.

## Features
- User authentication and authorization
- Create, edit, and delete notes
- Organize notes in different categories
- Search functionality
- Share notes with other users

## Architecture
The architecture follows a client-server model:
- **Frontend:** React.js for the user interface.
- **Backend:** Node.js with Express for handling API requests.
- **Database:** MongoDB for storing user notes and data.

## Setup Instructions
### Prerequisites
- Node.js and npm installed
- MongoDB installed and running

### Installation Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/1DaddyCool1/Google_keep.git
   cd Google_keep
   ```
2. Install backend dependencies:
   ```bash
   cd backend
   npm install
   ```
3. Configure your database connection in the `.env` file.
4. Start the backend server:
   ```bash
   npm start
   ```
5. In a new terminal, install frontend dependencies:
   ```bash
   cd frontend
   npm install
   ```
6. Start the frontend server:
   ```bash
   npm start
   ```

## API Examples
### Get All Notes
```bash
GET /api/notes
```
**Response:**
```json
[
  {"id": "1", "title": "Note 1", "content": "Content of note 1"},
  {"id": "2", "title": "Note 2", "content": "Content of note 2"}
]
```

### Create a New Note
```bash
POST /api/notes
```
**Request Body:**
```json
{
  "title": "New Note",
  "content": "Content of the new note"
}
```

### Testing
To run tests, navigate to the backend directory and use:
```bash
npm test
```

---

This README provides a comprehensive guide for getting started with the Google Keep project. Happy coding!