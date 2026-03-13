# FRC 2026 Robot Simulation - Quick Start Guide

## 🚀 Quick Start (3 Steps)

### 1. Start the Simulator
```
VS Code → Ctrl+Shift+P → "WPILib: Simulate Robot Code"
```

### 2. Launch Driver Station
- Open **FRC Driver Station** application
- It automatically connects to localhost
- Verify all lights are **GREEN** ✅

### 3. Enable and Drive
- Select **TeleOperated** mode
- Click **Enable** (or press Enter)
- Use your joysticks to control the robot! 🎮

---

## 📚 Complete Documentation

### Essential Guides

| Guide | Purpose | When to Use |
|-------|---------|-------------|
| **[DRIVER_STATION_LOCALHOST_CONFIG.md](DRIVER_STATION_LOCALHOST_CONFIG.md)** | Configure Driver Station for localhost | If connection fails or lights are red |
| **[DRIVER_STATION_SETUP.md](DRIVER_STATION_SETUP.md)** | Complete Driver Station setup | First-time setup or troubleshooting |
| **[SIMULATION_GUIDE.md](SIMULATION_GUIDE.md)** | Robot simulation features | Understanding simulation capabilities |

### Quick Links

- 🔧 **Connection Issues?** → [DRIVER_STATION_LOCALHOST_CONFIG.md](DRIVER_STATION_LOCALHOST_CONFIG.md#troubleshooting-connection-issues)
- 🎮 **Controller Setup?** → [DRIVER_STATION_SETUP.md](DRIVER_STATION_SETUP.md#joystickcontroller-setup)
- 🤖 **Add Simulation to Subsystem?** → [SIMULATION_GUIDE.md](SIMULATION_GUIDE.md#adding-simulation-to-new-subsystems)

---

## ✅ What's Already Configured

### Simulated Subsystems

All subsystems have realistic physics simulation:

- ✅ **Intake** - Roller velocity + tilt position control
- ✅ **Shooter** - Flywheel velocity + angle position control  
- ✅ **Feeder** - Roller velocity control
- ✅ **Climber** - Position control with multiple motors

### Simulation Features

- ✅ Realistic motor acceleration and deceleration
- ✅ Gravity effects on tilting mechanisms
- ✅ Position limits and safety constraints
- ✅ Current draw simulation
- ✅ PID control simulation
- ✅ Support for NEO, NEO 550, and Vortex motors

---

## 🔍 Troubleshooting

### Red Communications Light

**Problem**: Driver Station can't connect

**Quick Fix**:
1. Start simulator FIRST, then Driver Station
2. Set team number to `0` in Driver Station Setup tab
3. Restart both applications

**Detailed Help**: [DRIVER_STATION_LOCALHOST_CONFIG.md](DRIVER_STATION_LOCALHOST_CONFIG.md#issue-communications-light-stays-red)

### Red Robot Code Light

**Problem**: Code not detected

**Quick Fix**:
1. Check simulator console for errors
2. Rebuild: `./gradlew build`
3. Restart simulator

**Detailed Help**: [DRIVER_STATION_LOCALHOST_CONFIG.md](DRIVER_STATION_LOCALHOST_CONFIG.md#issue-robot-code-light-stays-red)

### Red Joysticks Light

**Problem**: Controllers not detected

**Quick Fix**:
1. Plug in controllers BEFORE starting Driver Station
2. Check USB Devices tab in Driver Station
3. Try different USB port

**Detailed Help**: [DRIVER_STATION_LOCALHOST_CONFIG.md](DRIVER_STATION_LOCALHOST_CONFIG.md#issue-joysticks-light-stays-red)

---

## 🎯 Common Tasks

### View Telemetry

**Using Shuffleboard**:
1. Launch Shuffleboard
2. It auto-connects to simulator
3. View motor speeds, positions, currents

**Using Glass**:
1. Launch Glass (lighter alternative)
2. Auto-connects to NetworkTables
3. View subsystem states

### Test Individual Subsystems

1. Select **Test** mode in Driver Station
2. Enable robot
3. Use Shuffleboard to send commands
4. Test each mechanism independently

### Run Autonomous

1. Select **Autonomous** mode
2. Enable robot
3. Auto routine runs for 15 seconds
4. Automatically switches to disabled

### Practice Match

1. Select **Practice** mode
2. Set match duration (default 2:30)
3. Enable to start
4. Auto runs first (15s), then teleop
5. Match ends automatically

---

## 📁 Project Structure

```
CODE_2026_Rebuilt/
├── src/main/java/frc/robot/
│   ├── Robot.java                    # Main robot class (simulation ready)
│   ├── subsystems/
│   │   ├── Intake.java              # ✅ Simulation configured
│   │   ├── Shooter.java             # ✅ Simulation configured
│   │   ├── Feeder.java              # ✅ Simulation configured
│   │   └── Climber.java             # ✅ Simulation configured
│   └── utils/
│       └── SparkMaxSimulation.java  # Simulation utility class
├── DRIVER_STATION_LOCALHOST_CONFIG.md  # Localhost configuration guide
├── DRIVER_STATION_SETUP.md             # Complete DS setup guide
├── SIMULATION_GUIDE.md                 # Simulation features guide
└── README_SIMULATION.md                # This file
```

---

## 🛠️ Development Workflow

### Making Code Changes

1. **Edit code** in VS Code
2. **Stop simulator** (if running)
3. **Rebuild**: `./gradlew build`
4. **Restart simulator**
5. **Test changes** with Driver Station

### Adding New Subsystem Simulation

See: [SIMULATION_GUIDE.md - Adding Simulation](SIMULATION_GUIDE.md#adding-simulation-to-new-subsystems)

Quick example:
```java
// In your subsystem constructor
if (Constants.currentMode == Constants.Mode.SIM) {
    motorSim = SparkMaxSimulation.createVelocitySim(
        motor, DCMotor.getNEO(1), gearRatio, 0.005
    );
}

// In simulationPeriodic()
if (motorSim != null) {
    motorSim.update(currentSetpoint, 0.02);
}
```

---

## 📊 Monitoring Simulation

### Key Metrics to Watch

| Metric | Where to View | What to Check |
|--------|---------------|---------------|
| Motor Velocity | Shuffleboard | Reaches target RPM |
| Motor Position | Shuffleboard | Reaches target angle |
| Current Draw | Shuffleboard | Realistic values |
| On Target Status | Shuffleboard | True when at setpoint |
| Console Logs | VS Code Terminal | No errors |

### Performance Tips

- Close unnecessary Shuffleboard widgets
- Reduce simulation update rate if laggy
- Monitor CPU usage in Task Manager
- Use Glass instead of Shuffleboard (lighter)

---

## 🎓 Learning Resources

### For New Programmers

1. Start with [DRIVER_STATION_SETUP.md](DRIVER_STATION_SETUP.md)
2. Learn basic controls and enabling
3. Practice with teleop mode
4. Experiment with test mode

### For Experienced Programmers

1. Review [SIMULATION_GUIDE.md](SIMULATION_GUIDE.md)
2. Understand simulation physics
3. Tune PID parameters
4. Add custom simulation features

### External Resources

- [WPILib Documentation](https://docs.wpilib.org/)
- [Chief Delphi Forums](https://www.chiefdelphi.com/)
- [REV Robotics Docs](https://docs.revrobotics.com/)

---

## ❓ FAQ

**Q: Do I need a real robot to test code?**  
A: No! The simulator provides realistic physics for testing.

**Q: Can I use keyboard instead of joysticks?**  
A: Yes, configure keyboard mappings in `simgui-ds.json`.

**Q: Why are my motors not moving?**  
A: Check that simulation is initialized in subsystem constructors.

**Q: How do I add more motors?**  
A: See [SIMULATION_GUIDE.md](SIMULATION_GUIDE.md#adding-simulation-to-new-subsystems).

**Q: Can I simulate sensors?**  
A: Basic sensor simulation is included. Advanced sensors need custom code.

**Q: Does this work on Mac/Linux?**  
A: Yes! Driver Station is Windows-only, but simulation works on all platforms.

---

## 🆘 Getting Help

1. **Check documentation** (this file and linked guides)
2. **Review console logs** for error messages
3. **Search Chief Delphi** for similar issues
4. **Ask your programming mentor**
5. **Post on Chief Delphi** with details

---

## ✨ Features Summary

### What Works in Simulation

✅ All SparkMax/SparkFlex motor controllers  
✅ Velocity control (flywheels, rollers)  
✅ Position control (arms, tilts)  
✅ Gravity simulation  
✅ Current draw  
✅ PID control  
✅ Driver Station integration  
✅ NetworkTables  
✅ Shuffleboard/Glass  
✅ Autonomous routines  
✅ Joystick input  

### What Doesn't Work

❌ Physical sensors (encoders work via simulation)  
❌ Pneumatics (requires custom simulation)  
❌ Vision processing (requires PhotonVision sim)  
❌ CAN bus timing (simulated as instant)  

---

**Last Updated**: 2026-03-12  
**Version**: 1.0  
**Status**: ✅ Ready for Use

**Happy Simulating! 🤖**