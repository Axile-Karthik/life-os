import { Component, input } from '@angular/core';

@Component({
  selector: 'app-progress-bar',
  standalone: true,
  template: `
    <div class="progress-bar">
      <div class="progress-bar__fill" [style.width.%]="value()" [style.background]="color()"></div>
    </div>
  `,
  styles: [`
    .progress-bar { width: 100%; height: 4px; background: var(--bg-tertiary); border-radius: var(--radius-full); overflow: hidden; }
    .progress-bar__fill { height: 100%; border-radius: var(--radius-full); transition: width 0.8s ease-out; animation: progressFill 1s ease-out; }
  `],
})
export class ProgressBarComponent {
  value = input<number>(0);
  color = input<string>('var(--accent-primary)');
}
