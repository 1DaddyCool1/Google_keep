import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Label } from './labels';

export interface NoteImage {
  id: string;
  imagePath: string;
}

export interface Note {
  id: number;
  title: string;
  content: string;
  username: string;
  createdAt: string; // ISO string from backend
  updatedAt: string; // ISO string from backend
  mediaIds: number[];
  labelIds: number[];
  isTrashed?: boolean;
  trashedAt?: string;
  isArchived?: boolean;
  archivedAt?: string;
}

export interface CreateNoteRequest {
  title: string;
  content: string;
  images?: string[]; // base64 strings
}

@Injectable({
  providedIn: 'root',
})
export class NotesService {
  private apiUrl = `${environment.apiBaseUrl}/api/notes`;
  private mediaApiUrl = `${environment.apiBaseUrl}/api/media`;

  constructor(private http: HttpClient) {}

  getAllNotes(): Observable<Note[]> {
    return this.http.get<Note[]>(this.apiUrl);
  }

  createNote(note: CreateNoteRequest): Observable<Note> {
    return this.http.post<Note>(this.apiUrl, note, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  getImage(imageId: string): Observable<Blob> {
    return this.http.get(`${this.mediaApiUrl}/${imageId}`, {
      responseType: 'blob'
    });
  }

  updateNote(note: any): Observable<Note> {
    return this.http.put<Note>(`${this.apiUrl}/${note.id}`, note);
  }

  deleteNote(noteId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${noteId}`);
  }

  updateNoteLabels(noteId: number, labelIds: number[]): Observable<Note> {
    return this.http.put<Note>(`${this.apiUrl}/${noteId}/labels`, { labelIds });
  }

  copyNote(noteId: number): Observable<Note> {
    return this.http.post<Note>(`${this.apiUrl}/${noteId}/copy`, {});
  }

  getNotesByLabel(labelId: number): Observable<Note[]> {
    return this.http.get<Note[]>(`${this.apiUrl}?labelId=${labelId}`);
  }

  getTrashedNotes(): Observable<Note[]> {
    return this.http.get<Note[]>(`${this.apiUrl}/trash`);
  }

  restoreNote(noteId: number): Observable<Note> {
    return this.http.post<Note>(`${this.apiUrl}/${noteId}/restore`, {});
  }

  deleteNotePermanently(noteId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${noteId}/permanent`);
  }

  emptyTrash(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/trash/empty`);
  }

  getArchivedNotes(): Observable<Note[]> {
    return this.http.get<Note[]>(`${this.apiUrl}/archived`);
  }

  archiveNote(noteId: number): Observable<Note> {
    return this.http.post<Note>(`${this.apiUrl}/${noteId}/archive`, {});
  }

  unarchiveNote(noteId: number): Observable<Note> {
    return this.http.post<Note>(`${this.apiUrl}/${noteId}/unarchive`, {});
  }
}
