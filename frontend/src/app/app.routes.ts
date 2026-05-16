import { Routes } from '@angular/router';
import { ShellComponent } from './core/layout/shell.component';

export const routes: Routes = [
  {
    path: '',
    component: ShellComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent), data: { title: 'Dashboard' } },
      { path: 'timeline', loadComponent: () => import('./features/timeline/timeline.component').then(m => m.TimelineComponent), data: { title: 'Timeline' } },
      { path: 'library', loadComponent: () => import('./features/library/library.component').then(m => m.LibraryComponent), data: { title: 'Library' } },
      { path: 'observatory', loadComponent: () => import('./features/observatory/observatory.component').then(m => m.ObservatoryComponent), data: { title: 'Observatory' } },
      { path: 'statistics', loadComponent: () => import('./features/statistics/statistics.component').then(m => m.StatisticsComponent), data: { title: 'Statistics' } },
      { path: 'tasks', loadComponent: () => import('./features/tasks/tasks.component').then(m => m.TasksComponent), data: { title: 'Tasks' } },
      { path: 'achievements', loadComponent: () => import('./features/achievements/achievements.component').then(m => m.AchievementsComponent), data: { title: 'Achievements' } },
      { path: 'settings', loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent), data: { title: 'Settings' } },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
