export interface RequestState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
  lastUpdatedAt?: number;
  isStale?: boolean;
}

export function initialRequestState<T>(): RequestState<T> {
  return {
    data: null,
    loading: true,
    error: null,
  };
}

export function loadedRequestState<T>(data: T): RequestState<T> {
  return {
    data,
    loading: false,
    error: null,
    lastUpdatedAt: Date.now(),
    isStale: false
  };
}

export function errorRequestState<T>(error: string, previousData: T | null = null): RequestState<T> {
  return {
    data: previousData,
    loading: false,
    error,
    lastUpdatedAt: previousData ? Date.now() : undefined,
    isStale: !!previousData
  };
}

export function loadingStaleRequestState<T>(previousData: T | null): RequestState<T> {
  return {
    data: previousData,
    loading: true,
    error: null,
    lastUpdatedAt: previousData ? Date.now() : undefined,
    isStale: true
  };
}
