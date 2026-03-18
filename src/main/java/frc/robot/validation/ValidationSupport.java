package frc.robot.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationSupport {
  private final String subsystem;
  private final List<ValidationResult> results = new ArrayList<>();
  private ValidationStatus status = ValidationStatus.NOT_RUN;

  public ValidationSupport(String subsystem) {
    this.subsystem = subsystem;
  }

  public void start() {
    results.clear();
    status = ValidationStatus.RUNNING;
  }

  public void addResult(ValidationResult result) {
    results.add(result);
  }

  public void finish() {
    if (results.isEmpty()) {
      status = ValidationStatus.FAIL;
      return;
    }
    status = results.stream().allMatch(ValidationResult::passed)
        ? ValidationStatus.PASS
        : ValidationStatus.FAIL;
  }

  public void fail(String checkName, String message) {
    addResult(ValidationResult.of(subsystem, checkName, false, ValidationUtils.measurements(), "", message));
    status = ValidationStatus.FAIL;
  }

  public List<ValidationResult> results() {
    return List.copyOf(results);
  }

  public ValidationStatus status() {
    return status;
  }

  public String summary() {
    if (results.isEmpty()) {
      return status.name();
    }
    long passCount = results.stream().filter(ValidationResult::passed).count();
    return String.format("%s (%d/%d)", status.name(), passCount, results.size());
  }
}
