import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { LabelsService, Label } from '../../services/labels';
import { EditLabelsDialog } from '../edit-labels-dialog/edit-labels-dialog';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterModule, MatIconModule, MatListModule, MatDialogModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar implements OnInit {
  private dialog = inject(MatDialog);
  private labelsService = inject(LabelsService);
  private router = inject(Router);
  
  isExpanded = signal(true);
  labels = signal<Label[]>([]);

  menuItems = [
    { name: 'Notes', icon: 'lightbulb_outline', route: '/notes' },
    { name: 'Reminders', icon: 'notifications_none', route: '/reminders' },
    { name: 'Edit Labels', icon: 'edit', route: null, action: () => this.openEditLabelsDialog() },
    { name: 'Archive', icon: 'archive', route: '/archived' },
    { name: 'Trash', icon: 'delete', route: '/trash' }
  ];

  ngOnInit() {
    this.labelsService.labels$.subscribe(labels => {
      this.labels.set(labels);
    });
  }

  openEditLabelsDialog() {
    this.dialog.open(EditLabelsDialog, {
      width: '450px',
      panelClass: 'edit-labels-dialog-container'
    });
  }

  selectLabel(labelId: number) {
    const label = this.labels().find(l => l.id === labelId);
    if (label) {
      this.router.navigate(['/notes'], { queryParams: { labelName: label.name } });
    }
  }

  toggleSidebar() {
    this.isExpanded.set(!this.isExpanded());
  }
}
