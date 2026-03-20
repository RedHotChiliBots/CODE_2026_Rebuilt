package frc.robot.constants;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import frc.robot.Constants;

public final class IntakeConstants {
  public static final int kIntakeMotorCanId = Constants.CANId.kIntakeIntakeCanId;
  public static final int kTiltMotorCanId = Constants.CANId.kIntakeTiltCanId;
  public static final double kIntakeMotorFreeSpeedRpm = Constants.MotorConstants.kNeoFreeSpeedRpm;

  // =========================== I N T A K E ======================

  // public static final double kIntakeTolerance = 50.0; // rpms
  public static final double kIntakeGearRatio = (Constants.GearBox.Max3 * Constants.GearBox.Max4);

  // Position is returned in native units of rotations and will be multiplied by
  // this conversion factor.
  // Divide by gear ratio to convert motor shaft rotations to output shaft
  // rotations
  public static final double kIntakePositionFactor = 1.0 / kIntakeGearRatio; // Output rotations per motor rotation
  public static final double kIntakeVelocityFactor = kIntakePositionFactor; // Output RPM per motor RPM

  public static final double kIntakeZeroOffset = 0.6643792;
  public static final boolean kIntakeZeroCentered = true;
  public static final boolean kIntakeMotorInverted = true;
  public static final boolean kIntakeEncoderInverted = true;
  public static final boolean kIntakeEncodeWrapping = false;
  public static final IdleMode kIntakeIdleMode = IdleMode.kBrake;

  public static final double kIntakeP = 0.0002; // maxmotion 0.025;
  public static final double kIntakeI = 0.0; // maxmotion 0.0
  public static final double kIntakeD = 0.0; // maxmotion 0.0
  public static final double kVelFF = 1.0 / (kIntakeMotorFreeSpeedRpm / kIntakeGearRatio);
  public static final double kIntakeMinOutput = -1.0;
  public static final double kIntakeMaxOutput = 1.0;

  // Aggressive (Fast, Less Smooth)
  // public static final double kIntakeMaxVel = 5000.0; // RPM (~88% of NEO max)
  // public static final double kIntakeMaxAccel = 16000.0; // RPM/sec (0.3125 sec
  // to full speed)
  public static final double kIntakeAllowedErr = 50.0; // RPM

  public static final int kIntakeCurrentLimit = 50; // amps

  // =========================== T I L T ======================

  public static final double kTiltGearRatio = 1.0; // direct output, no extra belt reduction

  // IMPORTANT: Through-bore encoder is mounted on the geared-down OUTPUT SHAFT.
  // Position factor converts output shaft rotations directly to degrees.
  public static final double kTiltPositionFactor = 360.0; // degrees per output rotation
  public static final double kTiltVelocityFactor = kTiltPositionFactor / 60.0; // degrees per second

  public static final double kTiltZeroOffset = 0.7301405;
  public static final boolean kTiltZeroCentered = true;
  public static final boolean kTiltMotorInverted = true;
  public static final boolean kTiltEncoderInverted = false;
  public static final boolean kTiltEncodeWrapping = false;
  public static final IdleMode kTiltIdleMode = IdleMode.kBrake;

  // From Shooter Tilt
  // public static final double kPosP = 0.07; //.035 // 0.8
  // public static final double kPosI = 0.0;
  // public static final double kPosD = 0.225; // 0.1

  public static final double kTiltP = 0.07; // maxmotion 0.025;
  public static final double kTiltI = 0.0; // maxmotion 0.0
  public static final double kTiltD = 0.225; // maxmotion 0.0
  public static final double kTiltMinOutput = -0.25;
  public static final double kTiltMaxOutput = 0.25;

  // Moderate (Balanced)
  // public static final double kTiltMaxVel = 120.0; // degrees/sec (~1.7 sec for
  // full 80° travel)
  // public static final double kTiltMaxAccel = 240.0; // degrees/sec² (0.5 sec to
  // max speed)
  public static final double kTiltAllowedErr = 1.0; // degrees

  public static final int kTiltCurrentLimit = 50; // amps

  private IntakeConstants() {
  }
}
