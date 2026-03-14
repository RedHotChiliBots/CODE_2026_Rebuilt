package frc.robot.validation;

import edu.wpi.first.wpilibj2.command.Command;
import java.util.List;

public interface SubsystemValidation {
  Command validateCommand();

  List<ValidationResult> validationResults();

  String validationSummary();

  ValidationStatus validationStatus();
}
