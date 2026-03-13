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
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.AbsoluteEncoderConfig;
import com.revrobotics.spark.config.MAXMotionConfig.MAXMotionPositionMode;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.constants.IntakeConstants;
import frc.robot.utils.Library;

public class Intake extends SubsystemBase {

  // ==============================================================
  // Define Intake & Tilt Motors
  // ==============================================================
  private final SparkMax intake = new SparkMax(
      IntakeConstants.kIntakeMotorCanId, MotorType.kBrushless);
  private final SparkMax tilt = new SparkMax(
      IntakeConstants.kTiltMotorCanId, MotorType.kBrushless);

  private final SparkMaxConfig intakeConfig = new SparkMaxConfig();
  private final SparkMaxConfig tiltConfig = new SparkMaxConfig();

  private SparkClosedLoopController intakeController = intake.getClosedLoopController();
  private SparkClosedLoopController tiltController = tilt.getClosedLoopController();

  private RelativeEncoder intakeEncoder = intake.getEncoder();
  private AbsoluteEncoder tiltEncoder = tilt.getAbsoluteEncoder();

  private Library lib = new Library();

  // ==============================================================
  // Define motor vel and pos enums
  // ==============================================================
  // The Feeder SP is stored as a percentage of RPMs
  public enum IntakeSP {
    OFF(0.0),
    LOW(50.0),
    MED(90.0),
    HI(100.0);

    private double pct;

    IntakeSP(double pct) {
      this.pct = pct;
    }

    public double getVel(boolean rpm) {
      if (rpm) {
        return pct * IntakeConstants.kIntakeMotorFreeSpeedRpm / 100.0;
      } else {
        return pct;
      }
    }
  }

  // The Tilt SP is in degrees
  public enum TiltSP {
    STOW(0.0),
    DEPLOY(0.9);

    private double pos;

    TiltSP(double pos) {
      this.pos = pos;
    }

    public double getPos() {
      return pos;
    }
  }

  // Define motor setpoints
  private IntakeSP intakeSP = IntakeSP.OFF;
  private TiltSP tiltSP = TiltSP.STOW;

  // ==============================================================
  // Initialize Dashboard entries
  // ==============================================================
  // private final ShuffleboardTab cmdTab = Shuffleboard.getTab("Commands");
  // private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");
  private final ShuffleboardTab intakeTab = Shuffleboard.getTab("Intake");
  private final ShuffleboardTab IntakeCommands = Shuffleboard.getTab("Intake Commands");

  private final GenericEntry sbTiltOnTgt = intakeTab.addPersistent("Tilt OnTgt", false)
      .withWidget("Boolean Box").withPosition(0, 1).withSize(2, 1).getEntry();
  private final GenericEntry sbTiltSP = intakeTab.addPersistent("Tilt SP", "")
      .withWidget("Text View").withPosition(2, 0).withSize(2, 1).getEntry();
  private final GenericEntry sbTiltSPPos = intakeTab.addPersistent("Tilt SP Pos", 0)
      .withWidget("Text View").withPosition(2, 1).withSize(2, 1).getEntry();

  private final GenericEntry sbTiltPos = intakeTab.addPersistent("Tilt Pos", 0)
      .withWidget("Text View").withPosition(4, 0).withSize(2, 1).getEntry();

  private final GenericEntry sbIntakeOnTgt = intakeTab.addPersistent("Intake OnTgt", false)
      .withWidget("Boolean Box").withPosition(0, 1).withSize(2, 1).getEntry();
  private final GenericEntry sbIntakeSP = intakeTab.addPersistent("Intake SP", "")
      .withWidget("Text View").withPosition(2, 0).withSize(2, 1).getEntry();
  private final GenericEntry sbIntakeSPPct = intakeTab.addPersistent("Intake SP Pct", 0)
      .withWidget("Text View").withPosition(2, 1).withSize(2, 1).getEntry();
  private final GenericEntry sbIntakeSPRPM = intakeTab.addPersistent("Intake SP RPM", 0)
      .withWidget("Text View").withPosition(2, 2).withSize(2, 1).getEntry();

  private final GenericEntry sbIntakeVelPct = intakeTab.addPersistent("Intake Vel Pct", 0)
      .withWidget("Text View").withPosition(4, 0).withSize(2, 1).getEntry();
  private final GenericEntry sbIntakeVelRPM = intakeTab.addPersistent("Intake Vel RPM", 0)
      .withWidget("Text View").withPosition(4, 1).withSize(2, 1).getEntry();

  // ==============================================================
  // Constructor
  // ==============================================================
  public Intake() {
    System.out.println("+++++ Starting Intake Constructor +++++");

    // Configure Intake motor
    intakeConfig
        .idleMode(IntakeConstants.kIntakeIdleMode)
        .smartCurrentLimit(IntakeConstants.kIntakeCurrentLimit)
        .inverted(IntakeConstants.kIntakeMotorInverted);
    intakeConfig.encoder
        .positionConversionFactor(IntakeConstants.kIntakePositionFactor)
        .velocityConversionFactor(IntakeConstants.kIntakeVelocityFactor);
    intakeConfig.closedLoop
        .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
        .p(IntakeConstants.kIntakeP)
        .i(IntakeConstants.kIntakeI)
        .d(IntakeConstants.kIntakeD)
        .outputRange(IntakeConstants.kIntakeMinOutput, IntakeConstants.kIntakeMaxOutput);
    intakeConfig.closedLoop.feedForward
        .kA(IntakeConstants.kVelFF);
    intakeConfig.closedLoop.maxMotion
        .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
        .cruiseVelocity(IntakeConstants.kIntakeMaxVel)
        .maxAcceleration(IntakeConstants.kIntakeMaxAccel)
        .allowedProfileError(IntakeConstants.kIntakeAllowedErr);

    intake.configure(
        intakeConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    // Configure Tilt motor
    tiltConfig
        .inverted(IntakeConstants.kTiltMotorInverted)
        .idleMode(IntakeConstants.kTiltIdleMode)
        .smartCurrentLimit(IntakeConstants.kTiltCurrentLimit);
    tiltConfig.absoluteEncoder
        .zeroOffset(IntakeConstants.kTiltZeroOffset)
        .zeroCentered(IntakeConstants.kTiltZeroCentered)
        .inverted(IntakeConstants.kTiltEncoderInverted)
        .positionConversionFactor(IntakeConstants.kTiltPositionFactor)
        .velocityConversionFactor(IntakeConstants.kTiltVelocityFactor)
				.apply(AbsoluteEncoderConfig.Presets.REV_ThroughBoreEncoderV2);
    tiltConfig.closedLoop
        .feedbackSensor(FeedbackSensor.kAbsoluteEncoder)
        .p(IntakeConstants.kTiltP)
        .i(IntakeConstants.kTiltI)
        .d(IntakeConstants.kTiltD)
        .outputRange(IntakeConstants.kTiltMinOutput, IntakeConstants.kTiltMaxOutput);
    tiltConfig.closedLoop.maxMotion
        .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
        .cruiseVelocity(IntakeConstants.kTiltMaxVel)
        .maxAcceleration(IntakeConstants.kTiltMaxAccel)
        .allowedProfileError(IntakeConstants.kIntakeAllowedErr);

    tilt.configure(
        tiltConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    // Add commands to Dashboard
    IntakeCommands.add("Intake Off", this.setIntake(IntakeSP.OFF))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    IntakeCommands.add("Intake Hi", this.setIntake(IntakeSP.HI))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    IntakeCommands.add("Intake Med", this.setIntake(IntakeSP.MED))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    IntakeCommands.add("Intake Low", this.setIntake(IntakeSP.LOW))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    IntakeCommands.add("Tilt Stow", this.setTilt(TiltSP.STOW))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    IntakeCommands.add("Tilt Deploy", this.setTilt(TiltSP.DEPLOY))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));

    // Initialize intake start positions
    setIntakeVel(IntakeSP.OFF);
    setTiltPos(TiltSP.STOW);

    System.out.println("----- Ending Intake Constructor -----");
  }

  // ==============================================================
  // Define subsystem commands
  // ==============================================================
  public Command setIntake(IntakeSP sp) {
    return run(() -> this.setIntakeVel(sp));
  }

  public Command setTilt(TiltSP sp) {
    return runOnce(() -> this.setTiltPos(sp));
  }

  // ==============================================================
  // Periodic methods
  // ==============================================================
  @Override
  public void periodic() {
    sbIntakeOnTgt.setBoolean(onIntakeTarget());
    sbIntakeSP.setString(getIntakeSP().name());
    sbIntakeSPPct.setDouble(lib.SBFormat(getIntakeSP(false)));
    sbIntakeSPRPM.setDouble(lib.SBFormat(getIntakeSP(true)));
    sbIntakeVelPct.setDouble(lib.SBFormat(getIntakeVel(false)));
    sbIntakeVelRPM.setDouble(lib.SBFormat(getIntakeVel(true)));

    sbTiltOnTgt.setBoolean(onTiltTarget());
    sbTiltSP.setString(getTiltSP().name());
    sbTiltSPPos.setDouble(lib.SBFormat(getTiltSP().getPos()));
    sbTiltPos.setDouble(lib.SBFormat(getTiltPos()));
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }

  // ==============================================================
  // Define subsystem methods
  // ==============================================================
  /**
   * Sets the intake setpoint.
   * 
   * @param sp The intake setpoint to set.
   *           The setpoint is stored as a percentage of RPMs, but the controller
   *           is set with the RPM value.
   */
  public void setIntakeSP(IntakeSP sp) {
    intakeSP = sp;
  }

  /**
   * Gets the intake setpoint enum.
   * 
   * @return The current intake setpoint.
   */
  public IntakeSP getIntakeSP() {
    return intakeSP;
  }

  /**
   * Gets the intake setpoint value.
   * 
   * @param rpm If true, returns the setpoint in RPMs. If false, returns the
   *            setpoint as a percentage.
   * @return The intake setpoint value.
   */
  public double getIntakeSP(boolean rpm) {
    return intakeSP.getVel(rpm);
  }

  public void setIntakeVel(IntakeSP sp) {
    setIntakeSP(sp);
    intakeController.setSetpoint(getIntakeSP(false) / 100.0 * 12.0, SparkBase.ControlType.kVoltage);
    // , SparkBase.ControlType.kVelocity);
  }

  public double getIntakeVel(boolean rpm) {
    if (rpm) {
      return intakeEncoder.getVelocity();
    } else {
      return intakeEncoder.getVelocity() / IntakeConstants.kIntakeMotorFreeSpeedRpm * 100.0;
    }
  }

  public boolean onIntakeTarget() {
    return Math.abs(getIntakeVel(true) - getIntakeSP(true)) < IntakeConstants.kIntakeTollerance;
  }

  public double getTiltPos() {
    return tiltEncoder.getPosition();
  }

  public void setTiltSP(TiltSP sp) {
    tiltSP = sp;
  }

  public void setTiltPos(TiltSP sp) {
    setTiltSP(sp);
    tiltController.setSetpoint(getTiltSP().getPos(), SparkBase.ControlType.kPosition);  //kMAXMotionPositionControl);
  }

  public TiltSP getTiltSP() {
    return tiltSP;
  }

  public boolean onTiltTarget() {
    return Math.abs(getTiltPos() - getTiltSP().getPos()) < IntakeConstants.kTiltTollerance;
  }
}
