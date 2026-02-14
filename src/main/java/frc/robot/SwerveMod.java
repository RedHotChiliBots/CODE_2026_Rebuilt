package frc.robot;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import frc.lib.LazyCANCoder;
import frc.lib.LazyTalonFX;
import frc.lib.math.Conversions;
import frc.lib.util.CTREModuleState;
import frc.lib.util.SwerveModuleConstants;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.VelocityDutyCycle;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.controls.PositionDutyCycle;

public class SwerveMod {
    public CANBus canBus;
    public int moduleNumber;
    private Rotation2d angleOffset;
    private Rotation2d lastAngle;

    private TalonFX mAngleMotor;
    private TalonFX mDriveMotor;
    private CANcoder angleEncoder;

    private CTREConfigs ctreConfigs = new CTREConfigs();

    SimpleMotorFeedforward feedforward = new SimpleMotorFeedforward(Constants.Swerve.driveKS, Constants.Swerve.driveKV, Constants.Swerve.driveKA);

    public SwerveMod(CANBus canBus, int moduleNumber, SwerveModuleConstants moduleConstants){
        this.canBus = canBus;
        this.moduleNumber = moduleNumber;
        this.angleOffset = moduleConstants.angleOffset;
        
        /* Angle Encoder Config */
        angleEncoder = new LazyCANCoder(
            canBus,
            moduleConstants.cancoderID, 
            ctreConfigs.swerveCanCoderConfig
        );

        /* Angle Motor Config */
        mAngleMotor = new LazyTalonFX(
            canBus,
            moduleConstants.angleMotorID,
            ctreConfigs.swerveAngleFXConfig,
            Constants.Swerve.angleNeutralMode,
            Constants.Swerve.angleMotorInvert,
            false
        );

        /* Drive Motor Config */
        mDriveMotor = new LazyTalonFX(
            canBus,
            moduleConstants.driveMotorID,
            ctreConfigs.swerveDriveFXConfig,
            Constants.Swerve.driveNeutralMode,
            Constants.Swerve.driveMotorInvert,
            false);

        lastAngle = getState().angle;
    }

    public void setDesiredState(SwerveModuleState desiredState, boolean isOpenLoop){
        /* This is a custom optimize function, since default WPILib optimize assumes continuous controller which CTRE and Rev onboard is not */
        desiredState = CTREModuleState.optimize(desiredState, getState().angle); 
        setAngle(desiredState);
        setSpeed(desiredState, isOpenLoop);
    }

    private void setSpeed(SwerveModuleState desiredState, boolean isOpenLoop){
        if(isOpenLoop){
            double percentOutput = desiredState.speedMetersPerSecond / Constants.Swerve.maxSpeed;
            mDriveMotor.setControl(new DutyCycleOut(percentOutput));
        } else {
            double velocity = Conversions.MPSToFalcon(desiredState.speedMetersPerSecond, Constants.Swerve.wheelCircumference, Constants.Swerve.driveGearRatio);
            mDriveMotor.setControl(new VelocityDutyCycle(velocity));
        }
    }

    private void setAngle(SwerveModuleState desiredState){
        Rotation2d angle = (Math.abs(desiredState.speedMetersPerSecond) <= (Constants.Swerve.maxSpeed * 0.01)) ? lastAngle : desiredState.angle; //Prevent rotating module if speed is less then 1%. Prevents Jittering.
        
        mAngleMotor.setControl(new PositionDutyCycle(Conversions.degreesToFalcon(angle.getDegrees(), Constants.Swerve.angleGearRatio)));

        lastAngle = angle;
    }

    private Rotation2d getAngle(){
        return Rotation2d.fromDegrees(Conversions.falconToDegrees(mAngleMotor.getPosition().getValueAsDouble(), Constants.Swerve.angleGearRatio));
    }

    public Rotation2d getCanCoder(){
        return Rotation2d.fromDegrees(angleEncoder.getAbsolutePosition().getValueAsDouble());
    }

    public void resetToAbsolute(){
        double absolutePosition = Conversions.degreesToFalcon(getCanCoder().getDegrees() - angleOffset.getDegrees(), Constants.Swerve.angleGearRatio);
        //mAngleMotor.setSelectedSensorPosition(absolutePosition);
        mAngleMotor.setPosition(absolutePosition);
    }

    public SwerveModuleState getState(){
        return new SwerveModuleState(
            Conversions.falconToMPS(mDriveMotor.getVelocity().getValueAsDouble(), 
            Constants.Swerve.wheelCircumference, 
            Constants.Swerve.driveGearRatio), 
            getAngle()
        ); 
    }

    public SwerveModulePosition getPosition(){
        return new SwerveModulePosition(
            Conversions.falconToMeters(mDriveMotor.getPosition().getValueAsDouble(), 
            Constants.Swerve.wheelCircumference, 
            Constants.Swerve.driveGearRatio), 
            getAngle()
        );
    }
}