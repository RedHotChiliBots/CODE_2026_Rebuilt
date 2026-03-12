# FRC Driver Station Connection to Simulator

## Overview

This guide explains how to connect the FRC Driver Station software to your robot simulator, allowing you to control the simulated robot just like you would control a real robot.

## Prerequisites

- **FRC Driver Station** installed (comes with FRC Game Tools)
- **Robot code** configured for simulation (already done in this project)
- **VS Code** with WPILib extension installed

## Step-by-Step Setup

### 1. Start the Robot Simulator

1. Open your robot project in VS Code
2. Press `Ctrl+Shift+P` (Windows/Linux) or `Cmd+Shift+P` (Mac)
3. Type and select: **"WPILib: Simulate Robot Code"**
4. Select your robot project from the list
5. The simulation GUI will open showing:
   - Robot code status
   - NetworkTables connection
   - Joystick inputs
   - System resources

### 2. Launch FRC Driver Station

1. Open the **FRC Driver Station** application
2. You should see the connection status in the top-left corner
3. The Driver Station will automatically try to connect to `localhost` (127.0.0.1)
4. **For detailed localhost configuration, see [DRIVER_STATION_LOCALHOST_CONFIG.md](DRIVER_STATION_LOCALHOST_CONFIG.md)**

### 3. Verify Connection

Check these indicators on the Driver Station:

✅ **Communications**: Should show **green** (connected)
✅ **Robot Code**: Should show **green** (code running)
✅ **Joysticks**: Should show **green** if controllers are connected

If any are red, see the Troubleshooting section below.

### 4. Enable the Robot

1. **Select a mode**:
   - **TeleOperated**: For manual control
   - **Autonomous**: For auto routines
   - **Practice**: For timed practice matches
   - **Test**: For testing individual mechanisms

2. **Enable the robot**:
   - Click the **Enable** button, or
   - Press `Enter` on your keyboard, or
   - Press the enable button on your joystick (if configured)

3. The robot is now active in simulation!

### 5. Control the Robot

- Use your joysticks/controllers as you would on a real robot
- All commands will execute in the simulator
- Motor movements will be simulated with realistic physics
- View telemetry in Shuffleboard or Glass

## Connection Architecture

```
┌─────────────────────┐
│  FRC Driver Station │
│   (localhost:1735)  │
└──────────┬──────────┘
           │
           │ NetworkTables
           │ & DS Protocol
           │
┌──────────▼──────────┐
│   Robot Simulator   │
│  (WPILib Sim GUI)   │
└──────────┬──────────┘
           │
           │ Simulated
           │ Hardware
           │
┌──────────▼──────────┐
│   Robot Code        │
│   (Your Program)    │
└─────────────────────┘
```

## Configuration Files

### simgui-ds.json

This file (in your project root) configures the simulation GUI for Driver Station integration:

```json
{
  "keyboardJoysticks": [
    {
      "guid": "Keyboard0"
    }
  ],
  "useGamepad": true
}
```

### Network Configuration

The simulator automatically configures:
- **Robot IP**: 127.0.0.1 (localhost)
- **NetworkTables Port**: 1735
- **Driver Station Port**: 1110-1115

No manual network configuration is needed!

## Using Shuffleboard/Glass

### With Shuffleboard

1. Launch Shuffleboard
2. It will automatically connect to the simulator
3. View and control robot telemetry
4. Send commands to subsystems
5. Monitor motor states and sensor values

### With Glass

1. Launch Glass (newer, lighter alternative)
2. Automatic connection to simulator
3. View NetworkTables data
4. Monitor subsystem states
5. Visualize robot pose and field position

## Joystick/Controller Setup

### Connecting Controllers

1. **Plug in your USB controllers** before starting the Driver Station
2. The Driver Station will detect them automatically
3. Check the **USB Devices** tab to see connected devices

### Configuring Joysticks

In your robot code ([`RobotContainer.java`](src/main/java/frc/robot/RobotContainer.java)):

```java
// Driver controller on port 0
private final CommandXboxController driverController = 
    new CommandXboxController(Constants.OIConstants.kDriverControllerPort);

// Operator controller on port 1
private final CommandXboxController operatorController = 
    new CommandXboxController(Constants.OIConstants.kOperatorControllerPort);
```

### Testing Joystick Input

1. Open the **USB Devices** tab in Driver Station
2. Move joystick axes and press buttons
3. You should see the values change in real-time
4. If not, check USB connection and driver installation

## Troubleshooting

### Driver Station Won't Connect

**Problem**: Communications light is red

**Solutions**:
1. Ensure the simulator is running first
2. Set team number to 0 or 1 in Driver Station Setup tab
3. Check that no firewall is blocking localhost connections
4. Restart both the simulator and Driver Station
5. Check Windows Firewall settings (allow Java/WPILib)
6. **See [DRIVER_STATION_LOCALHOST_CONFIG.md](DRIVER_STATION_LOCALHOST_CONFIG.md) for detailed troubleshooting**

### Robot Code Light is Red

**Problem**: Robot code not detected

**Solutions**:
1. Verify the simulator shows "Robot Code: Running"
2. Check the console for Java errors
3. Rebuild and restart the simulation
4. Check that `Robot.java` has no compilation errors

### Joysticks Not Working

**Problem**: USB Devices light is red or controllers not responding

**Solutions**:
1. Plug in controllers before starting Driver Station
2. Check USB connection
3. Update controller drivers
4. Try a different USB port
5. Restart Driver Station after connecting controllers

### Robot Doesn't Move

**Problem**: Robot is enabled but motors don't respond

**Solutions**:
1. Check that commands are being sent (view in Shuffleboard)
2. Verify motor simulation is initialized (check console logs)
3. Ensure subsystems are properly configured
4. Check for errors in the Driver Station console

### Simulation is Slow/Laggy

**Problem**: Poor performance

**Solutions**:
1. Close unnecessary applications
2. Reduce Shuffleboard widget count
3. Disable unused subsystems in simulation
4. Check CPU usage in Task Manager
5. Update graphics drivers

## Advanced Features

### Practice Mode

Simulate a full match with timing:
1. Select **Practice** mode
2. Set match duration (default: 2:30)
3. Enable to start the match
4. Autonomous runs first (15 seconds)
5. Teleop follows automatically
6. Match ends after time expires

### Test Mode

Test individual mechanisms:
1. Select **Test** mode
2. Enable the robot
3. Use Shuffleboard to send individual commands
4. Test motors, sensors, and subsystems independently

### Keyboard Control (No Joystick)

If you don't have a physical controller:
1. The simulator supports keyboard input
2. Configure keyboard mappings in `simgui-ds.json`
3. Use WASD for driving, other keys for mechanisms
4. See WPILib docs for keyboard mapping details

## Best Practices

### Before Each Simulation Session

1. ✅ Connect all USB controllers
2. ✅ Start the simulator first
3. ✅ Launch Driver Station second
4. ✅ Verify all three lights are green
5. ✅ Test joystick inputs before enabling

### During Simulation

1. 🎮 Use the same controls as on the real robot
2. 📊 Monitor telemetry in Shuffleboard
3. 🔍 Watch for errors in the console
4. 💾 Save Shuffleboard layouts for reuse
5. 📝 Document any issues or unexpected behavior

### After Simulation

1. 🛑 Disable the robot before closing
2. 💾 Save any configuration changes
3. 📋 Review console logs for warnings
4. 🔄 Close Driver Station before closing simulator

## Integration with Version Control

### Files to Commit

- ✅ `simgui-ds.json` - Driver Station simulation config
- ✅ `simgui.json` - Simulation GUI layout
- ✅ Robot code with simulation support

### Files to Ignore (.gitignore)

- ❌ `networktables.json` - Runtime NetworkTables data
- ❌ `*.log` - Log files
- ❌ Shuffleboard layouts (team preference)

## Resources

### Project Documentation
- **[DRIVER_STATION_LOCALHOST_CONFIG.md](DRIVER_STATION_LOCALHOST_CONFIG.md)** - Detailed localhost configuration guide
- **[SIMULATION_GUIDE.md](SIMULATION_GUIDE.md)** - Robot simulation guide

### External Resources
- [WPILib Simulation Docs](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/index.html)
- [Driver Station Manual](https://docs.wpilib.org/en/stable/docs/software/driverstation/driver-station.html)
- [NetworkTables Guide](https://docs.wpilib.org/en/stable/docs/software/networktables/index.html)
- [Shuffleboard Documentation](https://docs.wpilib.org/en/stable/docs/software/dashboards/shuffleboard/index.html)

## Quick Reference

### Keyboard Shortcuts (Driver Station)

- `Enter` - Enable/Disable robot
- `Space` - Emergency Stop
- `[` / `]` - Switch modes
- `\` - Reboot roboRIO (N/A in sim)

### Common Issues Quick Fix

| Issue | Quick Fix |
|-------|-----------|
| Red Communications | Restart simulator first, then DS |
| Red Robot Code | Check console for errors, rebuild |
| Red Joysticks | Plug in before starting DS |
| Robot won't enable | Check for code errors in console |
| Laggy simulation | Close Shuffleboard, reduce widgets |

---

**Last Updated**: 2026-03-12  
**For**: FRC Team Robot Simulation  
**Version**: 1.0