// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.util.Color;

/**
 * Robot-wide shared constants only.
 *
 * <p>Subsystem-specific constants should live in their matching constants files.
 */
public final class Constants {

  public static final Mode simMode = Mode.SIM;
  public static final Mode currentMode = RobotBase.isReal() ? Mode.REAL : simMode;

  public enum Mode {
    /** Running on a real robot. */
    REAL,

    /** Running a physics simulator. */
    SIM,

		/** Replaying from a log file. */
    REPLAY
  }

  public static final class OIConstants {
    public static final int kDriverControllerPort = 0;
    public static final int kOperatorControllerPort = 1;
    public static final double kDriveDeadband = 0.05;

    private OIConstants() {}
  }

  public static final class CANId {
    public static final int kPDPCanID = 1;
    public static final int kServoHubCanId = 0;

    public static final int kClimber1CanId = 40;
    public static final int kClimber2CanId = 41;
    public static final int kClimber3CanId = 42;
    public static final int kClimber4CanId = 43;

    public static final int kShooterLeftCanId = 50;
    public static final int kShooterRightCanId = 51;
    public static final int kShooterTiltCanId = 52;

    public static final int kIntakeIntakeCanId = 55;
    public static final int kIntakeTiltCanId = 56;

    public static final int kFeederCanId = 60;

    private CANId() {}
  }

  public static final class MotorConstants {
    public static final double kVortexFreeSpeedRpm = 6784;
    public static final double kNeoFreeSpeedRpm = 5676;
    public static final double k550FreeSpeedRpm = 11000;

    private MotorConstants() {}
  }

  public static final class ColorConstants {
    public static final String Stopped = Color.kRed.toHexString();
    public static final String Moving = Color.kYellow.toHexString();
    public static final String OnTarget = Color.kGreen.toHexString();

    private ColorConstants() {}
  }

  // Gear ratios shared across multiple subsystem constants files.
  
  // Gear ratios for Max and Ultra gearboxes
  public static final class GearBox {
    public static final double Max3 = 3.0;
    public static final double Max4 = 4.0;
    public static final double Max5 = 5.0;
    public static final double Max9 = 9.0;

    public static final double Ultra3 = 2.89;
    public static final double Ultra4 = 3.61;
    public static final double Ultra5 = 5.23;

    private GearBox() {}
  }

  private Constants() {}
}
