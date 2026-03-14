package frc.robot.constants;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import frc.robot.Constants;

public final class IntakeConstants {
  public static final int kIntakeMotorCanId = Constants.CANId.kIntakeIntakeCanId;
  public static final int kTiltMotorCanId = Constants.CANId.kIntakeTiltCanId;
  public static final double kIntakeMotorFreeSpeedRpm = Constants.MotorConstants.kNeoFreeSpeedRpm;

//	public static final double kIntakeTolerance = 50.0; // rpms
  public static final double kIntakeGearRatio = (Constants.GearBox.Max3 * Constants.GearBox.Max4);

  
	// Position is returned in native units of rotations and will be multiplied by
	// this conversion factor.
	// Divide by gear ratio to convert motor shaft rotations to output shaft rotations
  public static final double kIntakePositionFactor = 1.0 / kIntakeGearRatio; // Output rotations per motor rotation
  public static final double kIntakeVelocityFactor = kIntakePositionFactor; // Output RPM per motor RPM

  public static final double kIntakeZeroOffset = 0.6643792;
  public static final boolean kIntakeZeroCentered = true;
  public static final boolean kIntakeMotorInverted = true;
  public static final boolean kIntakeEncoderInverted = true;

  public static final IdleMode kIntakeIdleMode = IdleMode.kBrake;
  public static final boolean kIntakeEncodeWrapping = false;

  public static final double kIntakeP = 0.0002; // maxmotion 0.025;
  public static final double kIntakeI = 0.0; // maxmotion 0.0
  public static final double kIntakeD = 0.0; // maxmotion 0.0
  public static final double kVelFF = 1.0 / (kIntakeMotorFreeSpeedRpm / kIntakeGearRatio);
  public static final double kIntakeMinOutput = -1.0;
  public static final double kIntakeMaxOutput = 1.0;

  // Aggressive (Fast, Less Smooth)
  public static final double kIntakeMaxVel = 5000.0; // RPM (~88% of NEO max)
  public static final double kIntakeMaxAccel = 16000.0; // RPM/sec (0.3125 sec to full speed)
  public static final double kIntakeAllowedErr = 50.0; // RPM

  public static final int kIntakeCurrentLimit = 50; // amps

//	public static final double kTiltTolerance = 1.0; // degrees

  public static final double kTiltGearRatio = (Constants.GearBox.Max5 * Constants.GearBox.Max5);

  
	// IMPORTANT: Through-bore encoder is mounted on OUTPUT SHAFT, not motor shaft!
	// Position factor converts OUTPUT shaft rotations to degrees
	// 1 output rotation = 360 degrees (no gear ratio needed)
  public static final double kTiltPositionFactor = 360.0; // degrees per output rotation
  public static final double kTiltVelocityFactor = kTiltPositionFactor / 60.0; // degrees per second

  public static final double kTiltZeroOffset = 0.4019657;
  public static final boolean kTiltZeroCentered = true;
  public static final boolean kTiltMotorInverted = false;
  public static final boolean kTiltEncoderInverted = false;

  public static final double kTiltP = 0.8; // maxmotion 0.025;
  public static final double kTiltI = 0.0; // maxmotion 0.0
  public static final double kTiltD = 0.1; // maxmotion 0.0
  public static final double kTiltMinOutput = -1.0;
  public static final double kTiltMaxOutput = 1.0;

  // Moderate (Balanced)
  public static final double kTiltMaxVel = 120.0; // degrees/sec (~1.7 sec for full 80° travel)
  public static final double kTiltMaxAccel = 240.0; // degrees/sec² (0.5 sec to max speed)
  public static final double kTiltAllowedErr = 1.0; // degrees

  public static final IdleMode kTiltIdleMode = IdleMode.kBrake;
  public static final boolean kTiltEncodeWrapping = false;
  
  public static final int kTiltCurrentLimit = 50; // amps

  //Signal Config
  public static final int kPrimaryEncoderVelocityPeriod = 10; 
  public static final int kPrimaryEncoderPositionPeriod = 20; 
  public static final int kAbsoluteEncoderPositionPeriod = 200; 

  private IntakeConstants() {}
}
