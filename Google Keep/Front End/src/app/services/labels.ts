import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth';

export interface Label {
  id: number;
  name: string;
  createdAt: string; // ISO string from backend
}

@Injectable({
  providedIn: 'root'
})
export class LabelsService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiUrl = `${environment.apiBaseUrl}/api/labels`;
  
  private labelsSubject = new BehaviorSubject<Label[]>([]);
  public labels$ = this.labelsSubject.asObservable();

  constructor() {
    // Load labels only when user is authenticated
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.loadLabels();
      } else {
        // Clear labels when user logs out
        this.labelsSubject.next([]);
      }
    });
  }

  loadLabels(): void {
    this.http.get<Label[]>(this.apiUrl).subscribe({
      next: (labels) => {
        this.labelsSubject.next(labels);
      },
      error: (error) => {
        console.error('Error loading labels:', error);
      }
    });
  }

  getLabels(): Label[] {
    return this.labelsSubject.value;
  }

  createLabel(name: string): Observable<Label> {
    return this.http.post<Label>(this.apiUrl, { name: name.trim() }).pipe(
      tap(newLabel => {
        const currentLabels = this.labelsSubject.value;
        this.labelsSubject.next([...currentLabels, newLabel]);
      })
    );
  }

  deleteLabel(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        const currentLabels = this.labelsSubject.value;
        const updatedLabels = currentLabels.filter(label => label.id !== id);
        this.labelsSubject.next(updatedLabels);
      })
    );
  }

  updateLabel(id: number, name: string): Observable<Label> {
    return this.http.put<Label>(`${this.apiUrl}/${id}`, { name: name.trim() }).pipe(
      tap(updatedLabel => {
        const currentLabels = this.labelsSubject.value;
        const updatedLabels = currentLabels.map(label =>
          label.id === id ? updatedLabel : label
        );
        this.labelsSubject.next(updatedLabels);
      })
    );
  }
}
