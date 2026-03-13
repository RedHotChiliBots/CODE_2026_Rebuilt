package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import frc.robot.subsystems.Climber.ClimberSP;
import frc.robot.subsystems.Climber.HookSP;

public class ClimberTest {

    private final Climber climber = new Climber();

    @Test
    @DisplayName("Climber And Hook Setpoints")
    void testSetpoints() {
        climber.setClimberSP(ClimberSP.STOW);
        assertEquals("STOW", climber.getClimberSP().name());
        assertEquals(ClimberSP.STOW.getValue(), climber.getClimberSP().getValue());

        climber.setClimberSP(ClimberSP.LVLAUTON);
        assertEquals("LVLAUTON", climber.getClimberSP().name());
        assertEquals(ClimberSP.LVLAUTON.getValue(), climber.getClimberSP().getValue());

        climber.setClimberSP(ClimberSP.LVL1);
        assertEquals("LVL1", climber.getClimberSP().name());
        assertEquals(ClimberSP.LVL1.getValue(), climber.getClimberSP().getValue());

        climber.setClimberSP(ClimberSP.LVL2);
        assertEquals("LVL2", climber.getClimberSP().name());
        assertEquals(ClimberSP.LVL2.getValue(), climber.getClimberSP().getValue());

        climber.setClimberSP(ClimberSP.LVL3);
        assertEquals("LVL3", climber.getClimberSP().name());
        assertEquals(ClimberSP.LVL3.getValue(), climber.getClimberSP().getValue());
        
        climber.setHookSP(HookSP.STOW);
        assertEquals("STOW", climber.getHookSP().name());
        assertEquals(HookSP.STOW.getSpd(), climber.getHookSP().getSpd());

        climber.setHookSP(HookSP.STOP);
        assertEquals("STOP", climber.getHookSP().name());
        assertEquals(HookSP.STOP.getSpd(), climber.getHookSP().getSpd());

        climber.setHookSP(HookSP.DEPLOY);
        assertEquals("DEPLOY", climber.getHookSP().name());
        assertEquals(HookSP.DEPLOY.getSpd(), climber.getHookSP().getSpd());
    }
}
