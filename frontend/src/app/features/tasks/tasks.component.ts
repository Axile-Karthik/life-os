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
  templateUrl: './tasks.component.html',
  styleUrl: './tasks.component.css',
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
