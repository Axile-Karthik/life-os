import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './core/theme/theme.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet />`,
  styles: [`:host { display: block; }`],
})
export class App implements OnInit {
  private themeService = inject(ThemeService);

  ngOnInit(): void {
    // Theme service initializes in constructor via effect
  }
}
