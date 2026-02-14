package frc.robot;

import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix.sensors.AbsoluteSensorRange;
import com.ctre.phoenix.sensors.SensorInitializationStrategy;
import com.ctre.phoenix.sensors.SensorTimeBase;
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.MagnetSensorConfigs;

public final class CTREConfigs {
    public TalonFXConfiguration swerveAngleFXConfig;
    public TalonFXConfiguration swerveDriveFXConfig;
    public CANcoderConfiguration swerveCanCoderConfig;

    public CTREConfigs() {
        swerveAngleFXConfig = new TalonFXConfiguration();
        swerveDriveFXConfig = new TalonFXConfiguration();
        swerveCanCoderConfig = new CANcoderConfiguration();

        /* Swerve Angle Motor Configurations */
        SupplyCurrentLimitConfiguration angleSupplyLimit = new SupplyCurrentLimitConfiguration(
                Constants.Swerve.angleEnableCurrentLimit,
                Constants.Swerve.angleContinuousCurrentLimit,
                Constants.Swerve.anglePeakCurrentLimit,
                Constants.Swerve.anglePeakCurrentDuration);

        var slot0AngleConfigs = swerveAngleFXConfig.Slot0
                .withKP(Constants.Swerve.angleKP)
                .withKI(Constants.Swerve.angleKI)
                .withKD(Constants.Swerve.angleKD)
                .withKV(Constants.Swerve.angleKF);
        var currAngleConfigs = swerveAngleFXConfig.CurrentLimits
                .withSupplyCurrentLimitEnable(Constants.Swerve.angleEnableCurrentLimit)
                .withSupplyCurrentLimit(Constants.Swerve.angleContinuousCurrentLimit)
                .withSupplyTriggerThreshold(Constants.Swerve.anglePeakCurrentLimit)
                .withSupplyTriggerThresholdTime(Constants.Swerve.anglePeakCurrentDuration);

        swerveAngleFXConfig.supplyCurrLimit = angleSupplyLimit;

        /* Swerve Drive Motor Configuration */
        SupplyCurrentLimitConfiguration driveSupplyLimit = new SupplyCurrentLimitConfiguration(
                Constants.Swerve.driveEnableCurrentLimit,
                Constants.Swerve.driveContinuousCurrentLimit,
                Constants.Swerve.drivePeakCurrentLimit,
                Constants.Swerve.drivePeakCurrentDuration);

        var slot0DriveConfigs = swerveDriveFXConfig.Slot0
                .withKP(Constants.Swerve.driveKP)
                .withKI(Constants.Swerve.driveKI)
                .withKD(Constants.Swerve.driveKD)
                .withKV(Constants.Swerve.driveKF);
        swerveDriveFXConfig.supplyCurrLimit = driveSupplyLimit;
        swerveDriveFXConfig.openloopRamp = Constants.Swerve.openLoopRamp;
        swerveDriveFXConfig.closedloopRamp = Constants.Swerve.closedLoopRamp;

        /* Swerve CANCoder Configuration */
        MagnetSensorConfigs magnetCfg = new MagnetSensorConfigs();
        magnetCfg.absoluteSensorRange = AbsoluteSensorRangeValue.Unsigned_0To1;

        swerveCanCoderConfig.MagnetSensor.withAbsoluteSensorRange(AbsoluteSensorRangeValue.Unsigned_0To1);
        swerveCanCoderConfig.MagnetSensor.sensorDirection = Constants.Swerve.canCoderInvert;
        swerveCanCoderConfig.initializationStrategy = SensorInitializationStrategy.BootToAbsolutePosition;
        swerveCanCoderConfig.sensorTimeBase = SensorTimeBase.PerSecond;
    }
}