// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.util.Color;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide
 * numerical or boolean
 * constants. This class should not be used for any other purpose. All constants
 * should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>
 * It is advised to statically import this class (or one of its inner classes)
 * wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {

	public static final Mode simMode = Mode.SIM;
	public static final Mode currentMode = RobotBase.isReal() ? Mode.REAL : simMode;

	public static enum Mode {
		/** Running on a real robot. */
		REAL,

		/** Running a physics simulator. */
		SIM,

		/** Replaying from a log file. */
		REPLAY
	}

	public static class OIConstants {
		public static final int kDriverControllerPort = 0;
		public static final int kOperatorControllerPort = 1;
		public static final double kDriveDeadband = 0.05;
	}

	public static final class CANId {

		public static final int kPDHCanID = 1;
		public static final int kServoHubCanId = 2;

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
	}

	public static final class DIOId {
		public static final int kFuelAvail = 0;
	}

	public static final class AIOId {
		public static final int kFuelSensor = 0;
	}

	public static final class MotorConstants {
		public static final double kVortexFreeSpeedRpm = 6784;
		public static final double kNeoFreeSpeedRpm = 5676;
		public static final double k550FreeSpeedRpm = 11000;
	}

	public static final class ColorConstants {
		public static final String Stopped = Color.kRed.toHexString();
		public static final String Moving = Color.kYellow.toHexString();
		public static final String OnTarget = Color.kGreen.toHexString();
	}

	public static final class ChassisConstants {
		// not the maximum capable speeds of
		// the robot, rather the allowed maximum speeds
		public static final double kMaxSpeedMetersPerSecond = 4.8;
		public static final double kMaxAngularSpeed = 2 * Math.PI; // radians per second

		public static final double kDirectionSlewRate = 1.2; // radians per second
		public static final double kMagnitudeSlewRate = 1.8; // percent per second (1 = 100%)
		public static final double kRotationalSlewRate = 2.0; // percent per second (1 = 100%)

		// Chassis configuration
		public static final double kTrackWidth = Units.inchesToMeters(24.0);
		// Distance between centers of right and left wheels on robot
		public static final double kWheelBase = Units.inchesToMeters(24.5);
		// Distance between front and back wheels on robot
		public static final double kWheelRadius = Math.sqrt(2.0 * Math.pow(kWheelBase, 2)) / 2.0;

		public static final SwerveDriveKinematics kDriveKinematics = new SwerveDriveKinematics(
				new Translation2d(kWheelBase / 2, kTrackWidth / 2),
				new Translation2d(kWheelBase / 2, -kTrackWidth / 2),
				new Translation2d(-kWheelBase / 2, kTrackWidth / 2),
				new Translation2d(-kWheelBase / 2, -kTrackWidth / 2));

		// Angular offsets of the modules relative to the chassis in radians
		public static final double kFrontLeftChassisAngularOffset = Math.PI / 2;
		public static final double kFrontRightChassisAngularOffset = 0.0;
		public static final double kBackLeftChassisAngularOffset = Math.PI;
		public static final double kBackRightChassisAngularOffset = -Math.PI / 2;

		// public static final double kFrontLeftChassisAngularOffset = 0.0;
		// public static final double kFrontRightChassisAngularOffset = 0.0;
		// public static final double kBackLeftChassisAngularOffset = 0.0;
		// public static final double kBackRightChassisAngularOffset = 0.0;

		public static final boolean kGyroReversed = false;
	}

	public static final class DriveConstants {
		// Driving Parameters - Note that these are not the maximum capable speeds of
		// the robot, rather the allowed maximum speeds
		public static final double kMaxSpeedMetersPerSecond = 4.8;
		public static final double kMaxAngularSpeed = 2 * Math.PI; // radians per second

		// Chassis configuration
		public static final double kTrackWidth = Units.inchesToMeters(24.0);
		// Distance between centers of right and left wheels on robot
		public static final double kWheelBase = Units.inchesToMeters(24.5);
		// Distance between front and back wheels on robot
		public static final SwerveDriveKinematics kDriveKinematics = new SwerveDriveKinematics(
				new Translation2d(kWheelBase / 2, kTrackWidth / 2),
				new Translation2d(kWheelBase / 2, -kTrackWidth / 2),
				new Translation2d(-kWheelBase / 2, kTrackWidth / 2),
				new Translation2d(-kWheelBase / 2, -kTrackWidth / 2));

		// Angular offsets of the modules relative to the chassis in radians
		public static final double kFrontLeftChassisAngularOffset = -Math.PI / 2;
		public static final double kFrontRightChassisAngularOffset = 0;
		public static final double kBackLeftChassisAngularOffset = Math.PI;
		public static final double kBackRightChassisAngularOffset = Math.PI / 2;

		public static final boolean kGyroReversed = false;

		public static final int kDrivingMotorCurrentLimit = 50; // amps
		public static final int kTurningMotorCurrentLimit = 20; // amps
	}

	public static final class ModuleConstants {
		// The MAXSwerve module can be configured with one of three pinion gears: 12T,
		// 13T, or 14T. This changes the drive speed of the module (a pinion gear with
		// more teeth will result in a robot that drives faster).
		public static final int kDrivingMotorPinionTeeth = 16;

		// Calculations required for driving motor conversion factors and feed forward
		public static final double kDrivingMotorFreeSpeedRps = MotorConstants.kVortexFreeSpeedRpm / 60;
		public static final double kWheelDiameterMeters = Units.inchesToMeters(4.0); // 0.0762;
		public static final double kWheelCircumferenceMeters = kWheelDiameterMeters * Math.PI;
		// 45 teeth on the wheel's bevel gear, 22 teeth on the first-stage spur gear, 15
		// teeth on the bevel pinion
		public static final double kDrivingMotorReduction = 5.9; // (45.0 * 22) / (kDrivingMotorPinionTeeth * 15);
		public static final double kDriveWheelFreeSpeedRps = (kDrivingMotorFreeSpeedRps * kWheelCircumferenceMeters)
				/ kDrivingMotorReduction;
	}

	public static final class AutoConstants {
		public static final double kMaxSpeedMetersPerSecond = 3;
		public static final double kMaxAccelerationMetersPerSecondSquared = 3;
		public static final double kMaxAngularSpeedRadiansPerSecond = Math.PI;
		public static final double kMaxAngularSpeedRadiansPerSecondSquared = Math.PI;

		public static final double kPXController = 1;
		public static final double kPYController = 1;
		public static final double kPThetaController = 1;

		// Constraint for the motion profiled robot angle controller
		public static final TrapezoidProfile.Constraints kThetaControllerConstraints = new TrapezoidProfile.Constraints(
				kMaxAngularSpeedRadiansPerSecond, kMaxAngularSpeedRadiansPerSecondSquared);
	}

	// Gear ratios for Max and Ultra gearboxes
	public static final class GearBox {
		public static final double Max3 = 3.0;
		public static final double Max4 = 4.0;
		public static final double Max5 = 5.0;
		public static final double Max9 = 9.0;

		public static final double Ultra3 = 2.89;
		public static final double Ultra4 = 3.61;
		public static final double Ultra5 = 5.23;
	}

	public static final class Shooter {
		public static final double kShooterTollerance = 0.5; // degrees
		public static final double kTiltTollerance = 0.5; // degrees

		public static final double kLeftZeroOffset = 0.6643792;
		public static final boolean kLeftZeroCentered = true;
		public static final boolean kLeftMotorInverted = true;
		public static final boolean kLeftEncoderInverted = true;

		public static final double kRightZeroOffset = 0.4019657;
		public static final boolean kRightZeroCentered = true;
		public static final boolean kRightMotorInverted = false;
		public static final boolean kRightEncoderInverted = false;

		public static final double ktiltZeroOffset = 0.6643792;
		public static final boolean ktiltZeroCentered = true;
		public static final boolean ktiltMotorInverted = true;
		public static final boolean ktiltEncoderInverted = true;

		public static final boolean kLeftEncodeWrapping = false;
		public static final boolean kRightEncodeWrapping = false;
		public static final boolean ktiltEncodeWrapping = false;

		public static final double kTiltPositionFactor = 360; // 1.0 / (GearBox.Max9 * GearBox.Max5 * GearBox.Max5) *
																// 360.0; // degrees
		public static final double kTiltVelocityFactor = kTiltPositionFactor / 60.0; // degrees per second

		// Unsure if this needs to be uncommented out
		// public static final double kTiltPosP = 0.002;
		// public static final double kTiltPosI = 0.000001;
		// public static final double kTiltPosD = 0.0;

		public static final double kPosP = 0.01; // maxmotion 0.025;
		public static final double kPosI = 0.0; // maxmotion 0.0
		public static final double kPosD = 0.0; // maxmotion 0.0
		public static final double kPosMinOutput = -0.5; // maxmotion -1.0
		public static final double kPosMaxOutput = 0.5; // maxmotion 1.0

		public static final double kPosMaxVel = 100000.0; // 5000.0
		public static final double kPosMaxAccel = 40000.0; // 5000.0
		public static final double kPosAllowedErr = 0.1;

		public static final IdleMode kLeftIdleMode = IdleMode.kBrake;
		public static final IdleMode kRightIdleMode = IdleMode.kBrake;
		public static final IdleMode ktiltIdleMode = IdleMode.kBrake;

		public static final int kLeftCurrentLimit = 50; // amps
		public static final int kRightCurrentLimit = 50; // amps
		public static final int ktiltCurrentLimit = 50; // amps
	}

	public static final class Climber {
		public static final double kServoAmpLimit = 1.25;

		// Motor Inversion
		public static final boolean kClimberInverted = false;

		// Idle Mode
		public static final IdleMode kClimberIdleMode = IdleMode.kBrake;

		// Current Limit
		public static final int kClimberCurrentLimit = 50; // amps

		// Abs Encoder Configs
		public static final double kZeroOffset = 0.5; // WILL NEED ADJUSTMENT
		public static final boolean kZeroCentered = true;
		public static final boolean kEncoderInverted = true;
		public static final double kTiltPositionFactor = 360; // 1.0 / (GearBox.Max9 * GearBox.Max5 * GearBox.Max5) *
																// 360.0 degrees
		public static final double kTiltVelocityFactor = kTiltPositionFactor / 60.0; // degrees per second

		// Closed loop configs
		public static final double kPosP = 0.0; // maxmotion 0.0
		public static final double kPosI = 0.0; // maxmotion 0.0
		public static final double kPosD = 0.0; // maxmotion 0.0

		public static final double kPosMinOutput = -0.5; // max motion -1.0
		public static final double kPosMaxOutput = 0.5; // max motion 1.0

		public static final double kClimberTollerance = 0.5; // degrees
		public static final double kHookTollerance = 0.5; // degrees
	}

	public static final class Vision {
		public static final double kXP = 0.6;
		public static final double kXI = 0.0;
		public static final double kXD = 0.0;
		public static final double kXTollerance = 0.1;

		public static final double kYP = 0.6;
		public static final double kYI = 0.0;
		public static final double kYD = 0.0;
		public static final double kYTollerance = 0.1;

		public static final double kRP = 0.03;
		public static final double kRI = 0.0;
		public static final double kRD = 0.0;
		public static final double kRTollerance = 0.1;
		public static final double kRMin = 0.0;
		public static final double kRMax = 360.0;
	}

	public static final class Intake {
		public static final double kIntakeTollerance = 0.5; // degrees
		public static final double kTiltTollerance = 0.5; // degrees

		public static final double kIntakeZeroOffset = 0.6643792;
		public static final boolean kIntakeZeroCentered = true;
		public static final boolean kIntakeMotorInverted = true;
		public static final boolean kIntakeEncoderInverted = true;
		public static final double kIntakeVelFF = 0.0;

		public static final double kTiltZeroOffset = 0.4019657;
		public static final boolean kTiltZeroCentered = true;
		public static final boolean kTiltMotorInverted = false;
		public static final boolean kTiltEncoderInverted = false;

		public static final boolean kIntakeEncodeWrapping = false;
		public static final boolean kTiltEncodeWrapping = false;

		public static final double kIntakePositionFactor = 360; // 1.0 / (GearBox.Max9 * GearBox.Max5 * GearBox.Max5) *
																// 360.0; // degrees
		public static final double kIntakeVelocityFactor = kIntakePositionFactor / 60.0; // degrees per second

		public static final double kTiltPositionFactor = 360; // 1.0 / (GearBox.Max9 * GearBox.Max5 * GearBox.Max5) *
																// 360.0; // degrees
		public static final double kTiltVelocityFactor = kTiltPositionFactor / 60.0; // degrees per second

		public static final double kIntakeP = 0.01; // maxmotion 0.025;
		public static final double kIntakeI = 0.0; // maxmotion 0.0
		public static final double kIntakeD = 0.0; // maxmotion 0.0
		public static final double kIntakeMinOutput = -0.5; // maxmotion -1.0
		public static final double kIntakeMaxOutput = 0.5; // maxmotion 1.0

		public static final double kIntakeMaxVel = 100000.0; // 5000.0
		public static final double kIntakeMaxAccel = 40000.0; // 5000.0
		public static final double kIntakeAllowedErr = 0.1;

		public static final double kTiltP = 0.01; // maxmotion 0.025;
		public static final double kTiltI = 0.0; // maxmotion 0.0
		public static final double kTiltD = 0.0; // maxmotion 0.0
		public static final double kTiltMinOutput = -0.5; // maxmotion -1.0
		public static final double kTiltMaxOutput = 0.5; // maxmotion 1.0

		public static final double kTiltMaxVel = 100000.0; // 5000.0
		public static final double kTiltMaxAccel = 40000.0; // 5000.0
		public static final double kTiltAllowedErr = 0.1;

		public static final IdleMode kIntakeIdleMode = IdleMode.kBrake;
		public static final IdleMode kTiltIdleMode = IdleMode.kBrake;

		public static final int kIntakeCurrentLimit = 50; // amps
		public static final int kTiltCurrentLimit = 50; // amps
	}

	public static final class Feeder {
		public static final double kTollerance = 0.5; // RPMs

		public static final double kFeederZeroOffset = 0.6643792;
		public static final boolean kFeederZeroCentered = true;
		public static final boolean kFeederMotorInverted = true;
		public static final boolean kFeederEncoderInverted = true;
		public static final double kFeederVelFF = 0.0;

		public static final boolean kFeederEncodeWrapping = false;
		public static final IdleMode kFeederIdleMode = IdleMode.kBrake;

		public static final double kFeederPositionFactor = 360; // 1.0 / (GearBox.Max9 * GearBox.Max5 * GearBox.Max5) *
																// 360.0; // degrees
		public static final double kFeederVelocityFactor = kFeederPositionFactor / 60.0; // degrees per second

		public static final double kFeederP = 0.01; // maxmotion 0.025;
		public static final double kFeederI = 0.0; // maxmotion 0.0
		public static final double kFeederD = 0.0; // maxmotion 0.0
		public static final double kFeederMinOutput = -0.5; // maxmotion -1.0
		public static final double kFeederMaxOutput = 0.5; // maxmotion 1.0

		public static final double kFeederMaxVel = 100000.0; // 5000.0
		public static final double kFeederMaxAccel = 40000.0; // 5000.0
		public static final double kFeederAllowedErr = 0.1;

		public static final int kFeederCurrentLimit = 50; // amps
	}
}
