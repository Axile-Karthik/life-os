# Architecture Decision: Modular Monolith

## Context
The Life OS backend was historically structured by technical layers (`controllers`, `services`, `repositories`), which led to tight coupling between distinct business domains (e.g., metadata intelligence, telemetry processing, observability). As the platform evolves, microservices were considered but rejected due to premature complexity and operational overhead.

## Decision
We are adopting a **Modular Monolith** architecture. The codebase will be structured by domain/feature (bounded contexts) rather than technical layers. 
Each module (e.g., `metadata`, `telemetry`, `observability`) will internally encapsulate its own `controller`, `service`, `repository`, and `entity` layers.

## Consequences
- **Positive:** Improved cohesion, cleaner API boundaries, easier potential extraction to microservices later if necessary.
- **Negative:** Requires strict discipline to avoid cross-module coupling. Internal Spring events must be used for cross-module communication instead of direct service injections where possible.
