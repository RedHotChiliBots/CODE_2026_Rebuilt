// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.servohub.ServoChannel;
import com.revrobotics.servohub.ServoChannel.ChannelId;
import com.revrobotics.servohub.ServoHub;
import com.revrobotics.servohub.config.ServoChannelConfig;
import com.revrobotics.servohub.config.ServoHubConfig;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.PWMId;
import frc.robot.utils.Library;

//CLASS DEFINITION
public class Climber extends SubsystemBase {

  //Define Motors, Servos, Etc
  private final SparkMax climber1 = new SparkMax(
    Constants.CANId.kClimber1CanId, MotorType.kBrushless);

  private final SparkMax climber2 = new SparkMax(
    Constants.CANId.kClimber2CanId, MotorType.kBrushless);

  private final SparkMax climber3 = new SparkMax(
    Constants.CANId.kClimber3CanId, MotorType.kBrushless);

  private final SparkMax climber4 = new SparkMax(
    Constants.CANId.kClimber4CanId, MotorType.kBrushless);

  private final SparkMaxConfig climber1Config = new SparkMaxConfig();
  private final SparkMaxConfig climber2Config = new SparkMaxConfig();
  private final SparkMaxConfig climber3Config = new SparkMaxConfig();
  private final SparkMaxConfig climber4Config = new SparkMaxConfig();
  
  private final SparkClosedLoopController climber1Controller = climber1.getClosedLoopController();
  private final SparkClosedLoopController climber2Controller = climber2.getClosedLoopController();
  private final SparkClosedLoopController climber3Controller = climber3.getClosedLoopController();
  private final SparkClosedLoopController climber4Controller = climber4.getClosedLoopController();

  private final AbsoluteEncoder climber1AbsEncoder = climber1.getAbsoluteEncoder();
  private final AbsoluteEncoder climber2AbsEncoder = climber2.getAbsoluteEncoder();
  private final AbsoluteEncoder climber3AbsEncoder = climber3.getAbsoluteEncoder();
  private final AbsoluteEncoder climber4AbsEncoder = climber4.getAbsoluteEncoder();

  // private final Servo hook1 = new Servo(PWMId.kClimberHook1PWMId);
  // private final Servo hook2 = new Servo(PWMId.kClimberHook2PWMId);
  
  // private final ServoHub servoHub = new ServoHub(Constants.CANId.kClimberServoHubCanId);
  // private final ServoHubConfig servoHubConfig = new ServoHubConfig();

  // ServoChannel channel0 = servoHub.getServoChannel(ChannelId.kChannelId0);
  // ServoChannel channel1 = servoHub.getServoChannel(ChannelId.kChannelId1);

  
  public enum ClimberSP { //Climber Setpoints
    ZERO(0), //NUMBERS NEED TO CHANGE
    STOW(1), //NUMBERS NEED TO CHANGE
    READY(2), //NUMBERS NEED TO CHANGE
    CLIMB(3); //NUMBERS NEED TO CHANGE

    private final double sp;

		ClimberSP(final double sp) {
			this.sp = sp;
		}

		public double getValue() {
			return sp;
		}
  }

  // public enum ServoSP { //Servo Setpoints
  //   ZERO(0), //NUMBERS NEED TO CHANGE
  //   STOW(0), //NUMBERS NEED TO CHANGE 
  //   DEPLOY(1); //NUMBERS NEED TO CHANGE

  //   private final double sp;

	// 	ServoSP(final double sp) {
	// 		this.sp = sp;
	// 	}

	// 	public double getValue() {
	// 		return sp;
	// 	}
  // }

  private ClimberSP climberSP = Climber.ClimberSP.STOW;
	private Library lib = new Library();


  //CONSTRUCTOR
  public Climber() {
    System.out.println("+++++ Starting Climber Constructor +++++");

    // Climbing Motor Configs 1-4
    climber1Config
    .inverted(Constants.Climber.kClimberInverted)
    .idleMode(Constants.Climber.kClimberIdleMode)
    .smartCurrentLimit(Constants.Climber.kClimberCurrentLimit);
    climber1Config.absoluteEncoder
    .zeroOffset(Constants.Climber.kZeroOffset)
    .zeroCentered(Constants.Climber.kZeroCentered)
    .inverted(Constants.Climber.kEncoderInverted)
    .positionConversionFactor(Constants.Climber.kTiltPositionFactor)
    .velocityConversionFactor(Constants.Climber.kTiltVelocityFactor);
    climber1Config.closedLoop
    .feedbackSensor(FeedbackSensor.kAbsoluteEncoder)
    .p(Constants.Climber.kPosP)
    .i(Constants.Climber.kPosI)
    .d(Constants.Climber.kPosD)
    .outputRange(Constants.Climber.kPosMinOutput, Constants.Climber.kPosMaxOutput);
    
    climber2Config.follow(Constants.CANId.kClimber1CanId); //Mimics climber1
    climber3Config.follow(Constants.CANId.kClimber1CanId);
    climber4Config.follow(Constants.CANId.kClimber1CanId);

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

    // servoHubConfig
    // .channel0.pulseRange(500, 1500, 2500)
    // .disableBehavior(ServoChannelConfig.BehaviorWhenDisabled.kSupplyPower); //Default is 0-180, but can be changed if needed
    // servoHub.configure(
    //   servoHubConfig, 
    //   com.revrobotics.ResetMode.kResetSafeParameters);

    // Set the pulse period for channels 0-2 to 5ms (5000 microseconds)
    //servoHub.setBankPulsePeriod(ServoHub.Bank.kBank0_2, 5000);
    

    System.out.println("+++++ End of Climber Constructor +++++");
    }


 //COMMANDS
  // public Command exampleMethodCommand() {
  //   // Inline construction of command goes here.
  //   // Subsystem::RunOnce implicitly requires `this` subsystem.
  //   return runOnce(
  //       () -> {
  //         /* one-time action goes here */
  //       });
  // }

  //METHODS
  //Start of Climber Methods
  public double getClimberPos() {
   return climber1AbsEncoder.getPosition(); //All the other motors should match
  }

  public double getClimberVel() {
    return climber1AbsEncoder.getVelocity(); //All the other motors should match
  }
  
  public void setClimberPos(ClimberSP pos) {
    setClimberSP(pos);
    climber1Controller.setSetpoint(pos.getValue(), 
      SparkBase.ControlType.kPosition);
  }

  public void setClimberPos() {
    setClimberPos(getClimberSP()); 
    //Sets the desired position using the current setpoint,
    // which is updated by the other setClimberPos method.
  }

  public void setClimberSP(ClimberSP sp) {
    climberSP = sp;
  }

  public ClimberSP getClimberSP() {
    return climberSP;
  }

  public boolean onTarget() {
		return Math.abs(getClimberPos() - getClimberSP().getValue()) < Constants.Climber.kTollerance ||
				Math.abs(getClimberPos() - getClimberSP().getValue()) < Constants.Climber.kTollerance;
	}
  //End of Climber Methods
  //Start of Servo Methods

  



  //PERIOIDC
  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }
}
