package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import frc.robot.subsystems.Climber.ClimberSP;

public class ClimberTest {

    private final Climber climber = new Climber();

    @Test
    @DisplayName("Climber And Hook Setpoints")
    void testSetpoints() {
        climber.setClimberSP(ClimberSP.STOW);
        assertEquals("STOW", climber.getClimberSP().name());
        assertEquals(ClimberSP.STOW.getValue(), climber.getClimberSP().getValue());

        climber.setClimberSP(ClimberSP.LVL1);
        assertEquals("LVL1", climber.getClimberSP().name());
        assertEquals(ClimberSP.LVL1.getValue(), climber.getClimberSP().getValue());
    }
}
