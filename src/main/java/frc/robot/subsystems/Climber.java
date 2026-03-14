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
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.servohub.ServoChannel;
import com.revrobotics.servohub.ServoChannel.ChannelId;
import com.revrobotics.servohub.ServoHub;
import com.revrobotics.servohub.config.ServoChannelConfig;
import com.revrobotics.servohub.config.ServoHubConfig;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
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

  private final SparkMax climber3 = new SparkMax(
      ClimberConstants.kClimber3CanId, MotorType.kBrushless);

  private final SparkMax climber4 = new SparkMax(
      ClimberConstants.kClimber4CanId, MotorType.kBrushless);

  private final SparkMaxConfig climber1Config = new SparkMaxConfig();
  private final SparkMaxConfig climber2Config = new SparkMaxConfig();
  private final SparkMaxConfig climber3Config = new SparkMaxConfig();
  private final SparkMaxConfig climber4Config = new SparkMaxConfig();

  private final SparkClosedLoopController climber1Controller = climber1.getClosedLoopController();
  // private final SparkClosedLoopController climber2Controller =
  // climber2.getClosedLoopController();
  // private final SparkClosedLoopController climber3Controller =
  // climber3.getClosedLoopController();
  // private final SparkClosedLoopController climber4Controller =
  // climber4.getClosedLoopController();

  private final AbsoluteEncoder climber1AbsEncoder = climber1.getAbsoluteEncoder();
  // private final AbsoluteEncoder climber2AbsEncoder =
  // climber2.getAbsoluteEncoder();
  // private final AbsoluteEncoder climber3AbsEncoder =
  // climber3.getAbsoluteEncoder();
  // private final AbsoluteEncoder climber4AbsEncoder =
  // climber4.getAbsoluteEncoder();

  private final ServoHub servoHub = new ServoHub(ClimberConstants.kServoHubCanId);
  private final ServoChannel leftHook = servoHub.getServoChannel(ChannelId.kChannelId0);
  private final ServoChannel rightHook = servoHub.getServoChannel(ChannelId.kChannelId1);

  private final ServoHubConfig hubConfig = new ServoHubConfig();

  // Simulation objects
  private SparkMaxSimulation climber1Sim;


  // ==============================================================
  // Define motor and servo pos enums
  // ==============================================================
  public enum ClimberSP { // Climber Setpoints
    STOW(0.5), // NUMBERS NEED TO CHANGE
    TOP(1.0), // NUMBERS NEED TO CHANGE
    BOT(0.0), // NUMBERS NEED TO CHANGE
    LVLAUTON(0.25), // NUMBERS NEED TO CHANGE
    LVL1(1.0), // NUMBERS NEED TO CHANGE
    LVL2(0.0), // NUMBERS NEED TO CHANGE
    LVL3(1.0); // NUMBERS NEED TO CHANGE

    private final double sp;

    ClimberSP(final double sp) {
      this.sp = sp;
    }

    public double getValue() {
      return sp;
    }
  }

  public enum HookSP { // Climber Setpoints
    STOW(500), // NUMBERS NEED TO CHANGE
    STOP(1500),
    DEPLOY(2500); // NUMBERS NEED TO CHANGE

    private final int sp;

    HookSP(final int sp) {
      this.sp = sp;
    }

    public int getSpd() {
      return sp;
    }
  }

  // ==============================================================
  // Initialize motor setpoints
  // ==============================================================
  private ClimberSP climberSP = Climber.ClimberSP.STOW;
  private HookSP hookSP = Climber.HookSP.STOW;
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

  private final GenericEntry sbHookOnTgt = climberTab.addPersistent("Hook OnTgt", false)
      .withWidget("Boolean Box").withPosition(0, 1).withSize(2, 1).getEntry();
  private final GenericEntry sbHookSP = climberTab.addPersistent("Hook SP", "")
      .withWidget("Text View").withPosition(2, 0).withSize(2, 1).getEntry();
  private final GenericEntry sbHookSPSpd = climberTab.addPersistent("Hook SP Spd", 0)
      .withWidget("Text View").withPosition(2, 1).withSize(2, 1).getEntry();
  private final GenericEntry sbLeftHookAmp = climberTab.addPersistent("Hook Left Amp", 0)
      .withWidget("Text View").withPosition(2, 1).withSize(2, 1).getEntry();
  private final GenericEntry sbRightHookAmp = climberTab.addPersistent("Hook Right Amp", 0)
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
    climber1Config.signals
        .primaryEncoderVelocityPeriodMs(ClimberConstants. kPrimaryEncoderVelocityPeriod)
        .primaryEncoderPositionPeriodMs(ClimberConstants.kPrimaryEncoderPositionPeriod)
        .absoluteEncoderPositionPeriodMs(ClimberConstants.kAbsoluteEncoderPositionPeriod);

    climber2Config.follow(ClimberConstants.kClimber1CanId); // Mimics climber1
    climber3Config.follow(ClimberConstants.kClimber1CanId);
    climber4Config.follow(ClimberConstants.kClimber1CanId);

    climber1.configure(climber1Config,
        com.revrobotics.ResetMode.kNoResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    climber2.configure(climber2Config,
        com.revrobotics.ResetMode.kNoResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    climber3.configure(climber3Config,
        com.revrobotics.ResetMode.kNoResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    climber4.configure(climber4Config,
        com.revrobotics.ResetMode.kNoResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    hubConfig.channel0.pulseRange(500, 1500, 2500)
        .disableBehavior(ServoChannelConfig.BehaviorWhenDisabled.kSupplyPower); //
    // Default is 0-180, but can be changed

    hubConfig.channel1.pulseRange(500, 1500, 2500)
        .disableBehavior(ServoChannelConfig.BehaviorWhenDisabled.kSupplyPower); //
    // Default is 0-180, but can be changed

    // Servo config
    servoHub.configure(hubConfig, com.revrobotics.ResetMode.kResetSafeParameters);
    servoHub.clearFaults();

    if (servoHub.hasActiveWarning()) {
      System.out.println("Servo Hub " + servoHub.getDeviceId() + " has warnings!");
    } else {
      System.out.println("Servo Hub " + servoHub.getDeviceId() + " has NO warnings!");
    }

    if (servoHub.hasActiveFault()) {
      System.out.println("Servo Hub " + servoHub.getDeviceId() + " has faults!");
    } else {
      System.out.println("Servo Hub " + servoHub.getDeviceId() + " has NO faults!");
    }

    leftHook.setPowered(true);
    rightHook.setPowered(true);
    leftHook.setEnabled(true);
    rightHook.setEnabled(true);

    System.out.println("Left Ch. " + leftHook.getChannelId() + " Enabled: " + leftHook.isEnabled());
    System.out.println("Right Ch. " + leftHook.getChannelId() + " Enabled: " + rightHook.isEnabled());

    // Add commands to Dashboard
    ClimberCommands.add("Climber Stow", this.setClimber(ClimberSP.STOW))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    ClimberCommands.add("Climber Auto", this.setClimber(ClimberSP.LVLAUTON))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    ClimberCommands.add("Climber L1", this.setClimber(ClimberSP.LVL1))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    ClimberCommands.add("Climber L2", this.setClimber(ClimberSP.LVL2))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    ClimberCommands.add("Climber L3", this.setClimber(ClimberSP.LVL3))
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));

    ClimberCommands.add("Hooks Stow", this.stowHooks())
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));
    ClimberCommands.add("Hooks Deploy", this.deployHooks())
        .withProperties(Map.of("show_type", false, "maximize_button_space", false));

    // Initialize simulation
    if (Constants.currentMode == Constants.Mode.SIM) {
      // Climber motor simulation (position control)
      climber1Sim = SparkMaxSimulation.createPositionSim(
          climber1,
          DCMotor.getNEO(1),
          ClimberConstants.kClimberGearRatio,
          0.3, // arm length in meters
          ClimberSP.BOT.getValue(), // min position
          ClimberSP.TOP.getValue(), // max position
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

  public Command stowLeftHook() {
    final Timer timer = new Timer();
    return new FunctionalCommand(
        // Initialize
        // Start servo moving
        // Start timer
        () -> {
          this.setHook(leftHook, HookSP.STOW);
          timer.reset();
          timer.start();
        },
        // Execute - do nothing
        () -> {
        },
        // End
        // Stop the servo
        interrupted -> this.setHook(leftHook, HookSP.STOP),
        // Is finished
        // Timer has expired and amps are higher than threshold
        () -> ((this.getChannelAmps(leftHook) >= ClimberConstants.kServoAmpLimit)
            && timer.hasElapsed(ClimberConstants.kServoTimeout)));
  }

  public Command stowRightHook() {
    final Timer timer = new Timer();
    return new FunctionalCommand(
        // Initialize
        // Start servo moving
        // Start timer
        () -> {
          this.setHook(rightHook, HookSP.STOW);
          timer.reset();
          timer.start();
        },
        // Execute - do nothing
        () -> {
        },
        // End
        // Stop the servo
        interrupted -> this.setHook(rightHook, HookSP.STOP),
        // Is finished
        // Timer has expired and amps are higher than threshold
        () -> ((this.getChannelAmps(rightHook) >= ClimberConstants.kServoAmpLimit)
            && timer.hasElapsed(ClimberConstants.kServoTimeout)));
  }

  public Command stowLeftHook1() {
    return Commands.startEnd(
        () -> this.setHook(leftHook, HookSP.STOW),
        () -> this.setHook(leftHook, HookSP.STOP))
        .until(() -> this.getChannelAmps(leftHook) >= ClimberConstants.kServoAmpLimit);
  }

  public Command stowRightHook1() {
    return Commands.startEnd(
        () -> this.setHook(rightHook, HookSP.STOW),
        () -> this.setHook(rightHook, HookSP.STOP))
        .until(() -> this.getChannelAmps(rightHook) >= ClimberConstants.kServoAmpLimit);
  }

  public Command stowHooks() {
    return new ParallelCommandGroup(
        stowLeftHook(),
        stowRightHook());
  }

  public Command deployLeftHook() {
    return Commands.startEnd(
        () -> this.setHook(leftHook, HookSP.DEPLOY),
        () -> this.setHook(leftHook, HookSP.STOP))
        .withTimeout(5.0);
  }

  public Command deployRightHook() {
    return Commands.startEnd(
        () -> this.setHook(rightHook, HookSP.DEPLOY),
        () -> this.setHook(rightHook, HookSP.STOP))
        .withTimeout(5.0);
  }

  public Command deployHooks() {
    return new ParallelCommandGroup(
        deployLeftHook(),
        deployRightHook());
  }

  // ==============================================================
  // Periodic methods
  // ==============================================================
  @Override
  public void periodic() {
    sbClimberOnTgt.setBoolean(onClimberTarget());
    sbClimberSP.setString(getClimberSP().name());
    sbClimberSPPos.setDouble(Library.SBFormat(getClimberSP().getValue()));
    sbHookOnTgt.setBoolean(onHookTarget());
    sbHookSP.setString(getHookSP().name());
    sbHookSPSpd.setDouble(Library.SBFormat(getHookSP().getSpd()));
    sbLeftHookAmp.setDouble(Library.SBFormat(getChannelAmps(leftHook)));
    sbRightHookAmp.setDouble(Library.SBFormat(getChannelAmps(rightHook)));
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
  // Start of Servo Methods

  public void setHookSP(HookSP sp) {
    hookSP = sp;
  }

  public HookSP getHookSP() {
    return hookSP;
  }

  public double getHookSpd() {
    return ((leftHook.getPulseWidth() + rightHook.getPulseWidth()) / 2.0); // Average of the two hooks
  }

  public boolean onHookTarget() {
    return Math.abs(getHookSpd() - getHookSP().getSpd()) < ClimberConstants.kHookTolerance;
  }

  public void setHook(ServoChannel channel, HookSP sp) {
    channel.setPulseWidth(sp.getSpd());
  }

  public double getChannelAmps(ServoChannel channel) {
    return channel.getCurrent();
  }
}
