# FallGuard

An automated fall detection system that uses smartphone sensors to detect falls in real time and instantly alert emergency contacts. Built for elderly and at-risk individuals to reduce emergency response times when every second counts.

## How It Works

FallGuard runs as a background service on Android, continuously analyzing accelerometer and gyroscope data to identify fall patterns. When a fall is detected, it triggers an automated alert workflow:

1. **Detection** — A 3-phase algorithm identifies free-fall, impact, and body rotation signatures from raw sensor data
2. **Countdown** — The user gets a 15-second window to cancel if it's a false alarm
3. **Alert** — Emergency contacts receive push notifications with the user's name and status
4. **Response** — The first contact to respond triggers a check-in prompt back to the user
5. **Escalation** — If the user doesn't respond within 5 minutes, the system escalates to EMS

## Tech Stack

**Android App** — Kotlin, Jetpack Compose, Retrofit, Android Sensor APIs, Firebase Cloud Messaging

**Backend API** — Python, FastAPI, Pydantic

## Fall Detection Algorithm

The detection algorithm uses a state-machine approach with three conditions that must be met in sequence:

| Phase | Sensor | Threshold | Duration |
|-------|--------|-----------|----------|
| Free-fall | Accelerometer | < 2.0 m/s² | ≥ 250ms |
| Impact | Accelerometer | > 20.0 m/s² | Within 2s of free-fall |
| Rotation | Gyroscope | > 3.0 rad/s | Concurrent with impact |

The algorithm was validated against 7 real-world sensor recordings covering hard drops, jogging, walking, sitting/standing, and regular phone use — correctly classifying all clear-cut scenarios.

## Project Structure

```
├── android/          # Kotlin Android app (Jetpack Compose)
│   ├── sensor/       # Fall detection algorithm & foreground service
│   ├── viewmodel/    # Screen state management
│   ├── ui/screens/   # Compose UI (onboarding, home, countdown, alerts)
│   ├── network/      # Retrofit API client & data models
│   └── data/         # Local persistence & state management
├── backend/          # Python FastAPI server
│   ├── app.py        # REST API endpoints
│   ├── models.py     # Request/response schemas
│   └── *_service.py  # User, incident, and notification services
└── test_data/        # 7 labeled accelerometer/gyroscope recordings
```

## Key Features

- Real-time sensor monitoring via Android foreground service
- Prioritized emergency contact list with push notification alerts
- Contact acknowledgment and user check-in workflow
- Optional EMS escalation with configurable timeout
- Guided onboarding with permission handling
- Offline-capable local storage with SharedPreferences

## Running Locally

**Backend**
```bash
cd backend
pip install fastapi uvicorn pydantic
uvicorn app:app --reload --host 0.0.0.0 --port 8000
```

**Android**
1. Open the `android/` directory in Android Studio
2. Update the base URL in `ApiProvider.kt` to point to your backend
3. Build and run on a physical device (emulators lack real sensor data)

## Built By

**Mo Islam** — [GitHub](https://github.com/MoTIslam23) · mo.islam2023@gmail.com
