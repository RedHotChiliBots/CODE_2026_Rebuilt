// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.AbsoluteEncoder;
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

public class Intake extends SubsystemBase {

    // ==============================================================
    // Define Intake & Tilt Motors
    private final SparkMax intake = new SparkMax(
        Constants.CANId.kIntakeIntakeCanId, MotorType.kBrushless);
    private final SparkMax tilt = new SparkMax(
        Constants.CANId.kIntakeTiltCanId, MotorType.kBrushless);
  
    private final SparkMaxConfig intakeConfig = new SparkMaxConfig();
    private final SparkMaxConfig tiltConfig = new SparkMaxConfig();
  
    private SparkClosedLoopController intakeController = intake.getClosedLoopController();
    private SparkClosedLoopController tiltController = tilt.getClosedLoopController();

      // The Feeder SP is stored as a percentage of RPMs
  private enum IntakeSP {
    OFF(0.0),
    LOW(25.0),
    MED(50.0),
    HI(75.0);
    
    private double vel;

    IntakeSP (double vel) {
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
private enum TiltSP {
    OFF(0.0),
    LOW(25.0),
    MED(50.0),
    HI(75.0);

    private double vel;

    TiltSP (double vel) {
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


  private IntakeSP intakeSP = IntakeSP.OFF;

   /**************************************************************
   * Initialize Shuffleboard entries
   **************************************************************/
  // private final ShuffleboardTab cmdTab = Shuffleboard.getTab("Commands");
  // private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");
  private final ShuffleboardTab intakeTab = Shuffleboard.getTab("Intake");
private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");

	private final GenericEntry sbTiltOnTgt = compTab.addPersistent("Intake Tilt OnTgt", false)
			.withWidget("Boolean Box").withPosition(11, 1).withSize(2, 1).getEntry();
	private final GenericEntry sbTxtTiltSP = compTab.addPersistent("Intake Tilt SP", "")
			.withWidget("Text View").withPosition(11, 2).withSize(2, 1).getEntry();
	private final GenericEntry sbDblTiltSP = compTab.addPersistent("Intake Tilt SP (deg)", 0)
			.withWidget("Text View").withPosition(11, 3).withSize(2, 1).getEntry();
	private final GenericEntry sbTiltPos = compTab.addPersistent("Intake Tilt Pos", 0)
			.withWidget("Text View").withPosition(11, 4).withSize(2, 2).getEntry();

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

    private RelativeEncoder intakeEncoder = intake.getEncoder();
    private AbsoluteEncoder tiltEncoder = tilt.getAbsoluteEncoder();
  
    // ==============================================================
    // Constructor
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
          
      System.out.println("----- Ending Intake Constructor -----");
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
      sbIntakeOnTgt.setBoolean(onIntakeTarget());
    sbIntakeSP.setString(getIntakeSP().name());
    sbIntakeSPPct.setDouble(getIntakeSP(false));
    sbIntakeSPRPM.setDouble(getIntakeSP(true));
    sbIntakeVelPct.setDouble(getIntakeVel(false));
    sbIntakeVelRPM.setDouble(getIntakeVel(true));
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }

  public void setIntakeSP(IntakeSP sp) {
    intakeSP = sp;
  }

  public IntakeSP getIntakeSP() {
    return intakeSP;
  }

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

  public double getTiltSP() {
    return tiltController.getSetpoint();
  }

  public boolean onTiltTarget() {
		return Math.abs(getTiltPos() - getTiltSP()) < Constants.Intake.kTiltTollerance;
	}

}
