import { Component, inject, OnInit } from '@angular/core';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { AppCardComponent } from '../../shared/components/app-card.component';
import { SectionHeaderComponent } from '../../shared/components/section-header.component';
import { TaskApiService } from '../../core/api/services/task-api.service';
import { TaskItem } from '../../shared/models/activity.model';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [PageContainerComponent, AppCardComponent, SectionHeaderComponent],
  template: `
    <app-page-container>
      <app-section-header title="Tasks" subtitle="Your activity goals" />
      <div class="task-filters">
        @for (f of filters; track f) {
          <button class="tf" [class.tf--active]="f === activeFilter" (click)="activeFilter = f">{{ f }}</button>
        }
      </div>
      <div class="task-list">
        @for (task of filteredTasks(); track task.id) {
          <app-card [hoverable]="true" padding="compact">
            <div class="task-row">
              <button class="task-check" [class.task-check--done]="task.completed" (click)="task.completed = !task.completed">
                @if (task.completed) { <span>✓</span> }
              </button>
              <div class="task-info" [class.task-info--done]="task.completed">
                <span class="task-title">{{ task.title }}</span>
                <div class="task-meta">
                  <span class="task-cat">{{ task.category }}</span>
                  @if (task.dueDate) { <span class="task-due">{{ task.dueDate }}</span> }
                </div>
              </div>
              <span class="task-priority" [attr.data-priority]="task.priority">{{ task.priority }}</span>
            </div>
          </app-card>
        }
        @if (filteredTasks().length === 0) {
          <div class="empty-state">No tasks found.</div>
        }
      </div>
    </app-page-container>
  `,
  styles: [`
    .task-filters { display: flex; gap: 8px; margin-bottom: 16px; }
    .tf { padding: 6px 16px; border-radius: var(--radius-full); background: var(--card-bg); border: 1px solid var(--card-border); color: var(--text-secondary); font-size: 13px; cursor: pointer; transition: all var(--transition-fast); }
    .tf--active { background: var(--accent-primary-dim); border-color: var(--accent-primary); color: var(--accent-primary); }
    .task-list { display: flex; flex-direction: column; gap: 8px; }
    .task-row { display: flex; align-items: center; gap: 12px; }
    .task-check { width: 22px; height: 22px; border-radius: 50%; border: 2px solid var(--card-border); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 12px; color: white; transition: all var(--transition-fast); }
    .task-check--done { background: var(--accent-emerald); border-color: var(--accent-emerald); }
    .task-info { flex: 1; }
    .task-info--done .task-title { text-decoration: line-through; opacity: 0.5; }
    .task-title { font-size: 14px; font-weight: 500; color: var(--text-primary); }
    .task-meta { display: flex; gap: 8px; margin-top: 2px; }
    .task-cat { font-size: 11px; color: var(--accent-primary); background: var(--accent-primary-dim); padding: 1px 8px; border-radius: var(--radius-full); }
    .task-due { font-size: 11px; color: var(--text-tertiary); }
    .task-priority { font-size: 10px; font-weight: 600; text-transform: uppercase; padding: 2px 10px; border-radius: var(--radius-full); }
    .task-priority[data-priority="high"] { color: var(--accent-rose); background: var(--accent-rose-dim); }
    .task-priority[data-priority="medium"] { color: var(--accent-warm); background: var(--accent-warm-dim); }
    .task-priority[data-priority="low"] { color: var(--text-tertiary); background: var(--bg-tertiary); }
    .empty-state { text-align: center; padding: 40px; color: var(--text-secondary); font-size: 14px; }
  `],
})
export class TasksComponent implements OnInit {
  private taskApi = inject(TaskApiService);

  tasks: TaskItem[] = [];
  filters = ['All', 'Active', 'Completed'];
  activeFilter = 'All';

  ngOnInit() {
    this.taskApi.getTasks().subscribe({
      next: (data) => this.tasks = data,
      error: () => this.tasks = []
    });
  }

  filteredTasks() {
    if (this.activeFilter === 'Active') return this.tasks.filter(t => !t.completed);
    if (this.activeFilter === 'Completed') return this.tasks.filter(t => t.completed);
    return this.tasks;
  }
}
