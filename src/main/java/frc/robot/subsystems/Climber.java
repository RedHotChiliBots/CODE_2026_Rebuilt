// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;


import com.revrobotics.spark.config.SparkMaxConfig;

import java.util.Map;

import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkLimitSwitch;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.shuffleboard.SimpleWidget;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.robot.Constants;
import frc.robot.Constants.PWMId;
import frc.robot.utils.Library;

public class Climber extends SubsystemBase {

  //Define Motors, Servos, Etc
  private final SparkFlex climber1 = new SparkFlex(
    Constants.CANId.kClimber1CanId, MotorType.kBrushless);

  private final SparkFlex climber2 = new SparkFlex(
    Constants.CANId.kClimber2CanId, MotorType.kBrushless);

  private final SparkFlex climber3 = new SparkFlex(
    Constants.CANId.kClimber3CanId, MotorType.kBrushless);

  private final SparkFlex climber4 = new SparkFlex(
    Constants.CANId.kClimber4CanId, MotorType.kBrushless);

  private final SparkMaxConfig climber1Config = new SparkMaxConfig();
  private final SparkMaxConfig climber2Config = new SparkMaxConfig();
  private final SparkMaxConfig climber3Config = new SparkMaxConfig();
  private final SparkMaxConfig climber4Config = new SparkMaxConfig();
  
  private final SparkClosedLoopController climber1Controller = climber1.getClosedLoopController();
  private final SparkClosedLoopController climber2Controller = climber2.getClosedLoopController();
  private final SparkClosedLoopController climber3Controller = climber3.getClosedLoopController();
  private final SparkClosedLoopController climber4Controller = climber4.getClosedLoopController();

  private final AbsoluteEncoder climber1Encoder = climber1.getAbsoluteEncoder();
  private final AbsoluteEncoder climber2Encoder = climber2.getAbsoluteEncoder();
  private final AbsoluteEncoder climber3Encoder = climber3.getAbsoluteEncoder();
  private final AbsoluteEncoder climber4Encoder = climber4.getAbsoluteEncoder();

  private final Servo hook1 = new Servo(PWMId.kClimberHook1PWMId);
  private final Servo hook2 = new Servo(PWMId.kClimberHook2PWMId);


  public enum ClimberSP {
    STOW(0), //NUMBERS NEED TO CHANGE
    READY(1), //NUMBERS NEED TO CHANGE
    ZERO(2), //NUMBERS NEED TO CHANGE
    CLIMB(3); //NUMBERS NEED TO CHANGE

    private final double sp;

		ClimberSP(final double sp) {
			this.sp = sp;
		}

		public double getValue() {
			return sp;
		}
  }

  private enum PinSP {
		OPEN(-0.5), // degrees - up and out of way NUMBERS NEED TO CHANGE
		CLOSE(0.5); // degrees - full climb NUMBERS NEED TO CHANGE

		private final double sp;

		PinSP(final double sp) {
			this.sp = sp;
		}

		public double getValue() {
			return sp;
		}

  private ClimberSP climberSP = Climber.ClimberSP.STOW;
	private Library lib = new Library();
}
  public Climber() {
    
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
