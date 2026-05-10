"""
Fall detection algorithm test against CSV sensor data.

This script ports the Kotlin FallDetector logic to Python and replays
recorded accelerometer + gyroscope data to check whether each scenario
is correctly classified as a fall or not.

Usage:
    python test_csv.py [path_to_test_data_dir]

    If no path is given, defaults to: ~/Downloads/test_data
"""

import csv
import math
import os
import sys
from dataclasses import dataclass, field
from datetime import datetime
from enum import Enum, auto
from typing import Callable, Optional


class _Tee:
    """Write to both stdout and a file simultaneously."""
    def __init__(self, filepath: str):
        self._file = open(filepath, "w", encoding="utf-8")
        self._stdout = sys.stdout

    def write(self, text: str) -> None:
        self._stdout.write(text)
        self._file.write(text)

    def flush(self) -> None:
        self._stdout.flush()
        self._file.flush()

    def close(self) -> None:
        self._file.close()


# ── Algorithm constants (must match FallDetector.kt exactly) ────────────────

FREE_FALL_ACCEL_THRESHOLD = 2.0   # m/s² — below this is "free fall"
IMPACT_ACCEL_THRESHOLD    = 20.0  # m/s² — above this is "impact"
ROTATION_THRESHOLD        = 3.0   # rad/s — above this is "strong rotation"
FREE_FALL_MIN_DURATION_MS = 250   # ms — minimum free fall duration
IMPACT_WINDOW_MS          = 2000  # ms — window to find impact after free fall


# ── Python port of FallDetector.kt ──────────────────────────────────────────

class _State(Enum):
    IDLE             = auto()
    FREE_FALL_DETECTED = auto()


class FallDetector:
    """
    Direct Python port of FallDetector.kt.
    Calls on_fall_confirmed() when a fall pattern is detected.
    """

    def __init__(self, on_fall_confirmed: Callable[[], None]):
        self._on_fall_confirmed = on_fall_confirmed
        self._reset()

    def update_accelerometer(self, x: float, y: float, z: float, timestamp_ms: int) -> None:
        self._latest_accel_mag = self._magnitude(x, y, z)
        self._evaluate(timestamp_ms)

    def update_gyroscope(self, x: float, y: float, z: float, timestamp_ms: int) -> None:
        self._latest_gyro_mag = self._magnitude(x, y, z)
        self._evaluate(timestamp_ms)

    def _evaluate(self, now: int) -> None:
        if self._state == _State.IDLE:
            if self._latest_accel_mag < FREE_FALL_ACCEL_THRESHOLD:
                if self._free_fall_start is None:
                    self._free_fall_start = now
                duration = now - self._free_fall_start
                if duration >= FREE_FALL_MIN_DURATION_MS:
                    self._state = _State.FREE_FALL_DETECTED
                    self._last_free_fall_time = now
            else:
                self._free_fall_start = None

        elif self._state == _State.FREE_FALL_DETECTED:
            elapsed = now - (self._last_free_fall_time or now)
            impact   = self._latest_accel_mag > IMPACT_ACCEL_THRESHOLD
            rotation = self._latest_gyro_mag  > ROTATION_THRESHOLD

            if impact and rotation:
                self._reset()
                self._on_fall_confirmed()
                return

            if elapsed > IMPACT_WINDOW_MS:
                self._reset()

    @staticmethod
    def _magnitude(x: float, y: float, z: float) -> float:
        return math.sqrt(x * x + y * y + z * z)

    def _reset(self) -> None:
        self._state              = _State.IDLE
        self._latest_accel_mag   = 9.8   # gravity at rest
        self._latest_gyro_mag    = 0.0
        self._free_fall_start: Optional[int] = None
        self._last_free_fall_time: Optional[int] = None


# ── CSV helpers ──────────────────────────────────────────────────────────────

@dataclass
class SensorEvent:
    time_ms: int
    x: float
    y: float
    z: float
    kind: str  # "accel" or "gyro"


def _read_sensor_csv(path: str, kind: str) -> list[SensorEvent]:
    events: list[SensorEvent] = []
    with open(path, newline="") as f:
        reader = csv.reader(f)
        next(reader)  # skip header
        for row in reader:
            if len(row) < 4:
                continue
            t_s = float(row[0])
            x   = float(row[1])
            y   = float(row[2])
            z   = float(row[3])
            # CSV times can start slightly negative (offset from recording start).
            # We shift so the first reading is at t=0.
            events.append(SensorEvent(
                time_ms=round(t_s * 1000),
                x=x, y=y, z=z,
                kind=kind,
            ))
    # Normalise times so the earliest event is at t=0
    if events:
        t0 = events[0].time_ms
        for e in events:
            e.time_ms -= t0
    return events


def _load_scenario(scenario_dir: str) -> list[SensorEvent]:
    accel_path = os.path.join(scenario_dir, "Accelerometer.csv")
    gyro_path  = os.path.join(scenario_dir, "Gyroscope.csv")

    accel_events = _read_sensor_csv(accel_path, "accel")
    gyro_events  = _read_sensor_csv(gyro_path,  "gyro")

    # Merge and sort by time so events are replayed in chronological order
    all_events = accel_events + gyro_events
    all_events.sort(key=lambda e: e.time_ms)
    return all_events


# ── Per-scenario analysis helpers ────────────────────────────────────────────

@dataclass
class ScenarioStats:
    min_accel_mag: float = float("inf")
    max_accel_mag: float = float("-inf")
    max_gyro_mag:  float = float("-inf")
    free_fall_periods: int = 0          # times accel went below threshold
    impact_spikes: int = 0              # times accel exceeded impact threshold
    rotation_spikes: int = 0            # times gyro exceeded rotation threshold
    duration_ms: int = 0


def _run_scenario(events: list[SensorEvent]) -> tuple[bool, ScenarioStats]:
    """Run the detector and collect diagnostic stats. Returns (fall_detected, stats)."""
    fall_detected = False
    stats = ScenarioStats()

    # Track state for stats
    in_low_accel = False
    latest_accel = 9.8
    latest_gyro  = 0.0

    def on_fall():
        nonlocal fall_detected
        fall_detected = True

    detector = FallDetector(on_fall)

    for e in events:
        if e.kind == "accel":
            mag = math.sqrt(e.x**2 + e.y**2 + e.z**2)
            latest_accel = mag
            stats.min_accel_mag = min(stats.min_accel_mag, mag)
            stats.max_accel_mag = max(stats.max_accel_mag, mag)
            if mag > IMPACT_ACCEL_THRESHOLD:
                stats.impact_spikes += 1
            if mag < FREE_FALL_ACCEL_THRESHOLD and not in_low_accel:
                stats.free_fall_periods += 1
                in_low_accel = True
            elif mag >= FREE_FALL_ACCEL_THRESHOLD:
                in_low_accel = False
            detector.update_accelerometer(e.x, e.y, e.z, e.time_ms)

        else:  # gyro
            mag = math.sqrt(e.x**2 + e.y**2 + e.z**2)
            latest_gyro = mag
            stats.max_gyro_mag = max(stats.max_gyro_mag, mag)
            if mag > ROTATION_THRESHOLD:
                stats.rotation_spikes += 1
            detector.update_gyroscope(e.x, e.y, e.z, e.time_ms)

    if events:
        stats.duration_ms = events[-1].time_ms - events[0].time_ms

    return fall_detected, stats


# ── Known scenario labels (from Data-key.txt) ────────────────────────────────
# True  → real fall (phone dropped or person fell)
# False → non-fall activity
# None  → ambiguous (tripped but caught self / soft surface)

EXPECTED: dict[str, Optional[bool]] = {
    "Drop 2":         True,   # dropped ~4 feet onto carpet
    "Fall pocket":    None,   # tripped but caught with hands (borderline)
    "Jog":            False,  # running
    "Regular use":    False,  # raising to ear, setting down, walking
    "Sitting standing": False, # sit/stand transitions
    "Soft drop":      None,   # dropped ~2 feet onto couch (borderline)
    "Walking":        False,  # regular walking
}


# ── Main ─────────────────────────────────────────────────────────────────────

def _scenario_key(folder_name: str) -> str:
    """Strip the timestamp suffix from the folder name."""
    # e.g. "Drop 2 2026-04-06 17-14-20" → "Drop 2"
    parts = folder_name.rsplit(" ", 2)
    return parts[0] if len(parts) == 3 else folder_name


def main() -> None:
    data_dir = sys.argv[1] if len(sys.argv) > 1 else os.path.expanduser("~/Downloads/test_data")

    if not os.path.isdir(data_dir):
        print(f"ERROR: test data directory not found: {data_dir}")
        print("Usage: python test_csv.py [path_to_test_data_dir]")
        sys.exit(1)

    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    out_path = os.path.join(os.path.dirname(__file__), f"test_results_{timestamp}.txt")
    tee = _Tee(out_path)
    sys.stdout = tee
    print(f"Results saved to: {out_path}\n")

    scenarios = sorted(
        d for d in os.listdir(data_dir)
        if os.path.isdir(os.path.join(data_dir, d))
    )

    if not scenarios:
        print(f"ERROR: no scenario folders found in {data_dir}")
        sys.exit(1)

    print("=" * 70)
    print(f"FallGuard — CSV Algorithm Test")
    print(f"Data directory : {data_dir}")
    print(f"Scenarios found: {len(scenarios)}")
    print("=" * 70)

    thresholds = (
        f"  Free-fall threshold : accel < {FREE_FALL_ACCEL_THRESHOLD} m/s² "
        f"for ≥ {FREE_FALL_MIN_DURATION_MS} ms\n"
        f"  Impact threshold    : accel > {IMPACT_ACCEL_THRESHOLD} m/s²\n"
        f"  Rotation threshold  : gyro  > {ROTATION_THRESHOLD} rad/s\n"
        f"  Impact window       : {IMPACT_WINDOW_MS} ms after free-fall ends"
    )
    print(thresholds)
    print("=" * 70)

    passes = 0
    total_with_expectation = 0

    for folder in scenarios:
        scenario_path = os.path.join(data_dir, folder)
        key = _scenario_key(folder)
        expected = EXPECTED.get(key)

        print(f"\nScenario : {key}")
        print(f"Folder   : {folder}")

        try:
            events = _load_scenario(scenario_path)
        except FileNotFoundError as exc:
            print(f"  SKIP — missing CSV file: {exc}")
            continue

        fall_detected, stats = _run_scenario(events)

        # ── Results ──
        result_str = "FALL DETECTED" if fall_detected else "no fall"
        print(f"Result   : {result_str}")

        if expected is True:
            verdict = "PASS" if fall_detected else "FAIL"
            label   = "should be FALL"
        elif expected is False:
            verdict = "PASS" if not fall_detected else "FAIL"
            label   = "should be NO FALL"
        else:
            verdict = "N/A (ambiguous)"
            label   = "borderline scenario"

        print(f"Expected : {label}  →  [{verdict}]")

        if expected is not None:
            total_with_expectation += 1
            if verdict == "PASS":
                passes += 1

        # ── Diagnostic stats ──
        duration_s = stats.duration_ms / 1000
        print(f"  Duration         : {duration_s:.2f} s  ({len(events)} events)")
        print(f"  Accel min/max    : {stats.min_accel_mag:.2f} / {stats.max_accel_mag:.2f} m/s²")
        print(f"  Gyro max         : {stats.max_gyro_mag:.2f} rad/s")
        print(f"  Low-accel periods: {stats.free_fall_periods}  "
              f"(accel < {FREE_FALL_ACCEL_THRESHOLD} m/s²)")
        print(f"  Impact spikes    : {stats.impact_spikes}  "
              f"(accel > {IMPACT_ACCEL_THRESHOLD} m/s²)")
        print(f"  Rotation spikes  : {stats.rotation_spikes}  "
              f"(gyro > {ROTATION_THRESHOLD} rad/s)")

        # ── Hint if expected to fail but didn't (or vice versa) ──
        if expected is True and not fall_detected:
            if stats.free_fall_periods == 0:
                print("  HINT: No free-fall phase detected — "
                      "consider lowering FREE_FALL_ACCEL_THRESHOLD")
            elif stats.impact_spikes == 0:
                print("  HINT: No impact spike — "
                      "consider lowering IMPACT_ACCEL_THRESHOLD")
            elif stats.rotation_spikes == 0:
                print("  HINT: Impact found but no rotation — "
                      "consider lowering ROTATION_THRESHOLD")
            else:
                print("  HINT: All individual signals present but "
                      "not in the right time window — "
                      "consider increasing IMPACT_WINDOW_MS")
        elif expected is False and fall_detected:
            print("  HINT: False positive — consider tightening a threshold or "
                  "increasing FREE_FALL_MIN_DURATION_MS")

        print("-" * 70)

    # ── Summary ──
    print(f"\nSUMMARY: {passes}/{total_with_expectation} scenarios with clear expectations passed.")
    if passes == total_with_expectation:
        print("All expected scenarios passed!")
    else:
        print("Some scenarios failed — check the HINTs above to tune thresholds.")
    print("=" * 70)
    tee.close()
    sys.stdout = tee._stdout


if __name__ == "__main__":
    main()
