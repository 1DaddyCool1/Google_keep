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
import { EditNoteDialogComponent } from '../../components/edit-note-dialog/edit-note-dialog';
import { AddLabelToNoteDialogComponent } from '../../components/add-label-to-note-dialog/add-label-to-note-dialog';

@Component({
  selector: 'app-archived',
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatSnackBarModule
  ],
  templateUrl: './archived.html',
  styleUrl: './archived.scss',
})
export class Archived implements OnInit {
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
      this.loadArchivedNotes();
    } else {
      this.isLoading.set(false);
    }

    // Subscribe to authentication changes
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.loadArchivedNotes();
      } else {
        this.notes.set([]);
        this.isLoading.set(false);
      }
    });
  }

  loadArchivedNotes() {
    this.isLoading.set(true);
    this.notesService.getArchivedNotes().subscribe({
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
        console.error('Error loading archived notes:', error);
        this.snackBar.open('Failed to load archived notes', 'Close', { duration: 3000 });
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

  unarchiveNote(noteId: number, event: Event) {
    event.stopPropagation();
    this.notesService.unarchiveNote(noteId).subscribe({
      next: () => {
        // Remove the unarchived note from the list
        this.notes.update(notes => notes.filter(note => note.id !== noteId));
        this.snackBar.open('Note unarchived', 'Close', { duration: 2000 });
      },
      error: (error) => {
        const errorMessage = error?.error?.message || 'Failed to unarchive note';
        this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
      }
    });
  }

  editNote(note: Note) {
    const dialogRef = this.dialog.open(EditNoteDialogComponent, {
      data: { note, imageUrls: this.loadedImageUrls },
      disableClose: false,
      panelClass: 'edit-note-dialog-container'
    });

    dialogRef.afterClosed().subscribe(updatedNote => {
      if (updatedNote) {
        // Update the specific note in the list
        this.notes.update(notes => 
          notes.map(n => n.id === updatedNote.id ? updatedNote : n)
        );
        
        // Load images for the updated note
        if (updatedNote.mediaIds && updatedNote.mediaIds.length > 0) {
          updatedNote.mediaIds.forEach((imageId: number) => {
            const imageIdStr = String(imageId);
            if (!this.loadedImageUrls.has(imageIdStr)) {
              const imageIdNumber = Number(imageId);
              this.notesService.getImage(String(imageIdNumber)).subscribe({
                next: (blob: Blob) => {
                  const objectUrl = URL.createObjectURL(blob);
                  const safeUrl = this.sanitizer.bypassSecurityTrustUrl(objectUrl);
                  this.loadedImageUrls.set(imageIdStr, safeUrl);
                },
                error: (error) => {
                  console.error(`Error loading image ${imageId}:`, error);
                }
              });
            }
          });
        }
      }
    });
  }

  copyNote(note: Note) {
    this.notesService.copyNote(note.id).subscribe({
      next: (copiedNote) => {
        // Add the copied note to the top of the list
        this.notes.update(notes => [copiedNote, ...notes]);
        
        // Load images for the copied note
        if (copiedNote.mediaIds && copiedNote.mediaIds.length > 0) {
          copiedNote.mediaIds.forEach(imageId => {
            const imageIdStr = String(imageId);
            if (!this.loadedImageUrls.has(imageIdStr)) {
              this.notesService.getImage(imageIdStr).subscribe({
                next: (blob: Blob) => {
                  const objectUrl = URL.createObjectURL(blob);
                  const safeUrl = this.sanitizer.bypassSecurityTrustUrl(objectUrl);
                  this.loadedImageUrls.set(imageIdStr, safeUrl);
                },
                error: (error) => {
                  console.error(`Error loading image ${imageId}:`, error);
                }
              });
            }
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
            // Update the note in the list
            this.notes.update(notes => 
              notes.map(n => n.id === updatedNote.id ? updatedNote : n)
            );
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

  deleteNote(noteId: number) {
    this.notesService.deleteNote(noteId).subscribe({
      next: () => {
        // Remove the deleted note from the list
        this.notes.update(notes => notes.filter(note => note.id !== noteId));
        this.snackBar.open('Note moved to trash', 'Close', { duration: 3000 });
      },
      error: (error) => {
        const errorMessage = error?.error?.message || 'Failed to delete note';
        this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
      }
    });
  }
}
