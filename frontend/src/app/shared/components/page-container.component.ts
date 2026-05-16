import { Component } from '@angular/core';

@Component({
  selector: 'app-page-container',
  standalone: true,
  template: `
    <div class="page-container animate-fade-in-up">
      <ng-content />
    </div>
  `,
  styles: [`
    .page-container {
      padding: 24px;
      max-width: 1600px;
      width: 100%;
    }

    @media (max-width: 768px) {
      .page-container {
        padding: 16px;
      }
    }
  `],
})
export class PageContainerComponent {}
