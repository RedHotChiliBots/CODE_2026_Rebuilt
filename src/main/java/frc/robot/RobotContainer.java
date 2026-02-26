// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.*;

import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;

import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.Climber;
import frc.robot.subsystems.CommandSwerveDrivetrain;
import frc.robot.subsystems.Feeder;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Vision.VisionConstants;
import frc.robot.subsystems.Vision.VisionIOPhotonVision;
import frc.robot.subsystems.Vision.Vision;
import frc.robot.commands.Autos;

public class RobotContainer {
	private double MaxSpeed = 1.0 * TunerConstants.kSpeedAt12Volts.in(MetersPerSecond); // kSpeedAt12Volts desired
																						// top
																						// speed
	private double MaxAngularRate = RotationsPerSecond.of(0.75).in(RadiansPerSecond); // 3/4 of a rotation per
																						// second
																						// max angular velocity

	/* Setting up bindings for necessary control of the swerve drive platform */
	private final SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
			.withDeadband(MaxSpeed * 0.1).withRotationalDeadband(MaxAngularRate * 0.1) // Add a 10% deadband
			.withDriveRequestType(DriveRequestType.OpenLoopVoltage); // Use open-loop control for drive
																		// motors
	// Create the field-centric facing angle request
	private final SwerveRequest.FieldCentricFacingAngle driveAngle = new SwerveRequest.FieldCentricFacingAngle()
			.withDeadband(MaxSpeed * 0.1) // .withRotationalDeadband(MaxAngularRate * 0.1) // Add a 10% deadband
			.withDriveRequestType(DriveRequestType.OpenLoopVoltage) // Use open-loop control for drive motors
			.withHeadingPID(100.0, 0.0, 0.5);
	// .Position); // Use position control for steer motors

	private final SwerveRequest.SwerveDriveBrake brake = new SwerveRequest.SwerveDriveBrake();
	private final SwerveRequest.PointWheelsAt point = new SwerveRequest.PointWheelsAt();

	private final Telemetry logger = new Telemetry(MaxSpeed);

	private final CommandXboxController driverController = new CommandXboxController(
			Constants.OIConstants.kDriverControllerPort);
	private final CommandXboxController operatorController = new CommandXboxController(
			Constants.OIConstants.kOperatorControllerPort);

	public final CommandSwerveDrivetrain drivetrain = TunerConstants.createDrivetrain();
	private Intake intake = null;
	private Feeder feeder = null;
	private Shooter shooter = null;
	private Climber climber = null;
	private Vision vision = null;
	private Autos auton = null;

	public RobotContainer() {
		intake = new Intake();
		feeder = new Feeder();
		shooter = new Shooter(drivetrain);
		climber = new Climber();
		vision = new Vision(drivetrain::addVisionMeasurement,
				new VisionIOPhotonVision(VisionConstants.camera0Name, VisionConstants.robotToCamera0),
				new VisionIOPhotonVision(VisionConstants.camera1Name, VisionConstants.robotToCamera1),
				new VisionIOPhotonVision(VisionConstants.camera2Name, VisionConstants.robotToCamera2),
				new VisionIOPhotonVision(VisionConstants.camera3Name, VisionConstants.robotToCamera3));
		auton = new Autos(this, drivetrain, intake, feeder, shooter, climber);

		configureBindings();
	}

	private void configureBindings() {
		// Note that X is defined as forward according to WPILib convention,
		// and Y is defined as to the left according to WPILib convention.
		drivetrain.setDefaultCommand(
				// Drivetrain will execute this command periodically
				drivetrain.applyRequest(() -> drive
						// Drive forward with negative Y (forward)
						.withVelocityX(-driverController.getLeftY() * MaxSpeed)
						// Drive left with negative X (left)
						.withVelocityY(-driverController.getLeftX() * MaxSpeed)
						// Drive counterclockwise with negative X (left)
						.withRotationalRate(-driverController.getRightX() * MaxAngularRate)));

		// Track Hub when A button is held
		driverController.a().whileTrue(
			new ParallelCommandGroup(
				shooter.setShooter(shooter.getAutoShoot()),
				shooter.setTilt(shooter.getAutoTilt()),
				drivetrain.applyRequest(() -> driveAngle
						// Drive forward with negative Y (forward)
						.withVelocityX(-driverController.getLeftY() * MaxSpeed)
						// Drive left with negative X (left)
						.withVelocityY(-driverController.getLeftX() * MaxSpeed)
						// Drive pointing to hub
						.withTargetDirection(drivetrain.bearingToHub.minus(new Rotation2d(Math.PI))))));

		// Idle while the robot is disabled. This ensures the configured
		// neutral mode is applied to the drive motors while disabled.
		final var idle = new SwerveRequest.Idle();
		RobotModeTriggers.disabled().whileTrue(
				drivetrain.applyRequest(() -> idle).ignoringDisable(true));

		// joystick.a().whileTrue(drivetrain.applyRequest(() -> brake));
		driverController.b().whileTrue(drivetrain.applyRequest(
				() -> point.withModuleDirection(
						new Rotation2d(-driverController.getLeftY(), -driverController.getLeftX()))));

		// Run SysId routines when holding back/start and X/Y.
		// Note that each routine should be run exactly once in a single log.
		driverController.back().and(driverController.y()).whileTrue(drivetrain.sysIdDynamic(Direction.kForward));
		driverController.back().and(driverController.x()).whileTrue(drivetrain.sysIdDynamic(Direction.kReverse));
		driverController.start().and(driverController.y()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kForward));
		driverController.start().and(driverController.x()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kReverse));

		// Reset the field-centric heading on left bumper press.
		driverController.leftBumper().onTrue(drivetrain.runOnce(drivetrain::seedFieldCentric));

		drivetrain.registerTelemetry(logger::telemeterize);
	}

	public Command getAutonomousCommand() {
		// Simple drive forward auton
		final var idle = new SwerveRequest.Idle();
		return Commands.sequence(
				// Reset our field centric heading to match the robot
				// facing away from our alliance station wall (0 deg).
				drivetrain.runOnce(() -> drivetrain.seedFieldCentric(Rotation2d.kZero)),
				// Then slowly drive forward (away from us) for 5 seconds.
				drivetrain.applyRequest(() -> drive.withVelocityX(0.5)
						.withVelocityY(0)
						.withRotationalRate(0))
						.withTimeout(5.0),
				// Finally idle for the rest of auton
				drivetrain.applyRequest(() -> idle));
	}
}
