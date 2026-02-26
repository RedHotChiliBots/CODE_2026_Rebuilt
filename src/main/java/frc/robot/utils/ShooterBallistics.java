package frc.robot.utils;

import edu.wpi.first.math.util.Units;

public final class ShooterBallistics {
  private ShooterBallistics() {}

  // Physics
  public static final double g = 9.80665;

  // ====== TARGET SPECS (2026 manual) ======
  // Using center of opening for now. Manual gives opening edge at 72 inches.
  // Start with 72 inches as target height and calibrate later.
  public static final double kTargetHeightM = Units.inchesToMeters(72.0);

  // ====== MECHANISM SPECS ======
  // Shooter wheel: 4" diameter => 2" radius
  public static final double kShooterWheelRadiusM = Units.inchesToMeters(2.0);

  // Free-spin max RPM
  public static final double kMaxWheelRpm = 6784.0;

  // Tunable: accounts for slip/compression/energy loss.
  // Start at 1.0, then tune so predicted RPM matches what actually scores.
  public static final double kExitVelocityEfficiency = 1.0;

  // Hood angle limits (degrees from horizon)
  public static final double kMinAngleDeg = 50.0;
  public static final double kMaxAngleDeg = 72.5;

  // Shooter exit height (meters) varies with hood angle:
  // At max angle (72.5°): 626.8 mm
  // At min angle (50.0°): 683.0 mm
  public static final double kHeightAtMinAngleM = 0.6830;
  public static final double kHeightAtMaxAngleM = 0.6268;

  public record ShotSetpoint(
      double angleDeg,
      double exitSpeedMps,
      double wheelRpm,
      boolean feasible
  ) {}

  /** Linear interpolation of exit height based on your two measured points. */
  public static double shooterExitHeightM(double angleDeg) {
    double t = (angleDeg - kMinAngleDeg) / (kMaxAngleDeg - kMinAngleDeg);
    t = Math.max(0.0, Math.min(1.0, t));
    return kHeightAtMinAngleM + t * (kHeightAtMaxAngleM - kHeightAtMinAngleM);
  }

  /**
   * Required exit speed for a stationary shot at distanceM and angleDeg.
   * Returns NaN if physically impossible at that angle (won't clear height).
   */
  public static double requiredExitSpeedMps(double distanceM, double angleDeg) {
    double theta = Math.toRadians(angleDeg);
    double cos = Math.cos(theta);
    double tan = Math.tan(theta);

    double h = kTargetHeightM - shooterExitHeightM(angleDeg); // target - muzzle height

    // v^2 = g d^2 / (2 cos^2(theta) (d tan(theta) - h))
    double denom = 2.0 * cos * cos * (distanceM * tan - h);
    if (denom <= 0.0) return Double.NaN;

    double v2 = g * distanceM * distanceM / denom;
    return Math.sqrt(v2);
  }

  /** Convert exit speed to wheel RPM using surface-speed approximation. */
  public static double exitSpeedToWheelRpm(double exitSpeedMps) {
    double surfaceSpeedMps = exitSpeedMps / kExitVelocityEfficiency;
    return (surfaceSpeedMps / (2.0 * Math.PI * kShooterWheelRadiusM)) * 60.0;
  }

  /**
   * Find a stationary shot by sweeping the allowed angle range and minimizing required wheel RPM.
   * stepDeg: 0.25–1.0 recommended.
   */
  public static ShotSetpoint solveStationary(double distanceM, double stepDeg) {
    double bestAngle = Double.NaN;
    double bestExit = Double.POSITIVE_INFINITY;
    double bestRpm = Double.POSITIVE_INFINITY;

    for (double a = kMinAngleDeg; a <= kMaxAngleDeg + 1e-9; a += stepDeg) {
      double v = requiredExitSpeedMps(distanceM, a);
      if (Double.isNaN(v)) continue;

      double rpm = exitSpeedToWheelRpm(v);
      if (rpm > kMaxWheelRpm) continue;

      if (rpm < bestRpm) {
        bestRpm = rpm;
        bestAngle = a;
        bestExit = v;
      }
    }

    if (Double.isNaN(bestAngle)) {
      return new ShotSetpoint(Double.NaN, Double.NaN, Double.NaN, false);
    }

    return new ShotSetpoint(bestAngle, bestExit, bestRpm, true);
  }
}