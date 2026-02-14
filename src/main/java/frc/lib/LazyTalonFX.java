package frc.lib;

import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

/**
 * Thin Falcon wrapper to make setup easier.
 */
public class LazyTalonFX extends TalonFX {

    /**
     * Config using individual parameters.
     * 
     * @param deviceNumber
     * @param allConfigs
     * @param neutralMode
     * @param anglemotorinvert
     * @param slowStatusFrame
     */
    public LazyTalonFX(CANBus canBus, int deviceNumber, TalonFXConfiguration allConfigs, NeutralMode neutralMode,
            boolean anglemotorinvert, boolean slowStatusFrame) {
        super(deviceNumber, canBus);
        super.getConfigurator().apply(new TalonFXConfiguration());
        super.getConfigurator().apply(allConfigs);

        TalonFXConfiguration configs = new TalonFXConfiguration();
        configs.MotorOutput.NeutralMode = neutralMode == NeutralMode.Coast ? NeutralModeValue.Coast
                : NeutralModeValue.Brake;
        configs.MotorOutput.Inverted = anglemotorinvert ? InvertedValue.Clockwise_Positive
                : InvertedValue.CounterClockwise_Positive;
        super.getConfigurator().apply(configs);

        super.setPosition(0);

        if (slowStatusFrame) {
            super.getPosition().setUpdateFrequency(5);
            // super.setStatusFramePeriod(StatusFrame.Status_1_General, 255, 30);
            // super.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 255, 30);
        }
    }

    /**
     * Config using talonFxConstants.
     * 
     * @param talonFxConstants
     */
    public LazyTalonFX(CANBus canBus, TalonFxConstants talonFxConstants) {
        super(talonFxConstants.deviceNumber, canBus);
        super.getConfigurator().apply(new TalonFXConfiguration());
        super.getConfigurator().apply(talonFxConstants.allConfigs);

        TalonFXConfiguration configs = new TalonFXConfiguration();
        configs.MotorOutput.NeutralMode = talonFxConstants.neutralMode == NeutralMode.Coast ? NeutralModeValue.Coast
                : NeutralModeValue.Brake;
        configs.MotorOutput.Inverted = talonFxConstants.invertType == InvertType.InvertMotorOutput
                ? InvertedValue.CounterClockwise_Positive
                : InvertedValue.Clockwise_Positive;
        super.getConfigurator().apply(configs);

        super.setPosition(0);

        if (talonFxConstants.slowStatusFrame) {
            super.getPosition().setUpdateFrequency(5);
            // super.setStatusFramePeriod(StatusFrame.Status_1_General, 255, 30);
            // super.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 255, 30);
        }
    }
}
