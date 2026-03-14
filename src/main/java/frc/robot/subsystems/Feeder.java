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
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.constants.FeederConstants;
import frc.robot.constants.ValidationConstants;
import frc.robot.utils.Library;
import frc.robot.utils.SparkMaxSimulation;
import frc.robot.validation.SubsystemValidation;
import frc.robot.validation.ValidationResult;
import frc.robot.validation.ValidationStatus;
import frc.robot.validation.ValidationSupport;
import frc.robot.validation.ValidationUtils;
import edu.wpi.first.math.system.plant.DCMotor;

public class Feeder extends SubsystemBase implements SubsystemValidation {
  private final ValidationSupport validation = new ValidationSupport("Feeder");
  // ==============================================================
  // Define Feeder Motor
  // ==============================================================
  private final SparkMax feeder = new SparkMax(
      FeederConstants.kFeederCanId, MotorType.kBrushless);

  private final SparkMaxConfig feederConfig = new SparkMaxConfig();

  private SparkClosedLoopController feederController = feeder.getClosedLoopController();

  private RelativeEncoder feederEncoder = feeder.getEncoder();

  // Simulation objects
  private SparkMaxSimulation feederSim;

  // ==============================================================
  // Define trigger inputs
  // =============================================================
  private final AnalogInput fuelSensor = new AnalogInput(FeederConstants.kFuelSensorChannel);

  // ==============================================================
  // Define motor vel enum
  // ==============================================================
  /**
   * Enumeration of feeder motor speed setpoints.
   * The Feeder SP is stored as a percentage of RPMs.
   */
  public enum FeederSP {
    OFF(0.0),
    LOW(50.0),
    MED(75.0),
    HI(100.0);

    private double pct;

    /**
     * Constructs a FeederSP with the specified percentage.
     *
     * @param pct The percentage of maximum motor speed (0-100)
     */
    FeederSP(double pct) {
      this.pct = pct;
    }

    /**
     * Gets the velocity value for this setpoint.
     *
     * @param rpm If true, returns velocity in RPM; if false, returns as percentage
     * @return The velocity value in the requested units
     */
    public double getVel(boolean rpm) {
      if (rpm) {
        return Library.pctToRpm(pct, FeederConstants.kFeederMotorFreeSpeedRpm);
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
  // private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");
  private final ShuffleboardTab feederTab = Shuffleboard.getTab("Feeder Methods");
  private final ShuffleboardTab cmdTab = Shuffleboard.getTab("Feeder Commands");

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
  /**
   * Creates a new Feeder subsystem.
   * Configures the feeder motor with PID control, current limits, and velocity conversion factors.
   * Initializes the fuel sensor and sets up Shuffleboard dashboard entries.
   * Configures simulation if running in simulation mode.
   */
  public Feeder() {
    System.out.println("+++++ Starting Feeder Constructor +++++");
    // Configure Feeder motor
    feederConfig
        .idleMode(FeederConstants.kFeederIdleMode)
        .smartCurrentLimit(FeederConstants.kFeederCurrentLimit)
        .inverted(FeederConstants.kFeederMotorInverted);
    feederConfig.encoder
//        .positionConversionFactor(FeederConstants.kFeederPositionFactor)
        .velocityConversionFactor(FeederConstants.kFeederVelocityFactor);
    feederConfig.closedLoop
        .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
        .p(FeederConstants.kFeederP)
        .i(FeederConstants.kFeederI)
        .d(FeederConstants.kFeederD)
        .outputRange(FeederConstants.kFeederMinOutput, FeederConstants.kFeederMaxOutput);
    feederConfig.closedLoop.feedForward
        .kA(FeederConstants.kFeederVelFF);
    feederConfig.closedLoop.maxMotion
        .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
        .cruiseVelocity(FeederConstants.kFeederMaxVel)
        .maxAcceleration(FeederConstants.kFeederMaxAccel)
        .allowedProfileError(FeederConstants.kFeederAllowedErr);

    feeder.configure(
        feederConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    // Add commands to Dashboard
    cmdTab.add("Feeder Off", this.setFeeder(FeederSP.OFF))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    cmdTab.add("Feeder Hi", this.setFeeder(FeederSP.HI))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    cmdTab.add("Feeder Med", this.setFeeder(FeederSP.MED))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    cmdTab.add("Feeder Low", this.setFeeder(FeederSP.LOW))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));

    // Initialize intake start positions
    setFeederVel(FeederSP.OFF);

    // Initialize simulation
    if (Constants.currentMode == Constants.Mode.SIM) {
      // Feeder motor simulation (velocity control)
      feederSim = SparkMaxSimulation.createVelocitySim(
          feeder,
          DCMotor.getNEO(1),
          FeederConstants.kFeederGearRatio,
          0.003 // MOI in kg*m^2 for roller
      );
    }

    System.out.println("----- Ending Feeder Constructor -----");
  }

  // ==============================================================
  // Define subsystem commands
  // ==============================================================
  /**
   * Creates a command to set the feeder to a specific speed setpoint.
   *
   * @param sp The desired feeder speed setpoint
   * @return A command that sets the feeder velocity once
   */
  public Command setFeeder(FeederSP sp) {
    return runOnce(() -> this.setFeederVel(sp));
  }

  // ==============================================================
  // Periodic methods
  // ==============================================================
  @Override
  public void periodic() {
    sbFeederOnTgt.setBoolean(onFeederTarget());
    sbFuelAvail.setBoolean(isFuelAvail());
    sbFuelVolts.setDouble(Library.SBFormat(getFuelVolts()));
    sbFuelDist.setDouble(Library.SBFormat(getFuelDist()));
    sbFeederSP.setString(getFeederSP().name());
    sbFeederSPPct.setDouble(Library.SBFormat(getFeederSP(false)));
    sbFeederSPRPM.setDouble(Library.SBFormat(getFeederSP(true)));
    sbFeederVelPct.setDouble(Library.SBFormat(getFeederVel(false)));
    sbFeederVelRPM.setDouble(Library.SBFormat(getFeederVel(true)));
  }

  @Override
  public void simulationPeriodic() {
    // Update motor simulation
    if (feederSim != null) {
      feederSim.update(getFeederSP(true), 0.02);
    }
  }

  // ==============================================================
  // Define subsystem methods
  // ==============================================================
  /**
   * Sets the feeder speed setpoint without applying it to the motor.
   *
   * @param sp The desired feeder speed setpoint
   */
  public void setFeederSP(FeederSP sp) {
    feederSP = sp;
  }

  /**
   * Gets the current feeder speed setpoint.
   *
   * @return The current feeder speed setpoint enum value
   */
  public FeederSP getFeederSP() {
    return feederSP;
  }

  /**
   * Gets the current feeder speed setpoint as a numeric value.
   *
   * @param rpm If true, returns setpoint in RPM; if false, returns as percentage
   * @return The setpoint value in the requested units
   */
  public double getFeederSP(boolean rpm) {
    return feederSP.getVel(rpm);
  }

  /**
   * Sets the feeder velocity to the specified setpoint and applies it to the motor controller.
   * Uses MAXMotion velocity control for smooth acceleration profiles.
   *
   * @param sp The desired feeder speed setpoint
   */
  public void setFeederVel(FeederSP sp) {
    setFeederSP(sp);
    feederController.setSetpoint(getFeederSP(true), SparkBase.ControlType.kMAXMotionVelocityControl);
  }

  /**
   * Gets the current feeder motor velocity from the encoder.
   *
   * @param rpm If true, returns velocity in RPM; if false, returns as percentage of max speed
   * @return The current motor velocity in the requested units
   */
  public double getFeederVel(boolean rpm) {
    return rpm ? feederEncoder.getVelocity() : Library.rpmToPct(feederEncoder.getVelocity(), FeederConstants.kFeederMotorFreeSpeedRpm);
  }

  /**
   * Checks if the feeder motor is at the target velocity.
   *
   * @return True if the current velocity is within the allowed error of the setpoint, false otherwise
   */
  public boolean onFeederTarget() {
    return Math.abs(getFeederVel(true) - getFeederSP(true)) < FeederConstants.kFeederAllowedErr;
  }

  /**
   * Gets the raw voltage reading from the fuel sensor.
   *
   * @return The voltage reading from the analog fuel sensor
   */
  public double getFuelVolts() {
    return fuelSensor.getVoltage();
  }

  /**
   * Calculates the distance to the fuel (game piece) based on sensor voltage.
   * Converts the voltage reading to a distance measurement in inches.
   *
   * @return The calculated distance to the fuel in inches
   */
  public double getFuelDist() {
    return getFuelVolts() / (5.0 / 1024.0) / (25.4 / 5.0);
  }

  /**
   * Determines if fuel (game piece) is available in the feeder.
   * Checks if the distance reading indicates a game piece is present.
   *
   * @return True if fuel is detected within the valid range (≤21 inches or ≥30 inches), false otherwise
   */
  public boolean isFuelAvail() {
    return (getFuelDist() <= 21.0 || getFuelDist() >= 30.0);
  }

  private Command validationStep(
      String checkName,
      Runnable startAction,
      java.util.function.BooleanSupplier passCondition,
      java.util.function.Supplier<ValidationResult> resultSupplier,
      Runnable cleanup) {
    return Commands.sequence(
        Commands.runOnce(startAction, this),
        Commands.waitUntil(passCondition).withTimeout(ValidationConstants.Common.kMechanismTimeoutSec),
        Commands.runOnce(() -> validation.addResult(resultSupplier.get()), this),
        Commands.runOnce(cleanup, this));
  }

  private ValidationResult feederVelocityResult() {
    double setpointRpm = getFeederSP(true);
    double measuredRpm = getFeederVel(true);
    boolean passed = onFeederTarget();
    return ValidationResult.of(
        "Feeder",
        "Roller Velocity",
        passed,
        ValidationUtils.measurements(
            "setpointRpm", ValidationUtils.formatDouble(setpointRpm),
            "measuredRpm", ValidationUtils.formatDouble(measuredRpm),
            "onTarget", Boolean.toString(passed)),
        "+/- " + FeederConstants.kFeederAllowedErr + " RPM",
        passed ? "" : "Feeder did not reach target RPM");
  }

  private ValidationResult sensorResult() {
    double volts = getFuelVolts();
    double distance = getFuelDist();
    boolean inRange = ValidationUtils.isInRange(volts, 0.0, 5.0) && distance >= 0.0;
    return ValidationResult.of(
        "Feeder",
        "Fuel Sensor Sanity",
        inRange,
        ValidationUtils.measurements(
            "volts", ValidationUtils.formatDouble(volts),
            "distanceIn", ValidationUtils.formatDouble(distance),
            "fuelAvailable", Boolean.toString(isFuelAvail())),
        "0-5V and non-negative distance",
        inRange ? "" : "Feeder analog sensor returned an invalid reading");
  }

  private void safeValidationStop() {
    setFeederVel(FeederSP.OFF);
  }

  @Override
  public Command validateCommand() {
    return Commands.sequence(
            Commands.runOnce(validation::start),
            validationStep(
                "Roller Velocity",
                () -> setFeederVel(FeederSP.HI),
                this::onFeederTarget,
                this::feederVelocityResult,
                this::safeValidationStop),
            Commands.runOnce(() -> validation.addResult(sensorResult()), this),
            Commands.runOnce(validation::finish))
        .finallyDo(interrupted -> {
          safeValidationStop();
          if (interrupted && validation.status() == ValidationStatus.RUNNING) {
            validation.fail("Validation Interrupted", "Feeder validation was interrupted");
            validation.finish();
          }
        });
  }

  @Override
  public java.util.List<ValidationResult> validationResults() {
    return validation.results();
  }

  @Override
  public String validationSummary() {
    return validation.summary();
  }

  @Override
  public ValidationStatus validationStatus() {
    return validation.status();
  }
}
