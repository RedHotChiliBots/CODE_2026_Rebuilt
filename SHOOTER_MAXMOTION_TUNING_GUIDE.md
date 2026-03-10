# Shooter MAXMotion Tuning Guide

## Current Values Analysis

### Current Shooter Velocity Parameters (Lines 246-248)
```java
public static final double kMaxVel = 100000.0;      // UNREASONABLY HIGH!
public static final double kMaxAccel = 40000.0;     // UNREASONABLY HIGH!
public static final double kAllowedErr = 1.0;       // TOO TIGHT for velocity!
```

### Current Tilt Position Parameters (Lines 258-260)
```java
public static final double kPosMaxVel = 100000.0;   // UNREASONABLY HIGH!
public static final double kPosMaxAccel = 40000.0;  // UNREASONABLY HIGH!
public static final double kPosAllowedErr = 1.0;    // OK for position
```

---

## Problem: Current Values Are Unrealistic

### Shooter Flywheel Issues:

**Why 100,000 RPM is Impossible:**
- **Vortex free speed**: 6,784 RPM (from line 79 in Constants)
- **Current setting**: 100,000 RPM = **14.7x faster than physically possible**
- **Result**: MAXMotion will try to reach max speed instantly (no motion profiling benefit)

**Why 40,000 RPM/sec² is Too High:**
- At this acceleration, motor would reach full speed in 0.17 seconds
- Defeats the purpose of MAXMotion (smooth acceleration)
- **Critical for shooter**: Sudden acceleration causes inconsistent shots

**Why 1.0 RPM Tolerance is Too Tight:**
- Shooter velocity naturally fluctuates during operation
- 1.0 RPM tolerance will cause constant "not on target" status
- Recommended: 50-100 RPM tolerance for reliable shooting

### Tilt Mechanism Issues:

**Why 100,000°/sec is Impossible:**
- **Gear Ratio**: 75:1 (5:1 × 5:1 × 3:1 pulley from line 225)
- **Position Factor**: 360° / 75 = 4.8° per motor rotation (line 230)
- **Current setting**: Would complete 50° travel in 0.0005 seconds!
- **Result**: No motion profiling, instant movement

---

## Shooter-Specific Considerations

### Why Shooter Needs Smooth Acceleration:

1. **Shot Consistency**
   - Smooth spin-up ensures consistent wheel speed
   - Prevents velocity overshoot/undershoot
   - Critical for accurate shooting

2. **Ballistics Integration**
   - Auto-aim requires precise velocity control
   - Smooth acceleration improves shot timing
   - Better coordination with feeder

3. **Mechanical Longevity**
   - Flywheels are high-inertia components
   - Sudden acceleration causes belt slipping
   - Reduces bearing wear

4. **Energy Management**
   - Smooth acceleration reduces current spikes
   - Prevents brownouts during spin-up
   - Better battery management

### Why Tilt Needs Smooth Motion:

1. **Shot Accuracy**
   - Smooth tilt prevents oscillation
   - Allows time for angle to settle
   - Critical for long-range shots

2. **Ballistics Precision**
   - Auto-aim calculates precise angles
   - Smooth motion reaches target accurately
   - Prevents overshoot

3. **Safety**
   - Controlled motion prevents damage
   - Protects game pieces in shooter
   - Prevents mechanical stress

---

## Recommended Values

### 🎯 SHOOTER FLYWHEEL VELOCITY CONTROL (Recommended)

**Motor Specs:**
- **Motor**: Vortex Brushless (6,784 RPM free speed)
- **No gear reduction** - Direct drive to flywheel

```java
// Conservative (Smooth, Safe) - START HERE
public static final double kMaxVel = 5500.0;        // RPM (~81% of Vortex max)
public static final double kMaxAccel = 11000.0;     // RPM/sec (0.5 sec to full speed)
public static final double kAllowedErr = 75.0;      // RPM (realistic tolerance)

// Moderate (Balanced)
public static final double kMaxVel = 6000.0;        // RPM (~88% of Vortex max)
public static final double kMaxAccel = 15000.0;     // RPM/sec (0.4 sec to full speed)
public static final double kAllowedErr = 75.0;      // RPM

// Aggressive (Fast, Less Smooth)
public static final double kMaxVel = 6500.0;        // RPM (~96% of Vortex max)
public static final double kMaxAccel = 19500.0;     // RPM/sec (0.33 sec to full speed)
public static final double kAllowedErr = 100.0;     // RPM
```

**Reasoning:**
- **MaxVel**: 81-96% of Vortex free speed (5500-6500 RPM) provides good shooting power
- **MaxAccel**: 0.33-0.5 seconds to reach full speed balances speed and smoothness
- **AllowedErr**: 75-100 RPM tolerance accounts for natural velocity fluctuations

---

### 🎯 TILT POSITION CONTROL (Recommended)

**Tilt Mechanism Specs:**
- **Gear Ratio**: 75:1 (5:1 × 5:1 × 3:1 pulley)
- **Position Factor**: 4.8° per motor rotation
- **Travel Range**: 10.5° (LOW) to 50° (HI) = 39.5° total

```java
// Conservative (Smooth, Safe) - START HERE
public static final double kPosMaxVel = 60.0;       // degrees/sec (~1.6 sec for 40° travel)
public static final double kPosMaxAccel = 120.0;    // degrees/sec² (0.5 sec to max speed)
public static final double kPosAllowedErr = 0.5;    // degrees

// Moderate (Balanced)
public static final double kPosMaxVel = 90.0;       // degrees/sec (~1.1 sec for 40° travel)
public static final double kPosMaxAccel = 180.0;    // degrees/sec² (0.5 sec to max speed)
public static final double kPosAllowedErr = 0.5;    // degrees

// Aggressive (Fast, Less Smooth)
public static final double kPosMaxVel = 120.0;      // degrees/sec (~0.8 sec for 40° travel)
public static final double kPosMaxAccel = 240.0;    // degrees/sec² (0.5 sec to max speed)
public static final double kPosAllowedErr = 0.75;   // degrees
```

**Reasoning:**
- **MaxVel**: 60-120°/sec provides 0.8-1.6 second travel time for 40° range
- **MaxAccel**: 0.5 seconds to reach max speed provides smooth, controlled motion
- **AllowedErr**: 0.5-0.75° tolerance is appropriate for shooting accuracy

---

## Detailed Calculations

### Shooter Flywheel Calculations

**Vortex Motor Specs:**
- Free Speed: 6,784 RPM
- No gear reduction (direct drive)
- High-inertia flywheel

**Conservative Settings (RECOMMENDED START):**
```
MaxVel = 5500 RPM
  → Percentage of Max = 5500 / 6784 = 81%
  → Time to Full Speed = 5500 / 11000 = 0.5 seconds
  → Leaves 19% headroom for voltage drops

MaxAccel = 11000 RPM/sec
  → Smooth acceleration over 0.5 seconds
  → Reduces current spikes
  → Prevents belt slipping

AllowedErr = 75 RPM
  → Realistic tolerance for shooting
  → Accounts for load variations
  → Prevents false "not on target" readings
```

**Why These Values:**
1. **81% of max speed** - Plenty of power with safety margin
2. **0.5 second spin-up** - Fast enough for competition, smooth enough for consistency
3. **75 RPM tolerance** - Realistic for high-speed flywheel operation

---

### Tilt Position Calculations

**Tilt Mechanism Specs:**
- Gear Ratio: 75:1
- Position Factor: 4.8° per motor rotation
- Travel Range: 10.5° to 50° = 39.5° total

**Conservative Settings (RECOMMENDED START):**
```
MaxVel = 60 degrees/sec
  → Motor Speed = 60 / 4.8 = 12.5 rotations/sec = 750 RPM
  → Full 40° Travel Time = 40 / 60 = 0.67 seconds
  → Percentage of Vortex Max = 750 / 6784 = 11%

MaxAccel = 120 degrees/sec²
  → Time to Max Speed = 60 / 120 = 0.5 seconds
  → Smooth trapezoidal profile
  → Controlled motion for accuracy

AllowedErr = 0.5 degrees
  → Tight tolerance for shooting accuracy
  → Appropriate for position control
  → Allows quick "on target" detection
```

**Why These Values:**
1. **~0.7 second travel time** - Fast enough for auto-aim, slow enough for accuracy
2. **0.5 second acceleration** - Smooth motion prevents oscillation
3. **Low motor speed** - Only 11% of Vortex max, very safe and controlled

---

## Comparison Table

### Shooter Flywheel Velocity Control

| Setting | MaxVel (RPM) | MaxAccel (RPM/sec) | Time to Full Speed | % of Vortex Max | AllowedErr | Notes |
|---------|--------------|--------------------|--------------------|-----------------|------------|-------|
| **Current** | 100,000 | 40,000 | 2.5 sec | 1474% | 1.0 RPM | ❌ Impossible, no profiling, tolerance too tight |
| **Conservative** | 5,500 | 11,000 | 0.5 sec | 81% | 75 RPM | ✅ **START HERE** - Smooth, consistent |
| **Moderate** | 6,000 | 15,000 | 0.4 sec | 88% | 75 RPM | Good balance, faster spin-up |
| **Aggressive** | 6,500 | 19,500 | 0.33 sec | 96% | 100 RPM | Fast but less smooth |

### Tilt Position Control

| Setting | MaxVel (°/sec) | MaxAccel (°/sec²) | Time for 40° | Motor RPM | AllowedErr | Notes |
|---------|----------------|-------------------|--------------|-----------|------------|-------|
| **Current** | 100,000 | 40,000 | 0.0004 sec | 1,250,000 | 1.0° | ❌ Impossible, no profiling |
| **Conservative** | 60 | 120 | 0.67 sec | 750 | 0.5° | ✅ **START HERE** - Smooth, accurate |
| **Moderate** | 90 | 180 | 0.44 sec | 1,125 | 0.5° | Good balance, faster |
| **Aggressive** | 120 | 240 | 0.33 sec | 1,500 | 0.75° | Fast but may overshoot |

---

## Implementation Steps

### Step 1: Update Constants.java

**File**: [`Constants.java:246-248, 258-260`](src/main/java/frc/robot/Constants.java:246)

**Replace current values with CONSERVATIVE settings:**

```java
// Shooter Flywheel Velocity Control - CONSERVATIVE (START HERE)
public static final double kMaxVel = 5500.0;        // RPM (~81% of Vortex max)
public static final double kMaxAccel = 11000.0;     // RPM/sec (0.5 sec to full speed)
public static final double kAllowedErr = 75.0;      // RPM (realistic tolerance)

// Tilt Position Control - CONSERVATIVE (START HERE)
public static final double kPosMaxVel = 60.0;       // degrees/sec (~0.7 sec for 40° travel)
public static final double kPosMaxAccel = 120.0;    // degrees/sec² (0.5 sec to max speed)
public static final double kPosAllowedErr = 0.5;    // degrees (unchanged)
```

**Also update shooter tolerance on line 201:**
```java
public static final double kShooterTolerance = 75.0; // RPM (was 0.5, way too tight!)
```

---

### Step 2: Test and Tune

#### Testing Procedure:

1. **Deploy Code** with conservative settings
2. **Test Shooter Spin-Up**:
   - Command shooter to HI speed (100%)
   - Observe acceleration smoothness
   - Check time to reach target speed
   - Verify "on target" status is reached
   - Monitor current draw during spin-up
3. **Test Tilt Motion**:
   - Command tilt from LOW to HI
   - Observe motion smoothness
   - Check for overshoot/oscillation
   - Verify angle accuracy
   - Test auto-aim integration
4. **Test Ballistics Integration**:
   - Use auto-aim at various distances
   - Verify smooth transitions
   - Check shot consistency
   - Monitor coordination with feeder
5. **Adjust if Needed**:
   - Too slow? Increase MaxVel by 10-20%
   - Too jerky? Decrease MaxAccel by 20%
   - Not reaching target? Increase AllowedErr

#### Tuning Guidelines:

**If shooter spin-up is too slow:**
```java
// Increase MaxVel in 500 RPM increments
kMaxVel = 6000.0;  // Try this next
```

**If shooter acceleration is too jerky:**
```java
// Decrease MaxAccel by 20%
kMaxAccel = 8800.0;  // Slower acceleration
```

**If tilt is too slow:**
```java
// Increase MaxVel in 30°/sec increments
kPosMaxVel = 90.0;  // Try this next
```

**If tilt overshoots or oscillates:**
```java
// Decrease MaxAccel by 20%
kPosMaxAccel = 96.0;  // Slower acceleration
```

---

## Safety Considerations

### ⚠️ IMPORTANT SAFETY NOTES:

1. **Always start with CONSERVATIVE settings**
   - Test without game pieces first
   - Gradually increase speeds if needed
   - Have E-STOP ready during testing

2. **Signs of Too-Aggressive Settings:**
   - **Shooter**: Belt slipping, inconsistent shots, current spikes, brownouts
   - **Tilt**: Oscillation, overshoot, jerky motion, mechanical stress
   - **Both**: Loud noises, motor overheating, tripping current limits

3. **Signs of Good Settings:**
   - Smooth, controlled acceleration
   - Consistent shot velocity
   - Accurate tilt positioning
   - Quiet operation
   - Stable current draw

4. **Shooter-Specific Risks:**
   - High-inertia flywheels can cause brownouts
   - Belt slipping reduces shot consistency
   - Aggressive acceleration wastes energy
   - Poor velocity control affects auto-aim

5. **Tilt-Specific Risks:**
   - Overshoot can damage mechanisms
   - Oscillation ruins shot accuracy
   - Fast motion can eject game pieces
   - Poor positioning affects ballistics

---

## Expected Performance

### With Conservative Settings:

**Shooter Flywheel:**
- Smooth 0.5 second spin-up
- Reaches 81% of max speed (plenty for shooting)
- Consistent velocity for accurate shots
- Reduced current spikes and brownouts
- Better battery management

**Tilt:**
- Smooth motion from LOW to HI in ~0.7 seconds
- No overshoot or oscillation
- Accurate positioning for ballistics
- Safe, controlled motion
- Protects game pieces

### Benefits of Proper MAXMotion Tuning:

1. **Shot Consistency** - Smooth velocity profiles ensure repeatable shots
2. **Ballistics Accuracy** - Precise control enables effective auto-aim
3. **Energy Efficiency** - Smooth acceleration reduces current spikes
4. **Mechanical Longevity** - Reduced stress on belts, bearings, and structure
5. **Reliable Operation** - Consistent performance across battery voltage range

---

## Ballistics Integration Notes

### Current Ballistics Usage:

The shooter uses `ShooterBallistics.solveStationary()` for auto-aim:
- **Lines 300, 321, 338**: Calls ballistics solver with distance and 0.5 parameter
- **Calculates**: Required tilt angle and flywheel RPM for target
- **Safety**: Clamps angles to mechanical limits (10.5° - 50°)

### Impact of MAXMotion Settings:

**Shooter Velocity:**
- Ballistics may request any RPM from 0 to ~6500
- MAXMotion ensures smooth transitions between speeds
- Faster MaxAccel = quicker response to ballistics changes
- But too fast = inconsistent shots

**Tilt Position:**
- Ballistics calculates precise angles
- MAXMotion ensures smooth, accurate positioning
- Faster MaxVel = quicker angle changes
- But too fast = overshoot and oscillation

**Recommended for Ballistics:**
- Use MODERATE settings for good balance
- Prioritize accuracy over speed
- Test at various distances
- Verify smooth transitions

---

## Quick Reference Card

### 📋 Recommended Starting Values

```java
// SHOOTER FLYWHEEL VELOCITY CONTROL
kMaxVel = 5500.0;        // RPM
kMaxAccel = 11000.0;     // RPM/sec
kAllowedErr = 75.0;      // RPM
kShooterTolerance = 75.0; // RPM (line 201)

// TILT POSITION CONTROL
kPosMaxVel = 60.0;       // degrees/sec
kPosMaxAccel = 120.0;    // degrees/sec²
kPosAllowedErr = 0.5;    // degrees
```

### 🎯 Tuning Quick Tips

- **Shooter too slow?** → Increase kMaxVel by 10-20%
- **Shooter too jerky?** → Decrease kMaxAccel by 20%
- **Not reaching target?** → Increase kAllowedErr
- **Tilt too slow?** → Increase kPosMaxVel by 30°/sec
- **Tilt overshoots?** → Decrease kPosMaxAccel by 20%

---

## Summary

**Current values (100,000) are physically impossible and provide NO motion profiling benefit.**

**Critical issues found:**
1. ❌ Shooter MaxVel 100,000 RPM (14.7x too high)
2. ❌ Tilt MaxVel 100,000°/sec (would complete travel in 0.0004 seconds!)
3. ❌ MaxAccel values defeat MAXMotion purpose
4. ❌ Shooter tolerance 0.5 RPM (way too tight, should be 75 RPM)

**Recommended action:**
1. ✅ Replace with CONSERVATIVE values (5500 RPM shooter, 60°/sec tilt)
2. ✅ Update shooter tolerance to 75 RPM
3. ✅ Test without game pieces first
4. ✅ Test ballistics integration
5. ✅ Tune upward if needed
6. ✅ Document final values

**Expected improvement:**
- Smooth, controlled motion
- Consistent shot velocity
- Accurate tilt positioning
- Better ballistics integration
- Reduced mechanical stress
- More predictable robot behavior
- Better energy management

---

**Remember: Shooter consistency is more important than speed. Smooth operation wins matches!**