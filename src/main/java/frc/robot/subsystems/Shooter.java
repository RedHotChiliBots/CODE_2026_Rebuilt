// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.util.Map;
import java.util.Objects;

import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.MAXMotionConfig.MAXMotionPositionMode;
import com.revrobotics.spark.config.SparkFlexConfig;
import com.revrobotics.spark.config.AbsoluteEncoderConfig;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.constants.ShooterConstants;
import frc.robot.utils.Library;
import frc.robot.utils.ShooterBallistics;
import frc.robot.utils.SparkMaxSimulation;
import edu.wpi.first.math.system.plant.DCMotor;

public class Shooter extends SubsystemBase {
	// ==============================================================
	// Define Shooter & Tilt Motors
	// ==============================================================
	private final SparkFlex leftShooter = new SparkFlex(
			ShooterConstants.kLeftShooterCanId, MotorType.kBrushless);
	private final SparkFlex rightShooter = new SparkFlex(
			ShooterConstants.kRightShooterCanId, MotorType.kBrushless);
	private final SparkMax tilt = new SparkMax(
			ShooterConstants.kTiltMotorCanId, MotorType.kBrushless);

	private final SparkFlexConfig leftConfig = new SparkFlexConfig();
	private final SparkFlexConfig rightConfig = new SparkFlexConfig();
	private final SparkMaxConfig tiltConfig = new SparkMaxConfig();

	private SparkClosedLoopController leftController = leftShooter.getClosedLoopController();
	private SparkClosedLoopController tiltController = tilt.getClosedLoopController();

	private RelativeEncoder leftEncoder = leftShooter.getEncoder();
	private AbsoluteEncoder tiltEncoder = tilt.getAbsoluteEncoder();

	private CommandSwerveDrivetrain drivetrain = null;

	// Simulation objects
	private SparkMaxSimulation leftShooterSim;
	private SparkMaxSimulation tiltSim;

	// ==============================================================
	// Define motor vel and pos enums
	// ==============================================================
	// The Shooter SP is stored as a percentage of RPMs
	public enum ShooterSP {
		OFF(0.0),
		LOW(50.0),
		MED(75.0),
		HI(100.0);

		private double pct;

		ShooterSP(double pct) {
			this.pct = pct;
		}

		public double getVel(boolean rpm) {
			if (rpm) {
				return Library.pctToRpm(pct, ShooterConstants.kShooterMotorFreeSpeedRpm);
			} else {
				return pct;
			}
		}
	}

	// The Tilt SP is in degrees
	public enum TiltSP {
		LOW(-10.0),
		MED(0.0),
		HI(10.0);

		private double pos;

		TiltSP(double pos) {
			this.pos = pos;
		}

		public double getPos() {
			return pos;
		}
	}

	// ==============================================================
	// Initialize motor setpoints
	// ==============================================================
	private ShooterSP shooterSP = ShooterSP.OFF;
	private double shooterSPDbl = 0.0;
	private boolean shooterSpIsCustom = false;

	private TiltSP tiltSP = TiltSP.MED;
	private double tiltSPDbl = 0.0;
	private boolean tiltSpIsCustom = false;

	// ==============================================================
	// Initialize Dashboard entries
	// ==============================================================
	// private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");
	private final ShuffleboardTab shooterTab = Shuffleboard.getTab("Shooter Methods");
	private final ShuffleboardTab cmdTab = Shuffleboard.getTab("Shooter Commands");

	private final GenericEntry sbShooterOnTgt = shooterTab.addPersistent("Shooter OnTgt", false)
			.withWidget("Boolean Box").withPosition(0, 1).withSize(2, 1).getEntry();
	private final GenericEntry sbShooterSP = shooterTab.addPersistent("Shooter SP", "")
			.withWidget("Text View").withPosition(2, 0).withSize(2, 1).getEntry();
	private final GenericEntry sbShooterSPPct = shooterTab.addPersistent("Shooter SP Pct", 0)
			.withWidget("Text View").withPosition(2, 1).withSize(2, 1).getEntry();
	private final GenericEntry sbShooterSPRPM = shooterTab.addPersistent("Shooter SP RPM", 0)
			.withWidget("Text View").withPosition(2, 2).withSize(2, 1).getEntry();

	private final GenericEntry sbShooterVelPct = shooterTab.addPersistent("Shooter Vel Pct", 0)
			.withWidget("Text View").withPosition(4, 0).withSize(2, 1).getEntry();
	private final GenericEntry sbShooterVelRPM = shooterTab.addPersistent("Shooter Vel RPM", 0)
			.withWidget("Text View").withPosition(4, 1).withSize(2, 1).getEntry();

	private final GenericEntry sbTiltOnTgt = shooterTab.addPersistent("Tilt OnTgt", false)
			.withWidget("Boolean Box").withPosition(0, 1).withSize(2, 1).getEntry();
	private final GenericEntry sbTiltSP = shooterTab.addPersistent("Tilt SP", "")
			.withWidget("Text View").withPosition(2, 0).withSize(2, 1).getEntry();
	private final GenericEntry sbTiltSPPos = shooterTab.addPersistent("Tilt SP Pos", 0)
			.withWidget("Text View").withPosition(2, 1).withSize(2, 1).getEntry();

	private final GenericEntry sbTiltPos = shooterTab.addPersistent("Tilt Pos", 0)
			.withWidget("Text View").withPosition(4, 0).withSize(2, 1).getEntry();

	// Shuffleboard debug for auto-shot (feasible / angle / rpm)
	private final GenericEntry sbAutoFeasible = shooterTab.addPersistent("Auto Feasible", false)
			.withWidget("Boolean Box").withPosition(0, 2).withSize(2, 1).getEntry();
	private final GenericEntry sbAutoAngleDeg = shooterTab.addPersistent("Auto Angle Deg", 0.0)
			.withWidget("Text View").withPosition(2, 3).withSize(2, 1).getEntry();
	private final GenericEntry sbAutoRpm = shooterTab.addPersistent("Auto RPM", 0.0)
			.withWidget("Text View").withPosition(2, 4).withSize(2, 1).getEntry();

	// ==============================================================
	// Constructor
	// ==============================================================
	public Shooter(CommandSwerveDrivetrain drivetrain) {
		System.out.println("+++++ Starting Shooter Constructor +++++");

    	this.drivetrain = Objects.requireNonNull(drivetrain, "drivetrain cannot be null");

		// Configure Left Shooter motor
		leftConfig
				.inverted(ShooterConstants.kLeftMotorInverted)
				.idleMode(ShooterConstants.kLeftIdleMode)
				.smartCurrentLimit(ShooterConstants.kLeftCurrentLimit);
		leftConfig.encoder
				.velocityConversionFactor(ShooterConstants.kShooterVelocityFactor);
		leftConfig.closedLoop
				.feedbackSensor(FeedbackSensor.kPrimaryEncoder)
				.p(ShooterConstants.kP)
				.i(ShooterConstants.kI)
				.d(ShooterConstants.kD)
				.outputRange(ShooterConstants.kMinOutput, ShooterConstants.kMaxOutput);
		leftConfig.closedLoop.feedForward
				.kA(ShooterConstants.kVelFF);
		leftConfig.closedLoop.maxMotion
				.positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
				.cruiseVelocity(ShooterConstants.kMaxVel)
				.maxAcceleration(ShooterConstants.kMaxAccel)
				.allowedProfileError(ShooterConstants.kAllowedErr);

		leftShooter.configure(leftConfig,
				com.revrobotics.ResetMode.kResetSafeParameters,
				com.revrobotics.PersistMode.kPersistParameters);

		// Configure Right Shooter motor
		rightConfig
				.follow(leftShooter, true)
				.inverted(ShooterConstants.kRightMotorInverted);

		rightShooter.configure(rightConfig,
				com.revrobotics.ResetMode.kResetSafeParameters,
				com.revrobotics.PersistMode.kPersistParameters);

		// Configure Tilt motor
		tiltConfig
				.inverted(ShooterConstants.kTiltMotorInverted)
				.idleMode(ShooterConstants.kTiltIdleMode)
				.smartCurrentLimit(ShooterConstants.kTiltCurrentLimit);
		tiltConfig.absoluteEncoder
				.zeroOffset(ShooterConstants.kTiltZeroOffset)
				.zeroCentered(ShooterConstants.kTiltZeroCentered)
				.inverted(ShooterConstants.kTiltEncoderInverted)
				.positionConversionFactor(ShooterConstants.kTiltPositionFactor)
				.velocityConversionFactor(ShooterConstants.kTiltVelocityFactor)
				.apply(AbsoluteEncoderConfig.Presets.REV_ThroughBoreEncoderV2);
		tiltConfig.closedLoop
				.feedbackSensor(FeedbackSensor.kAbsoluteEncoder)
				.p(ShooterConstants.kPosP)
				.i(ShooterConstants.kPosI)
				.d(ShooterConstants.kPosD)
				.outputRange(ShooterConstants.kPosMinOutput, ShooterConstants.kPosMaxOutput)
				.positionWrappingEnabled(ShooterConstants.kTiltEncodeWrapping);
		tiltConfig.closedLoop.maxMotion
				.positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
				.cruiseVelocity(ShooterConstants.kPosMaxVel)
				.maxAcceleration(ShooterConstants.kPosMaxAccel)
				.allowedProfileError(ShooterConstants.kPosAllowedErr);

		tilt.configure(tiltConfig,
				com.revrobotics.ResetMode.kResetSafeParameters,
				com.revrobotics.PersistMode.kPersistParameters);

		// Add commands to Dashboard
		cmdTab.add("Shoot Off", this.setShooter(ShooterSP.OFF))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		cmdTab.add("Shoot Hi", this.setShooter(ShooterSP.HI))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		cmdTab.add("Shoot Med", this.setShooter(ShooterSP.MED))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		cmdTab.add("Shoot Low", this.setShooter(ShooterSP.LOW))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		cmdTab.add("Tilt Hi", this.setTilt(TiltSP.HI))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		cmdTab.add("Tilt Med", this.setTilt(TiltSP.MED))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		cmdTab.add("Tilt Low", this.setTilt(TiltSP.LOW))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));

		// Initialize intake start positions
		setShooterVel(ShooterSP.OFF);
		setTiltPos(TiltSP.MED);

		// Initialize simulation
		if (Constants.currentMode == Constants.Mode.SIM) {
			// Left shooter motor simulation (velocity control) - right follows
			leftShooterSim = SparkMaxSimulation.createVelocitySim(
					leftShooter,
					DCMotor.getNeoVortex(1),
					1.0, // Direct drive, no gearing
					0.01 // MOI in kg*m^2 for flywheel
			);

			// Tilt motor simulation (position control)
			tiltSim = SparkMaxSimulation.createPositionSim(
					tilt,
					DCMotor.getNEO(1),
					ShooterConstants.kTiltGearRatio,
					0.6, // arm length in meters
					TiltSP.LOW.getPos(), // min angle
					TiltSP.HI.getPos(), // max angle
					true, // simulate gravity
					TiltSP.LOW.getPos() // starting angle
			);
		}

		System.out.println("----- Ending Shooter Constructor -----");
	}

	// ==============================================================
	// Define subsystem commands
	// ==============================================================
	/**
	 * Creates a command to set the shooter velocity to a preset setpoint.
	 *
	 * @param sp The preset shooter velocity setpoint (OFF, LOW, MED, or HI)
	 * @return A command that sets the shooter velocity once
	 */
	public Command setShooter(ShooterSP sp) {
		return runOnce(() -> this.setShooterVel(sp));
	}

	/**
	 * Creates a command to set the shooter velocity to a custom RPM value.
	 *
	 * @param sp The desired shooter velocity in RPM
	 * @return A command that sets the shooter velocity once
	 */
	public Command setShooter(double sp) {
		return runOnce(() -> this.setShooterVel(sp));
	}

	/**
	 * Creates a command to set the tilt position to a preset setpoint.
	 *
	 * @param sp The preset tilt position setpoint (LOW, MED, or HI)
	 * @return A command that sets the tilt position once
	 */
	public Command setTilt(TiltSP sp) {
		return runOnce(() -> this.setTiltPos(sp));
	}

	/**
	 * Creates a command to set the tilt position to a custom angle.
	 *
	 * @param sp The desired tilt angle in degrees
	 * @return A command that sets the tilt position once
	 */
	public Command setTilt(double sp) {
		return runOnce(() -> this.setTiltPos(sp));
	}

	/**
	 * Creates a command to automatically shoot with specified tilt and shooter velocity.
	 * Runs tilt and shooter commands in parallel.
	 *
	 * @param tiltDeg The desired tilt angle in degrees
	 * @param shooterRpm The desired shooter velocity in RPM
	 * @return A parallel command group that sets both tilt and shooter
	 */
	public Command autoShoot(double tiltDeg, double shooterRpm) {
		return new ParallelCommandGroup(
				setTilt(tiltDeg),
				setShooter(shooterRpm));
	}

	/**
	 * Creates a command to automatically shoot using ballistics calculations.
	 * Calculates optimal tilt angle and shooter velocity based on distance to target.
	 * Runs tilt and shooter commands in parallel.
	 *
	 * @return A parallel command group that sets both tilt and shooter to calculated values
	 */
	public Command autoShoot() {
		return new ParallelCommandGroup(
				setTilt(getAutoTilt()),
				setShooter(getAutoShoot()));
	}

	// ==============================================================
	// Periodic methods
	// ==============================================================
	@Override
	public void periodic() {
		sbShooterOnTgt.setBoolean(onShooterTarget());
		sbShooterSP.setString(getShooterSPName());
		sbShooterSPPct.setDouble(Library.SBFormat(getShooterSP(false)));
		sbShooterSPRPM.setDouble(Library.SBFormat(getShooterSP(true)));
		sbShooterVelPct.setDouble(Library.SBFormat(getShooterVel(false)));
		sbShooterVelRPM.setDouble(Library.SBFormat(getShooterVel(true)));

		sbTiltOnTgt.setBoolean(onTiltTarget());
		sbTiltSP.setString(getTiltSPName());
		double tiltTarget = tiltSpIsCustom ? tiltSPDbl : tiltSP.getPos();
		sbTiltSPPos.setDouble(Library.SBFormat(tiltTarget));
		sbTiltPos.setDouble(Library.SBFormat(getTiltPos()));

		// Gives you live visibility of what the solver wants to do,
		// without actually commanding anything unless you call autoShoot().
		if (drivetrain != null) {
			var auto = ShooterBallistics.solveStationary(drivetrain.getDistToHub(), ShooterConstants.kBallisticsCoefficient);
			sbAutoFeasible.setBoolean(auto.feasible());
			sbAutoAngleDeg
					.setDouble(Library.SBFormat(auto.feasible() ? auto.angleDeg() : ShooterBallistics.kMinAngleDeg));
			sbAutoRpm.setDouble(Library.SBFormat(auto.feasible() ? auto.wheelRpm() : 0.0));
		}
	}

	@Override
	public void simulationPeriodic() {
		// Update motor simulations
		if (leftShooterSim != null) {
			leftShooterSim.update(getShooterSP(true), 0.02);
		}
		
		if (tiltSim != null) {
			double target = tiltSpIsCustom ? tiltSPDbl : tiltSP.getPos();
			tiltSim.update(target, 0.02);
		}
	}

	// ==============================================================
	// Define subsystem methods
	// ==============================================================

	/**
	 * Calculates the optimal shooter velocity for the current distance to target.
	 * Uses ballistics solver to determine required wheel RPM based on distance.
	 *
	 * @return The calculated shooter velocity in RPM, or 0.0 if calculation fails or drivetrain is null
	 */
	public double getAutoShoot() {
		if (drivetrain == null)
			return 0.0;
		double distToHubM = drivetrain.getDistToHub(); // already returns meters
		var sp = ShooterBallistics.solveStationary(distToHubM, ShooterConstants.kBallisticsCoefficient);

		if (!sp.feasible())
			return 0.0; // or hold last good value if you prefer

		// setShooterVel(double sp) expects RPM
		return sp.wheelRpm();
	}

	/**
	 * Calculates the optimal tilt angle for the current distance to target.
	 * Uses ballistics solver to determine required launch angle based on distance.
	 *
	 * Behavior:
	 * - If solver fails, returns minimum angle
	 * - If solver returns value outside bounds, clamps to mechanical limits
	 * - Ensures angle is always within safe operating range
	 *
	 * @return The calculated tilt angle in degrees, clamped to mechanical limits
	 */
	public double getAutoTilt() {
		if (drivetrain == null)
			return ShooterBallistics.kMinAngleDeg;
		double distToHubM = drivetrain.getDistToHub();
		var sp = ShooterBallistics.solveStationary(distToHubM, ShooterConstants.kBallisticsCoefficient);

		double angleDeg = sp.feasible()
				? sp.angleDeg()
				: ShooterBallistics.kMinAngleDeg;

		// Clamp to mechanical limits (defensive safety)
		angleDeg = Math.max(
				ShooterBallistics.kMinAngleDeg,
				Math.min(ShooterBallistics.kMaxAngleDeg, angleDeg));

		return angleDeg;
	}

	/**
	 * Returns the name of the active shooter setpoint for Shuffleboard display.
	 * 
	 * We support two modes:
	 * - Preset enum-based setpoints (LOW, MED, HI, etc.)
	 * - Custom numeric RPM setpoints (used by ballistics auto-aim)
	 *
	 * If a custom RPM is active, we return "Velocity" since there is no enum value.
	 */
	public String getShooterSPName() {
		return shooterSpIsCustom ? "Velocity" : shooterSP.name();
	}

	/**
	 * Sets the shooter setpoint to a preset enum value.
	 * Marks the setpoint as non-custom (enum-based).
	 *
	 * @param sp The preset shooter velocity setpoint
	 */
	public void setShooterSP(ShooterSP sp) {
		shooterSpIsCustom = false;
		shooterSP = sp;
	}

	/**
	 * Sets the shooter setpoint to a custom numeric RPM value.
	 * Marks the setpoint as custom (not enum-based).
	 *
	 * @param sp The custom shooter velocity in RPM
	 */
	public void setShooterSPDbl(double sp) {
		shooterSpIsCustom = true;
		shooterSPDbl = sp;
	}

	/**
	 * Gets the custom shooter setpoint value.
	 *
	 * @return The custom shooter velocity in RPM
	 */
	public double getShooterSPDbl() {
		return shooterSPDbl;
	}

	/**
	 * Gets the current shooter setpoint enum.
	 *
	 * @return The preset shooter velocity setpoint
	 */
	public ShooterSP getShooterSP() {
		return shooterSP;
	}

	/**
	 * Gets the current shooter setpoint value in the specified units.
	 * Handles both preset enum and custom numeric setpoints.
	 *
	 * @param rpm If true, returns RPM; if false, returns percentage of max speed
	 * @return The shooter setpoint in the requested units
	 */
	public double getShooterSP(boolean rpm) {
		if (shooterSpIsCustom) {
			return rpm ? shooterSPDbl : Library.rpmToPct(shooterSPDbl, ShooterConstants.kShooterMotorFreeSpeedRpm);

		}
		return shooterSP.getVel(rpm);
	}

	/**
	 * Sets the shooter velocity to a preset setpoint and commands the motor controller.
	 * Uses MAXMotion velocity control for smooth acceleration.
	 *
	 * @param sp The preset shooter velocity setpoint
	 */
	public void setShooterVel(ShooterSP sp) {
		setShooterSP(sp);
		leftController.setSetpoint(sp.getVel(true), SparkBase.ControlType.kMAXMotionVelocityControl);
	}

	/**
	 * Sets the shooter velocity to a custom RPM value and commands the motor controller.
	 * Uses MAXMotion velocity control for smooth acceleration.
	 *
	 * @param sp The desired shooter velocity in RPM
	 */
	public void setShooterVel(double sp) {
		setShooterSPDbl(sp);
		leftController.setSetpoint(sp, SparkBase.ControlType.kMAXMotionVelocityControl);
	}

	/**
	 * Gets the current shooter velocity from the encoder.
	 *
	 * @param rpm If true, returns RPM; if false, returns percentage of max speed
	 * @return The current shooter velocity in the requested units
	 */
	public double getShooterVel(boolean rpm) {
		return rpm ? leftEncoder.getVelocity() : Library.rpmToPct(leftEncoder.getVelocity(), ShooterConstants.kShooterMotorFreeSpeedRpm);
	}

	/**
	 * Gets the name of the current tilt setpoint for display purposes.
	 *
	 * @return "Degrees" if using custom angle, otherwise the enum name (LOW, MED, HI)
	 */
	public String getTiltSPName() {
		return tiltSpIsCustom ? "Degrees" : tiltSP.name();
	}

	/**
	 * Sets the tilt setpoint to a preset enum value.
	 * Marks the setpoint as non-custom (enum-based).
	 *
	 * @param sp The preset tilt position setpoint
	 */
	public void setTiltSP(TiltSP sp) {
		tiltSpIsCustom = false;
		tiltSP = sp;
	}

	/**
	 * Sets the tilt setpoint to a custom angle value.
	 * Marks the setpoint as custom (not enum-based).
	 *
	 * @param sp The custom tilt angle in degrees
	 */
	public void setTiltSPDbl(double sp) {
		tiltSpIsCustom = true;
		tiltSPDbl = sp;
	}

	/**
	 * Gets the custom tilt setpoint value.
	 *
	 * @return The custom tilt angle in degrees
	 */
	public double getTiltSPDbl() {
		return tiltSPDbl;
	}

	/**
	 * Gets the position value of the current preset tilt setpoint.
	 *
	 * @return The tilt angle in degrees from the enum
	 */
	public double getTiltSPPos() {
		return tiltSP.getPos();
	}

	/**
	 * Gets the current tilt setpoint enum.
	 *
	 * @return The preset tilt position setpoint
	 */
	public TiltSP getTiltSP() {
		return tiltSP;
	}

	/**
	 * Sets the tilt position to a preset setpoint and commands the motor controller.
	 * Validates the position is within safe mechanical limits before commanding.
	 * Uses MAXMotion position control for smooth motion.
	 *
	 * @param sp The preset tilt position setpoint
	 */
	public void setTiltPos(TiltSP sp) {
		setTiltSP(sp);
		// Validate enum value is within safe limits
		double pos = Library.clamp(sp.getPos(), TiltSP.LOW.getPos(), TiltSP.HI.getPos());
		tiltController.setSetpoint(pos, SparkBase.ControlType.kMAXMotionPositionControl);
	}

	/**
	 * Sets the tilt position to a custom angle and commands the motor controller.
	 * Clamps the angle to safe mechanical limits before commanding.
	 * Uses MAXMotion position control for smooth motion.
	 *
	 * @param sp The desired tilt angle in degrees
	 */
	public void setTiltPos(double sp) {
		sp = Library.clamp(sp, TiltSP.LOW.getPos(), TiltSP.HI.getPos());
		setTiltSPDbl(sp);
		tiltController.setSetpoint(sp, SparkBase.ControlType.kMAXMotionPositionControl);
	}

	/**
	 * Gets the current tilt position from the absolute encoder.
	 *
	 * @return The current tilt angle in degrees
	 */
	public double getTiltPos() {
		return tiltEncoder.getPosition();
	}

	/**
	 * Checks if the tilt mechanism is at the target position.
	 * Compares current position to setpoint within allowed error tolerance.
	 *
	 * @return True if within tolerance of target position, false otherwise
	 */
	public boolean onTiltTarget() {
		double target = tiltSpIsCustom ? tiltSPDbl : tiltSP.getPos();
		return Math.abs(getTiltPos() - target) < ShooterConstants.kPosAllowedErr;
	}

	/**
	 * Checks if the shooter is at the target velocity.
	 * Compares current velocity to setpoint within allowed error tolerance.
	 *
	 * @return True if within tolerance of target velocity, false otherwise
	 */
	public boolean onShooterTarget() {
		return Math.abs(getShooterVel(true) - getShooterSP(true)) < ShooterConstants.kAllowedErr;
	}
}
