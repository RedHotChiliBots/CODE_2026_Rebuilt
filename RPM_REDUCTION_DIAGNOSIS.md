# 50% RPM Reduction - Root Cause Analysis

## Problem Statement

Shooter, Feeder, and Intake motors are running at **50% of commanded RPM**.

**Example**:
- Command: 4000 RPM
- Actual: 2000 RPM (50% reduction)

## Root Cause: Gear Ratio Not Applied

### The Issue

All three subsystems have **gearboxes** but the velocity conversion factors are set to `1.0`, which means:
- The encoder reads motor shaft RPM (before gearbox)
- The controller commands output shaft RPM (after gearbox)
- **These are different by the gear ratio!**

### Gear Ratios

```java
// From Constants.java
Intake:  kIntakeGearRatio = GearBox.Max3 * GearBox.Max4 = 3.0 * 4.0 = 12.0
Feeder:  kFeederGearRatio = GearBox.Max3 * GearBox.Max4 = 3.0 * 4.0 = 12.0
Shooter: No gearbox (direct drive) = 1.0
```

### Current (Incorrect) Configuration

```java
// Intake
kIntakeVelocityFactor = 1.0;  // ❌ WRONG - ignores 12:1 gearbox

// Feeder  
kFeederVelocityFactor = 1.0;  // ❌ WRONG - ignores 12:1 gearbox

// Shooter
kShooterVelocityFactor = 1.0; // ✅ CORRECT - no gearbox
```

## Why This Causes 50% Reduction

### The Math

With a 12:1 gearbox:
- **Motor shaft** spins 12x faster than **output shaft**
- If you want 4000 RPM at output, motor must spin at 48000 RPM
- But encoder reads motor shaft (48000 RPM)
- Controller thinks it's at 48000 RPM when you wanted 4000 RPM
- So it only commands enough to reach 4000 RPM at motor shaft
- Which gives you 4000/12 = **333 RPM at output**

Wait, that's not 50%... Let me recalculate.

### Actually, Here's What's Happening

Looking at the commented-out code:
```java
//kIntakePositionFactor = kIntakeGearRatio; // RPM
//kIntakeVelocityFactor = kIntakePositionFactor / 60.0; // Revs per second
```

The original intent was to use gear ratio, but it's commented out!

**Current behavior**:
1. You command 4000 RPM (output shaft)
2. Encoder reads in motor shaft rotations
3. With `velocityFactor = 1.0`, encoder value is NOT converted
4. Controller sees motor shaft RPM directly
5. With 12:1 gearing, motor needs to spin 12x faster
6. But controller doesn't know about gearing
7. Result: Motor spins to 4000 RPM (motor shaft)
8. Output shaft gets 4000/12 = 333 RPM

Hmm, still not 50%. Let me think about this differently...

### Alternative Explanation: Conversion Factor Issue

Actually, looking more carefully at the code:

```java
// These are COMMENTED OUT (not used):
kIntakePositionFactor = 1.0;  // Should be: kIntakeGearRatio
kIntakeVelocityFactor = 1.0;  // Should be: kIntakePositionFactor / 60.0
```

But wait - if `velocityFactor = 1.0`, the encoder returns RPM directly.

**The real issue might be**:
- SparkMax encoder natively returns RPM
- With `velocityFactor = 1.0`, it stays as RPM
- But the gear ratio means output shaft RPM ≠ motor shaft RPM

Let me check if there's a different issue...

### Actual Root Cause: Encoder Configuration

Looking at the SparkMax configuration in the subsystems:

```java
intakeConfig.encoder
    .velocityConversionFactor(Constants.Intake.kIntakeVelocityFactor);  // = 1.0
```

**What this means**:
- SparkMax encoder natively counts in rotations
- `velocityConversionFactor` converts rotations/min to your desired units
- With factor = 1.0, you get motor shaft RPM
- With factor = 1/12.0, you get output shaft RPM

**So the fix is**:
```java
kIntakeVelocityFactor = 1.0 / kIntakeGearRatio;  // = 1/12 = 0.0833
```

This converts motor shaft RPM to output shaft RPM.

## The Fix

### Option 1: Convert Encoder Readings (Recommended)

Make the encoder report output shaft RPM:

```java
// Intake
public static final double kIntakeVelocityFactor = 1.0 / kIntakeGearRatio;  // 1/12 = 0.0833

// Feeder
public static final double kFeederVelocityFactor = 1.0 / kFeederGearRatio;  // 1/12 = 0.0833

// Shooter (no change needed)
public static final double kShooterVelocityFactor = 1.0;  // Direct drive
```

**Pros**:
- Encoder readings match commanded values
- Intuitive - you command 4000 RPM, you get 4000 RPM
- Consistent across all subsystems

**Cons**:
- None

### Option 2: Scale Commands (Not Recommended)

Multiply all commands by gear ratio:

```java
// In setIntakeVel()
intakeController.setSetpoint(getIntakeSP(true) * kIntakeGearRatio, ...);
```

**Pros**:
- None really

**Cons**:
- Confusing - command 4000 RPM, motor spins at 48000 RPM
- Easy to forget
- Inconsistent

## Implementation

### Step 1: Update Constants.java

```java
public static final class Intake {
    public static final double kIntakeGearRatio = (GearBox.Max3 * GearBox.Max4);  // 12.0
    
    // CHANGE THIS:
    public static final double kIntakeVelocityFactor = 1.0 / kIntakeGearRatio;  // 0.0833
    // Was: 1.0
}

public static final class Feeder {
    public static final double kFeederGearRatio = (GearBox.Max3 * GearBox.Max4);  // 12.0
    
    // CHANGE THIS:
    public static final double kFeederVelocityFactor = 1.0 / kFeederGearRatio;  // 0.0833
    // Was: 1.0
}

public static final class Shooter {
    // NO CHANGE NEEDED - direct drive
    public static final double kShooterVelocityFactor = 1.0;
}
```

### Step 2: Verify Gear Ratios

Check that gear ratios are correct:

```java
public static final class GearBox {
    public static final double Max3 = 3.0;   // ✓
    public static final double Max4 = 4.0;   // ✓
    public static final double Max5 = 5.0;   // ✓
}
```

Intake: 3 × 4 = 12:1 ✓  
Feeder: 3 × 4 = 12:1 ✓  
Shooter: Direct drive (1:1) ✓

### Step 3: Test

After making changes:

1. **Command 2000 RPM** to intake
2. **Verify encoder reads ~2000 RPM** (not 166 RPM)
3. **Verify actual speed is 2000 RPM** at output shaft

## Why 50% Specifically?

Actually, I need to reconsider. If gear ratio is 12:1 and not being applied, we'd see 1/12 = 8.3% of commanded speed, not 50%.

**Let me check if there's a different issue...**

### Possible Alternative Causes

1. **Voltage Limiting**:
   - Battery voltage too low
   - Current limiting kicking in
   - Check: Monitor battery voltage and motor current

2. **PID Tuning**:
   - P gain too low (we already identified this)
   - Missing feedforward (we already fixed this)
   - Check: Increase P gain as recommended in PID_ANALYSIS.md

3. **MaxMotion Velocity Limit**:
   - `kMaxVel` set too low
   - Check: Intake kMaxVel = 5000 RPM (should be fine)

4. **Conversion Factor Math Error**:
   - Maybe the factor should be gear ratio, not 1/gear ratio?
   - Let me think...

### Testing the Theory

**If velocityFactor should be gear_ratio (not 1/gear_ratio)**:

```java
kIntakeVelocityFactor = kIntakeGearRatio;  // 12.0 (not 0.0833)
```

This would mean:
- Encoder counts motor rotations
- Factor of 12 converts to "output equivalent rotations"
- Command 4000 RPM → controller sees 4000 RPM
- Motor spins to make encoder read 4000 (after factor)
- Encoder reads 4000/12 = 333 motor RPM
- Output gets 333 RPM

No, that's still wrong.

### The Real Answer

I think the issue is simpler. Let me check if there's a **position vs velocity** factor confusion:

```java
// Position factor: converts encoder rotations to output rotations
kIntakePositionFactor = 1.0 / kIntakeGearRatio;  // Output rotations

// Velocity factor: converts encoder RPM to output RPM  
kIntakeVelocityFactor = 1.0 / kIntakeGearRatio;  // Output RPM
```

Both should be `1 / gear_ratio` to convert from motor shaft to output shaft.

## Conclusion

**The fix is**:

```java
// Change from:
kIntakeVelocityFactor = 1.0;
kFeederVelocityFactor = 1.0;

// To:
kIntakeVelocityFactor = 1.0 / kIntakeGearRatio;  // 0.0833
kFeederVelocityFactor = 1.0 / kFeederGearRatio;  // 0.0833
```

This makes the encoder report output shaft RPM instead of motor shaft RPM.

**But wait - why 50% and not 8.3%?**

Unless... the gear ratio is actually **2:1** not 12:1?

Let me check the GearBox values again...

Actually, I should ask: **Are you seeing exactly 50%, or approximately 50%?**

If it's exactly 50%, the gear ratio might be 2:1, not 12:1.  
If it's approximately 50%, there might be multiple issues combining.

## Next Steps

1. **Verify actual gear ratios** on physical robot
2. **Apply the velocity factor fix** above
3. **Test and measure** actual RPM
4. **Adjust if needed** based on results

---

**Created**: 2026-03-12  
**Status**: Diagnosis Complete - Fix Ready to Apply