// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.DIOId;

public class Feeder extends SubsystemBase {
  /** Creates a new ExampleSubsystem. */

  // ==============================================================
  // Define Feeder Motor
  private final SparkMax feeder = new SparkMax(
      Constants.CANId.kFeederCanId, MotorType.kBrushless);

  private final SparkMaxConfig feederConfig = new SparkMaxConfig();

  private SparkClosedLoopController feederController = feeder.getClosedLoopController();

  private RelativeEncoder feederEncoder = feeder.getEncoder();

  // The Feeder SP is stored as a percentage of RPMs
  private enum FeederSP {
    OFF(0.0),
    LOW(25.0),
    MED(50.0),
    HI(75.0);

    private double vel;

    FeederSP(double vel) {
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

  private FeederSP feederSP = FeederSP.OFF;

  private final DigitalInput fuelSwitch = new DigitalInput(DIOId.kFuelAvail);

  /**************************************************************
   * Initialize Shuffleboard entries
   **************************************************************/
  // private final ShuffleboardTab cmdTab = Shuffleboard.getTab("Commands");
  // private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");
  private final ShuffleboardTab feederTab = Shuffleboard.getTab("Feeder");

  private final GenericEntry sbFuelAvail = feederTab.addPersistent("Fuel Avail", false)
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

  public Feeder() {
    System.out.println("+++++ Starting Feeder Constructor +++++");
    // Configure Feeder motor
    feederConfig
        .idleMode(Constants.Feeder.kFeederIdleMode)
        .smartCurrentLimit(Constants.Feeder.kFeederCurrentLimit);
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

    feeder.configure(
        feederConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    setFeederSP(FeederSP.LOW);
    //setFeederVel(feederSP);

    System.out.println("----- Ending Feeder Constructor -----");
  }

  /**
   * Example command factory method.
   *
   * @return a command
   */
  public Command exampleMethodCommand() {
    // Inline construction of command goes here.
    // Subsystem::RunOnce implicitly requires `this` subsystem.
    return runOnce(
        () -> {
          /* one-time action goes here */
        });
  }

  /**
   * An example method querying a boolean state of the subsystem (for example, a
   * digital sensor).
   *
   * @return value of some boolean subsystem state, such as a digital sensor.
   */
  public boolean exampleCondition() {
    // Query some boolean state, such as a digital sensor.
    return false;
  }

  @Override
  public void periodic() {
    sbFeederOnTgt.setBoolean(onFeederTarget());
    sbFuelAvail.setBoolean(isFuelAvail());
    sbFeederSP.setString(getFeederSP().name());
    sbFeederSPPct.setDouble(getFeederSP(false));
    sbFeederSPRPM.setDouble(getFeederSP(true));
    sbFeederVelPct.setDouble(getFeederVel(false));
    sbFeederVelRPM.setDouble(getFeederVel(true));
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }

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
    feederController.setSetpoint(getFeederSP(true), SparkBase.ControlType.kMAXMotionVelocityControl);
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

  public boolean isFuelAvail() {
    return fuelSwitch.get();
  }
}
