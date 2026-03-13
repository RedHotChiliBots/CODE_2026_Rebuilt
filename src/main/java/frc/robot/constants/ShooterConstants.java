package frc.robot.constants;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import frc.robot.Constants;

public final class ShooterConstants {
  public static final int kLeftShooterCanId = 50;
  public static final int kRightShooterCanId = 51;
  public static final int kTiltMotorCanId = 52;
  public static final double kShooterMotorFreeSpeedRpm = Constants.MotorConstants.kVortexFreeSpeedRpm;

  public static final double kShooterTollerance = 0.5; // degrees
  public static final double kTiltTollerance = 0.5; // degrees

  public static final double kLeftZeroOffset = 0.6643792;
  public static final boolean kLeftZeroCentered = true;
  public static final boolean kLeftMotorInverted = false;
  public static final boolean kLeftEncoderInverted = true;

  public static final double kRightZeroOffset = 0.4019657;
  public static final boolean kRightZeroCentered = true;
  public static final boolean kRightMotorInverted = false;
  public static final boolean kRightEncoderInverted = false;

  public static final double kTiltZeroOffset = 0.22696681;
  public static final boolean kTiltZeroCentered = true;
  public static final boolean kTiltMotorInverted = true;
  public static final boolean kTiltEncoderInverted = true;

  public static final boolean kLeftEncodeWrapping = false;
  public static final boolean kRightEncodeWrapping = false;
  public static final boolean kTiltEncodeWrapping = false;

  public static final double kTiltGearRatio = Constants.GearBox.Max5 * Constants.GearBox.Max5 * 3.0;

  public static final double kShooterPositionFactor = 1.0;
  public static final double kShooterVelocityFactor = kShooterPositionFactor / 60.0;

  public static final double kTiltPositionFactor = 360 / kTiltGearRatio;
  public static final double kTiltVelocityFactor = kTiltPositionFactor / 60.0;

  public static final double kP = 0.01;
  public static final double kI = 0.0;
  public static final double kD = 0.0;
  public static final double kVelFF = 0.0000037;

  public static final double kMinOutput = -0.5;
  public static final double kMaxOutput = 0.5;

  public static final double kMaxVel = 100000.0;
  public static final double kMaxAccel = 40000.0;
  public static final double kAllowedErr = 1.0;

  public static final double kPosP = 0.01;
  public static final double kPosI = 0.0;
  public static final double kPosD = 0.0;
  public static final double kPosFF = 0.0000037;

  public static final double kPosMinOutput = -0.5;
  public static final double kPosMaxOutput = 0.5;

  public static final double kPosMaxVel = 100000.0;
  public static final double kPosMaxAccel = 40000.0;
  public static final double kPosAllowedErr = 1.0;

  public static final IdleMode kLeftIdleMode = IdleMode.kCoast;
  public static final IdleMode kRightIdleMode = IdleMode.kCoast;
  public static final IdleMode kTiltIdleMode = IdleMode.kBrake;

  public static final int kLeftCurrentLimit = 50; // amps
  public static final int kRightCurrentLimit = 50; // amps
  public static final int kTiltCurrentLimit = 50; // amps

  private ShooterConstants() {}
}
