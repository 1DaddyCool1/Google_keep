import { Component, inject, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogData {
  title?: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  icon?: string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="confirm-dialog">
      @if (data.icon) {
        <div class="confirm-icon">
          <mat-icon>{{ data.icon }}</mat-icon>
        </div>
      }
      @if (data.title) {
        <h2 mat-dialog-title>{{ data.title }}</h2>
      }
      <mat-dialog-content>
        <p>{{ data.message }}</p>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button (click)="onCancel()">
          {{ data.cancelText || 'Cancel' }}
        </button>
        <button mat-button color="warn" (click)="onConfirm()">
          {{ data.confirmText || 'Delete' }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .confirm-dialog {
      padding: 8px;
    }

    .confirm-icon {
      text-align: center;
      padding: 16px 0;
      
      mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        color: #f44336;
      }
    }

    h2 {
      margin: 0 0 16px 0;
      font-size: 20px;
      font-weight: 500;
      color: #202124;
    }

    mat-dialog-content {
      padding: 0 0 16px 0;
      
      p {
        margin: 0;
        color: #5f6368;
        font-size: 14px;
        line-height: 1.5;
      }
    }

    mat-dialog-actions {
      padding: 8px 0 0 0;
      margin: 0;
    }
  `]
})
export class ConfirmDialogComponent {
  private dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);

  constructor(@Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
