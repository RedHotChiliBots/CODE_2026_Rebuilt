// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;
import java.util.Map;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.studica.frc.AHRS;
import com.studica.frc.AHRS.NavXComType;

import edu.wpi.first.hal.FRCNetComm.tInstances;
import edu.wpi.first.hal.FRCNetComm.tResourceType;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructPublisher;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import frc.robot.Constants.CANId;
import frc.robot.Constants.ChassisConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.utils.SwerveUtils;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Chassis extends SubsystemBase {

	// Create MAXSwerveModules
	private final MAXSwerveModule m_rearRight = new MAXSwerveModule(
			frc.robot.Constants.CANId.kRearRightDrivingCanId,
			frc.robot.Constants.CANId.kRearRightTurningCanId,
			DriveConstants.kBackRightChassisAngularOffset);

	private final MAXSwerveModule m_rearLeft = new MAXSwerveModule(
			frc.robot.Constants.CANId.kRearLeftDrivingCanId,
			frc.robot.Constants.CANId.kRearLeftTurningCanId,
			DriveConstants.kBackLeftChassisAngularOffset);

	private final MAXSwerveModule m_frontLeft = new MAXSwerveModule(
			frc.robot.Constants.CANId.kFrontLeftDrivingCanId,
			frc.robot.Constants.CANId.kFrontLeftTurningCanId,
			DriveConstants.kFrontLeftChassisAngularOffset);

	private final MAXSwerveModule m_frontRight = new MAXSwerveModule(
			frc.robot.Constants.CANId.kFrontRightDrivingCanId,
			frc.robot.Constants.CANId.kFrontRightTurningCanId,
			frc.robot.Constants.DriveConstants.kFrontRightChassisAngularOffset);

	// ==============================================================
	// Initialize NavX AHRS board
	// Alternatively: I2C.Port.kMXP, SerialPort.Port.kMXP or SerialPort.Port.kUSB
	private AHRS m_ahrs = new AHRS(NavXComType.kMXP_SPI, (byte) 100);
	private PowerDistribution m_pdh = new PowerDistribution(frc.robot.Constants.CANId.kPDHCanID, ModuleType.kRev);

	SwerveDrivePoseEstimator poseEstimator = null;
	SwerveDriveOdometry m_odometry = null;

	/**************************************************************
	 * Initialize Shuffleboard entries
	 **************************************************************/
	// private final ShuffleboardTab chassisTab = Shuffleboard.getTab("Chassis");
	private final ShuffleboardTab cmdTab = Shuffleboard.getTab("Commands");
	private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");

	// private final GenericEntry sbAngle = chassisTab.addPersistent("Angle", 0)
	// .withWidget("Text View").withPosition(3, 0).withSize(2, 1).getEntry();
	// private final GenericEntry sbFusedHeading =
	// chassisTab.addPersistent("FusedHeading", 0)
	// .withWidget("Text View").withPosition(3, 1).withSize(2, 1).getEntry();
	// private final GenericEntry sbCompassHeading =
	// chassisTab.addPersistent("CompassHeading", 0)
	// .withWidget("Text View").withPosition(3, 2).withSize(2, 1).getEntry();
	// private final GenericEntry sbRotDegree =
	// chassisTab.addPersistent("Rotation2d", 0)
	// .withWidget("Text View").withPosition(3, 3).withSize(2, 1).getEntry();
	// private final GenericEntry sbPitch = chassisTab.addPersistent("Pitch", 0)
	// .withWidget("Text View").withPosition(5, 0).withSize(2, 1).getEntry();
	// private final GenericEntry sbRoll = chassisTab.addPersistent("Roll", 0)
	// .withWidget("Text View").withPosition(5, 1).withSize(2, 1).getEntry();
	// private final GenericEntry sbYaw = chassisTab.addPersistent("Yaw", 0)
	// .withWidget("Text View").withPosition(5, 2).withSize(2, 1).getEntry();

	// private final GenericEntry sbXVel = chassisTab.addPersistent("X Vel", 0)
	// .withWidget("Text View").withPosition(5, 2).withSize(2, 1).getEntry();
	// private final GenericEntry sbYVel = chassisTab.addPersistent("Y Vel", 0)
	// .withWidget("Text View").withPosition(5, 2).withSize(2, 1).getEntry();
	// private final GenericEntry sbRVel = chassisTab.addPersistent("R Vel", 0)
	// .withWidget("Text View").withPosition(5, 2).withSize(2, 1).getEntry();

	private final StructPublisher<Pose2d> origPose = NetworkTableInstance.getDefault()
			.getStructTopic("OrigPose", Pose2d.struct).publish();
	private final StructPublisher<Pose2d> currPose = NetworkTableInstance.getDefault()
			.getStructTopic("CurrPose", Pose2d.struct).publish();

	private final ShuffleboardLayout chassisCommands = cmdTab
			.getLayout("Chassis", BuiltInLayouts.kList)
			.withSize(3, 3)
			.withPosition(13, 11)
			.withProperties(Map.of("Label position", "Hidden"));

	// private final ShuffleboardLayout chassisData = compTab
	// .getLayout("Chassis", BuiltInLayouts.kList)
	// .withSize(2, 5)
	// .withPosition(12, 10)
	// .withProperties(Map.of("Label position", "Top"));

	private double pitchOffset = 0.0;
	private double rollOffset = 0.0;

	/**************************************************************
	 * Constructor
	 **************************************************************/
	public Chassis() {
		System.out.println("+++++ Starting Chassis Constructor +++++");
		compTab.add("Chassis Current", this)
				.withWidget("Subsystem")
				.withPosition(22, 2)
				.withSize(4, 2);

		// Usage reporting for MAXSwerve template
		HAL.report(tResourceType.kResourceType_RobotDrive, tInstances.kRobotDriveSwerve_MaxSwerve);

		// ==============================================================
		// Initialize Rev PDH
		m_pdh.clearStickyFaults();

		// ==============================================================
		// Initialize NavX AHRS board
		// Alternatively: I2C.Port.kMXP, SerialPort.Port.kMXP or SerialPort.Port.kUSB
		try {
			// m_ahrs = new AHRS(SPI.Port.kMXP, (byte) 100); // 100 Hz
			m_ahrs.enableBoardlevelYawReset(true);
		} catch (RuntimeException ex) {
			DriverStation.reportError("Error instantiating navX-MXP:  " + ex.getMessage(), true);
		}

		// // Odometry class for tracking robot pose
		// m_odometry = new SwerveDriveOdometry(
		// DriveConstants.kDriveKinematics,
		// getRotation2d(),
		// new SwerveModulePosition[] {
		// m_frontLeft.getPosition(),
		// m_frontRight.getPosition(),
		// m_rearLeft.getPosition(),
		// m_rearRight.getPosition()
		// });

		// Load the RobotConfig from the GUI settings. You should probably
		// store this in your Constants file
		RobotConfig config = null;
		try {
			config = RobotConfig.fromGUISettings();
		} catch (Exception e) {
			// Handle exception as needed
			e.printStackTrace();
		}

		// Configure AutoBuilder last
		AutoBuilder.configure(
				this::getPose, // Robot pose supplier
				this::resetPose, // Method to reset odometry (will be called if your auto has a starting pose)
				this::getRobotRelativeSpeeds, // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE
				(speeds, feedforwards) -> driveRobotRelative(speeds), // Method that will drive the robot given ROBOT
																						// RELATIVE ChassisSpeeds. Also optionally outputs
																						// individual module feedforwards
				new PPHolonomicDriveController( // PPHolonomicController is the built in path following controller for
															// holonomic drive trains
						new PIDConstants(5.0, 0.0, 0.0), // Translation PID constants
						new PIDConstants(5.0, 0.0, 0.0) // Rotation PID constants
				),
				config, // The robot configuration
				() -> {
					// Boolean supplier that controls when the path will be mirrored for the red
					// alliance
					// This will flip the path being followed to the red side of the field.
					// THE ORIGIN WILL REMAIN ON THE BLUE SIDE

					var alliance = DriverStation.getAlliance();
					if (alliance.isPresent()) {
						return alliance.get() == DriverStation.Alliance.Red;
					}
					return false;
				},
				this // Reference to this subsystem to set requirements
		);

		// The robot pose estimator for tracking swerve odometry and applying vision
		// data
		poseEstimator = new SwerveDrivePoseEstimator(ChassisConstants.kDriveKinematics, getRotation2d(),
				getModulePositions(), new Pose2d(),
				// more on n1 and n2 = less trust in source
				VecBuilder.fill(0.05, 0.05, Units.degreesToRadians(5)), // robot position (wheel slipping) and robot heading
																							// (gyro)
				VecBuilder.fill(0.5, 0.5, Units.degreesToRadians(30))); // vision errors (x,y) and rotation

		m_ahrs.reset();

		zeroYaw();
		m_ahrs.setAngleAdjustment(0.0);

		// getPose().getRotation().getDegrees();
		// resetPose(getPose());

		// Pose2d startPose = new Pose2d(new Translation2d(5.79, 4.0),
		// Rotation2d.fromDegrees(180));
		// resetPose(startPose);
		// resetOdometry(startPose);

		origPose.set(getPose());

		setChannelOff();

		chassisCommands.add("setX", this.setX)
				.withProperties(Map.of("show type", false));

		// chassisData.add("Heading", this.getHeading());

		pitchOffset = -getRawPitch();
		rollOffset = -getRawRoll();

		System.out.println("----- Ending Chassis Constructor -----");
	}

	/**************************************************************
	 * Periodic
	 **************************************************************/
	@Override
	public void periodic() {
		// Update the odometry in the periodic block
		poseEstimator.update(
				getRotation2d(),
				new SwerveModulePosition[] {
						m_frontLeft.getPosition(),
						m_frontRight.getPosition(),
						m_rearLeft.getPosition(),
						m_rearRight.getPosition()
				});

		currPose.set(getPose());

		// sbXVel.setDouble(getPose().getX());
		// sbYVel.setDouble(getPose().getY());
		// sbRVel.setDouble(getPose().getRotation().getDegrees());

		// sbAngle.setDouble(getAngle());
		// sbYaw.setDouble(getYaw());
		// sbPitch.setDouble(getPitch());
		// sbRoll.setDouble(getRoll());
		// sbFusedHeading.setDouble(getFusedHeading());
		// sbCompassHeading.setDouble(getCompassHeading());
		// sbRotDegree.setDouble(getRotation2d().getDegrees());
	}

	/**************************************************************
	 * Commands
	 **************************************************************/
	public Command setX = new InstantCommand(() -> setX());

	public Command driveCmd(double xSpeed, double ySpeed, double rot, boolean fieldRelative) {
		return new InstantCommand(() -> this.drive(xSpeed, ySpeed, rot, fieldRelative));
	}

	/**************************************************************
	 * Methods
	 **************************************************************/

	public ChassisSpeeds getRobotRelativeSpeeds() {
		// Convert module states to chassis speeds
		ChassisSpeeds chassisSpeeds = frc.robot.Constants.ChassisConstants.kDriveKinematics.toChassisSpeeds(
				m_frontLeft.getState(), m_frontRight.getState(), m_rearLeft.getState(), m_rearRight.getState());

		return chassisSpeeds;
	}

	/**
	 * 
	 * @param xSpeed
	 * @param ySpeed
	 * @param rot
	 * @param fieldRelative
	 * @param rateLimit
	 */
	public void driveRobotRelative(ChassisSpeeds chassisSpeeds) {
		var swerveModuleStates = ChassisConstants.kDriveKinematics.toSwerveModuleStates(chassisSpeeds);
		SwerveDriveKinematics.desaturateWheelSpeeds(
				swerveModuleStates, ChassisConstants.kMaxSpeedMetersPerSecond);
		m_frontLeft.setDesiredState(swerveModuleStates[0]);
		m_frontRight.setDesiredState(swerveModuleStates[1]);
		m_rearLeft.setDesiredState(swerveModuleStates[2]);
		m_rearRight.setDesiredState(swerveModuleStates[3]);
	}

	private boolean flipPath = false;

	public void setFlipPath(boolean fp) {
		flipPath = fp;
	}

	public boolean getFlipPath() {
		return flipPath;
	}

	Transform2d poseZero = new Transform2d(new Translation2d(0.0, 0.0), new Rotation2d(0.0));
	Transform2d poseError = new Transform2d(new Translation2d(1.0, 1.0), new Rotation2d(0.0));

	public Transform2d poseTest = poseZero;

	public void setPoseErr() {
		poseTest = poseError;
	}

	public void setPoseZero() {
		poseTest = poseZero;
	}

	public void addVisionMeasurement(Pose2d visionPose, double timestampSeconds) { // , Matrix<N3, N1> stdDevs) {
		poseEstimator.addVisionMeasurement(visionPose.plus(poseTest), timestampSeconds);// , stdDevs);
	}

	public void addVisionMeasurement(
			Pose2d visionRobotPoseMeters,
			double timestampSeconds,
			Matrix<N3, N1> visionMeasurementStdDevs) {
		poseEstimator.addVisionMeasurement(
				visionRobotPoseMeters, timestampSeconds, visionMeasurementStdDevs);
	}

	/**
	 * Reset the estimated pose of the swerve drive on the field.
	 *
	 * @param pose         New robot pose.
	 * @param resetSimPose If the simulated robot pose should also be reset. This
	 *                     effectively
	 *                     teleports the robot and should only be used during the
	 *                     setup of the simulation world.
	 */
	public void resetPose(Pose2d pose, boolean resetSimPose) {
		poseEstimator.resetPosition(getRotation2d(), getModulePositions(), pose);
	}

	public void resetPose(Pose2d pose) {
		resetPose(pose, false);
	}

	/**
	 * Returns the currently-estimated pose of the robot.
	 *
	 * @return The pose.
	 */
	public Pose2d getPose() {
		return poseEstimator.getEstimatedPosition();
	}

	/**
	 * Resets the odometry to the specified pose.
	 *
	 * @param pose The pose to which to set the odometry.
	 */
	public void resetOdometry(Pose2d pose) {
		poseEstimator.resetPosition(
				getRotation2d(),
				getModulePositions(),
				pose);
	}

	public PowerDistribution getPDH() {
		return m_pdh;
	}

	public void setChannelOn() {
		m_pdh.setSwitchableChannel(true);
	}

	public void setChannelOff() {
		m_pdh.setSwitchableChannel(false);
	}

	/**
	 * Get the SwerveModulePosition of each swerve module (position, angle). The
	 * returned array order
	 * matches the kinematics module order.
	 */
	public SwerveModulePosition[] getModulePositions() {
		return new SwerveModulePosition[] {
				m_frontLeft.getPosition(),
				m_frontRight.getPosition(),
				m_rearLeft.getPosition(),
				m_rearRight.getPosition()
		};
	}

	public double spdMultiplier = 1.0;

	public void setSpdHigh() {
		spdMultiplier = 1.0;
	}

	public void setSpdLow() {
		spdMultiplier = 0.5;
	}

	private final double COLLISION_THRESHOLD_DELTA_G = 0.5;
	private double last_world_linear_accel_x = 0.0;
	private double last_world_linear_accel_y = 0.0;

	public boolean isJerk() {
		boolean collisionDetected = false;

		double curr_world_linear_accel_x = m_ahrs.getWorldLinearAccelX();
		double currentJerkX = curr_world_linear_accel_x - last_world_linear_accel_x;
		last_world_linear_accel_x = curr_world_linear_accel_x;
		double curr_world_linear_accel_y = m_ahrs.getWorldLinearAccelY();
		double currentJerkY = curr_world_linear_accel_y - last_world_linear_accel_y;
		last_world_linear_accel_y = curr_world_linear_accel_y;

		if ((Math.abs(currentJerkX) > COLLISION_THRESHOLD_DELTA_G) ||
				(Math.abs(currentJerkY) > COLLISION_THRESHOLD_DELTA_G)) {
			collisionDetected = true;
		} else {
			collisionDetected = false;
		}

		return collisionDetected;
	}

	/**
	 * Method to drive the robot using joystick info.
	 *
	 * @param xSpeed        Speed of the robot in the x direction (forward).
	 * @param ySpeed        Speed of the robot in the y direction (sideways).
	 * @param rot           Angular rate of the robot.
	 * @param fieldRelative Whether the provided x and y speeds are relative to the
	 *                      field.
	 */
	public void drive(double xSpeed, double ySpeed, double rot, boolean fieldRelative) {
		// Convert the commanded speeds into the correct units for the drivetrain
		double xSpeedDelivered = xSpeed * DriveConstants.kMaxSpeedMetersPerSecond;
		double ySpeedDelivered = ySpeed * DriveConstants.kMaxSpeedMetersPerSecond;
		double rotDelivered = rot * DriveConstants.kMaxAngularSpeed;

		var swerveModuleStates = DriveConstants.kDriveKinematics.toSwerveModuleStates(
				fieldRelative
						? ChassisSpeeds.fromFieldRelativeSpeeds(xSpeedDelivered, ySpeedDelivered, rotDelivered,
								getRotation2d())
						: new ChassisSpeeds(xSpeedDelivered, ySpeedDelivered, rotDelivered));

		SwerveDriveKinematics.desaturateWheelSpeeds(
				swerveModuleStates, DriveConstants.kMaxSpeedMetersPerSecond);

		m_frontLeft.setDesiredState(swerveModuleStates[0]);
		m_frontRight.setDesiredState(swerveModuleStates[1]);
		m_rearLeft.setDesiredState(swerveModuleStates[2]);
		m_rearRight.setDesiredState(swerveModuleStates[3]);
	}

	/**
	 * Sets the wheels into an X formation to prevent movement.
	 */
	// Flipped -45 for +45 and +45 for -45 for each wheel. This fixed setX. But,
	// still have rot issue in drive.
	public void setX() {
		m_frontLeft.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(-45)));
		m_frontRight.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(45)));
		m_rearLeft.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(45)));
		m_rearRight.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(-45)));
	}

	public Command setXCmd() {
		// Subsystem::RunOnce implicitly requires `this` subsystem.
		return runOnce(() -> {
			setX();
		});
	}

	/**
	 * Sets the swerve ModuleStates.
	 *
	 * @param desiredStates The desired SwerveModule states.
	 */
	public void setModuleStates(SwerveModuleState[] desiredStates) {
		SwerveDriveKinematics.desaturateWheelSpeeds(
				desiredStates, DriveConstants.kMaxSpeedMetersPerSecond);
		m_frontLeft.setDesiredState(desiredStates[0]);
		m_frontRight.setDesiredState(desiredStates[1]);
		m_rearLeft.setDesiredState(desiredStates[2]);
		m_rearRight.setDesiredState(desiredStates[3]);
	}

	/** Resets the drive encoders to currently read a position of 0. */
	public void resetEncoders() {
		m_frontLeft.resetEncoders();
		m_rearLeft.resetEncoders();
		m_frontRight.resetEncoders();
		m_rearRight.resetEncoders();
	}

	/** Zeroes the heading of the robot. */
	public void zeroHeading() {
		m_ahrs.reset();
	}

	/**
	 * Returns the heading of the robot.
	 *
	 * @return the robot's heading in degrees, from -180 to 180
	 */
	public double getHeading() {
		return Rotation2d.fromDegrees(m_ahrs.getAngle()).getDegrees();
	}

	/**
	 * Returns the turn rate of the robot.
	 *
	 * @return The turn rate of the robot, in degrees per second
	 */
	public double getTurnRate() {
		return m_ahrs.getRate() * (DriveConstants.kGyroReversed ? -1.0 : 1.0);
	}

	/** Zeroes the heading of the robot. */
	public void zeroYaw() {
		m_ahrs.zeroYaw();
	}

	/** Raw gyro yaw (this may not match the field heading!). */
	public double getYaw() {
		return m_ahrs.getYaw();
	}

	public double getRoll() {
		return Math.toDegrees(SwerveUtils.WrapAngle(Math.toRadians(-m_ahrs.getRoll()))) + rollOffset;
	}

	public double getRawRoll() {
		return Math.toDegrees(SwerveUtils.WrapAngle(Math.toRadians(-m_ahrs.getRoll())));
	}

	public double getPitch() {
		return m_ahrs.getPitch() + pitchOffset;
	}

	public double getRawPitch() {
		return m_ahrs.getPitch();
	}

	public Rotation2d getRotation2d() {
		return m_ahrs.getRotation2d();
	}

	public double getCompassHeading() {
		return m_ahrs.getCompassHeading();
	}

	public double getFusedHeading() {
		return m_ahrs.getFusedHeading();
	}

	public double getAngle() {
		// Negating the angle because WPILib gyros are CW positive.
		return m_ahrs.getAngle();
	}
}