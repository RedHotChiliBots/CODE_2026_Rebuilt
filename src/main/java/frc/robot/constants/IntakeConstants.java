package frc.robot.constants;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import frc.robot.Constants;

public final class IntakeConstants {
  public static final int kIntakeMotorCanId = 55;
  public static final int kTiltMotorCanId = 56;
  public static final double kIntakeMotorFreeSpeedRpm = Constants.MotorConstants.kNeoFreeSpeedRpm;

  public static final double kIntakeTollerance = 0.5; // degrees
  public static final double kTiltTollerance = 0.5; // degrees

  public static final double kIntakeZeroOffset = 0.6643792;
  public static final boolean kIntakeZeroCentered = true;
  public static final boolean kIntakeMotorInverted = true;
  public static final boolean kIntakeEncoderInverted = true;

  public static final double kTiltZeroOffset = 0.4019657;
  public static final boolean kTiltZeroCentered = true;
  public static final boolean kTiltMotorInverted = false;
  public static final boolean kTiltEncoderInverted = false;
  public static final double kTiltMaxVel = 100000.0;
  public static final double kTiltMaxAccel = 40000.0;
  public static final double kTiltAllowedErr = 1.0;

  public static final boolean kIntakeEncodeWrapping = false;
  public static final boolean kTiltEncodeWrapping = false;

  public static final double kIntakeGearRatio = Constants.GearBox.Max3 * Constants.GearBox.Max4;
  public static final double kTiltGearRatio = Constants.GearBox.Max5 * Constants.GearBox.Max5;

  public static final double kIntakePositionFactor = 1.0 / kIntakeGearRatio;
  public static final double kIntakeVelocityFactor = kIntakePositionFactor / 60.0;

  public static final double kTiltPositionFactor = 360.0 / kTiltGearRatio;
  public static final double kTiltVelocityFactor = kTiltPositionFactor / 60.0;

  public static final double kIntakeP = 0.01;
  public static final double kIntakeI = 0.0;
  public static final double kIntakeD = 0.0;
  public static final double kVelFF = 0.0;
  public static final double kIntakeMinOutput = -0.5;
  public static final double kIntakeMaxOutput = 0.5;

  public static final double kIntakeMaxVel = 100000.0;
  public static final double kIntakeMaxAccel = 40000.0;
  public static final double kIntakeAllowedErr = 1.0;

  public static final double kTiltP = 1.0;
  public static final double kTiltI = 0.0;
  public static final double kTiltD = 0.0;
  public static final double kTiltMinOutput = -0.5;
  public static final double kTiltMaxOutput = 0.5;

  public static final IdleMode kIntakeIdleMode = IdleMode.kBrake;
  public static final IdleMode kTiltIdleMode = IdleMode.kBrake;

  public static final int kIntakeCurrentLimit = 50; // amps
  public static final int kTiltCurrentLimit = 50; // amps

  private IntakeConstants() {}
}
