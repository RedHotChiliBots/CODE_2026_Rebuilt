package frc.robot.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ValidationSupportTest {

  @Test
  @DisplayName("Finish Marks Pass When All Checks Pass")
  void finishPass() {
    ValidationSupport support = new ValidationSupport("Test");
    support.start();
    support.addResult(ValidationResult.of("Test", "One", true, ValidationUtils.measurements(), "", ""));
    support.addResult(ValidationResult.of("Test", "Two", true, ValidationUtils.measurements(), "", ""));

    support.finish();

    assertEquals(ValidationStatus.PASS, support.status());
    assertEquals("PASS (2/2)", support.summary());
  }

  @Test
  @DisplayName("Finish Marks Fail When Any Check Fails")
  void finishFail() {
    ValidationSupport support = new ValidationSupport("Test");
    support.start();
    support.addResult(ValidationResult.of("Test", "One", true, ValidationUtils.measurements(), "", ""));
    support.addResult(ValidationResult.of("Test", "Two", false, ValidationUtils.measurements(), "", "bad"));

    support.finish();

    assertEquals(ValidationStatus.FAIL, support.status());
    assertEquals("FAIL (1/2)", support.summary());
  }
}
