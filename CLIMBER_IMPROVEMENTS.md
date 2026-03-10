# Climber.java Improvement Guide

This document outlines recommended improvements for `src/main/java/frc/robot/subsystems/Climber.java` based on a comprehensive code review.

## High Priority Issues

### 1. Placeholder Values in ClimberSP Enum Need Calibration (HIGH - Functionality)
**Location:** Lines 84-90  
**Issue:** All ClimberSP enum values have comments "NUMBERS NEED TO CHANGE" indicating they are placeholder values. This is **critical for climber safety**.

**Current Code:**
```java
public enum ClimberSP {
    STOW(0.5),      // NUMBERS NEED TO CHANGE
    TOP(1.0),       // NUMBERS NEED TO CHANGE
    BOT(0.0),       // NUMBERS NEED TO CHANGE
    LVLAUTON(0.25), // NUMBERS NEED TO CHANGE
    LVL1(1.0),      // NUMBERS NEED TO CHANGE
    LVL2(0.0),      // NUMBERS NEED TO CHANGE
    LVL3(1.0);      // NUMBERS NEED TO CHANGE
```

**Fix:**
1. Physically measure and test each climber position
2. Update values with actual calibrated positions
3. Add descriptive comments explaining what each position represents
4. Document the units (appears to be in rotations or degrees based on conversion factors)

```java
public enum ClimberSP {
    STOW(0.0),      // Fully retracted position for transport
    TOP(45.0),      // Maximum extension for high bar
    BOT(5.0),       // Minimum safe extension
    LVLAUTON(15.0), // Autonomous starting position
    LVL1(20.0),     // Level 1 climb position
    LVL2(30.0),     // Level 2 climb position
    LVL3(45.0);     // Level 3 climb position (max extension)
```

### 2. Placeholder Values in HookSP Enum Need Calibration (HIGH - Functionality)
**Location:** Lines 104-106  
**Issue:** HookSP enum values STOW and DEPLOY have comments "NUMBERS NEED TO CHANGE".

**Current Code:**
```java
public enum HookSP {
    STOW(500),   // NUMBERS NEED TO CHANGE
    STOP(1500),
    DEPLOY(2500); // NUMBERS NEED TO CHANGE
```

**Fix:**
1. Test servo positions to find actual stow and deploy pulse widths
2. Verify STOP (1500) is the correct neutral position
3. Update with calibrated values

```java
public enum HookSP {
    STOW(600),   // Hooks fully retracted (calibrated value)
    STOP(1500),  // Neutral position - no movement
    DEPLOY(2400); // Hooks fully extended (calibrated value)
```

---

## Medium Priority Issues

### 3. Magic Numbers in Servo Pulse Range Configuration (MEDIUM - Maintainability)
**Location:** Lines 192, 196  
**Issue:** The values 500, 1500, 2500 appear without explanation.

**Fix:**
```java
// Add constants at top of class
private static final int SERVO_MIN_PULSE_US = 500;   // Minimum pulse width in microseconds
private static final int SERVO_CENTER_PULSE_US = 1500; // Center/neutral pulse width
private static final int SERVO_MAX_PULSE_US = 2500;  // Maximum pulse width in microseconds

// Update configuration
hubConfig.channel0.pulseRange(SERVO_MIN_PULSE_US, SERVO_CENTER_PULSE_US, SERVO_MAX_PULSE_US)
    .disableBehavior(ServoChannelConfig.BehaviorWhenDisabled.kSupplyPower);

hubConfig.channel1.pulseRange(SERVO_MIN_PULSE_US, SERVO_CENTER_PULSE_US, SERVO_MAX_PULSE_US)
    .disableBehavior(ServoChannelConfig.BehaviorWhenDisabled.kSupplyPower);
```

### 4. Magic Timeout Value in Deploy Commands (MEDIUM - Maintainability)
**Location:** Lines 318, 324  
**Issue:** The timeout value 5.0 appears without explanation.

**Fix:**
```java
// Add to Constants.Climber class
public static final double kHookDeployTimeoutSeconds = 5.0;

// Update methods
public Command deployLeftHook() {
    return Commands.startEnd(
        () -> this.setHook(leftHook, HookSP.DEPLOY),
        () -> this.setHook(leftHook, HookSP.STOP))
        .withTimeout(Constants.Climber.kHookDeployTimeoutSeconds);
}

public Command deployRightHook() {
    return Commands.startEnd(
        () -> this.setHook(rightHook, HookSP.DEPLOY),
        () -> this.setHook(rightHook, HookSP.STOP))
        .withTimeout(Constants.Climber.kHookDeployTimeoutSeconds);
}
```

### 5. Duplicate Hook Stow Command Implementations (MEDIUM - Maintainability)
**Location:** Lines 252-306  
**Issue:** There are two implementations of stowLeftHook and stowRightHook. The '1' suffix versions appear to be alternatives.

**Fix:**
Choose the better implementation and remove the other:

**Option 1 (with Timer):** More complex but has explicit timeout handling
**Option 2 (with Commands.startEnd):** Simpler but relies on until() condition

Recommendation: Keep the Timer-based version (without '1' suffix) as it has better timeout handling, or combine both approaches:

```java
public Command stowLeftHook() {
    return Commands.startEnd(
        () -> this.setHook(leftHook, HookSP.STOW),
        () -> this.setHook(leftHook, HookSP.STOP))
        .until(() -> this.getChannelAmps(leftHook) >= Constants.Climber.kServoAmpLimit)
        .withTimeout(Constants.Climber.kServoTimeout);
}

public Command stowRightHook() {
    return Commands.startEnd(
        () -> this.setHook(rightHook, HookSP.STOW),
        () -> this.setHook(rightHook, HookSP.STOP))
        .until(() -> this.getChannelAmps(rightHook) >= Constants.Climber.kServoAmpLimit)
        .withTimeout(Constants.Climber.kServoTimeout);
}

// Remove stowLeftHook1() and stowRightHook1()
```

### 6. Servo Current Monitoring May Not Be Reliable (MEDIUM - Functionality)
**Location:** Lines 270-291  
**Issue:** The stow commands rely on servo current exceeding a threshold to detect when hooks are fully stowed.

**Fix:**
1. Test thoroughly to ensure current threshold is reliable
2. Consider adding position feedback if available
3. Document the expected behavior

```java
/**
 * Stows the left hook by moving it to the STOW position.
 * The command completes when either:
 * - Servo current exceeds threshold (indicating mechanical stop)
 * - Timeout expires (safety backup)
 * 
 * @return Command that stows the left hook
 */
public Command stowLeftHook() {
    return Commands.startEnd(
        () -> this.setHook(leftHook, HookSP.STOW),
        () -> this.setHook(leftHook, HookSP.STOP))
        .until(() -> this.getChannelAmps(leftHook) >= Constants.Climber.kServoAmpLimit)
        .withTimeout(Constants.Climber.kServoTimeout);
}
```

### 7. Missing JavaDoc Documentation (MEDIUM - Maintainability)
**Location:** Lines 248-414  
**Issue:** Most public methods lack JavaDoc documentation.

**Fix:** Add JavaDoc to all public methods:
```java
/**
 * Creates a command to set the climber to a specific position.
 * @param sp The climber setpoint enum value
 * @return Command that moves climber to the specified position
 */
public Command setClimber(ClimberSP sp) {
    return runOnce(() -> setClimberPos(sp));
}

/**
 * Gets the current position of the climber mechanism.
 * @return Current climber position in degrees (based on conversion factor)
 */
public double getClimberPos() {
    return climber1AbsEncoder.getPosition();
}

// Add similar documentation for all public methods
```

---

## Low Priority Issues

### 8. Commented-Out Code Should Be Removed (LOW - Maintainability)
**Location:** Lines 57-70  
**Issue:** Multiple controller and encoder declarations are commented out.

**Fix:**
```java
// Remove these lines if not needed:
// private final SparkClosedLoopController climber2Controller = climber2.getClosedLoopController();
// private final SparkClosedLoopController climber3Controller = climber3.getClosedLoopController();
// private final SparkClosedLoopController climber4Controller = climber4.getClosedLoopController();
// private final AbsoluteEncoder climber2AbsEncoder = climber2.getAbsoluteEncoder();
// private final AbsoluteEncoder climber3AbsEncoder = climber3.getAbsoluteEncoder();
// private final AbsoluteEncoder climber4AbsEncoder = climber4.getAbsoluteEncoder();

// Or if needed for future use, add explanatory comment:
// Note: climber2-4 follow climber1, so individual controllers/encoders not needed
```

### 9. Generic Variable Name 'lib' (LOW - Maintainability)
**Location:** Line 78  
**Issue:** Variable name 'lib' is too generic.

**Fix:**
```java
private Library utilities = new Library();
// Update all references from lib.method() to utilities.method()
```

### 10. Debug Print Statements (LOW - Maintainability)
**Location:** Lines 152, 205-214, 221-222, 241  
**Issue:** Using System.out.println instead of proper logging.

**Fix:**
```java
// Remove or replace with proper logging
// Option 1: Remove for production
// Option 2: Use WPILib DataLog
// Option 3: Keep only critical startup messages
```

### 11. Misleading Method Name 'getHookSpd' (LOW - Maintainability)
**Location:** Lines 400-402  
**Issue:** Method returns pulse width, not speed.

**Fix:**
```java
/**
 * Gets the average pulse width of both hooks.
 * @return Average pulse width in microseconds
 */
public double getHookPulseWidth() {
    return ((leftHook.getPulseWidth() + rightHook.getPulseWidth()) / 2.0);
}

// Update all references from getHookSpd() to getHookPulseWidth()
```

### 12. Unused setClimberPos() Method (LOW - Functionality)
**Location:** Lines 372-376  
**Issue:** Method is never called and its purpose is unclear.

**Fix:**
```java
// Remove if unused:
// public void setClimberPos() {
//     setClimberPos(getClimberSP());
// }

// Or document if needed:
/**
 * Sets climber position to the current setpoint.
 * Useful for re-applying position after a disturbance.
 */
public void setClimberPos() {
    setClimberPos(getClimberSP());
}
```

### 13. Inconsistent Spacing in Servo Configuration (LOW - Style)
**Location:** Lines 192-198  
**Issue:** Inconsistent comment placement and spacing.

**Fix:**
```java
// Configure servo channels with consistent formatting
hubConfig.channel0
    .pulseRange(SERVO_MIN_PULSE_US, SERVO_CENTER_PULSE_US, SERVO_MAX_PULSE_US)
    .disableBehavior(ServoChannelConfig.BehaviorWhenDisabled.kSupplyPower);

hubConfig.channel1
    .pulseRange(SERVO_MIN_PULSE_US, SERVO_CENTER_PULSE_US, SERVO_MAX_PULSE_US)
    .disableBehavior(ServoChannelConfig.BehaviorWhenDisabled.kSupplyPower);
```

### 14. Spelling Error: 'Tolerance' Should Be 'Tolerance' (LOW - Maintainability)
**Location:** Lines 387, 405 (Constants lines 308-309)  
**Issue:** Misspelling throughout codebase.

**Fix in Constants.java:**
```java
public static final double kClimberTolerance = 0.5;  // was kClimberTolerance
public static final double kHookTolerance = 0.5;     // was kHookTolerance
```

**Fix in Climber.java:**
```java
public boolean onClimberTarget() {
    return Math.abs(getClimberPos() - getClimberSP().getValue()) < Constants.Climber.kClimberTolerance;
}

public boolean onHookTarget() {
    return Math.abs(getHookPulseWidth() - getHookSP().getSpd()) < Constants.Climber.kHookTolerance;
}
```

---

## Implementation Priority

1. **CRITICAL - Calibrate Setpoints First**
   - Calibrate ClimberSP enum values
   - Calibrate HookSP enum values
   - Test thoroughly before deploying to robot

2. **High Priority**
   - Test servo current monitoring reliability
   - Add proper documentation

3. **Medium Priority**
   - Extract magic numbers to constants
   - Remove duplicate implementations
   - Fix spelling errors

4. **Low Priority**
   - Clean up commented code
   - Improve variable names
   - Remove debug statements

## Safety Considerations

⚠️ **CRITICAL SAFETY NOTES:**

1. **Climber Calibration:** The climber mechanism must be calibrated with actual measured values before use. Incorrect values could cause:
   - Robot damage from over-extension
   - Unsafe climbing attempts
   - Mechanical failures

2. **Hook Servo Testing:** Thoroughly test hook servo positions:
   - Verify STOW position fully retracts hooks
   - Verify DEPLOY position properly extends hooks
   - Test current monitoring threshold is reliable
   - Ensure timeout values are appropriate

3. **Testing Procedure:**
   - Test with robot safely supported (not on ground)
   - Start with conservative values
   - Gradually adjust to optimal positions
   - Document all calibrated values
   - Test under load conditions

## Testing Recommendations

After making changes:
1. Test climber movement through full range of motion
2. Verify all setpoint positions are safe and functional
3. Test hook deployment and stowing with current monitoring
4. Verify timeout values are appropriate
5. Test emergency stop functionality
6. Document actual positions and any adjustments made

## Architecture Notes

The Climber class follows a good pattern:
- Uses follower configuration for synchronized motors
- Implements proper command factories
- Has good separation between commands and subsystem methods

Consider for future:
- Add limit switches for absolute position verification
- Add encoder position validation
- Consider adding soft limits in software