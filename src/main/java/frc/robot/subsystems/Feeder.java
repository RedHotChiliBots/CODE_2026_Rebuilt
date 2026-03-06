// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.util.Map;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.MAXMotionConfig.MAXMotionPositionMode;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.AnalogInput;

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.utils.Library;

public class Feeder extends SubsystemBase {
  // ==============================================================
  // Define Feeder Motor
  // ==============================================================
  private final SparkMax feeder = new SparkMax(
      Constants.CANId.kFeederCanId, MotorType.kBrushless);

  private final SparkMaxConfig feederConfig = new SparkMaxConfig();

  private SparkClosedLoopController feederController = feeder.getClosedLoopController();

  private RelativeEncoder feederEncoder = feeder.getEncoder();

  // ==============================================================
  // Define trigger inputs
  // =============================================================
  private final AnalogInput fuelSensor = new AnalogInput(Constants.AIOId.kFuelSensor);

  private Library lib = new Library();

  // ==============================================================
  // Define motor vel enum
  // ==============================================================
  // The Feeder SP is stored as a percentage of RPMs
  public enum FeederSP {
    OFF(0.0),
    LOW(50.0),
    MED(75.0),
    HI(100.0);

    private double pct;

    FeederSP(double pct) {
      this.pct = pct;
    }

    public double getVel(boolean rpm) {
      if (rpm) {

        return (pct / 100.0) * Constants.MotorConstants.kNeoFreeSpeedRpm;
      } else {
        return pct;
      }
    }
  }

  // ==============================================================
  // Initialize motor setpoints
  // ==============================================================
  private FeederSP feederSP = FeederSP.OFF;

  // ==============================================================
  // Initialize Dashboard entries
  // ==============================================================
  // private final ShuffleboardTab cmdTab = Shuffleboard.getTab("Commands");
  // private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");
  private final ShuffleboardTab feederTab = Shuffleboard.getTab("Feeder");
  private final ShuffleboardTab FeederCommands = Shuffleboard.getTab("Feeder Commands");

  private final GenericEntry sbFuelAvail = feederTab.addPersistent("Fuel Avail", false)
      .withWidget("Boolean Box").withPosition(0, 0).withSize(2, 1).getEntry();
  private final GenericEntry sbFuelVolts = feederTab.addPersistent("Fuel Volts", 0.0)
      .withWidget("Boolean Box").withPosition(0, 2).withSize(2, 1).getEntry();
  private final GenericEntry sbFuelDist = feederTab.addPersistent("Fuel Dist", 0.0)
      .withWidget("Boolean Box").withPosition(0, 0).withSize(2, 1).getEntry();

  private final GenericEntry sbFeederOnTgt = feederTab.addPersistent("Feeder OnTgt", false)
      .withWidget("Boolean Box").withPosition(0, 1).withSize(2, 1).getEntry();
  private final GenericEntry sbFeederSP = feederTab.addPersistent("Feeder SP", "")
      .withWidget("Text View").withPosition(2, 0).withSize(2, 1).getEntry();
  private final GenericEntry sbFeederSPPct = feederTab.addPersistent("Feeder SP Pct", 0)
      .withWidget("Text View").withPosition(2, 1).withSize(2, 1).getEntry();
  private final GenericEntry sbFeederSPRPM = feederTab.addPersistent("Feeder SP RPM", 0)
      .withWidget("Text View").withPosition(2, 2).withSize(2, 1).getEntry();

  private final GenericEntry sbFeederVelPct = feederTab.addPersistent("Feeder Vel Pct", 0)
      .withWidget("Text View").withPosition(4, 0).withSize(2, 1).getEntry();
  private final GenericEntry sbFeederVelRPM = feederTab.addPersistent("Feeder Vel RPM", 0)
      .withWidget("Text View").withPosition(4, 1).withSize(2, 1).getEntry();

  // ==============================================================
  // Constructor
  // ==============================================================
  public Feeder() {
    System.out.println("+++++ Starting Feeder Constructor +++++");
    // Configure Feeder motor
    feederConfig
        .idleMode(Constants.Feeder.kFeederIdleMode)
        .smartCurrentLimit(Constants.Feeder.kFeederCurrentLimit)
				.inverted(Constants.Feeder.kFeederMotorInverted);
    feederConfig.encoder
        .positionConversionFactor(Constants.Feeder.kFeederPositionFactor)
        .velocityConversionFactor(Constants.Feeder.kFeederVelocityFactor);
    feederConfig.closedLoop
        .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
        .p(Constants.Feeder.kFeederP)
        .i(Constants.Feeder.kFeederI)
        .d(Constants.Feeder.kFeederD)
        .outputRange(Constants.Feeder.kFeederMinOutput, Constants.Feeder.kFeederMaxOutput)
        .positionWrappingEnabled(Constants.Feeder.kFeederEncodeWrapping);
    feederConfig.closedLoop.feedForward
        .kA(Constants.Feeder.kFeederVelFF);
    feederConfig.closedLoop.maxMotion
        .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
				.cruiseVelocity(Constants.Feeder.kFeederMaxVel)
				.maxAcceleration(Constants.Feeder.kFeederMaxAccel)
				.allowedProfileError(Constants.Feeder.kFeederAllowedErr);

    feeder.configure(
        feederConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    // Add commands to Dashboard
    FeederCommands.add("Feeder Off", this.setFeeder(FeederSP.OFF))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    FeederCommands.add("Feeder Hi", this.setFeeder(FeederSP.HI))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    FeederCommands.add("Feeder Med", this.setFeeder(FeederSP.MED))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    FeederCommands.add("Feeder Low", this.setFeeder(FeederSP.LOW))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));

    // Initialize intake start positions
    setFeederVel(FeederSP.OFF);

    System.out.println("----- Ending Feeder Constructor -----");
  }

  // ==============================================================
  // Define subsystem commands
  // ==============================================================
  public Command setFeeder(FeederSP sp) {
    return runOnce(() -> this.setFeederVel(sp));
  }

  // ========()======================================================
  // Periodic methods
  // ==============================================================
  @Override
  public void periodic() {
    sbFeederOnTgt.setBoolean(onFeederTarget());
    sbFuelAvail.setBoolean(isFuelAvail());
    sbFuelVolts.setDouble(lib.SBFormat(getFuelVolts()));
    sbFuelDist.setDouble(lib.SBFormat(getFuelDist()));
    sbFeederSP.setString(getFeederSP().name());
    sbFeederSPPct.setDouble(lib.SBFormat(getFeederSP(false)));
    sbFeederSPRPM.setDouble(lib.SBFormat(getFeederSP(true)));
    sbFeederVelPct.setDouble(lib.SBFormat(getFeederVel(false)));
    sbFeederVelRPM.setDouble(lib.SBFormat(getFeederVel(true)));
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }

  // ==============================================================
  // Define subsystem methods
  // ==============================================================
  public void setFeederSP(FeederSP sp) {
    feederSP = sp;
  }

  public FeederSP getFeederSP() {
    return feederSP;
  }

  public double getFeederSP(boolean rpm) {
    return feederSP.getVel(rpm);
  }

  public void setFeederVel(FeederSP sp) {
    setFeederSP(sp);
    feederController.setSetpoint(getFeederSP(false)/100.0*12.0, SparkBase.ControlType.kVoltage);
  //  .kMAXMotionVelocityControl);
//    feederController.setSetpoint(Constants.MotorConstants.kNeoFreeSpeedRpm * .80, SparkBase.ControlType.kMAXMotionVelocityControl);
  }

  public double getFeederVel(boolean rpm) {
    if (rpm) {
      return feederEncoder.getVelocity();
    } else {
      return feederEncoder.getVelocity() / Constants.MotorConstants.kNeoFreeSpeedRpm * 100.0;
    }
  }

  public boolean onFeederTarget() {
    return Math.abs(getFeederVel(true) - getFeederSP(true)) < Constants.Feeder.kTollerance;
  }

  public double getFuelVolts() {
    return fuelSensor.getVoltage();
  }

  public double getFuelDist() {
    return getFuelVolts() / (5.0 / 1024.0) / (25.4 / 5.0);
  }

  public boolean isFuelAvail() {
    return !(getFuelDist() > 21.0 && getFuelDist() < 30.0);
  }
}