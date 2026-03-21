import { Routes } from '@angular/router';
import { Notes } from './pages/notes/notes';
import { Trash } from './pages/trash/trash';
import { Archived } from './pages/archived/archived';

export const routes: Routes = [
  { path: '', redirectTo: '/notes', pathMatch: 'full' },
  { path: 'notes', component: Notes },
  { path: 'reminders', component: Notes }, // Placeholder
  { path: 'labels', component: Notes }, // Placeholder
  { path: 'archived', component: Archived },
  { path: 'trash', component: Trash },
];
