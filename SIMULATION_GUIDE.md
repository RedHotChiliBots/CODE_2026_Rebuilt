# FRC 2026 Robot Simulation Guide

## Overview

This project now includes comprehensive simulation support for all REV SparkMax and SparkFlex motor controllers. The simulation provides realistic physics-based behavior for both velocity control (flywheels, rollers) and position control (arms, tilts) mechanisms.

## What's Been Configured

### Simulation Utility Class

**`src/main/java/frc/robot/utils/SparkMaxSimulation.java`**

A lightweight simulation utility that provides:
- Velocity control simulation for flywheels and rollers
- Position control simulation for arms and tilt mechanisms
- Realistic motor physics including acceleration, damping, and gravity effects
- Support for both SparkMax and SparkFlex controllers

### Subsystems with Simulation

All subsystems with SparkMax/SparkFlex motors now have simulation support:

#### 1. **Intake Subsystem**
- **Intake Motor**: Velocity control simulation (NEO motor)
- **Tilt Motor**: Position control simulation with gravity (NEO motor)
- Simulates intake roller spinning and arm tilting

#### 2. **Shooter Subsystem**
- **Left/Right Shooter Motors**: Velocity control simulation (Vortex motors)
- **Tilt Motor**: Position control simulation with gravity (NEO motor)
- Simulates flywheel spin-up and shooter angle adjustment

#### 3. **Feeder Subsystem**
- **Feeder Motor**: Velocity control simulation (NEO motor)
- Simulates note feeding mechanism

#### 4. **Climber Subsystem**
- **Climber Motors**: Position control simulation (NEO motors)
- Simulates vertical climbing mechanism

## How to Use Simulation

### Running the Simulation

1. **Start the Robot Simulation**:
   - In VS Code, press `Ctrl+Shift+P` (or `Cmd+Shift+P` on Mac)
   - Type "WPILib: Simulate Robot Code"
   - Select your robot project

2. **Connect FRC Driver Station**:
   - Launch the FRC Driver Station application
   - It will automatically connect to the simulator (localhost)
   - Verify all three lights are green (Communications, Robot Code, Joysticks)
   - **See [DRIVER_STATION_SETUP.md](DRIVER_STATION_SETUP.md) for detailed instructions**

3. **Enable the Robot**:
   - Select your desired mode (TeleOperated, Autonomous, Test)
   - Click "Enable" or press Enter
   - The robot is now active in simulation

4. **Control the Robot**:
   - Use your joystick/controller as you would on a real robot
   - All motor commands will be simulated with realistic physics
   - Monitor telemetry in Shuffleboard or Glass

### Simulation Features

#### Velocity Control (Intake, Shooter, Feeder)
- Motors accelerate realistically toward target RPM
- Includes motor inertia and damping effects
- Simulates current draw based on load

#### Position Control (Tilt mechanisms, Climber)
- Arms move smoothly to target positions
- Gravity effects are simulated for tilting mechanisms
- Position limits are enforced
- PD control provides realistic motion profiles

### Monitoring Simulation

Use Shuffleboard or Glass to monitor:
- Motor velocities (RPM)
- Motor positions (degrees)
- Current draw (amps)
- On-target status
- Setpoints vs actual values

## Simulation Parameters

### Tuning Simulation Behavior

If you need to adjust simulation realism, modify these parameters in `SparkMaxSimulation.java`:

#### Velocity Control
```java
double kP = 0.1;  // Proportional gain for velocity control
double maxAccelRPMPerSec = 10000.0;  // Maximum acceleration
currentVelocityRPM *= 0.99;  // Damping factor
```

#### Position Control
```java
double kP = 2.0;  // Proportional gain for position
double kD = 0.1;  // Derivative gain for damping
double maxVelDegPerSec = 500.0;  // Maximum velocity
double accelDegPerSec2 = 2000.0;  // Maximum acceleration
```

### Motor Specifications

The simulation uses these motor types:
- **NEO**: `DCMotor.getNEO(1)` - 5676 RPM free speed
- **NEO 550**: `DCMotor.getNeo550(1)` - 11000 RPM free speed
- **NEO Vortex**: `DCMotor.getNeoVortex(1)` - 6784 RPM free speed

## Adding Simulation to New Subsystems

To add simulation to a new subsystem with SparkMax motors:

### 1. Import Required Classes

```java
import frc.robot.utils.SparkMaxSimulation;
import edu.wpi.first.math.system.plant.DCMotor;
```

### 2. Add Simulation Field

```java
private SparkMaxSimulation motorSim;
```

### 3. Initialize in Constructor

For velocity control (flywheel/roller):
```java
if (Constants.currentMode == Constants.Mode.SIM) {
    motorSim = SparkMaxSimulation.createVelocitySim(
        motor,                    // Your SparkMax/SparkFlex
        DCMotor.getNEO(1),       // Motor type
        gearRatio,               // Gear ratio
        0.005                    // Moment of inertia (kg*m^2)
    );
}
```

For position control (arm/tilt):
```java
if (Constants.currentMode == Constants.Mode.SIM) {
    motorSim = SparkMaxSimulation.createPositionSim(
        motor,                    // Your SparkMax/SparkFlex
        DCMotor.getNEO(1),       // Motor type
        gearRatio,               // Gear ratio
        0.5,                     // Arm length (meters)
        minAngleDeg,             // Minimum angle
        maxAngleDeg,             // Maximum angle
        true,                    // Simulate gravity
        startingAngleDeg         // Starting angle
    );
}
```

### 4. Update in simulationPeriodic()

```java
@Override
public void simulationPeriodic() {
    if (motorSim != null) {
        motorSim.update(currentSetpoint, 0.02);
    }
}
```

## Simulation Modes

The robot supports three modes (defined in `Constants.java`):

- **REAL**: Running on actual robot hardware
- **SIM**: Running in simulation
- **REPLAY**: Replaying from log files

Simulation is automatically enabled when `RobotBase.isReal()` returns false.

## Testing Your Code

### Unit Tests

The project includes unit tests that can run in simulation mode:
- `IntakeTest.java`
- `ShooterTest.java`
- `FeederTest.java`

Run tests with: `./gradlew test`

### Integration Testing

1. Start simulation
2. Enable robot
3. Test each subsystem command from Shuffleboard
4. Verify motor behavior matches expectations
5. Check for any errors in the console

## Troubleshooting

### Motors Not Moving in Simulation

- Check that `Constants.currentMode == Constants.Mode.SIM`
- Verify simulation objects are initialized in constructor
- Ensure `simulationPeriodic()` is calling `update()`

### Unrealistic Behavior

- Adjust PID gains in `SparkMaxSimulation.java`
- Verify moment of inertia values are reasonable
- Check gear ratios match your robot configuration

### Performance Issues

- Simulation runs at 50Hz (20ms updates)
- Reduce complexity if simulation is slow
- Check for infinite loops in periodic methods

## Advanced Features

### Custom Physics Models

You can extend `SparkMaxSimulation` to add:
- More sophisticated motor models
- Load-dependent behavior
- Temperature simulation
- Battery voltage effects

### Sensor Simulation

Future enhancements could include:
- Encoder noise simulation
- Absolute encoder simulation
- Current limit simulation

## Additional Documentation

- **[DRIVER_STATION_SETUP.md](DRIVER_STATION_SETUP.md)** - Complete guide for connecting FRC Driver Station to simulator
- [WPILib Simulation Documentation](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/index.html)
- [REV Robotics SparkMax Documentation](https://docs.revrobotics.com/sparkmax/)
- [FRC Control System Documentation](https://docs.wpilib.org/en/stable/index.html)

## Support

For issues or questions:
1. Check this guide first
2. Review WPILib simulation documentation
3. Consult with your programming mentor
4. Post on Chief Delphi forums

---

**Last Updated**: 2026-03-12
**Author**: Bob (AI Assistant)
**Version**: 1.0