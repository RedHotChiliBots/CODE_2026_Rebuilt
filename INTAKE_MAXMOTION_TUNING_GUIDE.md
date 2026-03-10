# Intake MAXMotion Tuning Guide

## Current Values Analysis

### Current Intake Velocity Parameters (Lines 359-361)
```java
public static final double kIntakeMaxVel = 100000.0;    // UNREASONABLY HIGH!
public static final double kIntakeMaxAccel = 40000.0;   // UNREASONABLY HIGH!
public static final double kIntakeAllowedErr = 1.0;     // OK
```

### Current Tilt Position Parameters (Lines 386-388)
```java
public static final double kTiltMaxVel = 100000.0;      // UNREASONABLY HIGH!
public static final double kTiltMaxAccel = 40000.0;     // UNREASONABLY HIGH!
public static final double kTiltAllowedErr = 1.0;       // OK
```

---

## Problem: Current Values Are Unrealistic

### Why 100,000 RPM is Impossible:
- **NEO free speed**: 5,676 RPM (from line 80)
- **Current setting**: 100,000 RPM = **17.6x faster than physically possible**
- **Result**: MAXMotion will try to reach max speed instantly (no motion profiling benefit)

### Why 40,000 RPM/sec² is Too High:
- At this acceleration, motor would reach full speed in 0.14 seconds
- This defeats the purpose of MAXMotion (smooth acceleration)
- Causes mechanical stress, game piece loss, and jerky motion

---

## Recommended Values

### 🎯 INTAKE VELOCITY CONTROL (Recommended)

```java
// Conservative (Smooth, Safe) - START HERE
public static final double kIntakeMaxVel = 4000.0;      // RPM (~70% of NEO max)
public static final double kIntakeMaxAccel = 8000.0;    // RPM/sec (0.5 sec to full speed)
public static final double kIntakeAllowedErr = 50.0;    // RPM

// Moderate (Balanced)
public static final double kIntakeMaxVel = 4500.0;      // RPM (~79% of NEO max)
public static final double kIntakeMaxAccel = 12000.0;   // RPM/sec (0.375 sec to full speed)
public static final double kIntakeAllowedErr = 50.0;    // RPM

// Aggressive (Fast, Less Smooth)
public static final double kIntakeMaxVel = 5000.0;      // RPM (~88% of NEO max)
public static final double kIntakeMaxAccel = 16000.0;   // RPM/sec (0.3125 sec to full speed)
public static final double kIntakeAllowedErr = 50.0;    // RPM
```

**Reasoning:**
- **MaxVel**: 70-88% of NEO free speed (4000-5000 RPM) provides good intake speed with margin
- **MaxAccel**: 0.3-0.5 seconds to reach full speed provides smooth acceleration
- **AllowedErr**: 50 RPM tolerance is reasonable for velocity control

---

### 🎯 TILT POSITION CONTROL (Recommended)

First, let's understand the tilt mechanism:
- **Gear Ratio**: 25:1 (5:1 × 5:1 from line 368)
- **Position Factor**: 360° / 25 = 14.4° per motor rotation (line 372)
- **Velocity Factor**: 14.4° / 60 = 0.24° per second per motor RPM (line 373)

**Current tilt range**: 0° (STOW) to 80° (DEPLOY) = 80° total travel

```java
// Conservative (Smooth, Safe) - START HERE
public static final double kTiltMaxVel = 90.0;          // degrees/sec (~2.2 sec for full 80° travel)
public static final double kTiltMaxAccel = 180.0;       // degrees/sec² (0.5 sec to max speed)
public static final double kTiltAllowedErr = 1.0;       // degrees

// Moderate (Balanced)
public static final double kTiltMaxVel = 120.0;         // degrees/sec (~1.7 sec for full 80° travel)
public static final double kTiltMaxAccel = 240.0;       // degrees/sec² (0.5 sec to max speed)
public static final double kTiltAllowedErr = 1.0;       // degrees

// Aggressive (Fast, Less Smooth)
public static final double kTiltMaxVel = 180.0;         // degrees/sec (~1.1 sec for full 80° travel)
public static final double kTiltMaxAccel = 360.0;       // degrees/sec² (0.5 sec to max speed)
public static final double kTiltAllowedErr = 1.0;       // degrees
```

**Reasoning:**
- **MaxVel**: 90-180°/sec provides 1-2 second travel time for 80° range
- **MaxAccel**: 0.5 seconds to reach max speed provides smooth, controlled motion
- **AllowedErr**: 1° tolerance is appropriate for position control

---

## Detailed Calculations

### Intake Velocity Calculations

**NEO Motor Specs:**
- Free Speed: 5,676 RPM
- Gear Ratio: 12:1 (3:1 × 4:1 from line 337)
- Effective Output Speed: 5,676 / 12 = 473 RPM at wheel

**Conservative Settings (RECOMMENDED START):**
```
MaxVel = 4000 RPM (motor speed)
  → Wheel Speed = 4000 / 12 = 333 RPM
  → Percentage of Max = 4000 / 5676 = 70%
  → Time to Full Speed = 4000 / 8000 = 0.5 seconds

MaxAccel = 8000 RPM/sec
  → Smooth acceleration over 0.5 seconds
  → Reduces mechanical stress
  → Prevents game piece ejection
```

**Why These Values:**
1. **70% of max speed** - Provides good intake performance with safety margin
2. **0.5 second acceleration** - Smooth enough to prevent game piece loss
3. **Leaves headroom** - Battery voltage drops won't cause issues

---

### Tilt Position Calculations

**Tilt Mechanism Specs:**
- Gear Ratio: 25:1 (5:1 × 5:1)
- Position Factor: 14.4° per motor rotation
- Travel Range: 0° to 80° = 80° total

**Conservative Settings (RECOMMENDED START):**
```
MaxVel = 90 degrees/sec
  → Motor Speed = 90 / 14.4 = 6.25 rotations/sec = 375 RPM
  → Full 80° Travel Time = 80 / 90 = 0.89 seconds
  → Percentage of NEO Max = 375 / 5676 = 6.6%

MaxAccel = 180 degrees/sec²
  → Time to Max Speed = 90 / 180 = 0.5 seconds
  → Smooth trapezoidal profile
  → Controlled motion prevents game piece loss
```

**Why These Values:**
1. **~1 second travel time** - Fast enough for competition, slow enough for control
2. **0.5 second acceleration** - Smooth motion prevents game piece ejection
3. **Low motor speed** - Only 6.6% of NEO max, very safe and controlled

---

## Implementation Steps

### Step 1: Update Constants.java

**File**: [`Constants.java:359-361, 386-388`](src/main/java/frc/robot/Constants.java:359)

**Replace current values with CONSERVATIVE settings:**

```java
// Intake Velocity Control - CONSERVATIVE (START HERE)
public static final double kIntakeMaxVel = 4000.0;      // RPM (~70% of NEO max)
public static final double kIntakeMaxAccel = 8000.0;    // RPM/sec (0.5 sec to full speed)
public static final double kIntakeAllowedErr = 50.0;    // RPM (increased from 1.0)

// Tilt Position Control - CONSERVATIVE (START HERE)
public static final double kTiltMaxVel = 90.0;          // degrees/sec (~0.9 sec for 80° travel)
public static final double kTiltMaxAccel = 180.0;       // degrees/sec² (0.5 sec to max speed)
public static final double kTiltAllowedErr = 1.0;       // degrees (unchanged)
```

---

### Step 2: Test and Tune

#### Testing Procedure:

1. **Deploy Code** with conservative settings
2. **Test Intake Velocity**:
   - Run intake at HI speed (100%)
   - Observe acceleration smoothness
   - Check if game pieces are collected reliably
   - Verify no mechanical stress sounds
3. **Test Tilt Position**:
   - Command tilt from STOW to DEPLOY
   - Observe motion smoothness
   - Check if game pieces stay in intake during tilt
   - Verify no mechanical stress
4. **Adjust if Needed**:
   - Too slow? Increase MaxVel by 10-20%
   - Too jerky? Decrease MaxAccel by 20%
   - Not reaching target? Increase AllowedErr

#### Tuning Guidelines:

**If intake is too slow:**
```java
// Increase MaxVel in 500 RPM increments
kIntakeMaxVel = 4500.0;  // Try this next
```

**If intake acceleration is too jerky:**
```java
// Decrease MaxAccel by 20%
kIntakeMaxAccel = 6400.0;  // Slower acceleration
```

**If tilt is too slow:**
```java
// Increase MaxVel in 30°/sec increments
kTiltMaxVel = 120.0;  // Try this next
```

**If tilt motion is too jerky:**
```java
// Decrease MaxAccel by 20%
kTiltMaxAccel = 144.0;  // Slower acceleration
```

---

## Comparison Table

### Intake Velocity Control

| Setting | MaxVel (RPM) | MaxAccel (RPM/sec) | Time to Full Speed | % of NEO Max | Notes |
|---------|--------------|--------------------|--------------------|--------------|-------|
| **Current** | 100,000 | 40,000 | 2.5 sec | 1762% | ❌ Impossible, no profiling |
| **Conservative** | 4,000 | 8,000 | 0.5 sec | 70% | ✅ **START HERE** - Smooth, safe |
| **Moderate** | 4,500 | 12,000 | 0.375 sec | 79% | Good balance |
| **Aggressive** | 5,000 | 16,000 | 0.3125 sec | 88% | Fast but less smooth |

### Tilt Position Control

| Setting | MaxVel (°/sec) | MaxAccel (°/sec²) | Time for 80° | Motor RPM | Notes |
|---------|----------------|-------------------|--------------|-----------|-------|
| **Current** | 100,000 | 40,000 | 0.0008 sec | 416,667 | ❌ Impossible, no profiling |
| **Conservative** | 90 | 180 | 0.89 sec | 375 | ✅ **START HERE** - Smooth, safe |
| **Moderate** | 120 | 240 | 0.67 sec | 500 | Good balance |
| **Aggressive** | 180 | 360 | 0.44 sec | 750 | Fast but less smooth |

---

## Safety Considerations

### ⚠️ IMPORTANT SAFETY NOTES:

1. **Always start with CONSERVATIVE settings**
   - Test thoroughly before increasing speeds
   - Have E-STOP ready during testing
   - Watch for mechanical stress indicators

2. **Signs of Too-Aggressive Settings:**
   - Loud mechanical noises
   - Game pieces ejecting from intake
   - Jerky, uncontrolled motion
   - Motor overheating
   - Tripping current limits

3. **Signs of Good Settings:**
   - Smooth, controlled acceleration
   - Game pieces stay in intake
   - Quiet operation
   - Consistent performance
   - Motors stay cool

4. **Battery Voltage Considerations:**
   - Settings should work at 11V (low battery)
   - Test at different battery levels
   - MAXMotion compensates for voltage drop

---

## Expected Performance

### With Conservative Settings:

**Intake:**
- Smooth acceleration over 0.5 seconds
- Reaches 70% of max speed (plenty for game pieces)
- No game piece ejection during acceleration
- Consistent performance regardless of battery voltage

**Tilt:**
- Smooth motion from STOW to DEPLOY in ~0.9 seconds
- No game piece loss during tilt
- Controlled, predictable motion
- Safe for mechanisms and game pieces

### Benefits of Proper MAXMotion Tuning:

1. **Mechanical Longevity** - Reduced stress on gears, bearings, and structure
2. **Game Piece Control** - Smooth motion prevents ejection
3. **Consistent Performance** - Works reliably across battery voltage range
4. **Predictable Behavior** - Easier to program autonomous routines
5. **Reduced Wear** - Less maintenance needed throughout season

---

## Quick Reference Card

### 📋 Recommended Starting Values

```java
// INTAKE VELOCITY CONTROL
kIntakeMaxVel = 4000.0;      // RPM
kIntakeMaxAccel = 8000.0;    // RPM/sec
kIntakeAllowedErr = 50.0;    // RPM

// TILT POSITION CONTROL
kTiltMaxVel = 90.0;          // degrees/sec
kTiltMaxAccel = 180.0;       // degrees/sec²
kTiltAllowedErr = 1.0;       // degrees
```

### 🎯 Tuning Quick Tips

- **Too slow?** → Increase MaxVel by 10-20%
- **Too jerky?** → Decrease MaxAccel by 20%
- **Overshooting?** → Increase AllowedErr slightly
- **Not reaching target?** → Check PID gains, increase AllowedErr

---

## Summary

**Current values (100,000) are physically impossible and provide NO motion profiling benefit.**

**Recommended action:**
1. ✅ Replace with CONSERVATIVE values (4000 RPM intake, 90°/sec tilt)
2. ✅ Test thoroughly
3. ✅ Tune upward if needed
4. ✅ Document final values

**Expected improvement:**
- Smooth, controlled motion
- Better game piece handling
- Reduced mechanical stress
- More predictable robot behavior
- Longer mechanism lifespan

---

**Remember: It's always easier to increase speeds than to repair broken mechanisms!**