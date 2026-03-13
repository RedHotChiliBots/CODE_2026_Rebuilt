package frc.robot.constants;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public final class CommandSwerveDrivetrainConstants {
  public static final double kSimLoopPeriod = 0.004; // 4 ms

  /* Blue alliance sees forward as 0 degrees (toward red alliance wall) */
  public static final Rotation2d kBlueAlliancePerspectiveRotation = Rotation2d.kZero;

  /* Red alliance sees forward as 180 degrees (toward blue alliance wall) */
  public static final Rotation2d kRedAlliancePerspectiveRotation = Rotation2d.k180deg;

  /* Hub pose for 2026  */
  public static final Pose2d kHubPose =
      new Pose2d(new Translation2d(4.660, 4.118), Rotation2d.fromDegrees(180.0));

  /* This is the blue side starting pose. AutoBuilder will mirror for red. */
  public static final Pose2d kStartPose =
      new Pose2d(new Translation2d(3.45, 2.75), Rotation2d.fromDegrees(42.5));

  public static final double kTranslationPidP = 5.0;
  public static final double kTranslationPidI = 0.0;
  public static final double kTranslationPidD = 0.0;

  public static final double kRotationPidP = 5.0;
  public static final double kRotationPidI = 0.0;
  public static final double kRotationPidD = 0.0;

  private CommandSwerveDrivetrainConstants() {}
}
