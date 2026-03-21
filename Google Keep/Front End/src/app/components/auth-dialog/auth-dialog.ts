import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService, LoginRequest, SignupRequest } from '../../services/auth';

@Component({
  selector: 'app-auth-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './auth-dialog.html',
  styleUrl: './auth-dialog.scss'
})
export class AuthDialog {
  private authService = inject(AuthService);
  private dialogRef = inject(MatDialogRef<AuthDialog>);
  private snackBar = inject(MatSnackBar);

  isSignup = signal(false);
  isLoading = signal(false);
  
  loginData: LoginRequest = {
    username: '',
    password: ''
  };

  signupData: SignupRequest = {
    username: '',
    email: '',
    password: ''
  };

  confirmPassword = '';

  toggleMode() {
    this.isSignup.update(v => !v);
  }

  onLogin() {
    if (!this.loginData.username || !this.loginData.password) {
      this.snackBar.open('Please fill in all fields', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading.set(true);
    this.authService.login(this.loginData).subscribe({
      next: () => {
        this.snackBar.open('Login successful!', 'Close', { duration: 2000 });
        this.dialogRef.close(true);
      },
      error: (error) => {
        console.error('Login error:', error);
        const errorMessage = error?.error?.message || 'Login failed. Please check your credentials.';
        this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
        this.isLoading.set(false);
      }
    });
  }

  onSignup() {
    if (!this.signupData.username || !this.signupData.email || !this.signupData.password) {
      this.snackBar.open('Please fill in all fields', 'Close', { duration: 3000 });
      return;
    }

    if (this.signupData.password !== this.confirmPassword) {
      this.snackBar.open('Passwords do not match', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading.set(true);
    this.authService.signup(this.signupData).subscribe({
      next: () => {
        this.snackBar.open('Signup successful!', 'Close', { duration: 2000 });
        this.dialogRef.close(true);
      },
      error: (error) => {
        console.error('Signup error:', error);
        const errorMessage = error?.error?.message || 'Signup failed. Please try again.';
        this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
        this.isLoading.set(false);
      }
    });
  }
}
