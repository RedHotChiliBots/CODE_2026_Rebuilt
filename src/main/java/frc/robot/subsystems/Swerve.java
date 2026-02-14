package frc.robot.subsystems;

import frc.robot.SwerveMod;
import frc.robot.Constants;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;

import org.littletonrobotics.junction.Logger;

import com.ctre.phoenix6.CANBus;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
//import com.pathplanner.lib.util.HolonomicPathFollowerConfig;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.util.PathPlannerLogging;
//import com.pathplanner.lib.util.ReplanningConfig;
import com.studica.frc.AHRS;
import com.studica.frc.AHRS.NavXComType;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringArrayPublisher;
import edu.wpi.first.networktables.Subscriber;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Swerve extends SubsystemBase {
    public SwerveDriveOdometry swerveOdometry;
    public SwerveMod[] mSwerveMods;
    // ==============================================================
	// Initialize NavX AHRS board
	// Alternatively: I2C.Port.kMXP, SerialPort.Port.kMXP or SerialPort.Port.kUSB
	private AHRS m_ahrs = new AHRS(NavXComType.kMXP_SPI, (byte) 100);
	private PowerDistribution m_pdh = new PowerDistribution(frc.robot.Constants.CANId.kPDHCanID, ModuleType.kRev);

    private Field2d field = new Field2d();

    public Swerve() {
        // ==============================================================
		// Initialize NavX AHRS board
		// Alternatively: I2C.Port.kMXP, SerialPort.Port.kMXP or SerialPort.Port.kUSB
		try {
			// m_ahrs = new AHRS(SPI.Port.kMXP, (byte) 100); // 100 Hz
			m_ahrs.enableBoardlevelYawReset(true);
		} catch (RuntimeException ex) {
			DriverStation.reportError("Error instantiating navX-MXP:  " + ex.getMessage(), true);
		}
        m_ahrs = new AHRS(NavXComType.kMXP_SPI, (byte) 100);
        //gyro = new Pigeon2(Constants.Swerve.pigeonID, "canivore");
        //gyro.configFactoryDefault();
        m_ahrs.reset();
        zeroGyro();

        CANBus canBus = new CANBus("canivore");

        mSwerveMods = new SwerveMod[] {
            new SwerveMod(canBus, 0, Constants.Swerve.Mod0.constants),
            new SwerveMod(canBus, 1, Constants.Swerve.Mod1.constants),
            new SwerveMod(canBus, 2, Constants.Swerve.Mod2.constants),
            new SwerveMod(canBus, 3, Constants.Swerve.Mod3.constants)
        };

        /* By pausing init for a second before setting module offsets, we avoid a bug with inverting motors.
         * See https://github.com/Team364/BaseFalconSwerve/issues/8 for more info.
         */
        Timer.delay(1.0);
        resetModulesToAbsolute();

        swerveOdometry = new SwerveDriveOdometry(Constants.Swerve.swerveKinematics, getYaw(), getModulePositions());

        		RobotConfig config = null;
		try {
			config = RobotConfig.fromGUISettings();

			// Configure AutoBuilder last
			AutoBuilder.configure(
					this::getPose, // Robot pose supplier
//					this::resetPose, // Method to reset odometry (will be called if your auto has a starting pose)
					this::resetOdometry, // Method to reset odometry (will be called if your auto has a starting pose)
					this::getRobotRelativeSpeeds, // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE
					(speeds, feedforwards) -> driveRobotRelative(speeds), // Method that will drive the robot given
																			// ROBOT
																			// RELATIVE ChassisSpeeds. Also optionally
																			// outputs
																			// individual module feedforwards
					new PPHolonomicDriveController( // PPHolonomicController is the built in path following controller
													// for holonomic drive trains
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

		} catch (Exception e) {
			// Handle error if settings.json is missing or corrupted
			DriverStation.reportError("Failed to configure AutoBuilder: " + e.getMessage(), e.getStackTrace());
		}

    //     // Configure the AutoBuilder last
    //     AutoBuilder.configureHolonomic(
    //      this::getPose, // Robot pose supplier
    //      this::resetOdometry, // Method to reset odometry (will be called if your auto has a starting pose)
    //      this::getRobotRelativeSpeeds, // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE
    //      this::driveRobotRelative, // Method that will drive the robot given ROBOT RELATIVE ChassisSpeeds
    //     new HolonomicPathFollowerConfig( // HolonomicPathFollowerConfig, this should likely live in your Constants class
    //         new PIDConstants(5.0, 0.0, 0.0), // Translation PID constants
    //         new PIDConstants(5.0, 0.0, 0.0), // Rotation PID constants
    //         4.5, // Max module speed, in m/s
    //         0.4, // Drive base radius in meters. Distance from robot center to furthest module.
    //         new ReplanningConfig() // Default path replanning config. See the API for the options here
    //         ),
    //         () -> {
    //             // Boolean supplier that controls when the path will be mirrored for the red alliance
    //             // This will flip the path being followed to the red side of the field.
    //             // THE ORIGIN WILL REMAIN ON THE BLUE SIDE

    //             var alliance = DriverStation.getAlliance();
    //             if (alliance.isPresent()) {
    //                 return alliance.get() == DriverStation.Alliance.Red;
    //             }
    //             return false;
    //         },
    //         this // Reference to this subsystem to set requirements
    // );
    
        // Set up custom logging to add the current path to a field 2d widget
        PathPlannerLogging.setLogActivePathCallback((poses) -> field.getObject("path").setPoses(poses));

        SmartDashboard.putData("Field", field);
    }

    public void drive(Translation2d translation, double rotation, boolean fieldRelative, boolean isOpenLoop) {
        SwerveModuleState[] swerveModuleStates =
            Constants.Swerve.swerveKinematics.toSwerveModuleStates(
                fieldRelative ? ChassisSpeeds.fromFieldRelativeSpeeds(
                                    translation.getX(), 
                                    translation.getY(), 
                                    rotation, 
                                    getYaw()
                                )
                                : new ChassisSpeeds(
                                    translation.getX(), 
                                    translation.getY(), 
                                    rotation)
                                );
        SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates, Constants.Swerve.maxSpeed);

        for(SwerveMod mod : mSwerveMods){
            mod.setDesiredState(swerveModuleStates[mod.moduleNumber], isOpenLoop);
        }
    }    

    /* Used by Pathplanner autobuilder */
    public void setModuleStates(SwerveModuleState[] desiredStates) {
        SwerveDriveKinematics.desaturateWheelSpeeds(desiredStates, Constants.Swerve.maxSpeed);
        
        for(SwerveMod mod : mSwerveMods){
            mod.setDesiredState(desiredStates[mod.moduleNumber], false);
        }
    }    

    public Pose2d getPose() {
        return swerveOdometry.getPoseMeters();
    }

    public void resetOdometry(Pose2d pose) {
        swerveOdometry.resetPosition(getYaw(), getModulePositions(), pose);
    }

    public SwerveModuleState[] getModuleStates(){
        SwerveModuleState[] states = new SwerveModuleState[4];
        for(SwerveMod mod : mSwerveMods){
            states[mod.moduleNumber] = mod.getState();
        }
        return states;
    }

    public SwerveModulePosition[] getModulePositions(){
        SwerveModulePosition[] positions = new SwerveModulePosition[4];
        for(SwerveMod mod : mSwerveMods){
            positions[mod.moduleNumber] = mod.getPosition();
        }
        return positions;
    }

    public ChassisSpeeds getRobotRelativeSpeeds(){
        return Constants.Swerve.swerveKinematics.toChassisSpeeds(getModuleStates());
    }

    public void driveRobotRelative(ChassisSpeeds speeds){
        SwerveModuleState[] states = Constants.Swerve.swerveKinematics.toSwerveModuleStates(speeds);
        SwerveDriveKinematics.desaturateWheelSpeeds(states, Constants.Swerve.maxSpeed);
        setModuleStates(states);
    }

    public void zeroGyro(){
        m_ahrs.reset();
    }

    public Rotation2d getYaw() {
        return (Constants.Swerve.invertGyro) ? Rotation2d.fromDegrees(360 - m_ahrs.getYaw()) : Rotation2d.fromDegrees(m_ahrs.getYaw());
    }

    public void resetModulesToAbsolute(){
        for(SwerveMod mod : mSwerveMods){
            mod.resetToAbsolute();
        }
    }

    @Override
    public void periodic(){
        swerveOdometry.update(getYaw(), getModulePositions());  
        field.setRobotPose(getPose());

         Logger.recordOutput("Mystates", getModuleStates());
         Logger.recordOutput("MyPose", getPose());

        for(SwerveMod mod : mSwerveMods){
            Logger.recordOutput("Mod " + mod.moduleNumber + " Cancoder", mod.getCanCoder().getDegrees());
            Logger.recordOutput("Mod " + mod.moduleNumber + " Integrated", mod.getPosition().angle.getDegrees());
            Logger.recordOutput("Mod " + mod.moduleNumber + " Velocity", mod.getState().speedMetersPerSecond); 
             
        }
    }
}