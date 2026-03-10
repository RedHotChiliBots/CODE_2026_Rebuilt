# Feeder.java Improvement Guide

This document outlines recommended improvements for `src/main/java/frc/robot/subsystems/Feeder.java` based on a comprehensive code review.

## High Priority Issues

### 1. Duplicate Shuffleboard Updates in periodic() (HIGH - Functionality)
**Location:** Lines 168-183  
**Issue:** The periodic() method contains duplicate code that updates the same Shuffleboard entries twice, wasting CPU cycles every 20ms. This is clearly a copy-paste error.

**Current Code:**
```java
@Override
public void periodic() {
    sbFeederOnTgt.setBoolean(onFeederTarget());
    sbFuelAvail.setBoolean(isFuelAvail());
    sbFuelVolts.setDouble(lib.SBFormat(getFuelVolts()));
    sbFuelDist.setDouble(lib.SBFormat(getFuelDist()));
    sbFuelVolts.setDouble(lib.SBFormat(getFuelVolts()));      // DUPLICATE
    sbFuelDist.setDouble(lib.SBFormat(getFuelDist()));        // DUPLICATE
    sbFeederSP.setString(getFeederSP().name());
    sbFeederSPPct.setDouble(lib.SBFormat(getFeederSP(false)));
    sbFeederSPRPM.setDouble(lib.SBFormat(getFeederSP(true)));
    sbFeederVelPct.setDouble(lib.SBFormat(getFeederVel(false)));
    sbFeederVelRPM.setDouble(lib.SBFormat(getFeederVel(true)));
    sbFeederSPPct.setDouble(lib.SBFormat(getFeederSP(false)));  // DUPLICATE
    sbFeederSPRPM.setDouble(lib.SBFormat(getFeederSP(true)));   // DUPLICATE
    sbFeederVelPct.setDouble(lib.SBFormat(getFeederVel(false))); // DUPLICATE
    sbFeederVelRPM.setDouble(lib.SBFormat(getFeederVel(true))); // DUPLICATE
}
```

**Fix:**
```java
@Override
public void periodic() {
    sbFeederOnTgt.setBoolean(onFeederTarget());
    sbFuelAvail.setBoolean(isFuelAvail());
    sbFuelVolts.setDouble(lib.SBFormat(getFuelVolts()));
    sbFuelDist.setDouble(lib.SBFormat(getFuelDist()));
    sbFeederSP.setString(getFeederSP().name());
    sbFeederSPPct.setDouble(lib.SBFormat(getFeederSP(false)));
    sbFeederSPRPM.setDouble(lib.SBFormat(getFeederSP(true)));
    sbFeederVelPct.setDouble(lib.SBFormat(getFeederVel(false)));
    sbFeederVelRPM.setDouble(lib.SBFormat(getFeederVel(true)));
}
```

---

## Medium Priority Issues

### 2. Multiple Magic Numbers in Fuel Sensor Calculations (MEDIUM - Maintainability)
**Location:** Lines 208, 230, 234  
**Issue:** Multiple magic numbers appear without explanation, making the code hard to understand and maintain.

**Current Code:**
```java
feederController.setSetpoint(getFeederSP(false)/100.0*12.0, SparkBase.ControlType.kVoltage);

public double getFuelDist() {
    return getFuelVolts() / (5.0 / 1024.0) / (25.4 / 5.0);
}

public boolean isFuelAvail() {
    return !(getFuelDist() > 21.0 && getFuelDist() < 30.0);
}
```

**Fix:**
```java
// Add constants at top of class
/** Maximum battery voltage used for voltage control mode */
private static final double MAX_VOLTAGE = 12.0;

/** Maximum voltage output from fuel sensor */
private static final double SENSOR_MAX_VOLTAGE = 5.0;

/** ADC resolution of fuel sensor */
private static final double SENSOR_RESOLUTION = 1024.0;

/** Conversion factor: millimeters per inch */
private static final double MM_PER_INCH = 25.4;

/** Minimum distance in mm indicating no fuel present */
private static final double FUEL_MIN_DIST_MM = 21.0;

/** Maximum distance in mm indicating no fuel present */
private static final double FUEL_MAX_DIST_MM = 30.0;

// Update methods
public void setFeederVel(FeederSP sp) {
    setFeederSP(sp);
    feederController.setSetpoint(getFeederSP(false) / 100.0 * MAX_VOLTAGE, SparkBase.ControlType.kVoltage);
}

/**
 * Gets the distance reading from the fuel sensor.
 * Converts sensor voltage to distance in millimeters.
 * @return Distance in millimeters
 */
public double getFuelDist() {
    double voltsPerUnit = SENSOR_MAX_VOLTAGE / SENSOR_RESOLUTION;
    double mmPerVolt = MM_PER_INCH / SENSOR_MAX_VOLTAGE;
    return getFuelVolts() / voltsPerUnit / mmPerVolt;
}

/**
 * Checks if fuel is available in the feeder.
 * Returns true when sensor detects an object (distance outside the 21-30mm "no fuel" range).
 * @return True if fuel is detected, false otherwise
 */
public boolean isFuelAvail() {
    double dist = getFuelDist();
    return !(dist > FUEL_MIN_DIST_MM && dist < FUEL_MAX_DIST_MM);
}
```

### 3. Unclear Fuel Sensor Distance Logic (MEDIUM - Functionality)
**Location:** Lines 233-235  
**Issue:** The `isFuelAvail()` method uses inverted logic that's hard to understand. It returns true when distance is NOT in a certain range.

**Fix:**
```java
/**
 * Checks if fuel is available in the feeder.
 * The sensor returns distances between 21-30mm when no fuel is present.
 * Any distance outside this range indicates fuel is detected.
 * @return True if fuel is detected, false otherwise
 */
public boolean isFuelAvail() {
    double dist = getFuelDist();
    // Fuel is present when distance is outside the "no fuel" range
    return dist <= FUEL_MIN_DIST_MM || dist >= FUEL_MAX_DIST_MM;
}
```

### 4. Duplicate RPM/Percentage Conversion Logic (MEDIUM - Maintainability)
**Location:** Lines 67, 217  
**Issue:** Conversion logic is duplicated.

**Fix:**
```java
// Add helper methods
private double pctToRpm(double pct) {
    return (pct / 100.0) * Constants.MotorConstants.kNeoFreeSpeedRpm;
}

private double rpmToPct(double rpm) {
    return (rpm / Constants.MotorConstants.kNeoFreeSpeedRpm) * 100.0;
}

// Update FeederSP enum
public double getVel(boolean rpm) {
    return rpm ? pctToRpm(pct) : pct;
}

// Update getFeederVel method
public double getFeederVel(boolean rpm) {
    return rpm ? feederEncoder.getVelocity() : rpmToPct(feederEncoder.getVelocity());
}
```

### 5. Missing JavaDoc for Public Methods (MEDIUM - Maintainability)
**Location:** Lines 160-235  
**Issue:** All public methods lack JavaDoc documentation, especially the fuel sensor methods which have complex calculations.

**Fix:** Add JavaDoc to all public methods (see examples in sections above and below).

---

## Low Priority Issues

### 6. Generic Variable Name 'lib' (LOW - Maintainability)
**Location:** Line 46  
**Issue:** Variable name 'lib' is too generic.

**Fix:**
```java
private Library utilities = new Library();
// Update all references from lib.method() to utilities.method()
```

### 7. Commented-Out Code Should Be Removed (LOW - Maintainability)
**Location:** Lines 82-83, 209-210  
**Issue:** Commented-out code should be removed or documented.

**Fix:**
```java
// Remove these lines:
// private final ShuffleboardTab cmdTab = Shuffleboard.getTab("Commands");
// private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");

// And these lines:
//  .kMAXMotionVelocityControl);
//    feederController.setSetpoint(Constants.MotorConstants.kNeoFreeSpeedRpm * .80, SparkBase.ControlType.kMAXMotionVelocityControl);
```

### 8. Debug Print Statements (LOW - Maintainability)
**Location:** Lines 112, 154  
**Issue:** Using `System.out.println` instead of proper logging.

**Fix:**
```java
// Remove or replace with proper logging
```

### 9. Inconsistent Indentation (LOW - Style)
**Location:** Lines 117, 132-134  
**Issue:** Uses tabs instead of spaces inconsistently.

**Fix:** Use consistent indentation throughout the file.

### 10. Typo in Section Comment (LOW - Maintainability)
**Location:** Line 164  
**Issue:** Comment has typo: `========()======`

**Fix:**
```java
// ==============================================================
// Periodic methods
// ==============================================================
```

### 11. Spelling Error: 'Tolerance' Should Be 'Tolerance' (LOW - Maintainability)
**Location:** Line 222 (Constants line 387)  
**Issue:** Misspelling throughout codebase.

**Fix in Constants.java:**
```java
public static final double kTolerance = 0.5;  // was kTolerance
```

**Fix in Feeder.java:**
```java
public boolean onFeederTarget() {
    return Math.abs(getFeederVel(true) - getFeederSP(true)) < Constants.Feeder.kTolerance;
}
```

### 12. Extra Blank Line in Enum Method (LOW - Style)
**Location:** Line 66  
**Issue:** Unnecessary blank line in the middle of method.

**Fix:**
```java
public double getVel(boolean rpm) {
    if (rpm) {
        return pctToRpm(pct);
    } else {
        return pct;
    }
}
```

---

## Complete JavaDoc Examples

```java
/**
 * Creates a command to set the feeder velocity to a preset setpoint.
 * @param sp The feeder setpoint enum value
 * @return Command that sets the feeder velocity once
 */
public Command setFeeder(FeederSP sp) {
    return runOnce(() -> this.setFeederVel(sp));
}

/**
 * Sets the feeder setpoint.
 * @param sp The feeder setpoint enum value
 */
public void setFeederSP(FeederSP sp) {
    feederSP = sp;
}

/**
 * Gets the current feeder setpoint.
 * @return The feeder setpoint enum value
 */
public FeederSP getFeederSP() {
    return feederSP;
}

/**
 * Gets the feeder setpoint value.
 * @param rpm If true, returns the setpoint in RPM. If false, returns as percentage.
 * @return The feeder setpoint value
 */
public double getFeederSP(boolean rpm) {
    return feederSP.getVel(rpm);
}

/**
 * Sets the feeder velocity to a preset setpoint.
 * @param sp The feeder setpoint enum value
 */
public void setFeederVel(FeederSP sp) {
    setFeederSP(sp);
    feederController.setSetpoint(getFeederSP(false) / 100.0 * MAX_VOLTAGE, SparkBase.ControlType.kVoltage);
}

/**
 * Gets the current feeder velocity.
 * @param rpm If true, returns velocity in RPM. If false, returns as percentage.
 * @return The feeder velocity
 */
public double getFeederVel(boolean rpm) {
    return rpm ? feederEncoder.getVelocity() : rpmToPct(feederEncoder.getVelocity());
}

/**
 * Checks if the feeder is at the target velocity.
 * @return True if within tolerance, false otherwise
 */
public boolean onFeederTarget() {
    return Math.abs(getFeederVel(true) - getFeederSP(true)) < Constants.Feeder.kTolerance;
}

/**
 * Gets the raw voltage reading from the fuel sensor.
 * @return Sensor voltage in volts
 */
public double getFuelVolts() {
    return fuelSensor.getVoltage();
}
```

---

## Implementation Priority

1. **Critical (Fix Immediately)**
   - Remove duplicate Shuffleboard updates in periodic() (lines 173-174, 180-183)

2. **High Priority**
   - Extract magic numbers to constants
   - Clarify fuel sensor logic and add documentation

3. **Medium Priority**
   - Centralize RPM/percentage conversion logic
   - Add JavaDoc to all public methods

4. **Low Priority**
   - Clean up commented code
   - Fix formatting and typos
   - Improve variable names
   - Fix spelling errors

---

## Testing Recommendations

After making changes:
1. **Critical:** Verify periodic() updates Shuffleboard correctly without duplicates
2. Test feeder velocity control at all setpoints (OFF, LOW, MED, HI)
3. **Important:** Test fuel sensor readings:
   - Verify voltage readings are correct
   - Verify distance calculations are accurate
   - Test isFuelAvail() with and without fuel present
   - Confirm the 21-30mm range correctly indicates "no fuel"
4. Verify RPM/percentage conversions are accurate
5. Test tolerance checking

---

## Fuel Sensor Calibration Notes

The fuel sensor logic assumes:
- Sensor returns distances between 21-30mm when **no fuel** is present
- Distances outside this range indicate **fuel is present**
- This may need calibration based on actual sensor behavior

**Recommended Testing:**
1. Measure actual sensor readings with no fuel
2. Measure actual sensor readings with fuel present
3. Adjust FUEL_MIN_DIST_MM and FUEL_MAX_DIST_MM constants if needed
4. Document the sensor model and expected behavior

---

## Overall Assessment

Feeder.java has **one critical issue** (duplicate periodic updates) that should be fixed immediately. The other issues are primarily code quality improvements.

**Priority Actions:**
1. Fix duplicate periodic() updates (2 minutes) - **CRITICAL**
2. Extract magic numbers to constants (10 minutes)
3. Clarify fuel sensor logic (5 minutes)
4. Add JavaDoc documentation (15 minutes)

Total estimated time for critical fixes: ~30 minutes

**Strengths:**
- Simple, focused subsystem
- Good use of enums for setpoints
- Includes fuel sensor integration

**Areas for Improvement:**
- Remove duplicate code
- Better documentation of fuel sensor behavior
- Extract magic numbers
- Complete documentation