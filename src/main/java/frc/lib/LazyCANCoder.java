package frc.lib;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.hardware.CANcoder;

/**
 * Thin CANCoder wrapper to make setup easier.
 */
public class LazyCANCoder extends CANcoder {

    public LazyCANCoder(CANBus canBus, int deviceNumber, CANcoderConfiguration allConfigs){
        super(deviceNumber, canBus);
        super.getConfigurator().apply(new CANcoderConfiguration());
        super.getConfigurator().apply(allConfigs);
    }
}
