# Shooter.java Improvement Guide

This document outlines recommended improvements for `src/main/java/frc/robot/subsystems/Shooter.java` based on a comprehensive code review.

## High Priority Issues

### 1. Missing Null Check for Drivetrain Dependency (HIGH - Functionality)
**Location:** Lines 300, 316, 331  
**Issue:** The `drivetrain` field is used without null checking, which will cause `NullPointerException` in tests where drivetrain is null.

**Fix:**
```java
// In periodic() method (line 300)
if (drivetrain != null) {
    var auto = ShooterBallistics.solveStationary(drivetrain.getDistToHub(), 0.5);
    sbAutoFeasible.setBoolean(auto.feasible());
    sbAutoAngleDeg.setDouble(lib.SBFormat(auto.feasible() ? auto.angleDeg() : ShooterBallistics.kMinAngleDeg));
    sbAutoRpm.setDouble(lib.SBFormat(auto.feasible() ? auto.wheelRpm() : 0.0));
}

// In getAutoShoot() method (line 316)
public double getAutoShoot() {
    if (drivetrain == null) return 0.0;
    double distToHubM = drivetrain.getDistToHub();
    var sp = ShooterBallistics.solveStationary(distToHubM, 0.5);
    if (!sp.feasible()) return 0.0;
    return sp.wheelRpm();
}

// In getAutoTilt() method (line 331)
public double getAutoTilt() {
    if (drivetrain == null) return ShooterBallistics.kMinAngleDeg;
    double distToHubM = drivetrain.getDistToHub();
    // ... rest of method
}
```

---

## Medium Priority Issues

### 2. Magic Numbers in Voltage Calculations (MEDIUM - Maintainability)
**Location:** Lines 391, 397  
**Issue:** The value `12.0` (battery voltage) appears without explanation.

**Fix:**
```java
// Add constant at top of class
private static final double MAX_VOLTAGE = 12.0;  // Maximum battery voltage

// Update methods
public void setShooterVel(ShooterSP sp) {
    setShooterSP(sp);
    leftController.setSetpoint(sp.getVel(false) / 100.0 * MAX_VOLTAGE, SparkBase.ControlType.kVoltage);
}

public void setShooterVel(double sp) {
    setShooterSPDbl(sp);
    leftController.setSetpoint(sp / MotorConstants.kVortexFreeSpeedRpm * MAX_VOLTAGE, SparkBase.ControlType.kVoltage);
}
```

### 3. Duplicate Conversion Logic (MEDIUM - Maintainability)
**Location:** Lines 383-384, 404-405  
**Issue:** RPM/percentage conversion logic is duplicated.

**Fix:**
```java
// Add helper methods
private double pctToRpm(double pct) {
    return (pct / 100.0) * Constants.MotorConstants.kVortexFreeSpeedRpm;
}

private double rpmToPct(double rpm) {
    return (rpm / Constants.MotorConstants.kVortexFreeSpeedRpm) * 100.0;
}

// Update getShooterSP method
public double getShooterSP(boolean rpm) {
    if (shooterSpIsCustom) {
        return rpm ? shooterSPDbl : rpmToPct(shooterSPDbl);
    }
    return shooterSP.getVel(rpm);
}

// Update getShooterVel method
public double getShooterVel(boolean rpm) {
    return rpm ? leftEncoder.getVelocity() : rpmToPct(leftEncoder.getVelocity());
}
```

### 4. Auto-Shoot Methods Don't Validate Ballistics Solution (MEDIUM - Functionality)
**Location:** Lines 273-277  
**Issue:** `autoShoot()` doesn't check if the ballistics solution is feasible.

**Fix:**
```java
public Command autoShoot() {
    return runOnce(() -> {
        if (drivetrain == null) return;
        
        double distToHubM = drivetrain.getDistToHub();
        var sp = ShooterBallistics.solveStationary(distToHubM, 0.5);
        
        if (sp.feasible()) {
            setTiltPos(sp.angleDeg());
            setShooterVel(sp.wheelRpm());
        }
        // If not feasible, maintain current position
    });
}
```

### 5. Missing JavaDoc Documentation (MEDIUM - Maintainability)
**Location:** Lines 251-457  
**Issue:** Most public methods lack JavaDoc comments.

**Fix:** Add JavaDoc to all public methods:
```java
/**
 * Creates a command to set the shooter velocity to a preset setpoint.
 * @param sp The shooter setpoint enum value
 * @return Command that sets the shooter velocity
 */
public Command setShooter(ShooterSP sp) {
    return runOnce(() -> this.setShooterVel(sp));
}

/**
 * Creates a command to set the shooter velocity to a custom RPM value.
 * @param sp The shooter velocity in RPM
 * @return Command that sets the shooter velocity
 */
public Command setShooter(double sp) {
    return runOnce(() -> this.setShooterVel(sp));
}

// Add similar documentation for all public methods
```

### 6. Unused Field (MEDIUM - Functionality)
**Location:** Line 111  
**Issue:** `tiltSetpointDeg` is declared but never used.

**Fix:** Remove the unused field:
```java
// DELETE this line:
private double tiltSetpointDeg = 0.0;
```

### 7. Tilt Position Clamping Inconsistency (MEDIUM - Security)
**Location:** Lines 435-444  
**Issue:** `setTiltPos(double)` clamps input, but `setTiltPos(TiltSP)` doesn't validate enum values.

**Fix:**
```java
public void setTiltPos(TiltSP sp) {
    setTiltSP(sp);
    // Validate enum value is within safe limits
    double pos = lib.clamp(sp.getPos(), TiltSP.LOW.getPos(), TiltSP.HI.getPos());
    tiltController.setSetpoint(pos, SparkBase.ControlType.kMAXMotionPositionControl);
}
```

---

## Low Priority Issues

### 8. Commented-Out Code (LOW - Maintainability)
**Location:** Lines 47-48, 52, 116-117, 377-378, 392, 398  
**Issue:** Multiple sections contain commented-out code.

**Fix:** Remove all commented-out code or add explanatory comments if needed for reference.

### 9. Generic Variable Name (LOW - Maintainability)
**Location:** Line 57  
**Issue:** Variable `lib` is too generic.

**Fix:**
```java
private Library utilities = new Library();
// Update all references from lib.method() to utilities.method()
```

### 10. Debug Print Statement (LOW - Maintainability)
**Location:** Line 437  
**Issue:** Using `System.out.println` instead of proper logging.

**Fix:**
```java
// Remove or replace with proper logging
// DELETE: System.out.println("+++++++++++++++++  setTilt: " + sp.getPos());
```

### 11. Ballistics Calculation Every Cycle (LOW - Performance)
**Location:** Lines 300-303  
**Issue:** Ballistics solver runs every 20ms even when not needed.

**Fix:**
```java
// Option 1: Add a flag to enable/disable
private boolean enableAutoDebug = true;  // Can be set via Shuffleboard

@Override
public void periodic() {
    // ... existing code ...
    
    if (enableAutoDebug && drivetrain != null) {
        var auto = ShooterBallistics.solveStationary(drivetrain.getDistToHub(), 0.5);
        sbAutoFeasible.setBoolean(auto.feasible());
        sbAutoAngleDeg.setDouble(lib.SBFormat(auto.feasible() ? auto.angleDeg() : ShooterBallistics.kMinAngleDeg));
        sbAutoRpm.setDouble(lib.SBFormat(auto.feasible() ? auto.wheelRpm() : 0.0));
    }
}

// Option 2: Cache result and only update when distance changes significantly
```

### 12. Inconsistent Indentation (LOW - Style)
**Location:** Lines 215-219  
**Issue:** Tilt configuration uses inconsistent indentation.

**Fix:** Use consistent tab indentation matching the rest of the file.

### 13. Inconsistent Enum Spacing (LOW - Style)
**Location:** Lines 63-99  
**Issue:** Enums have inconsistent blank line spacing.

**Fix:** Add consistent blank lines between enum values and methods.

---

## Constants.java Improvements

### Fix Inconsistent Naming in Constants
**Location:** `src/main/java/frc/robot/Constants.java` lines 214-217, 263, 267  
**Issue:** Tilt constants use lowercase 'k' prefix: `ktiltMotorInverted`, `ktiltIdleMode`, etc.

**Fix:**
```java
// In Constants.java, rename:
public static final double kTiltZeroOffset = 0.22696681;  // was ktiltZeroOffset
public static final boolean kTiltZeroCentered = true;     // was ktiltZeroCentered
public static final boolean kTiltMotorInverted = true;    // was ktiltMotorInverted
public static final boolean kTiltEncoderInverted = true;  // was ktiltEncoderInverted
public static final boolean kTiltEncodeWrapping = false;  // was ktiltEncodeWrapping
public static final IdleMode kTiltIdleMode = IdleMode.kBrake;  // was ktiltIdleMode
public static final int kTiltCurrentLimit = 50;           // was ktiltCurrentLimit

// Then update all references in Shooter.java
```

---

## Implementation Priority

1. **Start with High Priority** - Fix null checks first to prevent crashes
2. **Then Medium Priority** - Address magic numbers, DRY violations, and validation
3. **Finally Low Priority** - Clean up code style and remove dead code

## Testing Recommendations

After making changes:
1. Run existing unit tests: `./gradlew test`
2. Test with null drivetrain (simulates test environment)
3. Test auto-shoot functionality with real drivetrain
4. Verify Shuffleboard displays work correctly
5. Test both enum and custom setpoint modes

## Architecture Consideration

The Shooter class manages both shooter wheels and tilt mechanism. For better modularity, consider:
- Separating into `ShooterWheels` and `ShooterTilt` classes
- Using composition instead of inheritance
- This would improve testability and single responsibility

However, this is a larger refactoring and should be done carefully with full testing.