package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import frc.robot.constants.ShooterConstants;
import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.Shooter.ShooterSP;
import frc.robot.subsystems.Shooter.TiltSP;

public class ShooterTest {

        private final CommandSwerveDrivetrain drivetrain = TunerConstants.createDrivetrain();
        private final Feeder feeder = null;
        private final Shooter shooter = new Shooter(drivetrain, feeder);

        @Test
        @DisplayName("Shooter")
        void testSP() {
                shooter.setShooterSP(ShooterSP.OFF);
                assertEquals("OFF", shooter.getShooterSPName());
                assertEquals(ShooterSP.OFF.getVel(false), shooter.getShooterSP(false));
                assertEquals(ShooterSP.OFF.getVel(true), shooter.getShooterSP(true));
                assertEquals(ShooterSP.OFF.getVel(true),
                                shooter.getShooterSP(false) / 100.0 * ShooterConstants.kShooterMotorFreeSpeedRpm);
                shooter.setShooterSP(ShooterSP.HI);
                assertEquals("HI", shooter.getShooterSPName());
                assertEquals(ShooterSP.HI.getVel(false), shooter.getShooterSP(false));
                assertEquals(ShooterSP.HI.getVel(true), shooter.getShooterSP(true));
                assertEquals(ShooterSP.HI.getVel(true),
                                shooter.getShooterSP(false) / 100.0 * ShooterConstants.kShooterMotorFreeSpeedRpm);
                shooter.setShooterSP(ShooterSP.MED);
                assertEquals("MED", shooter.getShooterSPName());
                assertEquals(ShooterSP.MED.getVel(false), shooter.getShooterSP(false));
                assertEquals(ShooterSP.MED.getVel(true), shooter.getShooterSP(true));
                assertEquals(ShooterSP.MED.getVel(true),
                                shooter.getShooterSP(false) / 100.0 * ShooterConstants.kShooterMotorFreeSpeedRpm);
                shooter.setShooterSP(ShooterSP.LOW);
                assertEquals("LOW", shooter.getShooterSPName());
                assertEquals(ShooterSP.LOW.getVel(false), shooter.getShooterSP(false));
                assertEquals(ShooterSP.LOW.getVel(true), shooter.getShooterSP(true));
                assertEquals(ShooterSP.LOW.getVel(true),
                                shooter.getShooterSP(false) / 100.0 * ShooterConstants.kShooterMotorFreeSpeedRpm);

                shooter.setShooterSPDbl(1000.0);
                assertEquals("Velocity", shooter.getShooterSPName());
                assertEquals(1000.0, shooter.getShooterSPDbl());

                shooter.setTiltSP(TiltSP.HI);
                assertEquals("HI", shooter.getTiltSPName());
                assertEquals(TiltSP.HI.getPos(), shooter.getTiltSPPos());
                shooter.setTiltSP(TiltSP.MED);
                assertEquals("MED", shooter.getTiltSPName());
                assertEquals(TiltSP.MED.getPos(), shooter.getTiltSPPos());
                shooter.setTiltSP(TiltSP.LOW);
                assertEquals("LOW", shooter.getTiltSPName());
                assertEquals(TiltSP.LOW.getPos(), shooter.getTiltSPPos());

                shooter.setTiltSPDbl(45.0);
                assertEquals("Degrees", shooter.getTiltSPName());
                assertEquals(45.0, shooter.getTiltSPDbl());

        }
}
