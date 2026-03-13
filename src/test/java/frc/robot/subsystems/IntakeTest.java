package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import frc.robot.constants.IntakeConstants;
import frc.robot.subsystems.Intake.IntakeSP;

public class IntakeTest {

    private final Intake intake = new Intake();

    @Test
    @DisplayName("Intake")
    void testSP() {
        intake.setIntakeSP(IntakeSP.OFF);
        assertEquals("OFF", intake.getIntakeSP().name());
        assertEquals(IntakeSP.OFF.getVel(false), intake.getIntakeSP(false));
        assertEquals(IntakeSP.OFF.getVel(true), intake.getIntakeSP(true));
        assertEquals(IntakeSP.OFF.getVel(true), intake.getIntakeSP(false) * IntakeConstants.kIntakeMotorFreeSpeedRpm / 100.0);
        intake.setIntakeSP(IntakeSP.HI);
        assertEquals("HI", intake.getIntakeSP().name());
        assertEquals(IntakeSP.HI.getVel(false), intake.getIntakeSP(false));
        assertEquals(IntakeSP.HI.getVel(true), intake.getIntakeSP(true));
        assertEquals(IntakeSP.HI.getVel(true), intake.getIntakeSP(false) * IntakeConstants.kIntakeMotorFreeSpeedRpm / 100.0);
        intake.setIntakeSP(IntakeSP.MED);
        assertEquals("MED", intake.getIntakeSP().name());
        assertEquals(IntakeSP.MED.getVel(false), intake.getIntakeSP(false));
        assertEquals(IntakeSP.MED.getVel(true), intake.getIntakeSP(true));
        assertEquals(IntakeSP.MED.getVel(true), intake.getIntakeSP(false) * IntakeConstants.kIntakeMotorFreeSpeedRpm / 100.0);
        intake.setIntakeSP(IntakeSP.LOW);
        assertEquals("LOW", intake.getIntakeSP().name());
        assertEquals(IntakeSP.LOW.getVel(false), intake.getIntakeSP(false));
        assertEquals(IntakeSP.LOW.getVel(true), intake.getIntakeSP(true));
        assertEquals(IntakeSP.LOW.getVel(true), intake.getIntakeSP(false) * IntakeConstants.kIntakeMotorFreeSpeedRpm / 100.0);
    }


}
