# MAXMotion Not Working - Diagnostic Report

## Executive Summary

**CRITICAL ISSUES FOUND**: MAXMotion is configured but **NOT BEING USED** in Intake.java, Feeder.java, and Climber.java. The code is using basic control types instead of MAXMotion control types, rendering all MAXMotion configuration useless.

---

## Issue #1: Intake.java - Tilt Motor Using MAXMotion Correctly ✅, Intake Motor NOT Using MAXMotion ❌

### Tilt Motor (WORKING) ✅
**Location**: [`Intake.java:303`](src/main/java/frc/robot/subsystems/Intake.java:303)

```java
// CORRECT - Using kMAXMotionPositionControl
tiltController.setSetpoint(getTiltSP().getPos(), SparkBase.ControlType.kMAXMotionPositionControl);
```

**Status**: ✅ **WORKING** - Tilt motor correctly uses `kMAXMotionPositionControl`

---

### Intake Motor (NOT WORKING) ❌
**Location**: [`Intake.java:277`](src/main/java/frc/robot/subsystems/Intake.java:277)

**Current Code (WRONG)**:
```java
public void setIntakeVel(IntakeSP sp) {
  setIntakeSP(sp);
  intakeController.setSetpoint(getIntakeSP(false) / 100.0 * 12.0, SparkBase.ControlType.kVoltage);
  // , SparkBase.ControlType.kVelocity);  // <-- COMMENTED OUT!
}
```

**Problems**:
1. ❌ Using `kVoltage` control instead of MAXMotion
2. ❌ The correct control type `kVelocity` is commented out
3. ❌ MAXMotion configuration on lines 147-151 is completely ignored
4. ❌ Manual voltage calculation (`getIntakeSP(false) / 100.0 * 12.0`) bypasses closed-loop control

**MAXMotion Configuration (Being Ignored)**:
```java
// Lines 147-151 - This configuration is NEVER USED
intakeConfig.closedLoop.maxMotion
    .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
    .cruiseVelocity(Constants.Intake.kIntakeMaxVel)
    .maxAcceleration(Constants.Intake.kIntakeMaxAccel)
    .allowedProfileError(Constants.Intake.kIntakeAllowedErr);
```

**Fixed Code**:
```java
public void setIntakeVel(IntakeSP sp) {
  setIntakeSP(sp);
  // Use MAXMotion velocity control with configured cruise velocity and acceleration
  intakeController.setSetpoint(getIntakeSP(true), SparkBase.ControlType.kMAXMotionVelocityControl);
}
```

**Why This Matters**:
- Without MAXMotion, the intake has no acceleration limiting → jerky motion, mechanical stress
- Voltage control is open-loop → no feedback, inconsistent speeds
- The PID gains and feedforward configured on lines 141-146 are wasted

---

## Issue #2: Feeder.java - NOT Using MAXMotion ❌

**Location**: [`Feeder.java:208`](src/main/java/frc/robot/subsystems/Feeder.java:208)

**Current Code (WRONG)**:
```java
public void setFeederVel(FeederSP sp) {
  setFeederSP(sp);
  feederController.setSetpoint(getFeederSP(false)/100.0*12.0, SparkBase.ControlType.kVoltage);
//  .kMAXMotionVelocityControl);  // <-- COMMENTED OUT!
//  feederController.setSetpoint(Constants.MotorConstants.kNeoFreeSpeedRpm * .80, SparkBase.ControlType.kMAXMotionVelocityControl);
}
```

**Problems**:
1. ❌ Using `kVoltage` control instead of MAXMotion
2. ❌ The correct control type `kMAXMotionVelocityControl` is commented out (line 209)
3. ❌ MAXMotion configuration on lines 130-134 is completely ignored
4. ❌ Manual voltage calculation bypasses closed-loop control

**MAXMotion Configuration (Being Ignored)**:
```java
// Lines 130-134 - This configuration is NEVER USED
feederConfig.closedLoop.maxMotion
    .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
    .cruiseVelocity(Constants.Feeder.kFeederMaxVel)
    .maxAcceleration(Constants.Feeder.kFeederMaxAccel)
    .allowedProfileError(Constants.Feeder.kFeederAllowedErr);
```

**Fixed Code**:
```java
public void setFeederVel(FeederSP sp) {
  setFeederSP(sp);
  // Use MAXMotion velocity control with configured cruise velocity and acceleration
  feederController.setSetpoint(getFeederSP(true), SparkBase.ControlType.kMAXMotionVelocityControl);
}
```

**Why This Matters**:
- Feeder needs smooth acceleration to avoid jamming game pieces
- Voltage control provides no velocity feedback → inconsistent feeding
- The PID gains and feedforward configured on lines 123-129 are wasted

---

## Issue #3: Climber.java - NOT Using MAXMotion ❌

**Location**: [`Climber.java:368`](src/main/java/frc/robot/subsystems/Climber.java:368)

**Current Code (WRONG)**:
```java
public void setClimberPos(ClimberSP pos) {
  setClimberSP(pos);
  climber1Controller.setSetpoint(pos.getValue(),
      SparkBase.ControlType.kPosition);  // <-- WRONG! Should be kMAXMotionPositionControl
}
```

**Problems**:
1. ❌ Using basic `kPosition` control instead of MAXMotion
2. ❌ No MAXMotion configuration exists in the code (lines 165-170 only configure PID)
3. ❌ Climber will move at full speed with no acceleration limiting → **SAFETY HAZARD**

**Missing MAXMotion Configuration**:
```java
// THIS IS MISSING - Should be added after line 170
climber1Config.closedLoop.maxMotion
    .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
    .cruiseVelocity(Constants.Climber.kClimberMaxVel)  // NEEDS TO BE ADDED TO CONSTANTS
    .maxAcceleration(Constants.Climber.kClimberMaxAccel)  // NEEDS TO BE ADDED TO CONSTANTS
    .allowedProfileError(Constants.Climber.kClimberAllowedErr);  // NEEDS TO BE ADDED TO CONSTANTS
```

**Fixed Code**:
```java
public void setClimberPos(ClimberSP pos) {
  setClimberSP(pos);
  // Use MAXMotion position control with configured cruise velocity and acceleration
  climber1Controller.setSetpoint(pos.getValue(),
      SparkBase.ControlType.kMAXMotionPositionControl);
}
```

**Why This Matters**:
- **CRITICAL SAFETY ISSUE**: Climber moving at full speed could damage robot or injure students
- Climber needs smooth, controlled motion for safe operation
- Without acceleration limiting, sudden stops could cause mechanical damage

---

## Root Cause Analysis

### Why MAXMotion Isn't Working

1. **Wrong Control Types**: Code uses `kVoltage` or `kPosition` instead of `kMAXMotionVelocityControl` or `kMAXMotionPositionControl`
2. **Commented Out Code**: Correct control types are commented out (Intake line 278, Feeder line 209)
3. **Missing Configuration**: Climber has no MAXMotion configuration at all
4. **Manual Voltage Calculation**: Intake and Feeder calculate voltage manually, bypassing all closed-loop control

### Impact

| Subsystem | Configured? | Used? | Impact |
|-----------|-------------|-------|--------|
| **Intake (velocity)** | ✅ Yes (lines 147-151) | ❌ No | Jerky motion, inconsistent speeds |
| **Intake Tilt (position)** | ✅ Yes (lines 175-179) | ✅ Yes | **WORKING CORRECTLY** |
| **Feeder (velocity)** | ✅ Yes (lines 130-134) | ❌ No | Game piece jamming, inconsistent feeding |
| **Climber (position)** | ❌ No | ❌ No | **SAFETY HAZARD** - uncontrolled motion |

---

## Complete Fix Guide

### Fix #1: Intake.java - Enable MAXMotion for Intake Motor

**File**: [`Intake.java:277`](src/main/java/frc/robot/subsystems/Intake.java:277)

**Change**:
```java
// BEFORE (line 277)
intakeController.setSetpoint(getIntakeSP(false) / 100.0 * 12.0, SparkBase.ControlType.kVoltage);

// AFTER
intakeController.setSetpoint(getIntakeSP(true), SparkBase.ControlType.kMAXMotionVelocityControl);
```

**Explanation**:
- Change from `kVoltage` to `kMAXMotionVelocityControl`
- Use `getIntakeSP(true)` to get RPM value instead of percentage
- Remove manual voltage calculation

---

### Fix #2: Feeder.java - Enable MAXMotion for Feeder Motor

**File**: [`Feeder.java:208`](src/main/java/frc/robot/subsystems/Feeder.java:208)

**Change**:
```java
// BEFORE (line 208)
feederController.setSetpoint(getFeederSP(false)/100.0*12.0, SparkBase.ControlType.kVoltage);

// AFTER
feederController.setSetpoint(getFeederSP(true), SparkBase.ControlType.kMAXMotionVelocityControl);
```

**Explanation**:
- Change from `kVoltage` to `kMAXMotionVelocityControl`
- Use `getFeederSP(true)` to get RPM value instead of percentage
- Remove manual voltage calculation

---

### Fix #3: Climber.java - Add MAXMotion Configuration and Enable It

**Step 1: Add MAXMotion Configuration**

**File**: [`Climber.java:170`](src/main/java/frc/robot/subsystems/Climber.java:170)

**Add after line 170**:
```java
climber1Config.closedLoop.maxMotion
    .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
    .cruiseVelocity(Constants.Climber.kClimberMaxVel)
    .maxAcceleration(Constants.Climber.kClimberMaxAccel)
    .allowedProfileError(Constants.Climber.kClimberAllowedErr);
```

**Step 2: Add Missing Constants**

**File**: `Constants.java` (in the Climber section)

**Add these constants**:
```java
// MAXMotion parameters for climber position control
public static final double kClimberMaxVel = 50.0;  // degrees/sec - TUNE THIS VALUE
public static final double kClimberMaxAccel = 100.0;  // degrees/sec^2 - TUNE THIS VALUE
public static final double kClimberAllowedErr = 1.0;  // degrees - TUNE THIS VALUE
```

**Step 3: Change Control Type**

**File**: [`Climber.java:368`](src/main/java/frc/robot/subsystems/Climber.java:368)

**Change**:
```java
// BEFORE (line 368-369)
climber1Controller.setSetpoint(pos.getValue(),
    SparkBase.ControlType.kPosition);

// AFTER
climber1Controller.setSetpoint(pos.getValue(),
    SparkBase.ControlType.kMAXMotionPositionControl);
```

**Step 4: Add Missing Import**

**File**: [`Climber.java:15`](src/main/java/frc/robot/subsystems/Climber.java:15)

**Add this import** (if not already present):
```java
import com.revrobotics.spark.config.MAXMotionConfig.MAXMotionPositionMode;
```

---

## Testing Procedure

### After Making Fixes

1. **Deploy Code**: Deploy to robot
2. **Test Intake**:
   - Run intake at different speeds (LOW, MED, HI)
   - Verify smooth acceleration (not instant)
   - Check Shuffleboard for velocity tracking
3. **Test Feeder**:
   - Run feeder at different speeds
   - Verify smooth acceleration
   - Test with game pieces to ensure no jamming
4. **Test Climber** (⚠️ **SAFETY CRITICAL**):
   - **Start with LOW values** for kClimberMaxVel and kClimberMaxAccel
   - Test small movements first
   - Gradually increase velocity/acceleration if needed
   - Have E-STOP ready at all times
   - Verify smooth, controlled motion

### Tuning MAXMotion Parameters

If motion is too slow or too fast, adjust these constants:

**Intake** (already configured):
- `Constants.Intake.kIntakeMaxVel` - Maximum velocity in RPM
- `Constants.Intake.kIntakeMaxAccel` - Maximum acceleration in RPM/sec

**Feeder** (already configured):
- `Constants.Feeder.kFeederMaxVel` - Maximum velocity in RPM
- `Constants.Feeder.kFeederMaxAccel` - Maximum acceleration in RPM/sec

**Climber** (needs to be added):
- `Constants.Climber.kClimberMaxVel` - Maximum velocity in degrees/sec
- `Constants.Climber.kClimberMaxAccel` - Maximum acceleration in degrees/sec²

---

## Summary of Changes Required

### Intake.java
- ✅ Tilt already working correctly
- ❌ Change line 277: `kVoltage` → `kMAXMotionVelocityControl`
- ❌ Change line 277: `getIntakeSP(false) / 100.0 * 12.0` → `getIntakeSP(true)`

### Feeder.java
- ❌ Change line 208: `kVoltage` → `kMAXMotionVelocityControl`
- ❌ Change line 208: `getFeederSP(false)/100.0*12.0` → `getFeederSP(true)`

### Climber.java
- ❌ Add MAXMotion configuration after line 170
- ❌ Add constants to Constants.java (kClimberMaxVel, kClimberMaxAccel, kClimberAllowedErr)
- ❌ Change line 369: `kPosition` → `kMAXMotionPositionControl`
- ❌ Add import for MAXMotionPositionMode if missing

---

## Why This Happened

Looking at the commented-out code, it appears:
1. Someone tried to use MAXMotion velocity control
2. It didn't work or caused issues
3. They reverted to simple voltage control as a workaround
4. The MAXMotion configuration was left in place but never used

**The Real Problem**: The setpoint values were likely wrong. Using percentage (0-100) instead of RPM would cause MAXMotion to try to reach 100 RPM instead of the intended speed.

**The Solution**: Use `getIntakeSP(true)` and `getFeederSP(true)` to get RPM values, not percentages.

---

## Estimated Fix Time

- **Intake.java**: 1 minute (change 1 line)
- **Feeder.java**: 1 minute (change 1 line)
- **Climber.java**: 5 minutes (add config, add constants, change control type)
- **Testing**: 30 minutes (careful testing, especially climber)

**Total**: ~40 minutes

---

## Additional Notes

### Why MAXMotion Matters

**Without MAXMotion**:
- Motors accelerate instantly to target speed → mechanical stress, jerky motion
- No velocity/acceleration limiting → potential damage
- Open-loop voltage control → inconsistent speeds, no feedback

**With MAXMotion**:
- Smooth trapezoidal motion profiles → reduced mechanical stress
- Configurable velocity and acceleration limits → safe, controlled motion
- Closed-loop control with PID → consistent, accurate speeds
- Automatic trajectory generation → easier programming

### REV Documentation

For more information on MAXMotion:
- [REV MAXMotion Documentation](https://docs.revrobotics.com/brushless/spark-max/control-interfaces#maxmotion)
- [SPARK MAX API Documentation](https://codedocs.revrobotics.com/java/com/revrobotics/spark/sparkmax.html)

---

**All issues documented and ready for fixing!**