// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.util.Map;

import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.MAXMotionConfig.MAXMotionPositionMode;
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

	private final SparkMaxConfig leftConfig = new SparkMaxConfig();
	private final SparkMaxConfig rightConfig = new SparkMaxConfig();
	private final SparkMaxConfig tiltConfig = new SparkMaxConfig();

	private SparkClosedLoopController leftController = leftShooter.getClosedLoopController();
	// private SparkClosedLoopController rightController =
	// rightShooter.getClosedLoopController();
	private SparkClosedLoopController tiltController = tilt.getClosedLoopController();

	private RelativeEncoder leftEncoder = leftShooter.getEncoder();
	// private AbsoluteEncoder rightEncoder = rightShooter.getAbsoluteEncoder();
	private AbsoluteEncoder tiltEncoder = tilt.getAbsoluteEncoder();

	private CommandSwerveDrivetrain drivetrain = null;

	private Library lib = new Library();

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
				return (pct / 100.0) * ShooterConstants.kShooterMotorFreeSpeedRpm;
			} else {
				return pct;
			}
		}
	}

	// The Tilt SP is in degrees
	public enum TiltSP {
		LOW(10.5),
		MED((10.5 + 50.0) / 2.0),
		HI(50.0);

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

	private TiltSP tiltSP = TiltSP.LOW;
	private double tiltSPDbl = 0;
	private boolean tiltSpIsCustom = false;
	private double tiltSetpointDeg = 0.0;

	// ==============================================================
	// Initialize Dashboard entries
	// ==============================================================
	// private final ShuffleboardTab cmdTab = Shuffleboard.getTab("Commands");
	// private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");
	private final ShuffleboardTab shooterTab = Shuffleboard.getTab("Shooter");
	private final ShuffleboardTab ShooterCommands = Shuffleboard.getTab("Shooter Commands");

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

		this.drivetrain = drivetrain;

		// Configure Left Shooter motor
		leftConfig
				.inverted(ShooterConstants.kLeftMotorInverted)
				.idleMode(ShooterConstants.kLeftIdleMode)
				.smartCurrentLimit(ShooterConstants.kLeftCurrentLimit);
		leftConfig.encoder
				.positionConversionFactor(ShooterConstants.kShooterPositionFactor)
				.velocityConversionFactor(ShooterConstants.kShooterVelocityFactor);
		leftConfig.closedLoop
				.feedbackSensor(FeedbackSensor.kPrimaryEncoder)
				.p(ShooterConstants.kP)
				.i(ShooterConstants.kI)
				.d(ShooterConstants.kD)
				.outputRange(ShooterConstants.kPosMinOutput, ShooterConstants.kPosMaxOutput)
				.positionWrappingEnabled(ShooterConstants.kLeftEncodeWrapping);
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
				.positionWrappingEnabled(ShooterConstants.kLeftEncodeWrapping);
		tiltConfig.closedLoop.maxMotion
			    .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
			    .cruiseVelocity(ShooterConstants.kPosMaxVel)
			    .maxAcceleration(ShooterConstants.kPosMaxAccel)
			    .allowedProfileError(ShooterConstants.kPosAllowedErr);
		
		tilt.configure(tiltConfig,
				com.revrobotics.ResetMode.kResetSafeParameters,
				com.revrobotics.PersistMode.kPersistParameters);

		// Add commands to Dashboard
		ShooterCommands.add("Shoot Off", this.setShooter(ShooterSP.OFF))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		ShooterCommands.add("Shoot Hi", this.setShooter(ShooterSP.HI))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		ShooterCommands.add("Shoot Med", this.setShooter(ShooterSP.MED))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		ShooterCommands.add("Shoot Low", this.setShooter(ShooterSP.LOW))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		ShooterCommands.add("Tilt Hi", this.setTilt(TiltSP.HI))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		ShooterCommands.add("Tilt Med", this.setTilt(TiltSP.MED))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		ShooterCommands.add("Tilt Low", this.setTilt(TiltSP.LOW))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));

		// Initialize intake start positions
		setShooterVel(ShooterSP.OFF);
		setTiltPos(TiltSP.LOW);

		System.out.println("----- Ending Shooter Constructor -----");
	}

	// ==============================================================
	// Define subsystem commands
	// ==============================================================
	public Command setShooter(ShooterSP sp) {
		return runOnce(() -> this.setShooterVel(sp));
	}

	public Command setShooter(double sp) {
		return runOnce(() -> this.setShooterVel(sp));
	}

	public Command setTilt(TiltSP sp) {
		return runOnce(() -> this.setTiltPos(sp));
	}

	public Command setTilt(double sp) {
		return runOnce(() -> this.setTiltPos(sp));
	}

	public Command autoShoot(double tiltDeg, double shooterRpm) {
		return new ParallelCommandGroup(
				setTilt(tiltDeg),
				setShooter(shooterRpm));
	}

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
		sbShooterSPPct.setDouble(lib.SBFormat(getShooterSP(false)));
		sbShooterSPRPM.setDouble(lib.SBFormat(getShooterSP(true)));
		sbShooterVelPct.setDouble(lib.SBFormat(getShooterVel(false)));
		sbShooterVelRPM.setDouble(lib.SBFormat(getShooterVel(true)));

		sbTiltOnTgt.setBoolean(onTiltTarget());
		sbTiltSP.setString(getTiltSPName());
		// sbTiltSPPos.setDouble(lib.SBFormat(getTiltSPDbl()));
		double tiltTarget = tiltSpIsCustom ? tiltSPDbl : tiltSP.getPos();
		sbTiltSPPos.setDouble(lib.SBFormat(tiltTarget));
		sbTiltPos.setDouble(lib.SBFormat(getTiltPos()));

		// Gives you live visibility of what the solver wants to do,
		// without actually commanding anything unless you call autoShoot().
		var auto = ShooterBallistics.solveStationary(drivetrain.getDistToHub(), 0.5);
		sbAutoFeasible.setBoolean(auto.feasible());
		sbAutoAngleDeg.setDouble(lib.SBFormat(auto.feasible() ? auto.angleDeg() : ShooterBallistics.kMinAngleDeg));
		sbAutoRpm.setDouble(lib.SBFormat(auto.feasible() ? auto.wheelRpm() : 0.0));
	}

	@Override
	public void simulationPeriodic() {
		// This method will be called once per scheduler run during simulation
	}

	// ==============================================================
	// Define subsystem methods
	// ==============================================================

	public double getAutoShoot() {
		double distToHubM = drivetrain.getDistToHub(); // already returns meters
		var sp = ShooterBallistics.solveStationary(distToHubM, 0.5);

		if (!sp.feasible())
			return 0.0; // or hold last good value if you prefer

		// setShooterVel(double sp) expects RPM
		return sp.wheelRpm();
	}

	// What this does
	// * If solver fails, go to min angle
	// * If solver somehow returns something slightly outside bounds, clamp
	// * Does not change any other behavior
	public double getAutoTilt() {
		double distToHubM = drivetrain.getDistToHub();
		var sp = ShooterBallistics.solveStationary(distToHubM, 0.5);

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
	 * If a custom RPM is active, we return "CUSTOM" since there is no enum value.
	 */
	public String getShooterSPName() {
		return shooterSpIsCustom ? "Velocity" : shooterSP.name();
	}

	public void setShooterSP(ShooterSP sp) {
		shooterSpIsCustom = false;
		shooterSP = sp;
	}

	public void setShooterSPDbl(double sp) {
		shooterSpIsCustom = true;
		shooterSPDbl = sp;
	}

	public double getShooterSPDbl() {
		return shooterSPDbl;
	}

	public ShooterSP getShooterSP() {
		return shooterSP;
	}

	// public double getShooterSP(boolean rpm) {
	// 	return shooterSP.getVel(rpm);
	// }
	public double getShooterSP(boolean rpm) {
		if (shooterSpIsCustom) {
			if (rpm) return shooterSPDbl;
			return shooterSPDbl / ShooterConstants.kShooterMotorFreeSpeedRpm * 100.0;
		}
		return shooterSP.getVel(rpm);
	}

	public void setShooterVel(ShooterSP sp) {
		setShooterSP(sp);
		leftController.setSetpoint(sp.getVel(false) / 100.0 * 12.0, SparkBase.ControlType.kVoltage);
		// true), SparkBase.ControlType.kMAXMotionVelocityControl);
	}

	public void setShooterVel(double sp) {
		setShooterSPDbl(sp);
		leftController.setSetpoint(sp / ShooterConstants.kShooterMotorFreeSpeedRpm * 12.0, SparkBase.ControlType.kVoltage);
		// SparkBase.ControlType.kMAXMotionVelocityControl);
	}

	public double getShooterVel(boolean rpm) {
		if (rpm) {
			return leftEncoder.getVelocity();
		} else {
			return leftEncoder.getVelocity() / ShooterConstants.kShooterMotorFreeSpeedRpm * 100.0;
		}
	}

	public String getTiltSPName() {
		return tiltSpIsCustom ? "Degrees" : tiltSP.name();
	}

	public void setTiltSP(TiltSP sp) {
		tiltSpIsCustom = false;
		tiltSP = sp;
	}

	public void setTiltSPDbl(double sp) {
		tiltSpIsCustom = true;
		tiltSPDbl = sp;
	}

	public double getTiltSPDbl() {
		return tiltSPDbl;
	}

	public double getTiltSPPos() {
		return tiltSP.getPos();
	}

	public TiltSP getTiltSP() {
		return tiltSP;
	}

	public void setTiltPos(TiltSP sp) {
		setTiltSP(sp);
		System.out.println("+++++++++++++++++  setTilt: " + sp.getPos());
		tiltController.setSetpoint(sp.getPos(), SparkBase.ControlType.kMAXMotionPositionControl);
	}

	public void setTiltPos(double sp) {
		sp = lib.clamp(sp, TiltSP.LOW.getPos(), TiltSP.HI.getPos());
		setTiltSPDbl(sp);
		tiltController.setSetpoint(sp, SparkBase.ControlType.kMAXMotionPositionControl);
	}

	public double getTiltPos() {
		return tiltEncoder.getPosition();
	}

	public boolean onTiltTarget() {
		double target = tiltSpIsCustom ? tiltSPDbl : tiltSP.getPos();
		return Math.abs(getTiltPos() - target) < ShooterConstants.kTiltTollerance;
	}

	public boolean onShooterTarget() {
		return Math.abs(getShooterVel(true) - getShooterSP(true)) < ShooterConstants.kShooterTollerance;
	}
}
