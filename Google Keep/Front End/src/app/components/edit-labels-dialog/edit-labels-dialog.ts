import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatListModule } from '@angular/material/list';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { LabelsService, Label } from '../../services/labels';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-edit-labels-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatListModule,
    MatSnackBarModule
  ],
  templateUrl: './edit-labels-dialog.html',
  styleUrl: './edit-labels-dialog.scss'
})
export class EditLabelsDialog {
  private labelsService = inject(LabelsService);
  private dialogRef = inject(MatDialogRef<EditLabelsDialog>);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  labels = signal<Label[]>([]);
  isCreatingLabel = signal(false);
  newLabelName = '';
  editingLabelId: number | null = null;
  editingLabelName = '';

  constructor() {
    this.labels.set(this.labelsService.getLabels());
    
    // Subscribe to label changes
    this.labelsService.labels$.subscribe(labels => {
      this.labels.set(labels);
    });
  }

  startCreatingLabel() {
    this.isCreatingLabel.set(true);
    this.newLabelName = '';
  }

  cancelCreatingLabel() {
    this.isCreatingLabel.set(false);
    this.newLabelName = '';
  }

  createLabel() {
    if (this.newLabelName.trim()) {
      this.labelsService.createLabel(this.newLabelName).subscribe({
        next: () => {
          this.cancelCreatingLabel();
          this.snackBar.open('Label created successfully', 'Close', { duration: 2000 });
        },
        error: (error) => {
          console.error('Error creating label:', error);
          const errorMessage = error?.error?.message || 'Failed to create label. Please try again.';
          this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
        }
      });
    }
  }

  deleteLabel(id: number) {
    const confirmDialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete label?',
        message: "We'll delete this label and remove it from all of your Keep notes. Your notes won't be deleted.",
        confirmText: 'Delete',
        cancelText: 'Cancel',
        icon: 'warning'
      },
      width: '400px'
    });

    confirmDialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.labelsService.deleteLabel(id).subscribe({
          next: () => {
            this.snackBar.open('Label deleted successfully', 'Close', { duration: 2000 });
          },
          error: (error) => {
            console.error('Error deleting label:', error);
            const errorMessage = error?.error?.message || 'Failed to delete label. Please try again.';
            this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  startEditingLabel(label: Label) {
    this.editingLabelId = label.id;
    this.editingLabelName = label.name;
  }

  cancelEditingLabel() {
    this.editingLabelId = null;
    this.editingLabelName = '';
  }

  saveEditedLabel() {
    if (this.editingLabelId && this.editingLabelName.trim()) {
      this.labelsService.updateLabel(this.editingLabelId, this.editingLabelName).subscribe({
        next: () => {
          this.cancelEditingLabel();
          this.snackBar.open('Label updated successfully', 'Close', { duration: 2000 });
        },
        error: (error) => {
          console.error('Error updating label:', error);
          const errorMessage = error?.error?.message || 'Failed to update label. Please try again.';
          this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
        }
      });
    }
  }

  close() {
    this.dialogRef.close();
  }
}
