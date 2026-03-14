package frc.robot.validation;

import frc.robot.utils.Library;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ValidationUtils {
  private ValidationUtils() {}

  public static boolean isWithin(double actual, double expected, double tolerance) {
    return Math.abs(actual - expected) <= tolerance;
  }

  public static boolean isInRange(double value, double min, double max) {
    return value >= min && value <= max;
  }

  public static String formatDouble(double value) {
    return Double.toString(Library.SBFormat(value));
  }

  public static Map<String, String> measurements(String... keyValues) {
    Map<String, String> values = new LinkedHashMap<>();
    for (int i = 0; i + 1 < keyValues.length; i += 2) {
      values.put(keyValues[i], keyValues[i + 1]);
    }
    return values;
  }
}
