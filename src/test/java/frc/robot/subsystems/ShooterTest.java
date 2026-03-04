package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import frc.robot.Constants;
import frc.robot.subsystems.Shooter.ShooterSP;

public class ShooterTest {

  CommandSwerveDrivetrain drivetrain = null;
  private final Shooter shooter = new Shooter(drivetrain);

  @Test
  @DisplayName("Shooter")
  void testSP() {
    shooter.setShooterSP(ShooterSP.OFF);
    assertEquals(ShooterSP.OFF.getVel(false), shooter.getShooterSP(false));
    assertEquals(ShooterSP.OFF.getVel(true), shooter.getShooterSP(true));
    assertEquals(ShooterSP.OFF.getVel(true),
        shooter.getShooterSP(false) / 100.0 * Constants.MotorConstants.kVortexFreeSpeedRpm);
    shooter.setShooterSP(ShooterSP.HI);
    assertEquals(ShooterSP.HI.getVel(false), shooter.getShooterSP(false));
    assertEquals(ShooterSP.HI.getVel(true), shooter.getShooterSP(true));
    assertEquals(ShooterSP.HI.getVel(true),
        shooter.getShooterSP(false) / 100.0 * Constants.MotorConstants.kVortexFreeSpeedRpm);
    shooter.setShooterSP(ShooterSP.MED);
    assertEquals(ShooterSP.MED.getVel(false), shooter.getShooterSP(false));
    assertEquals(ShooterSP.MED.getVel(true), shooter.getShooterSP(true));
    assertEquals(ShooterSP.MED.getVel(true),
        shooter.getShooterSP(false) / 100.0 * Constants.MotorConstants.kVortexFreeSpeedRpm);
    shooter.setShooterSP(ShooterSP.LOW);
    assertEquals(ShooterSP.LOW.getVel(false), shooter.getShooterSP(false));
    assertEquals(ShooterSP.LOW.getVel(true), shooter.getShooterSP(true));
    assertEquals(ShooterSP.LOW.getVel(true),
        shooter.getShooterSP(false) / 100.0 * Constants.MotorConstants.kVortexFreeSpeedRpm);
  }

}
