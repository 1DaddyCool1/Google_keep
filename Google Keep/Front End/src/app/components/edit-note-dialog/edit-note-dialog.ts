import { Component, inject, Inject, signal, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { NotesService, Note } from '../../services/notes';

@Component({
  selector: 'app-edit-note-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
    MatDividerModule
  ],
  templateUrl: './edit-note-dialog.html',
  styleUrl: './edit-note-dialog.scss'
})
export class EditNoteDialogComponent implements AfterViewInit {
  private dialogRef = inject(MatDialogRef<EditNoteDialogComponent>);
  private notesService = inject(NotesService);
  private snackBar = inject(MatSnackBar);
  private sanitizer = inject(DomSanitizer);

  @ViewChild('contentInput') contentInputRef!: ElementRef<HTMLDivElement>;

  title = signal('');
  content = signal('');
  selectedImages = signal<File[]>([]);
  imagePreviewUrls = signal<SafeUrl[]>([]);
  existingImageUrls = signal<SafeUrl[]>([]);
  existingImageIds = signal<string[]>([]);

  constructor(@Inject(MAT_DIALOG_DATA) public data: { note: Note, imageUrls: Map<string, SafeUrl> }) {
    this.title.set(data.note.title);
    this.content.set(data.note.content);
    
    // Load existing images from mediaIds
    if (data.note.mediaIds && data.note.mediaIds.length > 0) {
      const imageIds = data.note.mediaIds.map(id => String(id));
      this.existingImageIds.set([...imageIds]);
      const urls = imageIds
        .map(id => data.imageUrls.get(id))
        .filter(url => url !== undefined) as SafeUrl[];
      this.existingImageUrls.set(urls);
    }

    // Auto-save when clicking outside the dialog
    this.dialogRef.backdropClick().subscribe(() => {
      this.saveNote();
    });
  }

  ngAfterViewInit(): void {
    // Set initial content after view is initialized to avoid cursor/direction issues
    if (this.contentInputRef && this.content()) {
      this.contentInputRef.nativeElement.innerHTML = this.content();
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      const newFiles = Array.from(input.files);
      this.selectedImages.update(images => [...images, ...newFiles]);

      newFiles.forEach(file => {
        const reader = new FileReader();
        reader.onload = (e: ProgressEvent<FileReader>) => {
          if (e.target?.result) {
            const safeUrl = this.sanitizer.bypassSecurityTrustUrl(e.target.result as string);
            this.imagePreviewUrls.update(urls => [...urls, safeUrl]);
          }
        };
        reader.readAsDataURL(file);
      });
    }
  }

  removeNewImage(index: number): void {
    this.selectedImages.update(images => images.filter((_, i) => i !== index));
    this.imagePreviewUrls.update(urls => urls.filter((_, i) => i !== index));
  }

  removeExistingImage(index: number): void {
    this.existingImageIds.update(ids => ids.filter((_, i) => i !== index));
    this.existingImageUrls.update(urls => urls.filter((_, i) => i !== index));
  }

  applyFormat(format: string): void {
    if (format === 'h1') {
      document.execCommand('formatBlock', false, '<h1>');
    } else if (format === 'h2') {
      document.execCommand('formatBlock', false, '<h2>');
    } else if (format === 'normal') {
      document.execCommand('formatBlock', false, '<p>');
    } else {
      document.execCommand(format, false, undefined);
    }
  }

  clearFormatting(): void {
    document.execCommand('removeFormat', false, undefined);
    document.execCommand('formatBlock', false, '<p>');
  }

  onContentInput(event: Event): void {
    const target = event.target as HTMLElement;
    this.content.set(target.innerHTML);
  }

  async saveNote(): Promise<void> {
    const titleValue = this.title();
    const contentValue = this.content();

    if (!titleValue && !contentValue && this.selectedImages().length === 0 && this.existingImageIds().length === 0) {
      this.snackBar.open('Note cannot be empty', 'Close', { duration: 3000 });
      return;
    }

    try {
      // Convert new images to base64
      const base64Images: string[] = [];
      for (const file of this.selectedImages()) {
        const base64 = await this.convertToBase64(file);
        base64Images.push(base64);
      }

      const updatedNote = {
        id: this.data.note.id,
        title: titleValue,
        content: contentValue,
        imageIds: this.existingImageIds(),
        newImages: base64Images
      };

      this.notesService.updateNote(updatedNote).subscribe({
        next: (updatedNote) => {
          this.snackBar.open('Note updated successfully', 'Close', { duration: 3000 });
          this.dialogRef.close(updatedNote);
        },
        error: (error) => {
          const errorMessage = error?.error?.message || 'Failed to update note';
          this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
        }
      });
    } catch (error) {
      this.snackBar.open('Failed to process images', 'Close', { duration: 3000 });
    }
  }

  private convertToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        resolve(reader.result as string);
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  close(): void {
    this.saveNote();
  }
}
