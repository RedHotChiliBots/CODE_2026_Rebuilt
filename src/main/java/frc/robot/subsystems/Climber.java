// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.util.Map;

import com.revrobotics.AbsoluteEncoder;
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
import frc.robot.constants.ClimberConstants;
import frc.robot.utils.Library;
import frc.robot.utils.SparkMaxSimulation;
import edu.wpi.first.math.system.plant.DCMotor;

//CLASS DEFINITION
public class Climber extends SubsystemBase {
  // ==============================================================
  // Define Climber Motors and Servos
  // ==============================================================
  private final SparkMax climber1 = new SparkMax(
      ClimberConstants.kClimber1CanId, MotorType.kBrushless);

  private final SparkMax climber2 = new SparkMax(
      ClimberConstants.kClimber2CanId, MotorType.kBrushless);

  private final SparkMaxConfig climber1Config = new SparkMaxConfig();
  private final SparkMaxConfig climber2Config = new SparkMaxConfig();

  private final SparkClosedLoopController climber1Controller = climber1.getClosedLoopController();
  // private final SparkClosedLoopController climber2Controller =
  // climber2.getClosedLoopController();

  private final AbsoluteEncoder climber1AbsEncoder = climber1.getAbsoluteEncoder();
  // private final AbsoluteEncoder climber2AbsEncoder =
  // climber2.getAbsoluteEncoder();

  // Simulation objects
  private SparkMaxSimulation climber1Sim;


  // ==============================================================
  // Define motor and servo pos enums
  // ==============================================================
  public enum ClimberSP { // Climber Setpoints
    STOW(0.5), // NUMBERS NEED TO CHANGE
    LVL1(1.0); // NUMBERS NEED TO CHANGE


    private final double sp;

    ClimberSP(final double sp) {
      this.sp = sp;
    }

    public double getValue() {
      return sp;
    }
  }

  // ==============================================================
  // Initialize motor setpoints
  // ==============================================================
  private ClimberSP climberSP = Climber.ClimberSP.STOW;
  // ==============================================================
  // Initialize Dashboard entries
  // ==============================================================
  private final ShuffleboardTab climberTab = Shuffleboard.getTab("Climber");
  private final ShuffleboardTab ClimberCommands = Shuffleboard.getTab("Climber Commands");

  private final GenericEntry sbClimberOnTgt = climberTab.addPersistent("Climber OnTgt", false)
      .withWidget("Boolean Box").withPosition(0, 1).withSize(2, 1).getEntry();
  private final GenericEntry sbClimberSP = climberTab.addPersistent("Climber SP", "")
      .withWidget("Text View").withPosition(2, 0).withSize(2, 1).getEntry();
  private final GenericEntry sbClimberSPPos = climberTab.addPersistent("Climber SP Pos", 0)
      .withWidget("Text View").withPosition(2, 1).withSize(2, 1).getEntry();

  // ==============================================================
  // Constructor
  // ==============================================================
  public Climber() {
    System.out.println("+++++ Starting Climber Constructor +++++");

    // Climbing Motor Configs 1-4
    climber1Config
        .inverted(ClimberConstants.kClimberInverted)
        .idleMode(ClimberConstants.kClimberIdleMode)
        .smartCurrentLimit(ClimberConstants.kClimberCurrentLimit);
    climber1Config.absoluteEncoder
        .zeroOffset(ClimberConstants.kZeroOffset)
        .zeroCentered(ClimberConstants.kZeroCentered)
        .inverted(ClimberConstants.kEncoderInverted)
        .positionConversionFactor(ClimberConstants.kTiltPositionFactor)
        .velocityConversionFactor(ClimberConstants.kTiltVelocityFactor);
    climber1Config.closedLoop
        .feedbackSensor(FeedbackSensor.kAbsoluteEncoder)
        .p(ClimberConstants.kPosP)
        .i(ClimberConstants.kPosI)
        .d(ClimberConstants.kPosD)
        .outputRange(ClimberConstants.kPosMinOutput, ClimberConstants.kPosMaxOutput);

    climber2Config.follow(ClimberConstants.kClimber1CanId); // Mimics climber1

    climber1.configure(climber1Config,
        com.revrobotics.ResetMode.kNoResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    climber2.configure(climber2Config,
        com.revrobotics.ResetMode.kNoResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    // Add commands to Dashboard
    ClimberCommands.add("Climber Stow", this.setClimber(ClimberSP.STOW))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    ClimberCommands.add("Climber L1", this.setClimber(ClimberSP.LVL1))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));


    // Initialize simulation
    if (Constants.currentMode == Constants.Mode.SIM) {
      // Climber motor simulation (position control)
      climber1Sim = SparkMaxSimulation.createPositionSim(
          climber1,
          DCMotor.getNEO(1),
          ClimberConstants.kClimberGearRatio,
          0.3, // arm length in meters
          ClimberSP.STOW.getValue(), // min position
          ClimberSP.LVL1.getValue(), // max position
          false, // don't simulate gravity for vertical climber
          ClimberSP.STOW.getValue() // starting position
      );
    }

    System.out.println("+++++ End of Climber Constructor +++++");
  }

  // ==============================================================
  // Define subsystem commands
  // ==============================================================

  public Command setClimber(ClimberSP sp) {
    return runOnce(() -> setClimberPos(sp));
  }

  // ==============================================================
  // Periodic methods
  // ==============================================================
  @Override
  public void periodic() {
    sbClimberOnTgt.setBoolean(onClimberTarget());
    sbClimberSP.setString(getClimberSP().name());
    sbClimberSPPos.setDouble(Library.SBFormat(getClimberSP().getValue()));
  }

  @Override
  public void simulationPeriodic() {
    // Update motor simulation
    if (climber1Sim != null) {
      climber1Sim.update(getClimberSP().getValue(), 0.02);
    }
  }

  // ==============================================================
  // Define subsystem methods
  // ==============================================================
  // Start of Climber Methods
  public double getClimberPos() {
    return climber1AbsEncoder.getPosition(); // All the other motors should match
  }

  public double getClimberVel() {
    return climber1AbsEncoder.getVelocity(); // All the other motors should match
  }

  public void setClimberPos(ClimberSP pos) {
    setClimberSP(pos);
    climber1Controller.setSetpoint(pos.getValue(),
        SparkBase.ControlType.kPosition);
  }

  public void setClimberPos() {
    setClimberPos(getClimberSP());
    // Sets the desired position using the current setpoint,
    // which is updated by the other setClimberPos method.
  }

  public void setClimberSP(ClimberSP sp) {
    climberSP = sp;
  }

  public ClimberSP getClimberSP() {
    return climberSP;
  }

  public boolean onClimberTarget() {
    return Math.abs(getClimberPos() - getClimberSP().getValue()) < ClimberConstants.kClimberTolerance;
  }
  // End of Climber Methods
}
