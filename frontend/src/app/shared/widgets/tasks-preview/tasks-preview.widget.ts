import { Component, Input, computed, signal } from '@angular/core';
import { TaskItem } from '../../models/activity.model';
import { WidgetContainerComponent } from '../../components/widget-container.component';
import { RequestState } from '../../models/request-state.model';

@Component({
  selector: 'app-tasks-preview',
  standalone: true,
  imports: [WidgetContainerComponent],
  template: `
    <app-widget-container title="Active Directives" icon="📋">
      <span widget-actions class="view-all-link">Kanban</span>
      <div class="tasks-list">
        @if (state.loading && !state.data) {
          @for (i of [1, 2, 3]; track i) {
            <div class="task-card skeleton-glow" style="height: 64px; display: flex; padding: 12px; gap: 12px; border: 1px solid rgba(255,255,255,0.05);">
              <div class="skeleton-shimmer" style="width: 4px; height: 100%; border-radius: 2px;"></div>
              <div style="flex: 1; display: flex; flex-direction: column; justify-content: center; gap: 8px;">
                <div class="skeleton-shimmer" style="width: 60%; height: 12px; border-radius: 4px;"></div>
                <div class="skeleton-shimmer" style="width: 40%; height: 10px; border-radius: 4px;"></div>
              </div>
            </div>
          }
        } @else if (state.data) {
          @for (task of activeTasks(); track task.id) {
            <div class="task-card hover-lift">
              <div class="task-card__indicator" [style.background]="getPriorityColor(task.priority)"></div>
              <div class="task-card__content">
                <div class="task-card__header">
                  <span class="task-id">{{ task.id }}</span>
                  @if (task.dueDate) {
                    <span class="task-due">{{ task.dueDate }}</span>
                  }
                </div>
                <div class="task-title">{{ task.title }}</div>
                <div class="task-category">{{ task.category }}</div>
              </div>
              <div class="task-action">
                <button class="task-btn">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
                </button>
              </div>
            </div>
          }
          @if (activeTasks().length === 0) {
            <div class="empty-state">No active directives.</div>
          }
        } @else if (state.error) {
          <div class="empty-state" style="color: var(--error);">Failed to load directives.</div>
        }
      </div>
    </app-widget-container>
  `,
  styles: [`
    .view-all-link { font-size: 11px; color: var(--accent-primary); cursor: pointer; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; }
    .view-all-link:hover { text-decoration: underline; }
    .tasks-list { display: flex; flex-direction: column; gap: 8px; }
    .task-card { display: flex; background: var(--bg-tertiary); border: 1px solid var(--card-border); border-radius: var(--radius-md); overflow: hidden; transition: all var(--transition-fast); cursor: pointer; }
    .task-card:hover { background: var(--card-bg-hover); border-color: var(--card-border-hover); }
    .task-card__indicator { width: 4px; flex-shrink: 0; }
    .task-card__content { padding: 12px; flex: 1; }
    .task-card__header { display: flex; justify-content: space-between; margin-bottom: 4px; }
    .task-id { font-size: 10px; color: var(--text-secondary); font-family: monospace; font-weight: 600; }
    .task-due { font-size: 10px; color: var(--warning); font-weight: 500; }
    .task-title { font-size: 13px; color: var(--text-primary); font-weight: 600; margin-bottom: 6px; }
    .task-category { font-size: 9px; color: var(--text-tertiary); text-transform: uppercase; letter-spacing: 0.5px; }
    .task-action { display: flex; align-items: center; padding: 0 12px; }
    .task-btn { width: 24px; height: 24px; border-radius: 50%; background: var(--bg-primary); display: flex; align-items: center; justify-content: center; color: var(--text-secondary); border: 1px solid transparent; transition: all var(--transition-fast); }
    .task-card:hover .task-btn { background: var(--accent-primary); color: white; }
    .empty-state { padding: 20px; text-align: center; color: var(--text-secondary); font-size: 12px; border: 1px dashed var(--card-border); border-radius: var(--radius-md); }
    
    /* Cinematic Skeletons */
    .skeleton-glow { background: rgba(255,255,255,0.02); animation: pulse 2s infinite ease-in-out; }
    .skeleton-shimmer { background: linear-gradient(90deg, rgba(255,255,255,0.02) 25%, rgba(255,255,255,0.05) 50%, rgba(255,255,255,0.02) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite linear; }
    @keyframes pulse { 0% { opacity: 0.5; } 50% { opacity: 0.8; } 100% { opacity: 0.5; } }
    @keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
  `]
})
export class TasksPreviewWidget {
  private _state = signal<RequestState<TaskItem[]>>({ data: null, loading: false, error: null });
  @Input({ required: true }) set state(val: RequestState<TaskItem[]>) {
    this._state.set(val);
  }
  get state() { return this._state(); }

  activeTasks = computed(() => {
    const d = this._state().data;
    return d ? d.filter(t => !t.completed).slice(0, 4) : [];
  });

  getPriorityColor(priority: string): string {
    switch (priority) {
      case 'high': return 'var(--error)';
      case 'medium': return 'var(--warning)';
      case 'low': return 'var(--info)';
      default: return 'var(--text-tertiary)';
    }
  }
}
