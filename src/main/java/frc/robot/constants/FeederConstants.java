package frc.robot.constants;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import frc.robot.Constants;

public final class FeederConstants {
  public static final int kFeederCanId = 60;
  public static final int kFuelSensorChannel = 0;
  public static final double kFeederMotorFreeSpeedRpm = Constants.MotorConstants.kNeoFreeSpeedRpm;

  public static final double kTollerance = 0.5; // RPMs

  public static final double kFeederZeroOffset = 0.6643792;
  public static final boolean kFeederZeroCentered = true;
  public static final boolean kFeederMotorInverted = true;
  public static final boolean kFeederEncoderInverted = false;

  public static final boolean kFeederEncodeWrapping = false;
  public static final IdleMode kFeederIdleMode = IdleMode.kBrake;

  public static final double kFeederGearRatio = Constants.GearBox.Max3 * Constants.GearBox.Max4;

  public static final double kFeederPositionFactor = 1.0;
  public static final double kFeederVelocityFactor = 1.0;

  public static final double kFeederP = 0.00009;
  public static final double kFeederI = 0.0;
  public static final double kFeederD = 0.0;
  public static final double kFeederVelFF = 0.0;

  public static final double kFeederMinOutput = -1.0;
  public static final double kFeederMaxOutput = 1.0;

  public static final double kFeederMaxVel = 100000.0;
  public static final double kFeederMaxAccel = 40000.0;
  public static final double kFeederAllowedErr = 0.1;

  public static final int kFeederCurrentLimit = 50; // amps

  private FeederConstants() {}
}
