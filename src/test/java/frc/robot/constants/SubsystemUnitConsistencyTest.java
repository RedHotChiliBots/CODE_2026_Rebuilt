package frc.robot.constants;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SubsystemUnitConsistencyTest {

  @Test
  @DisplayName("Intake Velocity Factor Matches Position Factor")
  void intakeFactors() {
    assertEquals(IntakeConstants.kIntakePositionFactor, IntakeConstants.kIntakeVelocityFactor, 1e-9);
  }

  @Test
  @DisplayName("Feeder Velocity Factor Matches Position Factor")
  void feederFactors() {
    assertEquals(FeederConstants.kFeederPositionFactor, FeederConstants.kFeederVelocityFactor, 1e-9);
  }

  @Test
  @DisplayName("Shooter Tilt Velocity Factor Matches Degrees Per Second")
  void shooterTiltFactors() {
    assertEquals(
        ShooterConstants.kTiltPositionFactor / 60.0,
        ShooterConstants.kTiltVelocityFactor,
        1e-9);
  }

  @Test
  @DisplayName("Climber Velocity Factor Matches Position Factor")
  void climberFactors() {
    assertEquals(
        ClimberConstants.kClimberPositionFactor / 60.0,
        ClimberConstants.kClimberVelocityFactor,
        1e-9);
  }
}
