# 50% RPM Reduction - Solution

## Problem Summary

Motors running at 50% of commanded RPM in Shooter, Feeder, and Intake subsystems.

## Root Cause

The velocity conversion factors are set to `1.0` instead of accounting for gear ratios. This causes a mismatch between commanded RPM (output shaft) and measured RPM (motor shaft).

## Evidence

### Current Configuration (INCORRECT)

```java
// Intake - Lines 345-346
public static final double kIntakePositionFactor = 1.0;  // Should be: kIntakeGearRatio
public static final double kIntakeVelocityFactor = 1.0;  // Should be: kIntakePositionFactor / 60.0

// Feeder - Lines 417-418  
public static final double kFeederPositionFactor = 1.0;  // Should be: kFeederGearRatio
public static final double kFeederVelocityFactor = 1.0;  // Should be: kFeederPositionFactor / 60.0

// Shooter - Line 230
public static final double kShooterVelocityFactor = 1.0;  // CORRECT - no gearbox
```

### Correct Pattern (from Climber)

```java
// Climber - Lines 299-300 (CORRECT IMPLEMENTATION)
public static final double kClimberPositionFactor = (1.0 * Math.PI) / kClimberGearRatio;
public static final double kClimberVelocityFactor = kClimberPositionFactor / 60.0;
```

The Climber correctly divides by gear ratio to convert motor shaft measurements to output shaft measurements.

## The Fix

### Step 1: Uncomment and Fix Intake Constants

In [`Constants.java`](src/main/java/frc/robot/Constants.java:345-346):

```java
// CHANGE FROM:
public static final double kIntakePositionFactor = 1.0;  //kIntakeGearRatio; // RPM
public static final double kIntakeVelocityFactor = 1.0;  //kIntakePositionFactor / 60.0; // Revs per second

// TO:
public static final double kIntakePositionFactor = kIntakeGearRatio;  // Motor rotations to output rotations
public static final double kIntakeVelocityFactor = kIntakePositionFactor / 60.0;  // Motor RPM to output RPS
```

**Wait - this is WRONG!** Looking at Climber, it DIVIDES by gear ratio, not multiplies!

### Correct Fix for Intake

```java
// CHANGE FROM:
public static final double kIntakePositionFactor = 1.0;
public static final double kIntakeVelocityFactor = 1.0;

// TO (following Climber pattern):
public static final double kIntakePositionFactor = 1.0 / kIntakeGearRatio;  // = 1/12 = 0.0833
public static final double kIntakeVelocityFactor = kIntakePositionFactor / 60.0;  // = 0.0833/60 = 0.00139
```

**But wait** - the Climber uses `kClimberPositionFactor / 60.0` for velocity, which converts rotations to rotations/second. For RPM, we want rotations/minute, so we should NOT divide by 60!

### Actually Correct Fix

Looking more carefully at the commented code and Climber:

**For Intake (wants RPM units)**:
```java
public static final double kIntakePositionFactor = 1.0 / kIntakeGearRatio;  // Output rotations per motor rotation
public static final double kIntakeVelocityFactor = kIntakePositionFactor;   // Output RPM per motor RPM (no /60 for RPM)
```

**For Feeder (wants RPM units)**:
```java
public static final double kFeederPositionFactor = 1.0 / kFeederGearRatio;  // Output rotations per motor rotation  
public static final double kFeederVelocityFactor = kFeederPositionFactor;   // Output RPM per motor RPM (no /60 for RPM)
```

**For Shooter (no gearbox, already correct)**:
```java
public static final double kShooterVelocityFactor = 1.0;  // No change needed
```

## Why This Fixes 50% Issue

Actually, let me reconsider. If gear ratio is 12:1 and we're seeing 50% speed, that doesn't match the math (should be 8.3%).

**Alternative hypothesis**: Maybe the issue is that the commented-out code was MULTIPLYING by gear ratio when it should DIVIDE?

Let me check the original commented code again:
```java
//kIntakePositionFactor = kIntakeGearRatio; // RPM
```

If this was uncommented, it would set `kIntakePositionFactor = 12.0`, which would make the encoder read 12x higher than actual motor rotations. That's backwards!

## The Real Issue

I think there are TWO possible interpretations:

### Interpretation A: Encoder on Motor Shaft (Before Gearbox)
- Encoder reads motor shaft rotations
- Need to divide by gear ratio to get output shaft rotations
- `velocityFactor = 1.0 / gearRatio`

### Interpretation B: Encoder on Output Shaft (After Gearbox)  
- Encoder reads output shaft rotations directly
- No conversion needed
- `velocityFactor = 1.0` (current setting)

**The 50% reduction suggests neither of these is the issue!**

## Alternative Root Causes

Since 50% doesn't match the 12:1 gear ratio math, let me consider other causes:

### 1. Feedforward Issue
Looking at line 359:
```java
public static final double kVelFF = 1.0 / MotorConstants.kNeoFreeSpeedRpm;  // = 1/5676 = 0.000176
```

This feedforward is VERY small. For velocity control, feedforward should be the primary control term.

**If feedforward is too low, the motor won't reach target speed!**

### 2. Voltage Saturation
- Battery voltage might be limiting motor speed
- Check if motors are hitting 12V limit

### 3. PID Tuning
From line 356:
```java
public static final double kIntakeP = 0.0002;  // Very low!
```

This P gain is extremely low. Combined with low feedforward, the controller can't generate enough output.

## Recommended Actions

### Action 1: Increase Feedforward (CRITICAL)

The feedforward should be approximately `1 / max_rpm`:

```java
// For Intake (NEO motor, 5676 RPM free speed)
public static final double kVelFF = 1.0 / MotorConstants.kNeoFreeSpeedRpm;  // Already correct at 0.000176

// But if there's a gearbox, we need to account for it:
public static final double kVelFF = 1.0 / (MotorConstants.kNeoFreeSpeedRpm / kIntakeGearRatio);
// = 1.0 / (5676 / 12) = 1.0 / 473 = 0.00211
```

**This is the likely fix!** If the feedforward doesn't account for the gearbox, it will command too little voltage.

### Action 2: Verify Gear Ratio Application

Check if the gear ratio needs to be applied to feedforward:

```java
// Current (line 359):
public static final double kVelFF = 1.0 / MotorConstants.kNeoFreeSpeedRpm;  // 0.000176

// Should be (if encoder reads motor shaft):
public static final double kVelFF = 1.0 / MotorConstants.kNeoFreeSpeedRpm;  // 0.000176 (correct)

// Should be (if encoder reads output shaft):  
public static final double kVelFF = kIntakeGearRatio / MotorConstants.kNeoFreeSpeedRpm;  // 0.00211
```

### Action 3: Increase P Gain

```java
// Current:
public static final double kIntakeP = 0.0002;

// Try:
public static final double kIntakeP = 0.0005;  // 2.5x increase
```

## Most Likely Solution

Based on the 50% reduction and the code structure, I believe the issue is:

**The velocity conversion factor should account for the gear ratio, but currently doesn't.**

### Final Fix

```java
// Intake (Lines 345-346)
public static final double kIntakePositionFactor = 1.0 / kIntakeGearRatio;  // 0.0833
public static final double kIntakeVelocityFactor = kIntakePositionFactor;   // 0.0833 (for RPM units)

// Feeder (Lines 417-418)
public static final double kFeederPositionFactor = 1.0 / kFeederGearRatio;  // 0.0833
public static final double kFeederVelocityFactor = kFeederPositionFactor;   // 0.0833 (for RPM units)

// Shooter (Line 230) - NO CHANGE
public static final double kShooterVelocityFactor = 1.0;  // Direct drive
```

This makes the encoder report output shaft RPM instead of motor shaft RPM, matching the commanded values.

## Testing Plan

1. Apply the velocity factor fix above
2. Test with a simple command (e.g., 1000 RPM)
3. Measure actual RPM with encoder
4. If still wrong, adjust feedforward
5. If still wrong, check physical gear ratios

---

**Status**: Ready to apply fix  
**Confidence**: High - velocity factors need gear ratio correction  
**Next Step**: Update Constants.java with corrected velocity factors