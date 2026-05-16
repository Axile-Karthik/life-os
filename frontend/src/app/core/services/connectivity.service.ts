import { Injectable, signal, computed } from '@angular/core';

export type ConnectivityState = 'online' | 'offline' | 'degraded';

@Injectable({ providedIn: 'root' })
export class ConnectivityService {
  private _isOnline = signal<boolean>(navigator.onLine);
  private _backendReachability = signal<boolean>(true);
  private _degradedTelemetry = signal<boolean>(false);

  constructor() {
    window.addEventListener('online', () => this._isOnline.set(true));
    window.addEventListener('offline', () => this._isOnline.set(false));
  }

  // Derived global state
  public readonly state = computed<ConnectivityState>(() => {
    if (!this._isOnline()) return 'offline';
    if (!this._backendReachability()) return 'offline';
    if (this._degradedTelemetry()) return 'degraded';
    return 'online';
  });

  public readonly isOnline = computed(() => this.state() !== 'offline');

  setBackendReachability(reachable: boolean) {
    this._backendReachability.set(reachable);
  }

  setDegradedTelemetry(degraded: boolean) {
    this._degradedTelemetry.set(degraded);
  }
}
