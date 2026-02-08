// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.util.Map;
import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class Shooter extends SubsystemBase {
  /** Creates a new ExampleSubsystem. */
	private final SparkFlex leftShooter = new SparkFlex(
			Constants.CANId.kShooterLeftCanId, MotorType.kBrushless);
	private final SparkFlex rightShooter = new SparkFlex(
			Constants.CANId.kShooterRightCanId, MotorType.kBrushless);
private final SparkMax tilt = new SparkMax(
			Constants.CANId.kShooterTiltCanId, MotorType.kBrushless);

	private final SparkMaxConfig leftConfig = new SparkMaxConfig();
	private final SparkMaxConfig rightConfig = new SparkMaxConfig();
  private final SparkMaxConfig tiltConfig = new SparkMaxConfig();

	private SparkClosedLoopController leftController = leftShooter.getClosedLoopController();
	private SparkClosedLoopController rightController = rightShooter.getClosedLoopController();
	private SparkClosedLoopController tiltController = tilt.getClosedLoopController();

	private AbsoluteEncoder leftEncoder = leftShooter.getAbsoluteEncoder();
	private AbsoluteEncoder rightEncoder = rightShooter.getAbsoluteEncoder();
  private AbsoluteEncoder tiltEncoder = tilt.getAbsoluteEncoder();

  public Shooter() {
		System.out.println("+++++ Starting Shooter Constructor +++++");

		// Configure Left Shooter motor
		leftConfig
				.inverted(Constants.Shooter.kLeftMotorInverted)
				.idleMode(Constants.Shooter.kLeftIdleMode)
				.smartCurrentLimit(Constants.Shooter.kLeftCurrentLimit);
		leftConfig.absoluteEncoder
				.zeroOffset(Constants.Shooter.kLeftZeroOffset)
				.zeroCentered(Constants.Shooter.kLeftZeroCentered)
				.inverted(Constants.Shooter.kLeftEncoderInverted)
				.positionConversionFactor(Constants.Shooter.kTiltPositionFactor)
				.velocityConversionFactor(Constants.Shooter.kTiltVelocityFactor);
		leftConfig.closedLoop
				.feedbackSensor(FeedbackSensor.kAbsoluteEncoder)
				.p(Constants.Shooter.kPosP)
				.i(Constants.Shooter.kPosI)
				.d(Constants.Shooter.kPosD)
				.outputRange(Constants.Shooter.kPosMinOutput, Constants.Shooter.kPosMaxOutput)
				.positionWrappingEnabled(Constants.Shooter.kLeftEncodeWrapping);
		//leftConfig.closedLoop.maxMotion
		// .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
		// .maxVelocity(Constants.Climber.kPosMaxVel)
		// .maxAcceleration(Constants.Climber.kPosMaxAccel)
		// .allowedClosedLoopError(Constants.Shooter.kPosAllowedErr);

		leftShooter.configure(leftConfig,
				com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

		// Configure Right Intake motor
		rightConfig
				// .follow(leftClimber, true)
				.inverted(Constants.Shooter.kRightMotorInverted)
				.idleMode(Constants.Shooter.kRightIdleMode)
				.smartCurrentLimit(Constants.Shooter.kRightCurrentLimit);
		rightConfig.absoluteEncoder
				.zeroOffset(Constants.Shooter.kRightZeroOffset)
				.zeroCentered(Constants.Shooter.kRightZeroCentered)
				.inverted(Constants.Shooter.kRightEncoderInverted)
				.positionConversionFactor(Constants.Shooter.kTiltPositionFactor)
				.velocityConversionFactor(Constants.Shooter.kTiltVelocityFactor);
		rightConfig.closedLoop
				.feedbackSensor(FeedbackSensor.kAbsoluteEncoder)
				.p(Constants.Shooter.kPosP)
				.i(Constants.Shooter.kPosI)
				.d(Constants.Shooter.kPosD)
				.outputRange(Constants.Shooter.kPosMinOutput, Constants.Shooter.kPosMaxOutput)
				.positionWrappingEnabled(Constants.Shooter.kRightEncodeWrapping);
		// rightConfig.closedLoop.maxMotion
		// .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
		// .maxVelocity(Constants.Shooter.kPosMaxVel)
		// .maxAcceleration(Constants.Shooter.kPosMaxAccel)
		// .allowedClosedLoopError(Constants.Shooter.kPosAllowedErr);

		rightShooter.configure(rightConfig,
				com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);


    tiltConfig
				.inverted(Constants.Shooter.ktiltMotorInverted)
				.idleMode(Constants.Shooter.ktiltIdleMode)
				.smartCurrentLimit(Constants.Shooter.ktiltCurrentLimit);
		tiltConfig.absoluteEncoder
				.zeroOffset(Constants.Shooter.ktiltZeroOffset)
				.zeroCentered(Constants.Shooter.ktiltZeroCentered)
				.inverted(Constants.Shooter.ktiltEncoderInverted)
				.positionConversionFactor(Constants.Shooter.kTiltPositionFactor)
				.velocityConversionFactor(Constants.Shooter.kTiltVelocityFactor);
		tiltConfig.closedLoop
				.feedbackSensor(FeedbackSensor.kAbsoluteEncoder)
				.p(Constants.Shooter.kPosP)
				.i(Constants.Shooter.kPosI)
				.d(Constants.Shooter.kPosD)
				.outputRange(Constants.Shooter.kPosMinOutput, Constants.Shooter.kPosMaxOutput)
				.positionWrappingEnabled(Constants.Shooter.kLeftEncodeWrapping);

        tilt.configure(tiltConfig,
				com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);


		/*ShooterCommands.add("shooter", this.shooter)
				.withProperties(Map.of("show type", false));
		ShooterCommands.add("Ready", this.ready)
				.withProperties(Map.of("show type", false));
		ShooterCommands.add("Zero", this.zero)
				.withProperties(Map.of("show type", false));
		// ShooterCommands.add("Stage", this.stage)
		// .withProperties(Map.of("show type", false));
		ShooterCommands.add("Stow", this.stow)
				.withProperties(Map.of("show type", false));
*/
		// Initialize intake start positions
  
		//setClimberPos(climberSP);

		System.out.println("----- Ending Shooter Constructor -----");
	
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
