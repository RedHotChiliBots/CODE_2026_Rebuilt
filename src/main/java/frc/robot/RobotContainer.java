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

import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.Climber;
import frc.robot.subsystems.CommandSwerveDrivetrain;
import frc.robot.subsystems.Feeder;
import frc.robot.subsystems.Feeder.FeederSP;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Intake.IntakeSP;
import frc.robot.subsystems.Intake.TiltSP;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Shooter.ShooterSP;
import frc.robot.subsystems.Vision.VisionConstants;
import frc.robot.subsystems.Vision.VisionIOPhotonVision;
import frc.robot.subsystems.Vision.Vision;
import frc.robot.commands.Autos;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Timer;

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
			.withHeadingPID(50.0, 0.0, 0.0);
	// .Position); // Use position control for steer motors

	private final SwerveRequest.SwerveDriveBrake brake = new SwerveRequest.SwerveDriveBrake();
	private final SwerveRequest.PointWheelsAt point = new SwerveRequest.PointWheelsAt();

	private final Telemetry logger = new Telemetry(MaxSpeed);

	private final CommandXboxController driverController = new CommandXboxController(
			Constants.OIConstants.kDriverControllerPort);
	private final CommandXboxController operatorController = new CommandXboxController(
			Constants.OIConstants.kOperatorControllerPort);

	// =============================================================
	// Operator controller haptic feedback (rumble)
	// =============================================================
	//
	// Goals:
	// 1) Shooter-ready confirmation without looking at Shuffleboard.
	// 2) Endgame warning at 5 seconds remaining (rule-based timing).
	//
	// Design principles:
	// - Simple, binary signals (no "always buzzing").
	// - Priority: endgame warning overrides shooter-ready.
	// - Use FMS/DriverStation match clock (DriverStation.getMatchTime()).
	// - Never rumble while disabled.
	//
	// NOTE: All distances are meters. "Range" will be tuned on-field.
	//
	private static final double kShootRangeM = 4.0; // TODO tune based on real scoring range
	private static final double kShootRangeDeadbandM = 0.25; // prevents on/off jitter at boundary

	private static final double kRumbleShootReady = 0.60; // steady rumble when ready to shoot (0..1)

	private static final double kEndgameWarnAtSec = 5.0; // start warning when <= 5s remaining in teleop
	private static final double kEndgamePulseOnSec = 0.20; // pulse pattern: on duration
	private static final double kEndgamePulseOffSec = 0.20; // pulse pattern: off duration
	private static final double kRumbleEndgame = 1.00; // endgame warning intensity (0..1)

	// Cache last rumble value to avoid spamming USB updates every loop.
	private double lastOperatorRumble = -1.0;

	// Timer used ONLY for pulse pattern timing (not match timing).
	private final Timer rumblePulseTimer = new Timer();
	private boolean endgamePulseActive = false;

	// =============================================================
	// Other variable declarations
	// =============================================================
	public final CommandSwerveDrivetrain drivetrain = TunerConstants.createDrivetrain();
	private Intake intake = null;
	private Feeder feeder = null;
	private Shooter shooter = null;
//	private Climber climber = null;
	private Vision vision = null;
	private Autos auton = null;

	public RobotContainer() {
		intake = new Intake();
		feeder = new Feeder();
		shooter = new Shooter(drivetrain, feeder);
//		climber = new Climber();
		vision = new Vision(drivetrain::addVisionMeasurement,
				new VisionIOPhotonVision(VisionConstants.camera0Name, VisionConstants.robotToCamera0),
				new VisionIOPhotonVision(VisionConstants.camera1Name, VisionConstants.robotToCamera1),
				new VisionIOPhotonVision(VisionConstants.camera2Name, VisionConstants.robotToCamera2),
				new VisionIOPhotonVision(VisionConstants.camera3Name, VisionConstants.robotToCamera3));
		auton = new Autos(this, drivetrain, intake, feeder, shooter);	//, climber);

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
		driverController.rightBumper().toggleOnTrue(
				new ParallelCommandGroup(
						// shooter.setShooter(shooter.getAutoShoot()).andThen(
						// shooter.setTilt(shooter.getAutoTilt())),
						drivetrain.applyRequest(() -> driveAngle
								// Drive forward with negative Y (forward)
								.withVelocityX(-driverController.getLeftY() * MaxSpeed)
								// Drive left with negative X (left)
								.withVelocityY(-driverController.getLeftX() * MaxSpeed)
								// Drive pointing to hub
								.withTargetDirection(drivetrain.bearingToHub.minus(new Rotation2d(Math.PI))))));

		operatorController.x().onTrue(intake.setTilt(TiltSP.DEPLOY));
		operatorController.b().onTrue(intake.setTilt(TiltSP.STOW));

		operatorController.y().onTrue(intake.setIntake(IntakeSP.HI));
		operatorController.a().onTrue(intake.setIntake(IntakeSP.OFF));

		operatorController.leftBumper().onTrue(shooter.setShooter(ShooterSP.MED));
		operatorController.x().onTrue(shooter.setShooter(ShooterSP.OFF));
		// operatorController.leftBumper().onFalse(shooter.setShooter(ShooterSP.OFF));

		operatorController.rightBumper().onTrue(feeder.setFeeder(FeederSP.HI));
		operatorController.b().onTrue(feeder.setFeeder(FeederSP.OFF));
		// operatorController.rightBumper().onFalse(feeder.setFeeder(FeederSP.OFF));

		// Idle while the robot is disabled. This ensures the configured
		// neutral mode is applied to the drive motors while disabled.
		final var idle = new SwerveRequest.Idle();
		RobotModeTriggers.disabled().whileTrue(
				drivetrain.applyRequest(() -> idle).ignoringDisable(true));

		// OPTIONAL SAFETY: hard-stop operator rumble when disabled.
		// (Not strictly required because updateOperatorRumble() also forces rumble
		// off.)
		RobotModeTriggers.disabled().onTrue(
				Commands.runOnce(() -> operatorController.getHID().setRumble(RumbleType.kBothRumble, 0.0)));

		// joystick.a().whileTrue(drivetrain.applyRequest(() -> brake));
		// driverController.b().whileTrue(drivetrain.applyRequest(
		// () -> point.withModuleDirection(
		// new Rotation2d(-driverController.getLeftY(),
		// -driverController.getLeftX()))));

		// Run SysId routines when holding back/start and X/Y.
		// Note that each routine should be run exactly once in a single log.
		// driverController.back().and(driverController.y()).whileTrue(drivetrain.sysIdDynamic(Direction.kForward));
		// driverController.back().and(driverController.x()).whileTrue(drivetrain.sysIdDynamic(Direction.kReverse));
		// driverController.start().and(driverController.y()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kForward));
		// driverController.start().and(driverController.x()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kReverse));

		// Reset the field-centric heading on left bumper press.
		// driverController.leftBumper().onTrue(drivetrain.runOnce(drivetrain::seedFieldCentric));

		drivetrain.registerTelemetry(logger::telemeterize);
	}

	/*
	 * Sets operator rumble intensity (0..1).
	 * Uses a small cache to reduce repeated setRumble calls.
	 */
	private void setOperatorRumble(double intensity) {
		intensity = Math.max(0.0, Math.min(1.0, intensity));

		// Reduce spam: only update if the value changed meaningfully.
		if (Math.abs(intensity - lastOperatorRumble) < 0.01)
			return;

		lastOperatorRumble = intensity;
		operatorController.getHID().setRumble(RumbleType.kBothRumble, intensity);
	}

	/*
	 * Returns true when robot is within the current “shooting range" distance to
	 * the hub.
	 * Includes a deadband to prevent rumble chatter when hovering near the
	 * boundary.
	 */
	private boolean inShootRange() {
		double d = drivetrain.getDistToHub(); // meters
		return d <= (kShootRangeM - kShootRangeDeadbandM);
	}

	/*
	 * Shooter-ready is a strict condition:
	 * - in range
	 * - shooter velocity on target
	 * - tilt position on target
	 * 
	 * This is intended to be a "green light" for the operator.
	 */
	private boolean shooterReady() {
		return inShootRange() && shooter.onShooterTarget() && shooter.onTiltTarget();
	}

	/*
	 * Endgame warning window based on the official match clock.
	 * 
	 * We DO NOT run our own match timer.
	 * DriverStation.getMatchTime() returns seconds remaining in the current period.
	 * 
	 * Returns true only during TELEOP and only when:
	 * - match time is known (>= 0)
	 * - time remaining is <= kEndgameWarnAtSec and > 0
	 */
	private boolean endgameWarningWindow() {
		if (!DriverStation.isTeleopEnabled())
			return false;

		double t = DriverStation.getMatchTime(); // seconds remaining, -1 if unknown
		if (t < 0.0)
			return false;

		return (t <= kEndgameWarnAtSec) && (t > 0.0);
	}

	/*
	 * Simple on/off rumble pulse pattern for endgame warning.
	 * Uses rumblePulseTimer ONLY for pulse timing.
	 */
	private double endgamePulseIntensity() {
		double period = kEndgamePulseOnSec + kEndgamePulseOffSec;
		double phase = rumblePulseTimer.get() % period;
		return (phase < kEndgamePulseOnSec) ? kRumbleEndgame : 0.0;
	}

	/**
	 * Central rumble update called continuously during robot operation.
	 * 
	 * Priority order:
	 * 1) Endgame warning pulses
	 * 2) Shooter-ready steady rumble
	 * 3) Otherwise: rumble off
	 * 
	 * Safety:
	 * - Never rumble while disabled.
	 */
	public void updateOperatorRumble() {
		// Absolute safety: never rumble while disabled.
		if (!DriverStation.isEnabled()) {
			setOperatorRumble(0.0);

			// Reset endgame pulse state so it starts clean on next enable.
			endgamePulseActive = false;
			rumblePulseTimer.stop();
			rumblePulseTimer.reset();
			return;
		}

		// Priority 1: Endgame warning pulses.
		if (endgameWarningWindow()) {
			if (!endgamePulseActive) {
				endgamePulseActive = true;
				rumblePulseTimer.reset();
				rumblePulseTimer.start();
			}

			setOperatorRumble(endgamePulseIntensity());
			return;
		}

		// If we leave the warning window, stop/reset the pulse timer.
		if (endgamePulseActive) {
			endgamePulseActive = false;
			rumblePulseTimer.stop();
			rumblePulseTimer.reset();
		}

		// Priority 2: Shooter-ready steady rumble.
		if (shooterReady()) {
			setOperatorRumble(kRumbleShootReady);
		} else {
			setOperatorRumble(0.0);
		}
	}

	public Command getAutonomousCommand() {
		// return new ChassisTimedDrive(chassis, 0.25, 1.0);
		return auton.getAutoChooser().getSelected();
	}
}
