import { Pipe, PipeTransform, NgZone, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { getRelativeTime } from '../utils/time/time.utils';

@Pipe({
  name: 'relativeTime',
  standalone: true,
  pure: false // Impure pipe to allow periodic updates without changing reference
})
export class RelativeTimePipe implements PipeTransform, OnDestroy {
  private timer: any;
  private currentRawValue: string | number | Date | null = null;
  private currentResult: string = '';

  constructor(private ngZone: NgZone, private cdr: ChangeDetectorRef) {}

  transform(value: string | number | Date | null | undefined): string {
    if (!value) return '';

    // If the value changes, update the reference and recalculate immediately
    if (this.currentRawValue !== value) {
      this.currentRawValue = value;
      this.currentResult = getRelativeTime(value);
      this.scheduleUpdate();
    }

    return this.currentResult;
  }

  private scheduleUpdate() {
    this.removeTimer();
    
    // Schedule the update outside Angular zone so we don't trigger global change detection loops
    this.ngZone.runOutsideAngular(() => {
      this.timer = setTimeout(() => {
        if (this.currentRawValue) {
          const newResult = getRelativeTime(this.currentRawValue);
          if (newResult !== this.currentResult) {
            this.currentResult = newResult;
            // Only trigger change detection for this specific component/pipe when the string actually changes
            this.ngZone.run(() => this.cdr.markForCheck());
          }
          // Reschedule
          this.scheduleUpdate();
        }
      }, 60000); // Check every minute
    });
  }

  private removeTimer() {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  }

  ngOnDestroy() {
    this.removeTimer();
  }
}
