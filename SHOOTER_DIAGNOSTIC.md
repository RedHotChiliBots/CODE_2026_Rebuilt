# Shooter Motor Diagnostic Checklist

## Current Status
- **Intake**: ✅ WORKING
- **Feeder**: ✅ WORKING  
- **Shooter**: ❌ NOT WORKING

## Configuration Comparison Summary

| Aspect | Intake | Feeder | Shooter | Match? |
|--------|--------|--------|---------|--------|
| **Motor Type** | SparkMax (NEO) | SparkMax (NEO) | SparkFlex (Vortex) | ✅ Different but correct |
| **Config Type** | SparkMaxConfig | SparkMaxConfig | SparkFlexConfig | ✅ Matches motor |
| **Velocity Factor** | 1.0/60.0 | 1.0 | 1.0 | ✅ Matches Feeder |
| **Enum Multiplies Factor** | YES | NO | NO | ✅ Matches Feeder |
| **Output Range** | kIntakeMin/Max | kFeederMin/Max | kMinOutput/MaxOutput | ✅ All -1.0 to 1.0 |
| **Position Wrapping** | Not present | Present (false) | Removed | ✅ Correct |
| **Control Type** | kMAXMotionVelocityControl | kMAXMotionVelocityControl | kMAXMotionVelocityControl | ✅ Match |
| **Feedforward** | .kA() | .kA() | .kA() | ✅ Match |
| **Initialization** | setIntakeVel(OFF) | setFeederVel(OFF) | setShooterVel(OFF) | ✅ Match |

## Fixes Applied

### Fix 1: Output Range Constants (Line 171)
```java
// BEFORE:
.outputRange(Constants.Shooter.kPosMinOutput, Constants.Shooter.kPosMaxOutput)

// AFTER:
.outputRange(Constants.Shooter.kMinOutput, Constants.Shooter.kMaxOutput)
```

### Fix 2: Position Wrapping (Line 172 - REMOVED)
```java
// BEFORE:
.positionWrappingEnabled(Constants.Shooter.kLeftEncodeWrapping);

// AFTER:
// Line removed - not needed for velocity control
```

### Fix 3: Velocity Conversion Factor (Constants.java Line 230)
```java
// BEFORE:
public static final double kShooterVelocityFactor = kShooterPositionFactor / 60.0;

// AFTER:
public static final double kShooterVelocityFactor = 1.0;
```

## Remaining Differences to Investigate

### 1. Motor Hardware Type
- **Intake/Feeder**: NEO motors (5676 RPM max)
- **Shooter**: Vortex motors (6784 RPM max)

**Question**: Are the Vortex motors wired correctly? Check:
- [ ] CAN IDs match Constants (50, 51)
- [ ] Motors are powered
- [ ] CAN bus connections are good
- [ ] Motors respond to Phoenix Tuner

### 2. Follower Configuration
Shooter has a follower motor (right follows left):
```java
rightConfig.follow(leftShooter, true)
```

**Question**: Is the follower configuration correct?
- [ ] Does follower motor need separate configuration?
- [ ] Is the `true` parameter correct for inversion?

### 3. PID Values
```java
// Shooter:
kP = 0.01, kI = 0.0, kD = 0.0, kVelFF = 0.0000037

// Intake:
kP = 0.001, kI = 0.0, kD = 0.001, kVelFF = 0.0

// Feeder:
kP = 0.00009, kI = 0.0, kD = 0.0, kVelFF = 0.0
```

**Observation**: Shooter kP is 10-100x higher than working subsystems
**Question**: Are these PID values tuned for Vortex motors?

### 4. MAXMotion Parameters
```java
// Shooter:
kMaxVel = 6000.0 RPM
kMaxAccel = 15000.0 RPM/sec
kAllowedErr = 75.0 RPM

// Intake:
kMaxVel = 5000.0 RPM
kMaxAccel = 16000.0 RPM/sec
kAllowedErr = 50.0 RPM

// Feeder:
kMaxVel = 4000.0 RPM
kMaxAccel = 10000.0 RPM/sec
kAllowedErr = 75.0 RPM
```

**Question**: Are these values appropriate for Vortex motors?

## Diagnostic Steps

### Step 1: Verify Hardware
1. Open Phoenix Tuner
2. Check CAN devices - verify Shooter motors appear (IDs 50, 51)
3. Try manual control in Tuner - do motors spin?
4. Check for CAN errors or faults

### Step 2: Test Basic Motor Control
Add temporary code to test without closed-loop:
```java
// In Shooter constructor, after configuration:
leftShooter.set(0.1); // 10% power
```
Does motor spin? If yes, hardware is good. If no, hardware issue.

### Step 3: Check Encoder Feedback
```java
// In periodic():
System.out.println("Shooter Encoder: " + leftEncoder.getVelocity());
```
When motor is commanded, does encoder value change?

### Step 4: Verify Setpoint
```java
// In setShooterVel():
System.out.println("Setting shooter to: " + sp.getVel(true) + " RPM");
```
Is the setpoint value reasonable? (Should be 0-6784 RPM)

### Step 5: Check Control Mode
Verify the controller is actually in velocity mode:
```java
// After setSetpoint():
System.out.println("Control type: " + SparkBase.ControlType.kMAXMotionVelocityControl);
```

### Step 6: Try Different PID Values
Test with Feeder's PID values (known working):
```java
kP = 0.00009
kI = 0.0
kD = 0.0
kVelFF = 0.0
```

### Step 7: Test Without MAXMotion
Try simple velocity control instead of MAXMotion:
```java
leftController.setSetpoint(sp.getVel(true), SparkBase.ControlType.kVelocity);
```

## Questions for User

1. **What exactly happens when you try to run the shooter?**
   - [ ] Motor doesn't spin at all
   - [ ] Motor spins but doesn't reach speed
   - [ ] Motor oscillates/vibrates
   - [ ] Motor runs but encoder shows wrong value
   - [ ] Other: _______________

2. **What do you see in Shuffleboard?**
   - Shooter SP RPM: _______
   - Shooter Vel RPM: _______
   - Shooter OnTgt: _______

3. **Any error messages in Driver Station or console?**

4. **Have the Vortex motors ever worked with this code?**

5. **Are you testing with the actual robot or simulation?**

## Next Steps

Based on the diagnostic results, we can:
1. Adjust PID values if needed
2. Fix hardware issues if found
3. Modify control strategy if MAXMotion isn't working
4. Check for firmware compatibility issues with SparkFlex

## Code Files Modified
- `src/main/java/frc/robot/subsystems/Shooter.java` (lines 171-172)
- `src/main/java/frc/robot/Constants.java` (line 230)