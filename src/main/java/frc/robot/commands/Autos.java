// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.


package frc.robot.commands;

import com.ctre.phoenix6.swerve.SwerveRequest;
import com.fasterxml.jackson.databind.util.Named;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.RobotContainer;
import frc.robot.subsystems.Climber;
import frc.robot.subsystems.Feeder;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.CommandSwerveDrivetrain;

public class Autos {
  private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");

  // Define a chooser for autonomous commands
  // private final SendableChooser<Command> chooser = new SendableChooser<>();
  private SendableChooser<Command> autoChooser = null;

  private RobotContainer robotContainer;
  private CommandSwerveDrivetrain drivetrain;
  private Intake intake;
  private Feeder feeder;
  private Shooter shooter;
  private Climber climber;

  // AutonLeave autonLeave;
  // AutonLeaveNScoreL1 autonLeaveNScoreL1;
  // AutonLeaveNScoreL4 autonLeaveNScoreL4;
  private Pose2d startPose;
  private Command resetPose;
  private Command resetOdo;
  private Command autoLeave;

  private Command hooksStow;
  private Command hooksDeploy;

  private Command climberStow;
  private Command climberAuto;

  /** Example static factory for an autonomous command. */
  // public static Command AutonLeave(Chassis chassis, Ladder ladder, Algae algae,
  // Coral coral, Climber climber) {
  // return Commands.sequence(new AutonLeave(chassis, ladder, algae, coral,
  // climber));
  // }

  public Autos(RobotContainer robotContainer, CommandSwerveDrivetrain drivetrain, Intake intake, Feeder feeder, Shooter shooter,
      Climber climber) {

    System.out.println("+++++ Starting Autos Constructor +++++");

    this.robotContainer = robotContainer;
    this.drivetrain = drivetrain;
    this.intake = intake;
    this.feeder = feeder;
    this.shooter = shooter;
    this.climber = climber;

    // this.autonLeave = new AutonLeave(chassis, ladder, algae, coral, climber);
    // this.autonLeaveNScoreL1 = new AutonLeaveNScoreL1(robotContainer, chassis,
    // ladder, algae, coral, climber);
    // this.autonLeaveNScoreL4 = new AutonLeaveNScoreL4(robotContainer, chassis,
    // ladder, algae, coral, climber);

    this.startPose = new Pose2d(new Translation2d(7.3, 4.0), Rotation2d.fromDegrees(180));
    // this.resetPose = new InstantCommand(() -> drive.resetPose(startPose));
    // this.resetOdo = new InstantCommand(() -> drive.resetOdometry(startPose));

    // this.autoLeave = new ChassisDriveDist(chassis, -0.5, 1.0);

    // ********************************************
    // Generate Auto commands
    // Note: Named commands used in Auto command must be defined
    // before defining the Auto command
    // NamedCommands.registerCommand("resetPose", resetPose);
    // NamedCommands.registerCommand("resetOdo", resetOdo);
    NamedCommands.registerCommand("HookDeploy", climber.deployHooks());
    NamedCommands.registerCommand("HookStow", climber.stowHooks());
    NamedCommands.registerCommand("ClimberAuto", climber.setClimber(Climber.ClimberSP.LVLAUTON));
    NamedCommands.registerCommand("ClimberL1", climber.setClimber(Climber.ClimberSP.LVL1));
    NamedCommands.registerCommand("ClimberL2", climber.setClimber(Climber.ClimberSP.LVL2));
    NamedCommands.registerCommand("ClimberL3", climber.setClimber(Climber.ClimberSP.LVL3));
    NamedCommands.registerCommand("ClimberStow", climber.setClimber(Climber.ClimberSP.STOW));

    NamedCommands.registerCommand("ShooterOff", shooter.setShooter(Shooter.ShooterSP.OFF));
    NamedCommands.registerCommand("ShooterLow", shooter.setShooter(Shooter.ShooterSP.LOW));
    NamedCommands.registerCommand("ShooterMed", shooter.setShooter(Shooter.ShooterSP.MED));
    NamedCommands.registerCommand("ShooterHi", shooter.setShooter(Shooter.ShooterSP.HI));
    NamedCommands.registerCommand("TiltLow", shooter.setTilt(Shooter.TiltSP.LOW));
    NamedCommands.registerCommand("TiltMed", shooter.setTilt(Shooter.TiltSP.MED));
    NamedCommands.registerCommand("TiltHi", shooter.setTilt(Shooter.TiltSP.HI));

    NamedCommands.registerCommand("IntakeOff", intake.setIntake(Intake.IntakeSP.OFF));
    NamedCommands.registerCommand("IntakeLow", intake.setIntake(Intake.IntakeSP.LOW));
    NamedCommands.registerCommand("IntakeMed", intake.setIntake(Intake.IntakeSP.MED));
    NamedCommands.registerCommand("IntakeHi", intake.setIntake(Intake.IntakeSP.HI));
    NamedCommands.registerCommand("TiltStow", intake.setTilt(Intake.TiltSP.STOW));
    NamedCommands.registerCommand("TiltDeploy", intake.setTilt(Intake.TiltSP.DEPLOY));

    NamedCommands.registerCommand("FeederOff", feeder.setFeeder(Feeder.FeederSP.OFF));
    NamedCommands.registerCommand("FeederLow", feeder.setFeeder(Feeder.FeederSP.LOW));
    NamedCommands.registerCommand("FeederMed", feeder.setFeeder(Feeder.FeederSP.MED));
    NamedCommands.registerCommand("FeederHi", feeder.setFeeder(Feeder.FeederSP.HI));

    // ********************************************
    // Initialize auto command chooser with auton commands
    autoChooser = AutoBuilder.buildAutoChooser("BigKahuna");
    autoChooser.addOption("AUTOLEAVE", autoLeave);

    // ********************************************
    // Add Auton Command chooser to Shuffleboard
    compTab.add("Auto Command", autoChooser)
        .withWidget("ComboBox Chooser")
        .withPosition(0, 0)
        .withSize(3, 1);
        
    String temp = AutoBuilder.isConfigured() ? "IS" : "IS NOT";
    DriverStation.reportWarning("AutoBuilder " + temp + " configured", false);
    temp = AutoBuilder.isPathfindingConfigured() ? "IS" : "IS NOT";
    DriverStation.reportWarning("AutoBuilder Pathfinding " + temp + " configured", false);

    System.out.println("----- Ending Autos Constructor -----");
  }

  // public Command getAutonomousCommand() {
	// 	// Simple drive forward auton
	// 	final var idle = new SwerveRequest.Idle();
	// 	return Commands.sequence(
	// 			// Reset our field centric heading to match the robot
	// 			// facing away from our alliance station wall (0 deg).
	// 			drivetrain.runOnce(() -> drivetrain.seedFieldCentric(Rotation2d.kZero)),
	// 			// Then slowly drive forward (away from us) for 5 seconds.
	// 			drivetrain.applyRequest(() -> drive.withVelocityX(0.5)
	// 					.withVelocityY(0)
	// 					.withRotationalRate(0))
	// 					.withTimeout(5.0),
	// 			// Finally idle for the rest of auton
	// 			drivetrain.applyRequest(() -> idle));
	// }

  public SendableChooser<Command> getAutoChooser() {
    return autoChooser;
  }
}