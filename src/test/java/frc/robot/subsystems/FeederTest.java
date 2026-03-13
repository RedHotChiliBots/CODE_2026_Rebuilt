package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import frc.robot.constants.FeederConstants;
import frc.robot.subsystems.Feeder.FeederSP;

public class FeederTest {

    private final Feeder feeder = new Feeder();

    @Test
    @DisplayName("Feeder")
    void testSP() {
        feeder.setFeederSP(FeederSP.OFF);
        assertEquals("OFF", feeder.getFeederSP().name());
        assertEquals(FeederSP.OFF.getVel(false), feeder.getFeederSP(false));
        assertEquals(FeederSP.OFF.getVel(true), feeder.getFeederSP(true));
        assertEquals(FeederSP.OFF.getVel(true), feeder.getFeederSP(false) * FeederConstants.kFeederMotorFreeSpeedRpm / 100.0);
        feeder.setFeederSP(FeederSP.HI);
        assertEquals("HI", feeder.getFeederSP().name());
        assertEquals(FeederSP.HI.getVel(false), feeder.getFeederSP(false));
        assertEquals(FeederSP.HI.getVel(true), feeder.getFeederSP(true));
        assertEquals(FeederSP.HI.getVel(true), feeder.getFeederSP(false) * FeederConstants.kFeederMotorFreeSpeedRpm / 100.0);
        feeder.setFeederSP(FeederSP.MED);
        assertEquals("MED", feeder.getFeederSP().name());
        assertEquals(FeederSP.MED.getVel(false), feeder.getFeederSP(false));
        assertEquals(FeederSP.MED.getVel(true), feeder.getFeederSP(true));
        assertEquals(FeederSP.MED.getVel(true), feeder.getFeederSP(false) * FeederConstants.kFeederMotorFreeSpeedRpm / 100.0);
        feeder.setFeederSP(FeederSP.LOW);
        assertEquals("LOW", feeder.getFeederSP().name());
        assertEquals(FeederSP.LOW.getVel(false), feeder.getFeederSP(false));
        assertEquals(FeederSP.LOW.getVel(true), feeder.getFeederSP(true));
        assertEquals(FeederSP.LOW.getVel(true), feeder.getFeederSP(false) * FeederConstants.kFeederMotorFreeSpeedRpm / 100.0);
    }


}
