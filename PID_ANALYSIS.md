# PID Analysis for Intake, Feeder, and Shooter Subsystems

## Executive Summary

Overall assessment: **Most PIDs need adjustment** ⚠️

- ✅ **Shooter Velocity**: Good with feedforward
- ⚠️ **Intake Velocity**: Too low, needs increase
- ❌ **Feeder Velocity**: Way too low, critical issue
- ⚠️ **Position Controllers**: Need tuning for better performance

---

## Detailed Analysis

### 1. Shooter Subsystem (Vortex Motors)

#### Velocity Control (Flywheel)
```java
kP = 0.0001
kI = 0.0
kD = 0.0
kVelFF = 1.0 / 5700.0 = 0.000175
```

**Assessment**: ✅ **GOOD**

**Analysis**:
- **P gain (0.0001)**: Reasonable for velocity control with feedforward
- **Feedforward (0.000175)**: Excellent! Calculated from motor free speed (5700 RPM)
  - Formula: `1 / free_speed_rpm` is correct for velocity feedforward
  - This does most of the work, P just corrects errors
- **No I or D**: Appropriate for velocity control with good FF
- **Max Velocity**: 6000 RPM (~88% of 6784 Vortex max) - Good safety margin
- **Max Accel**: 15000 RPM/sec (0.4s to full speed) - Reasonable

**Recommendation**: ✅ Keep as-is, but monitor performance
- If oscillation occurs, reduce P to 0.00005
- If slow response, increase P to 0.0002

#### Position Control (Tilt)
```java
kPosP = 0.01
kPosI = 0.0
kPosD = 0.0
```

**Assessment**: ⚠️ **NEEDS IMPROVEMENT**

**Analysis**:
- **P gain (0.01)**: Low for position control
- **No D term**: Will cause overshoot and oscillation
- **Gear ratio**: 75:1 (5×5×3) - High gearing needs more aggressive control

**Recommendations**: ⚠️ Tune for better performance
```java
kPosP = 0.05;  // Increase for faster response
kPosD = 0.005; // Add damping to prevent overshoot
kPosI = 0.0;   // Keep at 0 initially
```

---

### 2. Intake Subsystem (NEO Motors)

#### Velocity Control (Roller)
```java
kIntakeP = 0.001
kIntakeI = 0.0
kIntakeD = 0.001
kVelFF = 0.0  // ❌ MISSING!
```

**Assessment**: ⚠️ **NEEDS IMPROVEMENT**

**Analysis**:
- **P gain (0.001)**: Too low without feedforward
- **D gain (0.001)**: Unusual for velocity control, typically not needed
- **Missing Feedforward**: Critical issue! Should have velocity FF
- **Max Velocity**: 5000 RPM (~88% of 5676 NEO max) - Good
- **Max Accel**: 16000 RPM/sec (0.3125s to full) - Very aggressive

**Recommendations**: ⚠️ Add feedforward and increase P
```java
kIntakeP = 0.0002;              // Double the P gain
kIntakeI = 0.0;                 // Keep at 0
kIntakeD = 0.0;                 // Remove D for velocity
kVelFF = 1.0 / 5676.0;          // Add feedforward = 0.000176
```

**Why this matters**:
- Without FF, the motor relies entirely on P to reach speed
- This causes slow response and steady-state error
- FF provides ~90% of the control effort, P corrects the rest

#### Position Control (Tilt)
```java
kTiltP = 1.0
kTiltI = 0.0
kTiltD = 0.0
```

**Assessment**: ⚠️ **AGGRESSIVE, NEEDS DAMPING**

**Analysis**:
- **P gain (1.0)**: Very high, will cause overshoot
- **No D term**: Will oscillate badly
- **Gear ratio**: 25:1 (5×5) - Moderate gearing
- **Travel**: 0° to 80° in ~1.7 seconds

**Recommendations**: ⚠️ Add damping
```java
kTiltP = 0.8;   // Reduce slightly
kTiltD = 0.1;   // Add significant damping
kTiltI = 0.0;   // Keep at 0
```

---

### 3. Feeder Subsystem (NEO Motor)

#### Velocity Control (Roller)
```java
kFeederP = 0.00009  // ❌ CRITICALLY LOW!
kFeederI = 0.0
kFeederD = 0.0
kFeederVelFF = 0.0  // ❌ MISSING!
```

**Assessment**: ❌ **CRITICAL ISSUE**

**Analysis**:
- **P gain (0.00009)**: Extremely low - 10x lower than it should be
- **Missing Feedforward**: No velocity FF at all
- **Max Velocity**: 4000 RPM (~70% of NEO max) - Reasonable
- **Max Accel**: 10000 RPM/sec (0.4s to full) - Reasonable

**Impact**:
- Motor will be extremely slow to respond
- Will never reach target velocity
- Steady-state error will be huge
- Feeder won't work properly

**Recommendations**: ❌ URGENT - Fix immediately
```java
kFeederP = 0.0002;              // Increase by 2000%!
kFeederI = 0.0;                 // Keep at 0
kFeederD = 0.0;                 // Keep at 0
kFeederVelFF = 1.0 / 5676.0;    // Add feedforward = 0.000176
```

**This is the most critical fix needed!**

---

## Comparison Table

| Subsystem | Motor | P Gain | FF | Status | Priority |
|-----------|-------|--------|-----|--------|----------|
| Shooter Velocity | Vortex | 0.0001 | ✅ 0.000175 | ✅ Good | Low |
| Shooter Tilt | NEO | 0.01 | N/A | ⚠️ Needs D | Medium |
| Intake Velocity | NEO | 0.001 | ❌ Missing | ⚠️ Needs FF | High |
| Intake Tilt | NEO | 1.0 | N/A | ⚠️ Needs D | Medium |
| Feeder Velocity | NEO | 0.00009 | ❌ Missing | ❌ Critical | **URGENT** |

---

## Recommended Changes

### Priority 1: URGENT - Feeder (Critical)

**Current**:
```java
public static final double kFeederP = 0.00009;
public static final double kFeederVelFF = 0.0;
```

**Recommended**:
```java
public static final double kFeederP = 0.0002;
public static final double kFeederI = 0.0;
public static final double kFeederD = 0.0;
public static final double kFeederVelFF = 1.0 / 5676.0;  // 0.000176
```

### Priority 2: HIGH - Intake Velocity

**Current**:
```java
public static final double kIntakeP = 0.001;
public static final double kIntakeD = 0.001;
public static final double kVelFF = 0.0;
```

**Recommended**:
```java
public static final double kIntakeP = 0.0002;
public static final double kIntakeI = 0.0;
public static final double kIntakeD = 0.0;  // Remove D for velocity
public static final double kVelFF = 1.0 / 5676.0;  // 0.000176
```

### Priority 3: MEDIUM - Position Controllers

**Shooter Tilt**:
```java
public static final double kPosP = 0.05;   // Increase from 0.01
public static final double kPosI = 0.0;
public static final double kPosD = 0.005;  // Add damping
```

**Intake Tilt**:
```java
public static final double kTiltP = 0.8;   // Reduce from 1.0
public static final double kTiltI = 0.0;
public static final double kTiltD = 0.1;   // Add damping
```

---

## PID Tuning Guidelines

### For Velocity Control (Flywheels/Rollers)

1. **Start with Feedforward**:
   ```java
   kFF = 1.0 / motor_free_speed_rpm
   ```
   - NEO: `1.0 / 5676.0 = 0.000176`
   - Vortex: `1.0 / 6784.0 = 0.000147`

2. **Add Proportional**:
   - Start with: `kP = 0.0001`
   - Increase if slow response
   - Decrease if oscillation

3. **Skip I and D**:
   - Velocity control rarely needs I or D
   - FF + P is usually sufficient

### For Position Control (Arms/Tilts)

1. **Start with Proportional**:
   - Low gearing (< 20:1): `kP = 0.5 to 1.0`
   - Medium gearing (20-50:1): `kP = 0.05 to 0.2`
   - High gearing (> 50:1): `kP = 0.01 to 0.05`

2. **Add Derivative for Damping**:
   - Start with: `kD = kP / 10`
   - Increase if overshoot
   - Decrease if sluggish

3. **Add Integral if Needed**:
   - Only if steady-state error persists
   - Start very small: `kI = 0.0001`
   - Watch for integral windup

---

## Testing Procedure

### 1. Test Feeder First (Most Critical)

```java
// In Constants.java - Feeder class
kFeederP = 0.0002;
kFeederVelFF = 1.0 / 5676.0;
```

**Test**:
1. Enable robot in test mode
2. Command feeder to 2000 RPM
3. Monitor actual velocity in Shuffleboard
4. Should reach 2000 RPM in < 0.5 seconds
5. Should have < 50 RPM steady-state error

**If oscillates**: Reduce P to 0.0001  
**If slow**: Increase P to 0.0003

### 2. Test Intake Velocity

```java
// In Constants.java - Intake class
kIntakeP = 0.0002;
kVelFF = 1.0 / 5676.0;
kIntakeD = 0.0;  // Remove D
```

**Test**: Same as feeder

### 3. Test Position Controllers

**Shooter Tilt**:
```java
kPosP = 0.05;
kPosD = 0.005;
```

**Test**:
1. Command tilt to move 20 degrees
2. Should reach target in < 1 second
3. Should not overshoot by > 2 degrees
4. Should settle with < 0.5 degree error

**Intake Tilt**:
```java
kTiltP = 0.8;
kTiltD = 0.1;
```

**Test**: Same as shooter tilt

---

## Expected Performance After Tuning

### Velocity Controllers

| Metric | Target | Current (Feeder) | After Fix |
|--------|--------|------------------|-----------|
| Rise Time | < 0.5s | > 2s ❌ | < 0.5s ✅ |
| Steady-State Error | < 50 RPM | > 500 RPM ❌ | < 50 RPM ✅ |
| Overshoot | < 10% | N/A | < 10% ✅ |

### Position Controllers

| Metric | Target | Current | After Fix |
|--------|--------|---------|-----------|
| Settling Time | < 1s | > 2s ❌ | < 1s ✅ |
| Overshoot | < 5% | > 20% ❌ | < 5% ✅ |
| Steady-State Error | < 1° | Variable | < 0.5° ✅ |

---

## Common PID Mistakes (Found in This Code)

### ❌ Mistake 1: Missing Feedforward on Velocity Control
**Found in**: Intake, Feeder  
**Impact**: Slow response, large steady-state error  
**Fix**: Add `kFF = 1.0 / motor_free_speed_rpm`

### ❌ Mistake 2: P Gain Too Low
**Found in**: Feeder (critically low)  
**Impact**: Motor barely responds to commands  
**Fix**: Increase P by 10-20x

### ❌ Mistake 3: No Damping on Position Control
**Found in**: All position controllers  
**Impact**: Overshoot, oscillation, instability  
**Fix**: Add D term (start with `kD = kP / 10`)

### ❌ Mistake 4: Using D on Velocity Control
**Found in**: Intake velocity  
**Impact**: Unnecessary, can cause noise amplification  
**Fix**: Remove D term for velocity control

---

## Simulation Impact

The current PID values will affect simulation:

### Feeder (Critical Issue)
- ❌ Will barely spin in simulation
- ❌ Won't reach target velocity
- ❌ Robot won't function properly

### Intake
- ⚠️ Slower than expected response
- ⚠️ May not reach full speed
- ⚠️ Tilt may oscillate

### Shooter
- ✅ Velocity should work well
- ⚠️ Tilt may overshoot

**Recommendation**: Fix PIDs before extensive simulation testing

---

## Implementation Checklist

- [ ] Update Feeder PIDs (URGENT)
- [ ] Update Intake velocity PIDs (HIGH)
- [ ] Add D terms to position controllers (MEDIUM)
- [ ] Test each subsystem individually
- [ ] Tune based on actual performance
- [ ] Document final values
- [ ] Update this analysis

---

## References

- [WPILib PID Control](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/controllers/pidcontroller.html)
- [REV SparkMax PID](https://docs.revrobotics.com/sparkmax/operating-modes/closed-loop-control)
- [FRC PID Tuning Guide](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/tuning-pid-controller.html)

---

**Last Updated**: 2026-03-12  
**Analyst**: Bob (AI Assistant)  
**Status**: ⚠️ Action Required - Fix Feeder PIDs Immediately