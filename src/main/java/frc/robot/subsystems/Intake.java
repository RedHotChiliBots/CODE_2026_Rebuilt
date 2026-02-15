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
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class Intake extends SubsystemBase {

  // ==============================================================
  // Define Intake & Tilt Motors
  // ==============================================================
  private final SparkMax intake = new SparkMax(
      Constants.CANId.kIntakeIntakeCanId, MotorType.kBrushless);
  private final SparkMax tilt = new SparkMax(
      Constants.CANId.kIntakeTiltCanId, MotorType.kBrushless);

  private final SparkMaxConfig intakeConfig = new SparkMaxConfig();
  private final SparkMaxConfig tiltConfig = new SparkMaxConfig();

  private SparkClosedLoopController intakeController = intake.getClosedLoopController();
  private SparkClosedLoopController tiltController = tilt.getClosedLoopController();

  private RelativeEncoder intakeEncoder = intake.getEncoder();
  private AbsoluteEncoder tiltEncoder = tilt.getAbsoluteEncoder();

  // ==============================================================
  // Define motor vel and pos enums
  // ==============================================================
  // The Feeder SP is stored as a percentage of RPMs
  public enum IntakeSP {
    OFF(0.0),
    LOW(25.0),
    MED(50.0),
    HI(75.0);

    private double vel;

    IntakeSP(double vel) {
      this.vel = vel;
    }

    public double getVel(boolean rpm) {
      if (rpm) {
        return vel * Constants.MotorConstants.kNeoFreeSpeedRpm / 100.0;
      } else {
        return vel;
      }
    }
  }

  // The Tilt SP is in degrees
  public enum TiltSP {
    OFF(0.0),
    LOW(25.0),
    MED(50.0),
    HI(75.0);

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
  private TiltSP tiltSP = TiltSP.OFF;

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
        .idleMode(Constants.Intake.kIntakeIdleMode)
        .smartCurrentLimit(Constants.Intake.kIntakeCurrentLimit);
    intakeConfig.encoder
        .positionConversionFactor(Constants.Intake.kIntakePositionFactor)
        .velocityConversionFactor(Constants.Intake.kIntakeVelocityFactor);
    intakeConfig.closedLoop
        .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
        .p(Constants.Intake.kIntakeP)
        .i(Constants.Intake.kIntakeI)
        .d(Constants.Intake.kIntakeD)
        .outputRange(Constants.Intake.kIntakeMinOutput, Constants.Intake.kIntakeMaxOutput)
        .positionWrappingEnabled(Constants.Intake.kIntakeEncodeWrapping);
    intakeConfig.closedLoop.feedForward
        .kA(Constants.Intake.kIntakeVelFF);

    intake.configure(
        intakeConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    // Configure Tilt motor
    tiltConfig
        .inverted(Constants.Intake.kTiltMotorInverted)
        .idleMode(Constants.Intake.kTiltIdleMode)
        .smartCurrentLimit(Constants.Intake.kTiltCurrentLimit);
    tiltConfig.absoluteEncoder
        .zeroOffset(Constants.Intake.kTiltZeroOffset)
        .zeroCentered(Constants.Intake.kTiltZeroCentered)
        .inverted(Constants.Intake.kTiltEncoderInverted)
        .positionConversionFactor(Constants.Intake.kTiltPositionFactor)
        .velocityConversionFactor(Constants.Intake.kTiltVelocityFactor);
    tiltConfig.closedLoop
        .feedbackSensor(FeedbackSensor.kAbsoluteEncoder)
        .p(Constants.Intake.kTiltP)
        .i(Constants.Intake.kTiltI)
        .d(Constants.Intake.kTiltD)
        .outputRange(Constants.Intake.kTiltMinOutput, Constants.Intake.kTiltMaxOutput)
        .positionWrappingEnabled(Constants.Intake.kTiltEncodeWrapping);

    tilt.configure(
        tiltConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    // Add commands to Dashboard
		IntakeCommands.add("Shoot Off", this.setIntake(IntakeSP.OFF))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		IntakeCommands.add("Shoot Hi", this.setIntake(IntakeSP.HI))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		IntakeCommands.add("Shoot Med", this.setIntake(IntakeSP.MED))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		IntakeCommands.add("Shoot Low", this.setIntake(IntakeSP.LOW))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		IntakeCommands.add("Tilt Off", this.setTilt(TiltSP.OFF))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		IntakeCommands.add("Tilt Hi", this.setTilt(TiltSP.HI))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		IntakeCommands.add("Tilt Med", this.setTilt(TiltSP.MED))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
		IntakeCommands.add("Tilt Low", this.setTilt(TiltSP.LOW))
				.withProperties(Map.of("show_type", false, "maximize_button_space", false));
				
    // Initialize intake start positions
    setIntakeVel(IntakeSP.OFF);
    setTiltSP(TiltSP.OFF);

    System.out.println("----- Ending Intake Constructor -----");
  }

  // ==============================================================
  // Define subsystem commands
  // ==============================================================
  public Command setIntake(IntakeSP sp) {
    return runOnce(() -> setIntakeVel(sp));
  }

  public Command setTilt(TiltSP sp) {
    return runOnce(() -> setTiltPos(sp));
  }

  // ==============================================================
  // Periodic methods
  // ==============================================================
  @Override
  public void periodic() {
    sbIntakeOnTgt.setBoolean(onIntakeTarget());
    sbIntakeSP.setString(getIntakeSP().name());
    sbIntakeSPPct.setDouble(getIntakeSP(false));
    sbIntakeSPRPM.setDouble(getIntakeSP(true));
    sbIntakeVelPct.setDouble(getIntakeVel(false));
    sbIntakeVelRPM.setDouble(getIntakeVel(true));

    sbTiltOnTgt.setBoolean(onTiltTarget());
    sbTiltSP.setString(getTiltSP().name());
    sbTiltSPPos.setDouble(getTiltSP().getPos());
    sbTiltPos.setDouble(getTiltPos());

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
    intakeController.setSetpoint(getIntakeSP(true), SparkBase.ControlType.kMAXMotionVelocityControl);
  }

  public double getIntakeVel(boolean rpm) {
    if (rpm) {
      return intakeEncoder.getVelocity();
    } else {
      return intakeEncoder.getVelocity() / Constants.MotorConstants.kNeoFreeSpeedRpm * 100.0;
    }
  }

  public boolean onIntakeTarget() {
    return Math.abs(getIntakeVel(true) - getIntakeSP(true)) < Constants.Intake.kIntakeTollerance;
  }

  public double getTiltPos() {
    return tiltEncoder.getPosition();
  }

  public void setTiltSP(TiltSP sp) {
    tiltSP = sp;
  }

  public void setTiltPos(TiltSP sp) {
    setTiltSP(sp);
    tiltController.setSetpoint(getTiltSP().getPos(), SparkBase.ControlType.kMAXMotionPositionControl);
  }

  public TiltSP getTiltSP() {
    return tiltSP;
  }

  public boolean onTiltTarget() {
    return Math.abs(getTiltPos() - getTiltSP().getPos()) < Constants.Intake.kTiltTollerance;
  }
}
