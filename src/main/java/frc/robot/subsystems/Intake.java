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
import frc.robot.Constants;
import frc.robot.utils.Library;
import frc.robot.utils.SparkMaxSimulation;
import edu.wpi.first.math.system.plant.DCMotor;

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

  // Simulation objects
  private SparkMaxSimulation intakeSim;
  private SparkMaxSimulation tiltSim;

  // ==============================================================
  // Define motor vel and pos enums
  // ==============================================================
  // The Intake SP is stored as a percentage of max RPMs for motor
  public enum IntakeSP {
    OFF(0.0),
    LOW(50.0),
    MED(75.0),
    HI(100.0);

    private double pct;

    IntakeSP(double pct) {
      this.pct = pct;
    }

    public double getVel(boolean rpm) {
      if (rpm) {
        return (Library.pctToRpm(pct, Constants.MotorConstants.kNeoFreeSpeedRpm));  // * Constants.Intake.kIntakeVelocityFactor);
      } else {
        return pct;
      }
    }
  }

  // The Tilt SP is in degrees
  public enum TiltSP {
    STOW(0.0),
    DEPLOY(80.0);

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
  // private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");
  private final ShuffleboardTab intakeTab = Shuffleboard.getTab("Intake Methods");
  private final ShuffleboardTab cmdTab = Shuffleboard.getTab("Intake Commands");

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
        .smartCurrentLimit(Constants.Intake.kIntakeCurrentLimit)
        .inverted(Constants.Intake.kIntakeMotorInverted);
    intakeConfig.encoder
        .positionConversionFactor(Constants.Intake.kIntakePositionFactor)
        .velocityConversionFactor(Constants.Intake.kIntakeVelocityFactor);
    intakeConfig.closedLoop
        .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
        .p(Constants.Intake.kIntakeP)
        .i(Constants.Intake.kIntakeI)
        .d(Constants.Intake.kIntakeD)
        .outputRange(Constants.Intake.kIntakeMinOutput, Constants.Intake.kIntakeMaxOutput);
    intakeConfig.closedLoop.feedForward
        .kA(Constants.Intake.kVelFF);
    intakeConfig.closedLoop.maxMotion
        .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
        .cruiseVelocity(Constants.Intake.kIntakeMaxVel)
        .maxAcceleration(Constants.Intake.kIntakeMaxAccel)
        .allowedProfileError(Constants.Intake.kIntakeAllowedErr);

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
        .velocityConversionFactor(Constants.Intake.kTiltVelocityFactor)
        .apply(AbsoluteEncoderConfig.Presets.REV_ThroughBoreEncoderV2);
    tiltConfig.closedLoop
        .feedbackSensor(FeedbackSensor.kAbsoluteEncoder)
        .p(Constants.Intake.kTiltP)
        .i(Constants.Intake.kTiltI)
        .d(Constants.Intake.kTiltD)
        .outputRange(Constants.Intake.kTiltMinOutput, Constants.Intake.kTiltMaxOutput)
				.positionWrappingEnabled(Constants.Intake.kTiltEncodeWrapping);
    tiltConfig.closedLoop.maxMotion
        .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
        .cruiseVelocity(Constants.Intake.kTiltMaxVel)
        .maxAcceleration(Constants.Intake.kTiltMaxAccel)
        .allowedProfileError(Constants.Intake.kTiltAllowedErr);

    tilt.configure(
        tiltConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    // Add commands to Dashboard
    cmdTab.add("Intake Off", this.setIntake(IntakeSP.OFF))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    cmdTab.add("Intake Hi", this.setIntake(IntakeSP.HI))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    cmdTab.add("Intake Med", this.setIntake(IntakeSP.MED))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    cmdTab.add("Intake Low", this.setIntake(IntakeSP.LOW))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    cmdTab.add("Tilt Stow", this.setTilt(TiltSP.STOW))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    cmdTab.add("Tilt Deploy", this.setTilt(TiltSP.DEPLOY))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));

    // Initialize intake start positions
    setIntakeVel(IntakeSP.OFF);
    setTiltPos(TiltSP.STOW);

    // Initialize simulation
    if (Constants.currentMode == Constants.Mode.SIM) {
      // Intake motor simulation (velocity control)
      intakeSim = SparkMaxSimulation.createVelocitySim(
          intake,
          DCMotor.getNEO(1),
          Constants.Intake.kIntakeGearRatio,
          0.005 // MOI in kg*m^2
      );

      // Tilt motor simulation (position control)
      tiltSim = SparkMaxSimulation.createPositionSim(
          tilt,
          DCMotor.getNEO(1),
          Constants.Intake.kTiltGearRatio,
          0.5, // arm length in meters
          TiltSP.STOW.getPos(), // min angle
          TiltSP.DEPLOY.getPos(), // max angle
          true, // simulate gravity
          TiltSP.STOW.getPos() // starting angle
      );
    }

    System.out.println("----- Ending Intake Constructor -----");
  }

  // ==============================================================
  // Define subsystem commands
  // ==============================================================
  public Command setIntake(IntakeSP sp) {
    return runOnce(() -> this.setIntakeVel(sp));
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
    sbIntakeSPPct.setDouble(Library.SBFormat(getIntakeSP(false)));
    sbIntakeSPRPM.setDouble(Library.SBFormat(getIntakeSP(true)));
    sbIntakeVelPct.setDouble(Library.SBFormat(getIntakeVel(false)));
    sbIntakeVelRPM.setDouble(Library.SBFormat(getIntakeVel(true)));

    sbTiltOnTgt.setBoolean(onTiltTarget());
    sbTiltSP.setString(getTiltSP().name());
    sbTiltSPPos.setDouble(Library.SBFormat(getTiltSP().getPos()));
    sbTiltPos.setDouble(Library.SBFormat(getTiltPos()));
  }

  @Override
  public void simulationPeriodic() {
    // Update motor simulations
    if (intakeSim != null) {
      intakeSim.update(getIntakeSP(true), 0.02);
      // Note: In real simulation, you would update the encoder values here
      // For now, the simulation tracks state internally
    }
    
    if (tiltSim != null) {
      tiltSim.update(getTiltSP().getPos(), 0.02);
      // Note: In real simulation, you would update the encoder values here
    }
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
    return rpm ? intakeEncoder.getVelocity() : Library.rpmToPct(intakeEncoder.getVelocity(), Constants.MotorConstants.kNeoFreeSpeedRpm);
  }

  public boolean onIntakeTarget() {
    return Math.abs(getIntakeVel(true) - getIntakeSP(true)) < Constants.Intake.kIntakeAllowedErr;
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
    return Math.abs(getTiltPos() - getTiltSP().getPos()) < Constants.Intake.kTiltAllowedErr;
  }
}
