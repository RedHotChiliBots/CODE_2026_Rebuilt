# Intake.java Improvement Guide

This document outlines recommended improvements for `src/main/java/frc/robot/subsystems/Intake.java` based on a comprehensive code review.

## High Priority Issues

**Good News:** Intake.java has no high-priority issues! The code is generally well-structured and functional.

---

## Medium Priority Issues

### 1. Incorrect Constant Used in Tilt Configuration (MEDIUM - Functionality)
**Location:** Line 179  
**Issue:** Uses `Constants.Intake.kIntakeAllowedErr` for tilt configuration instead of `Constants.Intake.kTiltAllowedErr`. This is a copy-paste error that could affect tilt motion profiling.

**Current Code:**
```java
tiltConfig.closedLoop.maxMotion
    .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
    .cruiseVelocity(Constants.Intake.kTiltMaxVel)
    .maxAcceleration(Constants.Intake.kTiltMaxAccel)
    .allowedProfileError(Constants.Intake.kIntakeAllowedErr);  // WRONG CONSTANT
```

**Fix:**
```java
tiltConfig.closedLoop.maxMotion
    .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
    .cruiseVelocity(Constants.Intake.kTiltMaxVel)
    .maxAcceleration(Constants.Intake.kTiltMaxAccel)
    .allowedProfileError(Constants.Intake.kTiltAllowedErr);  // CORRECT CONSTANT
```

### 2. Magic Number 12.0 in Voltage Calculation (MEDIUM - Maintainability)
**Location:** Line 277  
**Issue:** The value `12.0` appears without explanation.

**Fix:**
```java
// Add constant at top of class
/** Maximum battery voltage used for voltage control mode */
private static final double MAX_VOLTAGE = 12.0;

// Update method
public void setIntakeVel(IntakeSP sp) {
    setIntakeSP(sp);
    intakeController.setSetpoint(getIntakeSP(false) / 100.0 * MAX_VOLTAGE, SparkBase.ControlType.kVoltage);
}
```

### 3. Duplicate RPM/Percentage Conversion Logic (MEDIUM - Maintainability)
**Location:** Lines 66, 285  
**Issue:** Conversion logic is duplicated between the enum and getIntakeVel method.

**Fix:**
```java
// Add helper methods
private double pctToRpm(double pct) {
    return (pct / 100.0) * Constants.MotorConstants.kNeoFreeSpeedRpm;
}

private double rpmToPct(double rpm) {
    return (rpm / Constants.MotorConstants.kNeoFreeSpeedRpm) * 100.0;
}

// Update IntakeSP enum
public double getVel(boolean rpm) {
    return rpm ? pctToRpm(pct) : pct;
}

// Update getIntakeVel method
public double getIntakeVel(boolean rpm) {
    return rpm ? intakeEncoder.getVelocity() : rpmToPct(intakeEncoder.getVelocity());
}
```

### 4. setIntake Command Uses run() Instead of runOnce() (MEDIUM - Functionality)
**Location:** Line 211  
**Issue:** `setIntake` uses `run()` which continuously calls `setIntakeVel`, while `setTilt` uses `runOnce()`. This inconsistency may cause unnecessary repeated calls.

**Current Code:**
```java
public Command setIntake(IntakeSP sp) {
    return run(() -> this.setIntakeVel(sp));  // Runs continuously
}

public Command setTilt(TiltSP sp) {
    return runOnce(() -> this.setTiltPos(sp));  // Runs once
}
```

**Fix:**
```java
/**
 * Creates a command to set the intake velocity to a preset setpoint.
 * @param sp The intake setpoint enum value
 * @return Command that sets the intake velocity once
 */
public Command setIntake(IntakeSP sp) {
    return runOnce(() -> this.setIntakeVel(sp));
}
```

**Note:** If continuous updates are intentionally required for the intake (e.g., to maintain velocity against varying loads), document this with a comment:
```java
/**
 * Creates a command to continuously set the intake velocity.
 * Uses run() instead of runOnce() to maintain velocity setpoint.
 * @param sp The intake setpoint enum value
 * @return Command that continuously updates intake velocity
 */
public Command setIntake(IntakeSP sp) {
    return run(() -> this.setIntakeVel(sp));
}
```

---

## Low Priority Issues

### 5. Generic Variable Name 'lib' (LOW - Maintainability)
**Location:** Line 46  
**Issue:** Variable name 'lib' is too generic.

**Fix:**
```java
private Library utilities = new Library();
// Update all references from lib.method() to utilities.method()
```

### 6. Commented-Out Code Should Be Removed (LOW - Maintainability)
**Location:** Lines 96-97, 278  
**Issue:** Commented-out code should be removed or documented.

**Fix:**
```java
// Remove these lines:
// private final ShuffleboardTab cmdTab = Shuffleboard.getTab("Commands");
// private final ShuffleboardTab compTab = Shuffleboard.getTab("Competition");

// And this line:
// , SparkBase.ControlType.kVelocity);
```

### 7. Debug Print Statements (LOW - Maintainability)
**Location:** Lines 129, 204  
**Issue:** Using `System.out.println` instead of proper logging.

**Fix:**
```java
// Remove or replace with proper logging
// Option 1: Remove for production
// Option 2: Use WPILib DataLog
// Option 3: Keep only critical startup messages
```

### 8. Misleading Comment in IntakeSP Enum (LOW - Maintainability)
**Location:** Line 51  
**Issue:** Comment says "The Feeder SP" but this is the Intake enum.

**Fix:**
```java
// The Intake SP is stored as a percentage of RPMs
public enum IntakeSP {
```

### 9. Spelling Error: 'Tolerance' Should Be 'Tolerance' (LOW - Maintainability)
**Location:** Lines 290, 311 (Constants lines 332-333)  
**Issue:** Misspelling throughout codebase.

**Fix in Constants.java:**
```java
public static final double kIntakeTolerance = 0.5;  // was kIntakeTolerance
public static final double kTiltTolerance = 0.5;    // was kTiltTolerance
```

**Fix in Intake.java:**
```java
public boolean onIntakeTarget() {
    return Math.abs(getIntakeVel(true) - getIntakeSP(true)) < Constants.Intake.kIntakeTolerance;
}

public boolean onTiltTarget() {
    return Math.abs(getTiltPos() - getTiltSP().getPos()) < Constants.Intake.kTiltTolerance;
}
```

### 10. Incomplete JavaDoc Coverage (LOW - Maintainability)
**Location:** Lines 275-312  
**Issue:** Some methods have JavaDoc while others don't.

**Fix:** Add JavaDoc to all public methods:
```java
/**
 * Sets the intake velocity to a preset setpoint.
 * @param sp The intake setpoint enum value
 */
public void setIntakeVel(IntakeSP sp) {
    setIntakeSP(sp);
    intakeController.setSetpoint(getIntakeSP(false) / 100.0 * MAX_VOLTAGE, SparkBase.ControlType.kVoltage);
}

/**
 * Gets the current intake velocity.
 * @param rpm If true, returns velocity in RPM. If false, returns as percentage.
 * @return The intake velocity
 */
public double getIntakeVel(boolean rpm) {
    if (rpm) {
        return intakeEncoder.getVelocity();
    } else {
        return rpmToPct(intakeEncoder.getVelocity());
    }
}

/**
 * Checks if the intake is at the target velocity.
 * @return True if within tolerance, false otherwise
 */
public boolean onIntakeTarget() {
    return Math.abs(getIntakeVel(true) - getIntakeSP(true)) < Constants.Intake.kIntakeTolerance;
}

/**
 * Gets the current tilt position.
 * @return The tilt position in degrees
 */
public double getTiltPos() {
    return tiltEncoder.getPosition();
}

/**
 * Sets the tilt setpoint.
 * @param sp The tilt setpoint enum value
 */
public void setTiltSP(TiltSP sp) {
    tiltSP = sp;
}

/**
 * Sets the tilt position to a preset setpoint.
 * @param sp The tilt setpoint enum value
 */
public void setTiltPos(TiltSP sp) {
    setTiltSP(sp);
    tiltController.setSetpoint(getTiltSP().getPos(), SparkBase.ControlType.kMAXMotionPositionControl);
}

/**
 * Gets the current tilt setpoint.
 * @return The tilt setpoint enum value
 */
public TiltSP getTiltSP() {
    return tiltSP;
}

/**
 * Checks if the tilt is at the target position.
 * @return True if within tolerance, false otherwise
 */
public boolean onTiltTarget() {
    return Math.abs(getTiltPos() - getTiltSP().getPos()) < Constants.Intake.kTiltTolerance;
}
```

---

## Implementation Priority

1. **High Priority (Fix First)**
   - Fix incorrect constant in tilt configuration (line 179)
   - Decide on run() vs runOnce() for setIntake command

2. **Medium Priority**
   - Extract magic number to constant
   - Centralize RPM/percentage conversion logic

3. **Low Priority**
   - Clean up commented code
   - Improve variable names
   - Fix spelling errors
   - Complete JavaDoc coverage
   - Remove debug statements

---

## Code Quality Summary

**Strengths:**
- Well-organized structure with clear sections
- Good use of enums for setpoints
- Proper motor configuration
- Some methods already have JavaDoc documentation
- Good separation of concerns

**Areas for Improvement:**
- Fix the copy-paste error in tilt configuration
- Standardize command creation (run vs runOnce)
- Extract magic numbers
- Complete documentation
- Remove dead code

---

## Testing Recommendations

After making changes:
1. Test intake velocity control at all setpoints (OFF, LOW, MED, HI)
2. Verify tilt motion with corrected allowedProfileError constant
3. Test that setIntake command behaves as expected (once vs continuous)
4. Verify RPM/percentage conversions are accurate
5. Test tolerance checking for both intake and tilt
6. Verify Shuffleboard displays update correctly

---

## Comparison with Similar Subsystems

Intake.java is very similar to Shooter.java in structure:
- Both have velocity-controlled wheels and position-controlled tilt mechanisms
- Both use similar enum patterns for setpoints
- Both have the same issues with magic numbers and conversion logic

**Recommendation:** Consider creating a base class or utility methods for common patterns shared between Intake and Shooter subsystems to reduce code duplication and maintain consistency.

---

## Overall Assessment

Intake.java is in **good condition** with mostly minor issues. The most important fix is the incorrect constant on line 179, which could affect tilt performance. The other issues are primarily code quality improvements that will make the code more maintainable and consistent.

**Priority Actions:**
1. Fix line 179 constant (5 minutes)
2. Decide on run() vs runOnce() for setIntake (5 minutes + testing)
3. Extract MAX_VOLTAGE constant (2 minutes)
4. Fix spelling errors (5 minutes)

Total estimated time for critical fixes: ~20 minutes