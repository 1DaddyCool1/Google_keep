import { Component, signal, ViewChild, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from './components/sidebar/sidebar';
import { TopBar } from './components/top-bar/top-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AuthService } from './services/auth';
import { AuthDialog } from './components/auth-dialog/auth-dialog';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Sidebar, TopBar, MatDialogModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  
  protected readonly title = signal('Google Keep Clone');
  @ViewChild(Sidebar) sidebar!: Sidebar;

  ngOnInit() {
    if (!this.authService.isAuthenticated) {
      this.openAuthDialog();
    }
  }

  openAuthDialog() {
    const dialogRef = this.dialog.open(AuthDialog, {
      width: '450px',
      disableClose: true,
      panelClass: 'auth-dialog-container'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (!result && !this.authService.isAuthenticated) {
        setTimeout(() => this.openAuthDialog(), 500);
      }
    });
  }

  onMenuToggle() {
    this.sidebar?.toggleSidebar();
  }
}
