package frc.robot.constants;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import frc.robot.Constants;

public final class ShooterConstants {
  public static final int kLeftShooterCanId = Constants.CANId.kShooterLeftCanId;
  public static final int kRightShooterCanId = Constants.CANId.kShooterRightCanId;
  public static final int kTiltMotorCanId = Constants.CANId.kShooterTiltCanId;
  public static final double kShooterMotorFreeSpeedRpm = Constants.MotorConstants.kVortexFreeSpeedRpm;

  public static final double kBallisticsCoefficient = 0.5; // Drag coefficient for projectile

//  public static final double kLeftZeroOffset = 0.6643792;
//  public static final boolean kLeftZeroCentered = true;
  public static final boolean kLeftMotorInverted = false;
  public static final boolean kLeftEncoderInverted = true;
  public static final boolean kLeftEncodeWrapping = false;

  // public static final double kRightZeroOffset = 0.4019657;
  // public static final boolean kRightZeroCentered = true;
  public static final boolean kRightMotorInverted = false;
  // public static final boolean kRightEncoderInverted = false;
  // public static final boolean kRightEncodeWrapping = false;

  public static final double kTiltZeroOffset = 0.94554865;
  public static final boolean kTiltZeroCentered = true;
  public static final boolean kTiltMotorInverted = true;
  public static final boolean kTiltEncoderInverted = false;
  public static final boolean kTiltEncodeWrapping = false;

  // Position is returned in native units of rotations and will be multiplied by
	// this conversion factor.
  public static final double kShooterGearRatio = 1.0; // 1.0 is no reduction

//	public static final double kShooterPositionFactor = 1.0;
  public static final double kShooterVelocityFactor = 1.0; // Keep at 1.0 to match RPM units

  // Position is returned in native units of rotations and will be multiplied by
	// this conversion factor.
  public static final double kTiltGearRatio = 1.0; // direct output, no extra belt reduction

	// IMPORTANT: Through-bore encoder is mounted on the geared-down OUTPUT SHAFT.
	// Position factor converts output shaft rotations directly to degrees.
  public static final double kTiltPositionFactor = 360.0; // degrees per output rotation
  public static final double kTiltVelocityFactor = kTiltPositionFactor / 60.0; // degrees per second

  public static final double kP = 0.0002; // maxmotion 0.025;
  public static final double kI = 0.0; // maxmotion 0.0
  public static final double kD = 0.0; // maxmotion 0.0
  public static final double kVelFF = 1.0 / kShooterMotorFreeSpeedRpm;

  public static final double kMinOutput = -1.0;
  public static final double kMaxOutput = 1.0;

  // Moderate (Balanced)
  // public static final double kMaxVel = 6000.0; // RPM (~88% of Vortex max)
  // public static final double kMaxAccel = 15000.0; // maxmotion 0.0
  public static final double kAllowedErr = 75.0; // maxmotion 0.0

  public static final double kPosP = 0.07; //.035   // 0.8
  public static final double kPosI = 0.0;
  public static final double kPosD = 0.225;   // 0.1
//  public static final double kPosFF = 1.0 / kShooterMotorFreeSpeedRpm;

	// public static final double kPosS = 0.05;
	// public static final double kPosV = 0.02;
	// public static final double kPosG = 0.5;
//	public static final double kPosA = 0.0000037;

  public static final double kPosMinOutput = -1.0;
  public static final double kPosMaxOutput = 1.0;

  // Moderate (Balanced)
  // public static final double kPosMaxVel = 80.0; //90.0; // degrees/sec (~1.1 sec for 40° travel)
  // public static final double kPosMaxAccel = 150.0; //180.0; // degrees/sec² (0.5 sec to max speed)
  public static final double kPosAllowedErr = 0.1; //0.5; // degrees

  public static final IdleMode kLeftIdleMode = IdleMode.kCoast;
  public static final IdleMode kRightIdleMode = IdleMode.kCoast;
  public static final IdleMode kTiltIdleMode = IdleMode.kBrake;
  
  public static final int kLeftCurrentLimit = 50; // amps
  public static final int kRightCurrentLimit = 50; // amps
  public static final int kTiltCurrentLimit = 50; // amps

  private ShooterConstants() {}
}
