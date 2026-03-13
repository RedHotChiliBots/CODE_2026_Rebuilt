package frc.robot.constants;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import frc.robot.Constants;

public final class ClimberConstants {
  public static final int kServoHubCanId = Constants.CANId.kServoHubCanId;
  public static final int kClimber1CanId = Constants.CANId.kClimber1CanId;
  public static final int kClimber2CanId = Constants.CANId.kClimber2CanId;
  public static final int kClimber3CanId = Constants.CANId.kClimber3CanId;
  public static final int kClimber4CanId = Constants.CANId.kClimber4CanId;

  public static final double kServoAmpLimit = 0.5; // amps
  public static final double kServoTimeout = 0.5; // seconds

  // Motor Inversion
  public static final boolean kClimberInverted = false;

  // Idle Mode
  public static final IdleMode kClimberIdleMode = IdleMode.kBrake;

  // Current Limit
  public static final int kClimberCurrentLimit = 50; // amps

  // Abs Encoder Configs
  public static final double kZeroOffset = 0.5;
  public static final boolean kZeroCentered = true;
  public static final boolean kEncoderInverted = true;

  public static final double kClimberGearRatio = Constants.GearBox.Max5 * Constants.GearBox.Max5;
  public static final double kTiltGearRatio = Constants.GearBox.Max5 * Constants.GearBox.Max5;

  // Position is returned in native units of rotations and will be multiplied by
	// this conversion factor.
  public static final double kClimberPositionFactor = (1.0 * Math.PI) / kClimberGearRatio; // inches
  public static final double kClimberVelocityFactor = kClimberPositionFactor / 60.0; // inches per second
  
	// Position is returned in native units of rotations and will be multiplied by
	// this conversion factor.
  public static final double kTiltPositionFactor = 360 / kTiltGearRatio;
  public static final double kTiltVelocityFactor = kTiltPositionFactor / 60.0;

  // Closed loop configs
  public static final double kPosP = 0.0; // maxmotion 0.0
  public static final double kPosI = 0.0; // maxmotion 0.0
  public static final double kPosD = 0.0; // maxmotion 0.0

  public static final double kPosMinOutput = -0.5; // max motion -1.0
  public static final double kPosMaxOutput = 0.5; // max motion 1.0

  public static final double kClimberTolerance = 0.5; // degrees
  public static final double kHookTolerance = 0.5; // degrees

  private ClimberConstants() {}
}
