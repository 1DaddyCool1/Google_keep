import { Component, signal, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { NotesService, Note } from '../../services/notes';
import { AuthService } from '../../services/auth';
import { LabelsService, Label } from '../../services/labels';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { forkJoin } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-trash',
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatSnackBarModule
  ],
  templateUrl: './trash.html',
  styleUrl: './trash.scss',
})
export class Trash implements OnInit {
  private notesService = inject(NotesService);
  private authService = inject(AuthService);
  private labelsService = inject(LabelsService);
  private snackBar = inject(MatSnackBar);
  private sanitizer = inject(DomSanitizer);
  private dialog = inject(MatDialog);
  
  isLoading = signal(true);
  loadedImageUrls = new Map<string, SafeUrl>();
  notes = signal<Note[]>([]);

  ngOnInit() {
    // Load notes only if user is authenticated
    if (this.authService.isAuthenticated) {
      this.loadTrashedNotes();
    } else {
      this.isLoading.set(false);
    }

    // Subscribe to authentication changes
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.loadTrashedNotes();
      } else {
        this.notes.set([]);
        this.isLoading.set(false);
      }
    });
  }

  loadTrashedNotes() {
    this.isLoading.set(true);
    this.notesService.getTrashedNotes().subscribe({
      next: (notes) => {
        // Collect all image load requests
        const imageLoadRequests: any[] = [];
        
        notes.forEach(note => {
          if (note.mediaIds && note.mediaIds.length > 0) {
            note.mediaIds.forEach(imageId => {
              const imageIdStr = String(imageId);
              if (!this.loadedImageUrls.has(imageIdStr)) {
                imageLoadRequests.push(
                  this.notesService.getImage(imageIdStr).pipe(
                    tap((blob: Blob) => {
                      const objectUrl = URL.createObjectURL(blob);
                      const safeUrl = this.sanitizer.bypassSecurityTrustUrl(objectUrl);
                      this.loadedImageUrls.set(imageIdStr, safeUrl);
                    })
                  )
                );
              }
            });
          }
        });

        // Wait for all images to load, or complete immediately if no images
        if (imageLoadRequests.length > 0) {
          forkJoin(imageLoadRequests).subscribe({
            next: () => {
              this.notes.set(notes);
              this.isLoading.set(false);
            },
            error: (error: any) => {
              console.error('Error loading images:', error);
              // Still show notes even if some images fail
              this.notes.set(notes);
              this.isLoading.set(false);
            }
          });
        } else {
          // No images to load, display notes immediately
          this.notes.set(notes);
          this.isLoading.set(false);
        }
      },
      error: (error) => {
        console.error('Error loading trashed notes:', error);
        this.snackBar.open('Failed to load trashed notes', 'Close', { duration: 3000 });
        this.isLoading.set(false);
      }
    });
  }

  getImageUrl(imageId: string): SafeUrl | undefined {
    return this.loadedImageUrls.get(imageId);
  }

  getLabelsForNote(note: Note): Label[] {
    if (!note.labelIds || note.labelIds.length === 0) {
      return [];
    }
    const allLabels = this.labelsService.getLabels();
    return allLabels.filter(label => note.labelIds.includes(Number(label.id)));
  }

  getMediaIdsAsStrings(note: Note): string[] {
    return note.mediaIds ? note.mediaIds.map(id => String(id)) : [];
  }

  restoreNote(noteId: number, event: Event) {
    event.stopPropagation();
    this.notesService.restoreNote(noteId).subscribe({
      next: () => {
        // Remove the restored note from the trash list
        this.notes.update(notes => notes.filter(note => note.id !== noteId));
        this.snackBar.open('Note restored', 'Close', { duration: 2000 });
      },
      error: (error) => {
        const errorMessage = error?.error?.message || 'Failed to restore note';
        this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
      }
    });
  }

  deleteNotePermanently(noteId: number, event: Event) {
    event.stopPropagation();
    this.notesService.deleteNotePermanently(noteId).subscribe({
      next: () => {
        // Remove the deleted note from the trash list
        this.notes.update(notes => notes.filter(note => note.id !== noteId));
        this.snackBar.open('Note permanently deleted', 'Close', { duration: 2000 });
      },
      error: (error) => {
        const errorMessage = error?.error?.message || 'Failed to permanently delete note';
        this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
      }
    });
  }

  emptyTrash() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Empty trash?',
        message: 'All notes in Trash will be permanently deleted.',
        confirmText: 'Empty Trash',
        cancelText: 'Cancel'
      },
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.notesService.emptyTrash().subscribe({
          next: () => {
            this.notes.set([]);
            this.snackBar.open('Trash emptied', 'Close', { duration: 2000 });
          },
          error: (error) => {
            const errorMessage = error?.error?.message || 'Failed to empty trash';
            this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
          }
        });
      }
    });
  }
}
