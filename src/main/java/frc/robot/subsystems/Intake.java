// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

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
    // This method will be called once per scheduler run
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }
}
