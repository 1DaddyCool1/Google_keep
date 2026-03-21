import { Component, signal, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog } from '@angular/material/dialog';
import { NotesService, Note, NoteImage } from '../../services/notes';
import { AuthService } from '../../services/auth';
import { LabelsService, Label } from '../../services/labels';
import { provideHttpClient } from '@angular/common/http';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { forkJoin, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { EditNoteDialogComponent } from '../../components/edit-note-dialog/edit-note-dialog';
import { AddLabelToNoteDialogComponent } from '../../components/add-label-to-note-dialog/add-label-to-note-dialog';

@Component({
  selector: 'app-notes',
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatMenuModule,
    MatDividerModule
  ],
  templateUrl: './notes.html',
  styleUrl: './notes.scss',
})
export class Notes implements OnInit {
  private notesService = inject(NotesService);
  private authService = inject(AuthService);
  private labelsService = inject(LabelsService);
  private snackBar = inject(MatSnackBar);
  private sanitizer = inject(DomSanitizer);
  private dialog = inject(MatDialog);
  private route = inject(ActivatedRoute);
  
  isAddingNote = signal(false);
  isLoading = signal(true);
  isFileDialogOpen = false;
  isFormattingMenuOpen = false;
  selectedLabelId = signal<number | null>(null);
  selectedLabelName = signal<string | null>(null);
  newNote = {
    title: '',
    content: '',
    images: [] as string[] // base64 strings
  };
  imagePreviewUrls = signal<SafeUrl[]>([]);
  loadedImageUrls = new Map<string, SafeUrl>();

  notes = signal<Note[]>([]);

  ngOnInit() {
    // Subscribe to query params for label filtering
    this.route.queryParams.subscribe(params => {
      const labelName = params['labelName'];
      if (labelName) {
        // Find the label by name to get its ID
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
      if (this.authService.isAuthenticated) {
        this.loadNotes();
      }
    });

    // Load notes only if user is authenticated
    if (this.authService.isAuthenticated) {
      this.loadNotes();
    } else {
      this.isLoading.set(false);
    }

    // Subscribe to authentication changes
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.loadNotes();
      } else {
        this.notes.set([]);
        this.isLoading.set(false);
      }
    });

    // Subscribe to label changes to refresh notes when labels are deleted
    let previousLabelCount = 0;
    this.labelsService.labels$.subscribe(labels => {
      const currentLabelCount = labels.length;
      // If labels were deleted (count decreased) and we have notes loaded, reload them
      if (previousLabelCount > 0 && currentLabelCount < previousLabelCount && this.notes().length > 0) {
        this.loadNotes();
      }
      previousLabelCount = currentLabelCount;
    });
  }

  loadNotes() {
    this.isLoading.set(true);
    const labelId = this.selectedLabelId();
    const notesObservable = labelId 
      ? this.notesService.getNotesByLabel(labelId)
      : this.notesService.getAllNotes();
    
    notesObservable.subscribe({
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
        console.error('Error loading notes:', error);
        this.snackBar.open('Failed to load notes', 'Close', { duration: 3000 });
        this.isLoading.set(false);
      }
    });
  }

  loadImagesForNotes(notes: Note[]) {
    notes.forEach(note => {
      if (note.mediaIds && note.mediaIds.length > 0) {
        note.mediaIds.forEach(imageId => {
          this.loadImage(String(imageId));
        });
      }
    });
  }

  loadImage(imageId: string) {
    if (this.loadedImageUrls.has(imageId)) {
      return;
    }

    this.notesService.getImage(imageId).subscribe({
      next: (blob) => {
        const objectUrl = URL.createObjectURL(blob);
        const safeUrl = this.sanitizer.bypassSecurityTrustUrl(objectUrl);
        this.loadedImageUrls.set(imageId, safeUrl);
      },
      error: (error) => {
        console.error(`Error loading image ${imageId}:`, error);
      }
    });
  }

  startAddingNote() {
    this.isAddingNote.set(true);
  }

  cancelAddNote() {
    if (!this.newNote.title && !this.newNote.content) {
      this.isAddingNote.set(false);
      this.resetNewNote();
    }
  }

  onImageSelect(event: Event) {
    this.isFileDialogOpen = false;
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const files = Array.from(input.files);
      
      // Convert files to base64 and generate preview URLs
      files.forEach(file => {
        const reader = new FileReader();
        reader.onload = (e) => {
          const base64String = e.target?.result as string;
          this.newNote.images.push(base64String);
          
          // Generate preview URL
          const safeUrl = this.sanitizer.bypassSecurityTrustUrl(base64String);
          this.imagePreviewUrls.update(urls => [...urls, safeUrl]);
        };
        reader.readAsDataURL(file);
      });

      // Reset input value so same file can be selected again
      input.value = '';
    }
  }

  removeImage(index: number) {
    this.newNote.images.splice(index, 1);
    this.imagePreviewUrls.update(urls => {
      const newUrls = [...urls];
      newUrls.splice(index, 1);
      return newUrls;
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

  saveNote() {
    if (this.newNote.title.trim() || this.newNote.content.trim() || this.newNote.images.length > 0) {
      const noteRequest = {
        title: this.newNote.title.trim(),
        content: this.newNote.content.trim(),
        images: this.newNote.images
      };

      this.notesService.createNote(noteRequest).subscribe({
        next: (note) => {
          this.notes.update(notes => [note, ...notes]);
          // Load images for the newly saved note
          if (note.mediaIds && note.mediaIds.length > 0) {
            note.mediaIds.forEach(imageId => {
              this.loadImage(String(imageId));
            });
          }
          this.resetNewNote();
          this.snackBar.open('Note saved', 'Close', { duration: 2000 });
        },
        error: (error) => {
          console.error('Error saving note:', error);
          this.snackBar.open('Failed to save note', 'Close', { duration: 3000 });
        }
      });
    }
    this.isAddingNote.set(false);
  }

  resetNewNote() {
    this.newNote = { title: '', content: '', images: [] };
    this.imagePreviewUrls.set([]);
  }

  onAddCardBlur(event: FocusEvent) {
    // Don't auto-save if file dialog or formatting menu is open
    if (this.isFileDialogOpen || this.isFormattingMenuOpen) {
      return;
    }

    // Check if the new focus target is outside the add-note-card
    const relatedTarget = event.relatedTarget as HTMLElement;
    const currentTarget = event.currentTarget as HTMLElement;
    
    if (!relatedTarget || !currentTarget.contains(relatedTarget)) {
      setTimeout(() => this.saveNote(), 150);
    }
  }

  openFileDialog() {
    this.isFileDialogOpen = true;
  }

  onContentInput(event: Event) {
    const element = event.target as HTMLElement;
    this.newNote.content = element.innerHTML;
  }

  onContentFocus(event: Event) {
    const element = event.target as HTMLElement;
    element.setAttribute('dir', 'ltr');
  }

  onFormattingMenuOpened() {
    this.isFormattingMenuOpen = true;
  }

  onFormattingMenuClosed() {
    this.isFormattingMenuOpen = false;
  }

  applyFormat(format: string) {
    const selection = window.getSelection();
    if (!selection || selection.rangeCount === 0) return;

    const range = selection.getRangeAt(0);
    
    switch (format) {
      case 'h1':
        document.execCommand('formatBlock', false, '<h1>');
        break;
      case 'h2':
        document.execCommand('formatBlock', false, '<h2>');
        break;
      case 'normal':
        document.execCommand('formatBlock', false, '<p>');
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
              this.loadImage(imageIdStr);
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
            this.loadImage(String(imageId));
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
            // If we're filtering by a label, check if the updated note still has that label
            const currentLabelId = this.selectedLabelId();
            if (currentLabelId) {
              const noteStillHasLabel = updatedNote.labelIds?.includes(Number(currentLabelId));
              if (!noteStillHasLabel) {
                // Remove the note from the list since it no longer has the filtered label
                this.notes.update(notes => notes.filter(n => n.id !== updatedNote.id));
              } else {
                // Update the note in the list
                this.notes.update(notes => 
                  notes.map(n => n.id === updatedNote.id ? updatedNote : n)
                );
              }
            } else {
              // Not filtering, just update the note
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

  archiveNote(noteId: number) {
    this.notesService.archiveNote(noteId).subscribe({
      next: () => {
        // Remove the archived note from the list
        this.notes.update(notes => notes.filter(note => note.id !== noteId));
        this.snackBar.open('Note archived', 'Close', { duration: 2000 });
      },
      error: (error) => {
        const errorMessage = error?.error?.message || 'Failed to archive note';
        this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
      }
    });
  }

  clearLabelFilter() {
    this.selectedLabelId.set(null);
    this.selectedLabelName.set(null);
    window.history.replaceState({}, '', '/notes');
    this.loadNotes();
  }
}
