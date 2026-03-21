import { Component, output, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-top-bar',
  imports: [CommonModule, MatIconModule, MatButtonModule, MatToolbarModule, MatMenuModule, MatDividerModule],
  templateUrl: './top-bar.html',
  styleUrl: './top-bar.scss',
})
export class TopBar {
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  
  menuToggle = output<void>();
  currentUser$ = this.authService.currentUser$;

  onMenuClick() {
    this.menuToggle.emit();
  }

  onLogout() {
    this.authService.logout();
    this.snackBar.open('Logged out successfully', 'Close', { duration: 2000 });
    window.location.reload();
  }
}
