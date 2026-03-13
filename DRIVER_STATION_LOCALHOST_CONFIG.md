# Configuring FRC Driver Station for Localhost (Simulation)

## Overview

This guide provides step-by-step instructions for configuring the FRC Driver Station to connect to your robot simulator running on localhost (127.0.0.1).

## Prerequisites

- ✅ FRC Driver Station installed (from FRC Game Tools)
- ✅ Robot simulator running in VS Code
- ✅ Windows Firewall configured to allow connections

---

## Method 1: Automatic Configuration (Recommended)

The FRC Driver Station **automatically detects** localhost when the simulator is running. No manual configuration is typically needed!

### Steps:

1. **Start your robot simulator first**
   ```
   VS Code → Ctrl+Shift+P → "WPILib: Simulate Robot Code"
   ```

2. **Launch FRC Driver Station**
   - The Driver Station will automatically detect the simulator
   - It connects to `127.0.0.1` (localhost) by default in simulation mode

3. **Verify Connection**
   - Check the status lights at the top of the Driver Station window
   - All three should be **GREEN**:
     - 🟢 **Communications** - Connected to robot
     - 🟢 **Robot Code** - Code is running
     - 🟢 **Joysticks** - Controllers connected (if you have any)

✅ **If all lights are green, you're done! Skip to the "Testing Connection" section.**

---

## Method 2: Manual Team Number Configuration

If automatic detection doesn't work, manually configure the team number:

### Steps:

1. **Open FRC Driver Station**

2. **Click on the "Setup" tab** (top of window)

3. **Set Team Number to 0**
   - In the "Team Number" field, enter: `0`
   - This tells the Driver Station to use localhost

4. **Alternative: Use Team Number 1**
   - Some versions work better with team number `1`
   - Try this if `0` doesn't work

5. **Click "Apply" or press Enter**

6. **Return to "Operation" tab**

7. **Verify connection** (lights should turn green)

---

## Method 3: Direct IP Configuration (Advanced)

For explicit localhost configuration:

### Steps:

1. **Open FRC Driver Station**

2. **Go to "Setup" tab**

3. **Find "Team Station" section**

4. **Configure Network Settings**:
   - **Team Number**: `0` or `1`
   - **Practice Mode**: Unchecked (unless you want timed matches)

5. **Windows Network Configuration** (if needed):
   - Open Windows Settings → Network & Internet
   - Ensure localhost (127.0.0.1) is accessible
   - Check that no VPN is blocking local connections

---

## Verifying Localhost Connection

### Check Connection Status

In the FRC Driver Station window, look for these indicators:

#### Communications Light
- 🟢 **GREEN**: Connected to robot simulator
- 🔴 **RED**: Not connected
  - **Fix**: Ensure simulator is running first
  - **Fix**: Check team number is set to 0 or 1
  - **Fix**: Restart Driver Station

#### Robot Code Light
- 🟢 **GREEN**: Robot code is running
- 🔴 **RED**: No code detected
  - **Fix**: Check simulator console for errors
  - **Fix**: Rebuild robot code
  - **Fix**: Restart simulator

#### Joysticks Light
- 🟢 **GREEN**: Controllers connected
- 🔴 **RED**: No controllers
  - **Fix**: Plug in USB controllers before starting Driver Station
  - **Fix**: Check USB connection
  - **Note**: This can be red if you don't have controllers (that's OK)

### Network Diagnostics

1. **Open Command Prompt** (Windows) or **Terminal** (Mac/Linux)

2. **Test localhost connectivity**:
   ```bash
   ping 127.0.0.1
   ```
   
   Expected output:
   ```
   Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
   Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
   ```

3. **Check if simulator is listening**:
   ```bash
   netstat -an | findstr "1735"
   ```
   
   Should show NetworkTables port 1735 is listening

---

## Troubleshooting Connection Issues

### Issue: Communications Light Stays Red

**Symptoms**: Driver Station can't connect to simulator

**Solutions**:

1. **Start simulator BEFORE Driver Station**
   ```
   ✅ Correct order:
   1. Start simulator in VS Code
   2. Wait for "Robot code started" message
   3. Launch Driver Station
   ```

2. **Check Windows Firewall**
   - Open Windows Defender Firewall
   - Click "Allow an app through firewall"
   - Ensure these are checked:
     - ✅ Java(TM) Platform SE binary
     - ✅ FRC Driver Station
     - ✅ WPILib applications

3. **Verify Team Number**
   - Setup tab → Team Number = `0` or `1`
   - Click Apply

4. **Restart Both Applications**
   - Close Driver Station
   - Stop simulator (click Stop in VS Code)
   - Start simulator first
   - Launch Driver Station second

5. **Check for Port Conflicts**
   - Ensure no other application is using port 1735
   - Close any other robot simulators
   - Close LabVIEW or other FRC tools

### Issue: Robot Code Light Stays Red

**Symptoms**: Driver Station connects but doesn't see robot code

**Solutions**:

1. **Check Simulator Console**
   - Look for Java errors or exceptions
   - Verify "Robot code started" message appears

2. **Rebuild Robot Code**
   ```
   VS Code → Terminal → ./gradlew build
   ```

3. **Check Robot.java**
   - Ensure no compilation errors
   - Verify `Robot` class extends `TimedRobot`

4. **Restart Simulator**
   - Stop current simulation
   - Clean build: `./gradlew clean build`
   - Start simulator again

### Issue: Joysticks Light Stays Red

**Symptoms**: Controllers not detected

**Solutions**:

1. **Connect Controllers BEFORE Starting Driver Station**
   - Plug in all USB controllers
   - Wait for Windows to recognize them
   - Then launch Driver Station

2. **Check USB Devices Tab**
   - In Driver Station, click "USB Devices" tab
   - You should see your controllers listed
   - Move joysticks to verify they're working

3. **Update Controller Drivers**
   - Open Device Manager
   - Find your controller under "Human Interface Devices"
   - Right-click → Update driver

4. **Try Different USB Port**
   - Some USB 3.0 ports have issues
   - Try USB 2.0 ports instead

### Issue: Connection Drops Randomly

**Symptoms**: Lights turn red intermittently

**Solutions**:

1. **Check System Resources**
   - Open Task Manager
   - Ensure CPU < 80%
   - Ensure RAM is available
   - Close unnecessary applications

2. **Disable Power Saving**
   - Windows Settings → System → Power & Sleep
   - Set to "High Performance" mode
   - Disable USB selective suspend

3. **Check Network Adapter**
   - Device Manager → Network Adapters
   - Right-click adapter → Properties
   - Power Management tab
   - Uncheck "Allow computer to turn off this device"

---

## Advanced Configuration

### Custom NetworkTables Port

If you need to use a different port:

1. **In your robot code**, modify `Robot.java`:
   ```java
   NetworkTableInstance.getDefault().setServerTeam(0);
   NetworkTableInstance.getDefault().startServer(1735); // Custom port
   ```

2. **Configure Driver Station** to match

### Multiple Simulators

To run multiple robot simulators:

1. **First simulator**: Uses default ports (team 0)
2. **Second simulator**: Change team number to 1
3. **Third simulator**: Change team number to 2
4. Each gets its own NetworkTables instance

### Firewall Rules (If Needed)

Create explicit firewall rules:

1. **Open Windows Defender Firewall with Advanced Security**

2. **Create Inbound Rule**:
   - Rule Type: Port
   - Protocol: TCP
   - Specific local ports: `1735, 1110-1115`
   - Action: Allow the connection
   - Profile: All
   - Name: "FRC Robot Simulator"

3. **Create Outbound Rule** (same settings)

---

## Configuration Files

### Driver Station Configuration

Location: `C:\Users\[YourName]\FRC Driver Station\`

Files:
- `FRC DS Data Storage.ini` - Main configuration
- `FRC DS Log Files\` - Connection logs

### Simulator Configuration

Location: Your robot project directory

Files:
- `simgui-ds.json` - Driver Station simulation config
- `networktables.json` - NetworkTables data (runtime)

### Example simgui-ds.json

```json
{
  "keyboardJoysticks": [
    {
      "guid": "Keyboard0"
    }
  ],
  "useGamepad": true,
  "HALProvider": {
    "SimDevice": {
      "SPARK MAX [1]": {
        "open": true
      }
    }
  }
}
```

---

## Testing Your Configuration

### Step-by-Step Test

1. **Start Simulator**
   ```
   VS Code → Simulate Robot Code
   ```

2. **Launch Driver Station**
   - Should auto-connect to localhost

3. **Verify All Lights Green**
   - Communications: 🟢
   - Robot Code: 🟢
   - Joysticks: 🟢 (if controllers connected)

4. **Select TeleOperated Mode**

5. **Enable Robot**
   - Click "Enable" button
   - Or press Enter key

6. **Test Motor Control**
   - Move joysticks
   - Motors should respond in simulation
   - Check Shuffleboard for telemetry

7. **Disable Robot**
   - Click "Disable" button
   - Or press Space key (E-Stop)

### Success Criteria

✅ All status lights are green  
✅ Robot enables without errors  
✅ Joystick input controls robot  
✅ Telemetry appears in Shuffleboard  
✅ No errors in console  
✅ Motors simulate realistically  

---

## Quick Reference

### Connection Checklist

- [ ] Simulator started first
- [ ] Driver Station launched second
- [ ] Team number set to 0 or 1
- [ ] All three lights are green
- [ ] Controllers plugged in (if using)
- [ ] Firewall allows connections
- [ ] No port conflicts

### Common Team Numbers

| Team Number | Use Case |
|-------------|----------|
| 0 | Default for localhost simulation |
| 1 | Alternative localhost configuration |
| 2-9999 | Real robot team numbers |

### Important Ports

| Port | Purpose |
|------|---------|
| 1735 | NetworkTables |
| 1110-1115 | Driver Station communication |
| 5800-5810 | Camera streams |

### Keyboard Shortcuts

| Key | Action |
|-----|--------|
| Enter | Enable/Disable robot |
| Space | Emergency Stop |
| [ | Previous mode |
| ] | Next mode |
| \ | Reboot roboRIO (N/A in sim) |

---

## Additional Resources

- **Driver Station Manual**: [WPILib Docs](https://docs.wpilib.org/en/stable/docs/software/driverstation/driver-station.html)
- **Simulation Guide**: [SIMULATION_GUIDE.md](SIMULATION_GUIDE.md)
- **NetworkTables**: [WPILib NetworkTables](https://docs.wpilib.org/en/stable/docs/software/networktables/index.html)

---

## Support

If you continue to have connection issues:

1. Check the [DRIVER_STATION_SETUP.md](DRIVER_STATION_SETUP.md) guide
2. Review WPILib simulation documentation
3. Check Chief Delphi forums for similar issues
4. Consult with your programming mentor

---

**Last Updated**: 2026-03-12  
**Version**: 1.0  
**For**: FRC 2026 Robot Simulation