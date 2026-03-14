package frc.robot.constants;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import frc.robot.Constants;

public final class FeederConstants {
  public static final int kFeederCanId = Constants.CANId.kFeederCanId;
  public static final int kFuelSensorChannel = 0;
  public static final double kFeederMotorFreeSpeedRpm = Constants.MotorConstants.kNeoFreeSpeedRpm;

//	public static final double kTolerance = 75.0; // RPMs

  public static final double kFeederZeroOffset = 0.6643792;
  public static final boolean kFeederZeroCentered = true;
  public static final boolean kFeederMotorInverted = true;
  public static final boolean kFeederEncoderInverted = false;

  public static final boolean kFeederEncodeWrapping = false;
  public static final IdleMode kFeederIdleMode = IdleMode.kBrake;

  public static final double kFeederGearRatio = (Constants.GearBox.Max3 * Constants.GearBox.Max4);
  
  // Position is returned in native units of rotations and will be multiplied by
  // this conversion factor.
  // Divide by gear ratio to convert motor shaft rotations to output shaft rotations
  public static final double kFeederPositionFactor = 1.0 / kFeederGearRatio; // Output rotations per motor rotation
  public static final double kFeederVelocityFactor = kFeederPositionFactor; // Output RPM per motor RPM

  public static final double kFeederP = 0.0002;
  public static final double kFeederI = 0.0; // 000001;
  public static final double kFeederD = 0.0; // 1;
  public static final double kFeederVelFF = 1.0 / (kFeederMotorFreeSpeedRpm / kFeederGearRatio); // 0000037;

  public static final double kFeederMinOutput = -1.0;
  public static final double kFeederMaxOutput = 1.0;

  // Moderate (Balanced)
  public static final double kFeederMaxVel = 4000.0; // RPM (~70% of NEO max)
  public static final double kFeederMaxAccel = 10000.0; // RPM/sec (0.4 sec to full speed)
  public static final double kFeederAllowedErr = 75.0; // RPM
  
  public static final int kFeederCurrentLimit = 50; // amps

  //Signal Config
  public static final int kPrimaryEncoderVelocityPeriod = 10; 
  public static final int kPrimaryEncoderPositionPeriod = 20; 
  public static final int kAbsoluteEncoderPositionPeriod = 200; 

  private FeederConstants() {}
}
