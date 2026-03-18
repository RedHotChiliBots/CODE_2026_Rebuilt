package frc.robot.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ValidationUtilsTest {

  @Test
  @DisplayName("Within Tolerance Uses Inclusive Bounds")
  void withinTolerance() {
    assertTrue(ValidationUtils.isWithin(10.0, 10.5, 0.5));
    assertFalse(ValidationUtils.isWithin(10.0, 10.6, 0.5));
  }

  @Test
  @DisplayName("Range Check Uses Inclusive Bounds")
  void rangeCheck() {
    assertTrue(ValidationUtils.isInRange(5.0, 0.0, 5.0));
    assertFalse(ValidationUtils.isInRange(-0.1, 0.0, 5.0));
  }
}
