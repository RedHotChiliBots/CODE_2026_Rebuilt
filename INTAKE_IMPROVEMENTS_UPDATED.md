# Intake.java - Updated Code Improvement Guide

## Overview
**File**: [`Intake.java`](src/main/java/frc/robot/subsystems/Intake.java:1)  
**Purpose**: Controls intake wheels and tilt mechanism for game piece collection  
**Total Issues Found**: 8 (1 Critical, 1 High, 1 Medium, 5 Low)  
**Recent Changes**: Library made static, helper methods added, encoder preset applied

---

## Summary of Issues

### Critical Priority (1)
1. **MAXMotion configured but not used for intake velocity control** - Using voltage control instead

### High Priority (1)
1. **MAXMotion configured but not used for tilt position control** - Using basic position control instead

### Medium Priority (1)
1. **Inconsistent command creation pattern** - runOnce() vs run() for velocity control

### Low Priority (5)
1. **TiltSP.DEPLOY value changed without documentation** - Changed from 80.0 to 0.9 without explanation
2. **Helper method pctToVolt is public but only used internally** - Should be private
3. **Helper methods lack JavaDoc** - No documentation for conversion methods
4. **Inconsistent ternary operator formatting** - Mixed styles in similar methods
5. **Magic number 12.0 in pctToVolt helper** - Should use named constant

---

## Detailed Issues and Fixes

### 1. CRITICAL: MAXMotion configured but not used for intake velocity control
**Location**: [`Intake.java:289`](src/main/java/frc/robot/subsystems/Intake.java:289)  
**Issue**: Line 289 uses `kVoltage` control instead of `kMAXMotionVelocityControl`. The MAXMotion configuration on lines 145-149 is completely ignored.

**Current Code (WRONG)**:
```java
public void setIntakeVel(IntakeSP sp) {
  setIntakeSP(sp);
  intakeController.setSetpoint(pctToVolt(getIntakeSP(false)), SparkBase.ControlType.kVoltage);
  // , SparkBase.ControlType.kVelocity);  // <-- COMMENTED OUT!
}
```

**Problems**:
1. ❌ Using open-loop voltage control → no velocity feedback
2. ❌ MAXMotion configuration (lines 145-149) is completely wasted
3. ❌ No acceleration limiting → jerky motion, mechanical stress
4. ❌ Inconsistent speeds under varying battery voltage

**Fixed Code**:
```java
public void setIntakeVel(IntakeSP sp) {
  setIntakeSP(sp);
  // Use MAXMotion velocity control with configured cruise velocity and acceleration
  intakeController.setSetpoint(getIntakeSP(true), SparkBase.ControlType.kMAXMotionVelocityControl);
}
```

**Why This Matters**:
- Voltage control provides no feedback → speed varies with battery voltage
- No acceleration limiting → sudden starts/stops damage mechanisms
- MAXMotion provides smooth trapezoidal velocity profiles
- Closed-loop control maintains consistent speeds

**Estimated Fix Time**: 1 minute

---

### 2. HIGH: MAXMotion configured but not used for tilt position control
**Location**: [`Intake.java:311`](src/main/java/frc/robot/subsystems/Intake.java:311)  
**Issue**: Line 311 uses basic `kPosition` control instead of `kMAXMotionPositionControl`. The MAXMotion configuration on lines 174-178 is ignored.

**Current Code (WRONG)**:
```java
public void setTiltPos(TiltSP sp) {
  setTiltSP(sp);
  tiltController.setSetpoint(getTiltSP().getPos(), SparkBase.ControlType.kPosition); // kMAXMotionPositionControl);
}
```

**Problems**:
1. ❌ Using basic position control → no motion profiling
2. ❌ MAXMotion configuration (lines 174-178) is wasted
3. ❌ Tilt moves at full speed → potential for damage or game piece loss

**Fixed Code**:
```java
public void setTiltPos(TiltSP sp) {
  setTiltSP(sp);
  // Use MAXMotion position control with configured cruise velocity and acceleration
  tiltController.setSetpoint(getTiltSP().getPos(), SparkBase.ControlType.kMAXMotionPositionControl);
}
```

**Why This Matters**:
- Basic position control moves at full speed → jerky motion
- MAXMotion provides smooth trapezoidal motion profiles
- Controlled acceleration prevents game piece loss during tilt
- Reduces mechanical stress on tilt mechanism

**Estimated Fix Time**: 1 minute

---

### 3. MEDIUM: Inconsistent command creation pattern
**Location**: [`Intake.java:210`](src/main/java/frc/robot/subsystems/Intake.java:210)  
**Issue**: Line 210 uses `runOnce()` for setIntake() but velocity control typically needs continuous execution.

**Current Code**:
```java
public Command setIntake(IntakeSP sp) {
  return runOnce(() -> this.setIntakeVel(sp));
}
```

**Analysis**:
- `runOnce()` executes once and ends immediately
- For velocity control, this is actually correct IF using closed-loop control
- However, with current voltage control (line 289), continuous execution might be needed
- Once MAXMotion is enabled (Fix #1), `runOnce()` is appropriate

**Recommendation**:
```java
public Command setIntake(IntakeSP sp) {
  // runOnce is correct for closed-loop velocity control
  // The controller maintains the setpoint automatically
  return runOnce(() -> this.setIntakeVel(sp));
}
```

**Note**: This is only an issue because voltage control is being used. Once MAXMotion velocity control is enabled (Fix #1), `runOnce()` is the correct pattern.

**Estimated Fix Time**: 0 minutes (will be resolved by Fix #1)

---

### 4. LOW: TiltSP.DEPLOY value changed without documentation
**Location**: [`Intake.java:75`](src/main/java/frc/robot/subsystems/Intake.java:75)  
**Issue**: DEPLOY changed from 80.0 degrees to 0.9 (likely rotations) without explanation.

**Current Code**:
```java
public enum TiltSP {
  STOW(0.0),
  DEPLOY(0.9);  // <-- Changed from 80.0 degrees, no comment explaining why
```

**Fixed Code**:
```java
public enum TiltSP {
  STOW(0.0),
  DEPLOY(0.9);  // 0.9 rotations = ~324 degrees (encoder now uses rotations via REV preset)
```

**Why This Matters**:
- Significant value change without documentation is confusing
- Future developers won't understand the unit change
- Comment explains the encoder configuration change (line 167)

**Estimated Fix Time**: 1 minute

---

### 5. LOW: Helper method pctToVolt is public but only used internally
**Location**: [`Intake.java:275`](src/main/java/frc/robot/subsystems/Intake.java:275)  
**Issue**: `pctToVolt()` is public but only used within this class.

**Current Code**:
```java
public double pctToVolt(double percent) {
  return percent / 100.0 * 12.0;
}
```

**Fixed Code**:
```java
/**
 * Converts a percentage (0-100) to voltage (0-12V).
 * 
 * @param percent The percentage value (0-100)
 * @return The voltage value (0-12V)
 */
private double pctToVolt(double percent) {
  return percent / 100.0 * Constants.MAX_VOLTAGE;
}
```

**Why This Matters**:
- Public methods are part of the class's API
- Internal helper methods should be private
- Encapsulation prevents misuse from other classes

**Estimated Fix Time**: 1 minute

---

### 6. LOW: Helper methods lack JavaDoc
**Location**: [`Intake.java:275-285`](src/main/java/frc/robot/subsystems/Intake.java:275)  
**Issue**: Helper methods (pctToVolt, pctToRpm, rpmToPct) lack documentation.

**Fixed Code**:
```java
/**
 * Converts a percentage (0-100) to voltage (0-12V).
 * 
 * @param percent The percentage value (0-100)
 * @return The voltage value (0-12V)
 */
private double pctToVolt(double percent) {
  return percent / 100.0 * Constants.MAX_VOLTAGE;
}

/**
 * Converts a percentage (0-100) to RPM based on NEO free speed.
 * 
 * @param pct The percentage value (0-100)
 * @return The RPM value
 */
private double pctToRpm(double pct) {
  return (pct / 100.0) * Constants.MotorConstants.kNeoFreeSpeedRpm;
}

/**
 * Converts RPM to a percentage (0-100) based on NEO free speed.
 * 
 * @param rpm The RPM value
 * @return The percentage value (0-100)
 */
private double rpmToPct(double rpm) {
  return (rpm / Constants.MotorConstants.kNeoFreeSpeedRpm) * 100.0;
}
```

**Estimated Fix Time**: 3 minutes

---

### 7. LOW: Inconsistent ternary operator formatting
**Location**: [`Intake.java:294`](src/main/java/frc/robot/subsystems/Intake.java:294)  
**Issue**: Line 294 uses inline ternary while line 271 uses if-else for similar logic.

**Current Code**:
```java
// Line 271 - if-else block
public double getIntakeSP(boolean rpm) {
  return intakeSP.getVel(rpm);
}

// Line 294 - ternary operator
public double getIntakeVel(boolean rpm) {
  return rpm ? intakeEncoder.getVelocity() : rpmToPct(intakeEncoder.getVelocity());
}
```

**Recommendation**: Use ternary operators consistently for simple boolean returns:
```java
public double getIntakeSP(boolean rpm) {
  return rpm ? intakeSP.getVel(true) : intakeSP.getVel(false);
}

public double getIntakeVel(boolean rpm) {
  return rpm ? intakeEncoder.getVelocity() : rpmToPct(intakeEncoder.getVelocity());
}
```

**Estimated Fix Time**: 1 minute

---

### 8. LOW: Magic number 12.0 in pctToVolt helper
**Location**: [`Intake.java:276`](src/main/java/frc/robot/subsystems/Intake.java:276)  
**Issue**: Magic number 12.0 for voltage conversion should use a named constant.

**Current Code**:
```java
public double pctToVolt(double percent) {
  return percent / 100.0 * 12.0;  // <-- Magic number
}
```

**Fixed Code**:
```java
// Add to Constants.java
public static final double MAX_VOLTAGE = 12.0;

// In Intake.java
private double pctToVolt(double percent) {
  return percent / 100.0 * Constants.MAX_VOLTAGE;
}
```

**Why This Matters**:
- Named constant makes the purpose clear
- Easier to change if voltage limits change
- Consistent with other constants in the codebase

**Estimated Fix Time**: 2 minutes

---

## Positive Changes Since Last Review

### ✅ Improvements Made
1. **Library made static** (line 224-232) - Correct usage of static utility methods
2. **Helper methods added** (lines 275-285) - Good code organization
3. **Encoder preset applied** (line 167) - Using REV ThroughBoreEncoderV2 preset
4. **Tab names updated** (lines 96-97) - More descriptive names

### ✅ Good Practices Maintained
1. **Proper encoder configuration** - Absolute encoder for tilt, relative for intake
2. **Shuffleboard integration** - Good telemetry for debugging
3. **Enum-based setpoints** - Type-safe setpoint management
4. **Proper motor configuration** - Current limits, idle modes, inversions

---

## Complete Fix Implementation

### Step 1: Enable MAXMotion for Intake Velocity Control (CRITICAL)

**File**: [`Intake.java:289`](src/main/java/frc/robot/subsystems/Intake.java:289)

**Change**:
```java
// BEFORE
intakeController.setSetpoint(pctToVolt(getIntakeSP(false)), SparkBase.ControlType.kVoltage);

// AFTER
intakeController.setSetpoint(getIntakeSP(true), SparkBase.ControlType.kMAXMotionVelocityControl);
```

**Remove line 290** (commented-out code):
```java
// , SparkBase.ControlType.kVelocity);  // <-- DELETE THIS LINE
```

---

### Step 2: Enable MAXMotion for Tilt Position Control (HIGH)

**File**: [`Intake.java:311`](src/main/java/frc/robot/subsystems/Intake.java:311)

**Change**:
```java
// BEFORE
tiltController.setSetpoint(getTiltSP().getPos(), SparkBase.ControlType.kPosition); // kMAXMotionPositionControl);

// AFTER
tiltController.setSetpoint(getTiltSP().getPos(), SparkBase.ControlType.kMAXMotionPositionControl);
```

---

### Step 3: Add Documentation and Fix Helper Methods (LOW)

**File**: [`Intake.java:75`](src/main/java/frc/robot/subsystems/Intake.java:75)

**Add comment**:
```java
DEPLOY(0.9);  // 0.9 rotations = ~324 degrees (encoder now uses rotations via REV preset)
```

**File**: [`Intake.java:275-285`](src/main/java/frc/robot/subsystems/Intake.java:275)

**Update helper methods**:
```java
/**
 * Converts a percentage (0-100) to voltage (0-12V).
 * 
 * @param percent The percentage value (0-100)
 * @return The voltage value (0-12V)
 */
private double pctToVolt(double percent) {
  return percent / 100.0 * Constants.MAX_VOLTAGE;
}

/**
 * Converts a percentage (0-100) to RPM based on NEO free speed.
 * 
 * @param pct The percentage value (0-100)
 * @return The RPM value
 */
private double pctToRpm(double pct) {
  return (pct / 100.0) * Constants.MotorConstants.kNeoFreeSpeedRpm;
}

/**
 * Converts RPM to a percentage (0-100) based on NEO free speed.
 * 
 * @param rpm The RPM value
 * @return The percentage value (0-100)
 */
private double rpmToPct(double rpm) {
  return (rpm / Constants.MotorConstants.kNeoFreeSpeedRpm) * 100.0;
}
```

---

### Step 4: Add MAX_VOLTAGE Constant

**File**: `Constants.java`

**Add to appropriate section**:
```java
/** Maximum battery voltage for voltage control calculations */
public static final double MAX_VOLTAGE = 12.0;
```

---

## Testing Procedure

### After Enabling MAXMotion

1. **Deploy Code**: Deploy updated code to robot
2. **Test Intake Velocity**:
   - Run intake at LOW, MED, HI speeds
   - Verify smooth acceleration (not instant)
   - Check Shuffleboard for velocity tracking
   - Verify consistent speeds regardless of battery voltage
3. **Test Tilt Position**:
   - Command tilt between STOW and DEPLOY
   - Verify smooth, controlled motion
   - Check that game pieces don't fall out during tilt
   - Verify position accuracy

### Tuning MAXMotion Parameters

If motion is too slow or too fast, adjust these constants in Constants.java:

**Intake Velocity**:
- `Constants.Intake.kIntakeMaxVel` - Maximum velocity in RPM
- `Constants.Intake.kIntakeMaxAccel` - Maximum acceleration in RPM/sec

**Tilt Position**:
- `Constants.Intake.kTiltMaxVel` - Maximum velocity in degrees/sec (or rotations/sec)
- `Constants.Intake.kTiltMaxAccel` - Maximum acceleration in degrees/sec² (or rotations/sec²)

---

## Implementation Priority

### Phase 1: Critical Fixes (2 minutes)
1. ✅ Enable MAXMotion for intake velocity control (line 289)
2. ✅ Enable MAXMotion for tilt position control (line 311)

### Phase 2: Code Quality (5 minutes)
1. Add documentation to TiltSP.DEPLOY (line 75)
2. Make helper methods private and add JavaDoc (lines 275-285)
3. Add MAX_VOLTAGE constant to Constants.java

### Phase 3: Testing (30 minutes)
1. Test intake velocity control with MAXMotion
2. Test tilt position control with MAXMotion
3. Tune parameters if needed

**Total Estimated Time**: 37 minutes

---

## Why MAXMotion Matters

### Without MAXMotion (Current State):
- ❌ Intake uses voltage control → no velocity feedback, inconsistent speeds
- ❌ Tilt uses basic position control → full-speed motion, jerky
- ❌ No acceleration limiting → mechanical stress, game piece loss
- ❌ Battery voltage affects performance

### With MAXMotion (After Fixes):
- ✅ Closed-loop velocity control → consistent speeds
- ✅ Smooth trapezoidal motion profiles → reduced stress
- ✅ Configurable velocity and acceleration limits
- ✅ Automatic trajectory generation
- ✅ Battery voltage compensation built-in

---

## Related Files
- [`Constants.java`](src/main/java/frc/robot/Constants.java:1) - Configuration constants
- [`Library.java`](src/main/java/frc/robot/utils/Library.java:1) - Utility methods (now static)
- [`MAXMOTION_ISSUES.md`](MAXMOTION_ISSUES.md:1) - Comprehensive MAXMotion diagnostic

---

**All 8 issues have been added to the Bob Findings panel for tracking.**