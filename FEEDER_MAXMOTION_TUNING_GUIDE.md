# Feeder MAXMotion Tuning Guide

## Current Values Analysis

### Current Feeder Velocity Parameters (Lines 423-425)
```java
public static final double kFeederMaxVel = 100000.0;    // UNREASONABLY HIGH!
public static final double kFeederMaxAccel = 40000.0;   // UNREASONABLY HIGH!
public static final double kFeederAllowedErr = 0.1;     // TOO TIGHT!
```

### ✅ Good News: MAXMotion Already Enabled!
Line 198 in Feeder.java correctly uses:
```java
feederController.setSetpoint(getFeederSP(true), SparkBase.ControlType.kMAXMotionVelocityControl);
```

---

## Problem: Current Values Are Unrealistic

### Why 100,000 RPM is Impossible:
- **NEO free speed**: 5,676 RPM (from Constants line 80)
- **Gear ratio**: 12:1 (3:1 × 4:1 from line 408)
- **Current setting**: 100,000 RPM = **17.6x faster than physically possible**
- **Result**: MAXMotion will try to reach max speed instantly (no motion profiling benefit)

### Why 40,000 RPM/sec² is Too High:
- At this acceleration, motor would reach full speed in 0.14 seconds
- This defeats the purpose of MAXMotion (smooth acceleration)
- **Critical for feeder**: Sudden acceleration can jam or eject game pieces

### Why 0.1 RPM Tolerance is Too Tight:
- Feeder velocity naturally fluctuates when feeding game pieces
- 0.1 RPM tolerance will cause constant "not on target" status
- Recommended: 50-100 RPM tolerance for reliable operation

---

## Feeder-Specific Considerations

### Why Feeder Needs Smooth Acceleration:

1. **Game Piece Jamming Prevention**
   - Sudden acceleration can cause game pieces to jam
   - Smooth acceleration allows pieces to flow naturally
   - Prevents damage to game pieces

2. **Consistent Feeding**
   - Smooth velocity profiles ensure consistent feeding rate
   - Prevents "burping" or erratic feeding
   - Better coordination with shooter

3. **Mechanical Longevity**
   - Feeder mechanisms are often delicate
   - Smooth motion reduces wear on rollers/belts
   - Prevents belt slipping

4. **Sensor Reliability**
   - Fuel sensor (line 44) needs stable feeding for accurate detection
   - Smooth motion improves sensor readings
   - Better game piece tracking

---

## Recommended Values

### 🎯 FEEDER VELOCITY CONTROL (Recommended)

```java
// Conservative (Smooth, Safe) - START HERE
public static final double kFeederMaxVel = 3500.0;      // RPM (~62% of NEO max)
public static final double kFeederMaxAccel = 7000.0;    // RPM/sec (0.5 sec to full speed)
public static final double kFeederAllowedErr = 75.0;    // RPM (increased from 0.1)

// Moderate (Balanced)
public static final double kFeederMaxVel = 4000.0;      // RPM (~70% of NEO max)
public static final double kFeederMaxAccel = 10000.0;   // RPM/sec (0.4 sec to full speed)
public static final double kFeederAllowedErr = 75.0;    // RPM

// Aggressive (Fast, Less Smooth)
public static final double kFeederMaxVel = 4500.0;      // RPM (~79% of NEO max)
public static final double kFeederMaxAccel = 13500.0;   // RPM/sec (0.33 sec to full speed)
public static final double kFeederAllowedErr = 100.0;   // RPM
```

**Reasoning:**
- **MaxVel**: 62-79% of NEO free speed (3500-4500 RPM) provides good feeding speed
- **MaxAccel**: 0.33-0.5 seconds to reach full speed prevents jamming
- **AllowedErr**: 75-100 RPM tolerance accounts for natural velocity fluctuations during feeding

---

## Detailed Calculations

### Feeder Mechanism Specs:
- **Motor**: NEO Brushless (5,676 RPM free speed)
- **Gear Ratio**: 12:1 (3:1 × 4:1 from line 408)
- **Output Speed**: 5,676 / 12 = 473 RPM at roller
- **Conversion Factors**: 1.0 (lines 412-413) - direct motor RPM

### Conservative Settings (RECOMMENDED START):
```
MaxVel = 3500 RPM (motor speed)
  → Roller Speed = 3500 / 12 = 292 RPM
  → Percentage of Max = 3500 / 5676 = 62%
  → Time to Full Speed = 3500 / 7000 = 0.5 seconds

MaxAccel = 7000 RPM/sec
  → Smooth acceleration over 0.5 seconds
  → Prevents game piece jamming
  → Allows pieces to settle before full speed

AllowedErr = 75 RPM
  → Realistic tolerance for feeding operations
  → Accounts for load variations
  → Prevents false "not on target" readings
```

**Why These Values:**
1. **62% of max speed** - Plenty fast for feeding, with safety margin
2. **0.5 second acceleration** - Smooth enough to prevent jamming
3. **75 RPM tolerance** - Realistic for feeding operations with varying loads

---

## Comparison with Intake

### Feeder vs Intake Differences:

| Aspect | Intake | Feeder | Reason |
|--------|--------|--------|--------|
| **MaxVel** | 4000 RPM | 3500 RPM | Feeder needs more control |
| **MaxAccel** | 8000 RPM/sec | 7000 RPM/sec | Feeder more sensitive to jamming |
| **AllowedErr** | 50 RPM | 75 RPM | Feeder has more load variation |
| **Purpose** | Collect pieces | Feed to shooter | Different requirements |

**Why Feeder is Slower:**
- Feeder operates in confined space (more jamming risk)
- Needs to coordinate with shooter timing
- Game pieces are already captured (no need for high speed)
- Sensor detection requires stable feeding

---

## Implementation Steps

### Step 1: Update Constants.java

**File**: [`Constants.java:423-425`](src/main/java/frc/robot/Constants.java:423)

**Replace current values with CONSERVATIVE settings:**

```java
// Feeder Velocity Control - CONSERVATIVE (START HERE)
public static final double kFeederMaxVel = 3500.0;      // RPM (~62% of NEO max)
public static final double kFeederMaxAccel = 7000.0;    // RPM/sec (0.5 sec to full speed)
public static final double kFeederAllowedErr = 75.0;    // RPM (realistic tolerance)
```

**Also update tolerance on line 398:**
```java
public static final double kTolerance = 75.0; // RPMs (was 0.5, way too tight!)
```

---

### Step 2: Test and Tune

#### Testing Procedure:

1. **Deploy Code** with conservative settings
2. **Test Without Game Pieces**:
   - Run feeder at HI speed (100%)
   - Observe acceleration smoothness
   - Check for mechanical stress sounds
   - Verify "on target" status is reached
3. **Test With Game Pieces**:
   - Load game pieces into feeder
   - Run at different speeds (LOW, MED, HI)
   - Check for jamming
   - Verify smooth feeding to shooter
   - Monitor fuel sensor readings
4. **Test Fuel Sensor Integration**:
   - Verify sensor detects game pieces reliably
   - Check that feeding doesn't cause false readings
   - Ensure smooth operation with sensor feedback
5. **Adjust if Needed**:
   - Too slow? Increase MaxVel by 10-20%
   - Jamming? Decrease MaxAccel by 20%
   - False "not on target"? Increase AllowedErr

#### Tuning Guidelines:

**If feeder is too slow:**
```java
// Increase MaxVel in 500 RPM increments
kFeederMaxVel = 4000.0;  // Try this next
```

**If game pieces are jamming:**
```java
// Decrease MaxAccel by 20%
kFeederMaxAccel = 5600.0;  // Slower acceleration
```

**If "on target" status is unreliable:**
```java
// Increase AllowedErr
kFeederAllowedErr = 100.0;  // More tolerance
```

**If feeding is inconsistent:**
```java
// Try moderate settings
kFeederMaxVel = 4000.0;
kFeederMaxAccel = 10000.0;
```

---

## Comparison Table

### Feeder Velocity Control

| Setting | MaxVel (RPM) | MaxAccel (RPM/sec) | Time to Full Speed | % of NEO Max | AllowedErr | Notes |
|---------|--------------|--------------------|--------------------|--------------|------------|-------|
| **Current** | 100,000 | 40,000 | 2.5 sec | 1762% | 0.1 RPM | ❌ Impossible, no profiling, tolerance too tight |
| **Conservative** | 3,500 | 7,000 | 0.5 sec | 62% | 75 RPM | ✅ **START HERE** - Smooth, prevents jamming |
| **Moderate** | 4,000 | 10,000 | 0.4 sec | 70% | 75 RPM | Good balance, faster feeding |
| **Aggressive** | 4,500 | 13,500 | 0.33 sec | 79% | 100 RPM | Fast but higher jamming risk |

---

## Feeder-Specific Issues to Watch For

### 1. Game Piece Jamming
**Symptoms:**
- Feeder stalls or stutters
- Current spikes
- Game pieces get stuck

**Solutions:**
- Decrease MaxAccel (slower acceleration)
- Decrease MaxVel (lower top speed)
- Check mechanical alignment

### 2. Inconsistent Feeding
**Symptoms:**
- Erratic feeding rate
- Game pieces feed in bursts
- "On target" status flickers

**Solutions:**
- Increase AllowedErr (more tolerance)
- Adjust PID gains (kFeederP on line 415)
- Check for mechanical friction

### 3. Fuel Sensor Issues
**Symptoms:**
- Sensor doesn't detect game pieces
- False positive/negative readings
- Unreliable game piece tracking

**Solutions:**
- Ensure smooth, consistent feeding
- Check sensor positioning
- Verify sensor thresholds (lines 222-227)

### 4. Coordination with Shooter
**Symptoms:**
- Game pieces miss shooter
- Inconsistent shot timing
- Shooter jams

**Solutions:**
- Match feeder speed to shooter needs
- Add delay between feeder and shooter
- Coordinate acceleration profiles

---

## Safety Considerations

### ⚠️ IMPORTANT SAFETY NOTES:

1. **Always start with CONSERVATIVE settings**
   - Test without game pieces first
   - Gradually add game pieces
   - Have E-STOP ready during testing

2. **Signs of Too-Aggressive Settings:**
   - Game pieces jamming frequently
   - Loud mechanical noises
   - Current limit trips
   - Game pieces ejecting unexpectedly
   - Fuel sensor giving erratic readings

3. **Signs of Good Settings:**
   - Smooth, consistent feeding
   - No jamming
   - Reliable fuel sensor readings
   - Quiet operation
   - Consistent shot timing

4. **Feeder-Specific Risks:**
   - Jamming can damage game pieces
   - Aggressive feeding can break mechanisms
   - Poor coordination affects shooter performance
   - Sensor issues cause autonomous failures

---

## Expected Performance

### With Conservative Settings:

**Feeder Operation:**
- Smooth acceleration over 0.5 seconds
- Reaches 62% of max speed (plenty for feeding)
- No game piece jamming
- Consistent feeding rate
- Reliable fuel sensor operation

**Integration Benefits:**
1. **Shooter Coordination** - Predictable feeding timing
2. **Sensor Reliability** - Stable motion improves detection
3. **Mechanical Longevity** - Reduced wear on rollers/belts
4. **Game Piece Care** - Gentle handling prevents damage
5. **Autonomous Reliability** - Consistent performance

---

## Fuel Sensor Integration Notes

### Current Sensor Logic (Lines 218-227):

```java
public double getFuelVolts() {
  return fuelSensor.getVoltage();
}

public double getFuelDist() {
  return getFuelVolts() / (5.0 / 1024.0) / (25.4 / 5.0);
}

public boolean isFuelAvail() {
  return (getFuelDist() <= 21.0 && getFuelDist() >= 30.0);  // ⚠️ Logic error!
}
```

**⚠️ CRITICAL BUG FOUND:**
Line 227 has impossible logic: `<= 21.0 && >= 30.0` can never be true!

**Should probably be:**
```java
public boolean isFuelAvail() {
  return (getFuelDist() >= 21.0 && getFuelDist() <= 30.0);  // Fixed logic
}
```

**Impact on MAXMotion Tuning:**
- Smooth feeder motion helps sensor get accurate readings
- Erratic motion causes sensor fluctuations
- Conservative settings improve sensor reliability

---

## Quick Reference Card

### 📋 Recommended Starting Values

```java
// FEEDER VELOCITY CONTROL
kFeederMaxVel = 3500.0;      // RPM
kFeederMaxAccel = 7000.0;    // RPM/sec
kFeederAllowedErr = 75.0;    // RPM
kTolerance = 75.0;           // RPM (line 398)
```

### 🎯 Tuning Quick Tips

- **Too slow?** → Increase MaxVel by 10-20%
- **Jamming?** → Decrease MaxAccel by 20%
- **Not reaching target?** → Increase AllowedErr
- **Inconsistent feeding?** → Check PID gains, increase AllowedErr
- **Sensor issues?** → Ensure smooth motion, check sensor position

---

## Summary

**Current values (100,000 RPM) are physically impossible and provide NO motion profiling benefit.**

**Critical issues found:**
1. ❌ MaxVel 100,000 RPM (17.6x too high)
2. ❌ MaxAccel 40,000 RPM/sec² (defeats MAXMotion purpose)
3. ❌ AllowedErr 0.1 RPM (way too tight, causes false negatives)
4. ❌ kTolerance 0.5 RPM (line 398, also too tight)
5. ⚠️ Fuel sensor logic error (line 227)

**Recommended action:**
1. ✅ Replace with CONSERVATIVE values (3500 RPM, 7000 RPM/sec, 75 RPM tolerance)
2. ✅ Fix fuel sensor logic (line 227)
3. ✅ Test without game pieces first
4. ✅ Test with game pieces
5. ✅ Tune upward if needed
6. ✅ Document final values

**Expected improvement:**
- Smooth, controlled feeding
- No game piece jamming
- Better shooter coordination
- Reliable fuel sensor operation
- Reduced mechanical stress
- More predictable robot behavior

---

**Remember: Feeder is critical for scoring - smooth operation is more important than speed!**