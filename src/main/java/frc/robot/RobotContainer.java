// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;

import frc.robot.Constants.OIConstants;
//import frc.robot.Constants.OperatorConstants;
import frc.robot.commands.Autos;
import frc.robot.subsystems.Chassis;
import frc.robot.subsystems.Climber;
import frc.robot.subsystems.Feeder;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Vision.VisionIOPhotonVision;
import frc.robot.subsystems.Vision.VisionConstants;
import frc.robot.subsystems.Vision.Vision;
import java.util.Map;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in
 * the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of
 * the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {

	// The robot's subsystems and commands are defined here...
	private final Chassis chassis = new Chassis();
	private final Intake intake = new Intake();
	private final Feeder feeder = new Feeder();
	private final Shooter shooter = new Shooter();
	private final Climber climber = new Climber();
	@SuppressWarnings("unused")
	private final Vision vision = new Vision(
			chassis::addVisionMeasurement,
			new VisionIOPhotonVision(VisionConstants.camera0Name,
					VisionConstants.robotToCamera0),
			new VisionIOPhotonVision(VisionConstants.camera1Name,
					VisionConstants.robotToCamera1),
			new VisionIOPhotonVision(VisionConstants.camera2Name,
					VisionConstants.robotToCamera2),
			new VisionIOPhotonVision(VisionConstants.camera3Name,
					VisionConstants.robotToCamera3));

	// Define HIDs
	private final CommandXboxController m_driverController = new CommandXboxController(
			OIConstants.kDriverControllerPort);
	private final CommandXboxController m_operatorController = new CommandXboxController(
			OIConstants.kOperatorControllerPort);
	private final GenericHID m_operatorHID = new GenericHID(
			OIConstants.kOperatorControllerPort);

	
	private final Autos auton = new Autos(this, chassis, intake, feeder, shooter, climber);

	private final ShuffleboardTab cmdTab = Shuffleboard.getTab("Commands");

	/**
	 * The container for the robot. Contains subsystems, OI devices, and commands.
	 */
	public RobotContainer() {

		// Configure the trigger bindings
		configureBindings();

		// Configure default commands
		chassis.setDefaultCommand(
				// The left stick controls translation of the robot.
				// Turning is controlled by the X axis of the right stick.
				new RunCommand(
						() -> chassis.drive(
								-MathUtil.applyDeadband(m_driverController.getLeftY()
										* chassis.spdMultiplier, OIConstants.kDriveDeadband),
								-MathUtil.applyDeadband(m_driverController.getLeftX()
										* chassis.spdMultiplier, OIConstants.kDriveDeadband),
								-MathUtil.applyDeadband(m_driverController.getRightX()
										* chassis.spdMultiplier, OIConstants.kDriveDeadband),
								true),
						chassis));
	}

	/**
	 * Use this method to define your trigger->command mappings. Triggers can be
	 * created via the
	 * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with
	 * an arbitrary predicate, or via the named factories in {@link
	 * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for
	 * {@link
	 * CommandXboxController
	 * Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
	 * PS4} controllers or
	 * {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
	 * joysticks}.
	 */


	/*********************************/

	// CONTROLLER BINDINGS

	/*********************************/

	private void configureBindings() {

		// m_driverController.leftBumper()
		// 		.onFalse(new InstantCommand(() -> chassis.setSpdHigh()))
		// 		.onTrue(new InstantCommand(() -> chassis.setSpdLow()));

		// m_driverController.rightBumper()
		// 		.onFalse(new InstantCommand(() -> chassis.setPoseErr()))
		// 		.onTrue(new InstantCommand(() -> chassis.setPoseZero()));

		// m_operatorController.y().onTrue(this.goL4);
		// m_operatorController.x().onTrue(this.goL3);
		// m_operatorController.b().onTrue(this.goL2);
		// m_operatorController.a().onTrue(algae.intake);

		// new POVButton(m_operatorHID, 0).onTrue(this.goBarge);
		// new POVButton(m_operatorHID, 90).onTrue(this.goStation);
		// new POVButton(m_operatorHID, 270).onTrue(this.goProcessor);
		// new POVButton(m_operatorHID, 180).onTrue(this.goFloor);

		// // m_operatorController.start().onTrue(climber.ready);
		// m_operatorController.back().onTrue(this.goStow);
		// m_operatorController.start().onTrue(climber.climb);

		// m_operatorController.leftBumper().onTrue(this.goL35);
		// m_operatorController.rightBumper().onTrue(this.coral.eject); // was doAction 10:35 AM

		// m_operatorController.rightStick().onTrue(algae.eject);
		// m_operatorController.leftStick().onTrue(coral.intake);

	}

	public RobotContainer getRobotContainer() {
		return this;
	}

	/**
	 * Use this to pass the autonomous command to the main {@link Robot} class.
	 *
	 * @return the command to run in autonomous
	 */
	public Command getAutonomousCommand() {
		// return new ChassisTimedDrive(chassis, 0.25, 1.0);
		
		return auton.getAutoChooser().getSelected();
	}
}
