import { Component, inject, Inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LabelsService, Label } from '../../services/labels';
import { Note } from '../../services/notes';

@Component({
  selector: 'app-add-label-to-note-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatInputModule,
    MatFormFieldModule
  ],
  templateUrl: './add-label-to-note-dialog.html',
  styleUrl: './add-label-to-note-dialog.scss'
})
export class AddLabelToNoteDialogComponent {
  private dialogRef = inject(MatDialogRef<AddLabelToNoteDialogComponent>);
  private labelsService = inject(LabelsService);
  private snackBar = inject(MatSnackBar);

  availableLabels = signal<Label[]>([]);
  selectedLabelIds = signal<Set<number>>(new Set());
  newLabelName = signal('');
  isCreatingLabel = signal(false);

  constructor(@Inject(MAT_DIALOG_DATA) public data: { note: Note }) {
    // Set currently selected labels from the note's labelIds FIRST
    if (data.note.labelIds && data.note.labelIds.length > 0) {
      const labelIds = new Set(data.note.labelIds);
      this.selectedLabelIds.set(labelIds);
    }

    // Load available labels
    this.labelsService.labels$.subscribe(labels => {
      this.availableLabels.set(labels);
    });
  }

  isLabelSelected(labelId: number): boolean {
    // Explicitly call the signal to ensure change detection
    return this.selectedLabelIds().has(Number(labelId));
  }

  setLabelSelected(labelId: number, checked: boolean): void {
    const selected = new Set(this.selectedLabelIds());
    if (checked) {
      selected.add(Number(labelId));
    } else {
      selected.delete(Number(labelId));
    }
    this.selectedLabelIds.set(selected);
  }

  toggleLabel(labelId: number): void {
    const selected = new Set(this.selectedLabelIds());
    if (selected.has(Number(labelId))) {
      selected.delete(Number(labelId));
    } else {
      selected.add(Number(labelId));
    }
    this.selectedLabelIds.set(selected);
  }

  createNewLabel(): void {
    const labelName = this.newLabelName().trim();
    if (!labelName) {
      this.snackBar.open('Label name cannot be empty', 'Close', { duration: 3000 });
      return;
    }

    this.isCreatingLabel.set(true);
    this.labelsService.createLabel(labelName).subscribe({
      next: (newLabel) => {
        // Automatically select the newly created label
        const selected = new Set(this.selectedLabelIds());
        selected.add(newLabel.id);
        this.selectedLabelIds.set(selected);
        
        this.newLabelName.set('');
        this.isCreatingLabel.set(false);
        this.snackBar.open('Label created and added', 'Close', { duration: 2000 });
      },
      error: (error) => {
        const errorMessage = error?.error?.message || 'Failed to create label';
        this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
        this.isCreatingLabel.set(false);
      }
    });
  }

  save(): void {
    const selectedLabelIds = Array.from(this.selectedLabelIds());
    this.dialogRef.close(selectedLabelIds);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
