# GameTime Tracker - Technical Documentation

This document explains the technical architecture, data flow, and implementation details of the GameTime Tracker application.

## 1. Core Architecture

The application is built using modern Android development practices, leveraging the following components:

- **UsageStatsManager**: The primary source of truth for app usage data.
- **Room Database**: For persistent storage of tracked game sessions.
- **Foreground Service**: Ensures near-real-time tracking by performing periodic scans.
- **Coroutine-based Asynchrony**: Handles database operations and periodic tasks efficiently.

---

## 2. How Usage Data is Fetched

Android does not allow apps to observe other apps' lifecycles directly for privacy reasons. GameTime Tracker uses the **`UsageStatsManager`** API, which is the official way to query app usage history.

### The `UsageTracker` Class
Located in `com.karthik.lifeos.tracker.tracker.UsageTracker`, this class wraps the system service.

- **Event Mapping**: It translates raw system events into a simplified `AppEvent` model.
    - **API 29+ (Android 10+):** Maps `ACTIVITY_RESUMED` to `FOREGROUND` and `ACTIVITY_PAUSED` to `BACKGROUND`.
    - **API 26-28:** Maps `MOVE_TO_FOREGROUND` and `MOVE_TO_BACKGROUND` (deprecated in newer versions).
- **Scan Window**: It queries events within a specific time range (e.g., the last 5 minutes).

---

## 3. Game Detection Strategy

The `GameDetector` class determines if an application is a game using a two-tier approach:

1.  **System Metadata**: It checks `ApplicationInfo.category`. If it matches `CATEGORY_GAME`, the app is treated as a game.
2.  **Manual Allowlist**: Since some games do not correctly declare their category in the Manifest, a hardcoded list of popular package names (e.g., `com.mojang.minecraftpe`, `com.tencent.ig`) is maintained.
3.  **Caching**: Results are cached in memory to avoid redundant `PackageManager` lookups during high-frequency scans.

---

## 4. Periodic Tracking Workflow

Tracking is performed by the **`TrackerService`**, a Foreground Service that remains active even when the app is in the background.

### The Scan Cycle
1.  **Interval**: Runs every **90 seconds**.
2.  **Window**: Queries the last **5 minutes** of events. This overlap ensures no events are missed due to system delays.
3.  **Session Healing**:
    - The service first retrieves "open" sessions (sessions where a start was detected but no end was found in previous scans) from the database.
    - It checks current events to see if a `BACKGROUND` event for that package has appeared.
    - If found, it "heals" the session by updating its `endTime` and calculating the final duration.
4.  **Session Reconstruction**:
    - New events are processed to find `FOREGROUND` → `BACKGROUND` pairs.
    - Identified game sessions are saved to the Room database.

---

## 5. Data Storage (Room Database)

Data is stored in a SQLite database via the Room persistence library.

### `GameSession` Entity
Stores individual play sessions:
- `packageName`: Unique identifier for the app.
- `gameName`: Human-readable name (resolved via `PackageManager`).
- `startTime`: Timestamp of the `FOREGROUND` event.
- `endTime`: Timestamp of the `BACKGROUND` event (or 0 if still active).
- `durationMillis`: Total duration in milliseconds.

### Conflict Resolution
To handle the overlapping scan windows, the `GameSessionDao` uses `OnConflictStrategy.IGNORE`. If a session with the same start time and package already exists, the insert is ignored, preventing duplicate entries.

---

## 6. Statistics and UI

The application calculates statistics directly via SQL queries in Room:
- **Aggregation**: Groups sessions by `packageName` to calculate total play time and session counts.
- **UI Refresh**: Uses Coroutines to fetch the latest sessions and stats, updating the RecyclerView in `MainActivity`.

---

## 7. Permissions Required

- **`PACKAGE_USAGE_STATS`**: Must be granted by the user in system settings. This is a "special" permission.
- **`FOREGROUND_SERVICE`**: Required to run the background scan.
- **`POST_NOTIFICATIONS`**: Required on Android 13+ to show the persistent service notification.
