package frc.robot.constants;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import frc.robot.Constants;

public final class ClimberConstants {
  public static final int kServoHubCanId = 2;
  public static final int kClimber1CanId = 40;
  public static final int kClimber2CanId = 41;
  public static final int kClimber3CanId = 42;
  public static final int kClimber4CanId = 43;

  public static final double kServoAmpLimit = 0.5; // amps
  public static final double kServoTimeout = 0.5; // seconds

  public static final boolean kClimberInverted = false;
  public static final IdleMode kClimberIdleMode = IdleMode.kBrake;
  public static final int kClimberCurrentLimit = 50; // amps

  public static final double kZeroOffset = 0.5;
  public static final boolean kZeroCentered = true;
  public static final boolean kEncoderInverted = true;

  public static final double kClimberGearRatio = Constants.GearBox.Max5 * Constants.GearBox.Max5;
  public static final double kTiltGearRatio = Constants.GearBox.Max5 * Constants.GearBox.Max5;

  public static final double kClimberPositionFactor = (1.0 * Math.PI) / kClimberGearRatio;
  public static final double kClimberVelocityFactor = kClimberPositionFactor / 60.0;

  public static final double kTiltPositionFactor = 360 / kTiltGearRatio;
  public static final double kTiltVelocityFactor = kTiltPositionFactor / 60.0;

  public static final double kPosP = 0.0;
  public static final double kPosI = 0.0;
  public static final double kPosD = 0.0;

  public static final double kPosMinOutput = -0.5;
  public static final double kPosMaxOutput = 0.5;

  public static final double kClimberTollerance = 0.5; // degrees
  public static final double kHookTollerance = 0.5; // degrees

  private ClimberConstants() {}
}
