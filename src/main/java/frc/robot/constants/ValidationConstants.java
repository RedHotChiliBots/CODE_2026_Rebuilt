package frc.robot.constants;

public final class ValidationConstants {
  private ValidationConstants() {}

  public static final class UI {
    /**
     * This warning is intentionally long and explicit because validation is allowed
     * to move the drivetrain and mechanisms on real hardware. Operators should see
     * the safety expectation every time they interact with the validation surface.
     */
    public static final String kWarningMessage =
        "Place robot on blocks and clear people before running validation.";

    /**
     * A short summary for the dashboard before any checks have run. Keeping this as
     * a constant avoids magic strings in the UI update path.
     */
    public static final String kNotRunSummary = "NOT_RUN";

    private UI() {}
  }

  public static final class Common {
    /**
     * Validation waits should be long enough for closed-loop mechanisms to settle
     * under normal pit conditions, but short enough to fail fast when a controller
     * is disconnected, misconfigured, or commanding the wrong direction.
     */
    public static final double kMechanismTimeoutSec = 1.5;

    /**
     * Vision validation samples after a brief delay so camera inputs have one
     * scheduler cycle to populate fresh data before pass/fail is evaluated.
     */
    public static final double kVisionSampleDelaySec = 0.1;

    private Common() {}
  }

  public static final class Drivetrain {
    /**
     * The validation drive pulse is intentionally short to reduce risk while the
     * robot is on blocks, but long enough to create measurable odometry and gyro
     * change beyond startup noise.
     */
    public static final double kDrivePulseSeconds = 0.35;

    /**
     * A modest forward speed keeps the test low-risk and cart-safe while still
     * proving that the drive state changes in response to commands.
     */
    public static final double kForwardMetersPerSecond = 0.35;

    /**
     * A small simultaneous rotation makes it possible to verify yaw response
     * without requiring a large chassis movement.
     */
    public static final double kOmegaRadiansPerSecond = 0.5;

    /**
     * Pose delta must clear this threshold to distinguish real drivetrain motion
     * from estimator noise or trivial numerical drift.
     */
    public static final double kMinimumPoseDeltaMeters = 0.02;

    /**
     * Yaw delta must exceed this threshold so the validation can detect a truly
     * responsive gyro rather than a nearly static heading signal.
     */
    public static final double kMinimumYawDeltaDegrees = 2.0;

    private Drivetrain() {}
  }
}
