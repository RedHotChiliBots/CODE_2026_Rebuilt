// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class Feeder extends SubsystemBase {
  /** Creates a new ExampleSubsystem. */

  // ==============================================================
  // Define Feeder Motor
    private final SparkMax feeder = new SparkMax(
      Constants.CANId.kFeederCanId, MotorType.kBrushless);
    
    private final SparkMaxConfig feederConfig = new SparkMaxConfig();
    
  private SparkClosedLoopController feederController = feeder.getClosedLoopController();

  private RelativeEncoder feederEncoder = feeder.getEncoder();

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
   * An example method querying a boolean state of the subsystem (for example, a digital sensor).
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
