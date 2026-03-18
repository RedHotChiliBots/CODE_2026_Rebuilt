package frc.robot.validation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ValidationResult(
    String subsystem,
    String checkName,
    boolean passed,
    Map<String, String> measurements,
    String expected,
    String message) {

  public ValidationResult {
    measurements = Collections.unmodifiableMap(new LinkedHashMap<>(measurements));
    expected = expected == null ? "" : expected;
    message = message == null ? "" : message;
  }

  public static ValidationResult of(
      String subsystem,
      String checkName,
      boolean passed,
      Map<String, String> measurements,
      String expected,
      String message) {
    return new ValidationResult(subsystem, checkName, passed, measurements, expected, message);
  }
}
