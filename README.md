# Life OS Ecosystem

Life OS is a unified ecosystem platform optimized for metadata intelligence, telemetry, and observability.

## Architecture Overview

This monorepo uses a clean ecosystem-oriented architecture optimized for modular monolith backend development, an Angular observatory frontend, an Android telemetry tracker, and observability infrastructure.

The core philosophy:
> One unified Life OS ecosystem. Not three unrelated applications.

### Modules

- **`android/`**: Android telemetry tracker.
- **`backend/`**: Modular monolith Spring Boot application, separating metadata intelligence and observability.
- **`frontend/`**: Angular standalone architecture for cinematic observatory widgets.
- **`infrastructure/`**: Observability and infrastructure-related configurations (Grafana, Prometheus, etc.).
- **`shared/`**: Minimal shared directory for API contracts and design tokens.
- **`docs/`**: Architecture and decision documentation.
- **`scripts/`**: Automation scripts for dev, setup, backup, etc.

## Local Setup

### Infrastructure
Use the root `docker-compose.yml` to spin up the infrastructure:
```bash
docker-compose up -d postgres prometheus grafana
```

### Backend
Navigate to `backend/` and run:
```bash
./gradlew bootRun
```

### Frontend
Navigate to `frontend/` and run:
```bash
npm install
npm run start
```

## Observability Stack

The platform is built with an observability-first mindset:
- **Metrics**: Micrometer + Prometheus
- **Dashboards**: Grafana
- **Logging**: (Planned) Loki

## Development Flow
Ensure all code added adheres to the defined boundaries. Infrastructure concerns must remain outside the backend, and the shared kernel should remain lightweight.
