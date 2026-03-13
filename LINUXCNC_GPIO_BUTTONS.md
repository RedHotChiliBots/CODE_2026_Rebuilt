# LinuxCNC GPIO Button Setup for Raspberry Pi 5

## Overview

This guide explains how to connect physical start cycle and stop cycle buttons to a Raspberry Pi 5's GPIO pins for use with LinuxCNC.

## Hardware Requirements

- Raspberry Pi 5
- 2x Momentary push buttons (normally open)
- 2x 10kΩ pull-down resistors (optional but recommended)
- Jumper wires
- Breadboard (optional)

## GPIO Pin Selection

### Recommended Pins

| Function | GPIO Pin | Physical Pin | Notes |
|----------|----------|--------------|-------|
| Start Cycle | GPIO 17 | Pin 11 | Safe for button input |
| Stop Cycle | GPIO 27 | Pin 13 | Safe for button input |
| Ground | GND | Pin 9 or 14 | Common ground |
| 3.3V | 3.3V | Pin 1 or 17 | For pull-up (if needed) |

**Important**: Raspberry Pi GPIO pins are 3.3V tolerant. Never connect 5V directly!

## Wiring Diagram

### Option 1: Internal Pull-Down (Recommended)

```
Button 1 (Start Cycle):
┌─────────┐
│ Button  │
│  (NO)   │
└────┬────┘
     │
     ├──────────── GPIO 17 (Pin 11)
     │
     └──────────── GND (Pin 9)

Button 2 (Stop Cycle):
┌─────────┐
│ Button  │
│  (NO)   │
└────┬────┘
     │
     ├──────────── GPIO 27 (Pin 13)
     │
     └──────────── GND (Pin 14)
```

### Option 2: External Pull-Down Resistors

```
                    3.3V (Pin 1)
                       │
Button 1:              │
┌─────────┐            │
│ Button  │            │
│  (NO)   │            │
└────┬────┘            │
     │                 │
     ├──────────────── GPIO 17 (Pin 11)
     │                 │
     └─── 10kΩ ────────┘
          Resistor
          
Button 2: (Same configuration for GPIO 27)
```

## Physical Connection Steps

### Step 1: Power Off Raspberry Pi
```bash
sudo shutdown -h now
```
Wait for Pi to fully power down before connecting wires.

### Step 2: Connect Buttons

1. **Start Cycle Button**:
   - One terminal → GPIO 17 (Physical Pin 11)
   - Other terminal → GND (Physical Pin 9)

2. **Stop Cycle Button**:
   - One terminal → GPIO 27 (Physical Pin 13)
   - Other terminal → GND (Physical Pin 14)

### Step 3: Verify Connections

Double-check:
- ✅ No shorts between 3.3V and GND
- ✅ Buttons are normally open (NO)
- ✅ Correct GPIO pins used
- ✅ Solid connections

## LinuxCNC Configuration

### Step 1: Edit HAL Configuration

Open your LinuxCNC HAL file using `vi`:
```bash
vi ~/linuxcnc/configs/your_config/custom.hal
```

**Vi Quick Reference**:
- Press `i` to enter INSERT mode (to type)
- Press `Esc` to exit INSERT mode
- Type `:wq` and press Enter to save and quit
- Type `:q!` and press Enter to quit without saving
- Use arrow keys to navigate

### Step 2: Add GPIO Component

**Using vi to add GPIO configuration**:

1. Open the file:
   ```bash
   vi ~/linuxcnc/configs/your_config/custom.hal
   ```

2. Press `i` to enter INSERT mode

3. Type or paste these lines:
   ```hal
   # Load the Raspberry Pi GPIO component
   loadrt hal_pi_gpio dir=17:in,27:in

   # Add GPIO to servo thread
   addf hal_pi_gpio.read servo-thread
   ```

4. Press `Esc` to exit INSERT mode

5. Type `:wq` and press Enter to save and quit

### Step 3: Configure Button Inputs

**Continue editing in vi**:

1. If you closed vi, reopen:
   ```bash
   vi ~/linuxcnc/configs/your_config/custom.hal
   ```

2. Press `Shift+G` to go to end of file

3. Press `o` to open a new line below and enter INSERT mode

4. Add button signal definitions:
   ```hal
   # Create signals for buttons
   net start-cycle-btn <= hal_pi_gpio.pin-17-in
   net stop-cycle-btn  <= hal_pi_gpio.pin-27-in

   # Invert signals if needed (buttons pull to ground)
   setp hal_pi_gpio.pin-17-in-invert true
   setp hal_pi_gpio.pin-27-in-invert true
   ```

5. Press `Esc`, then type `:wq` and press Enter

### Step 4: Connect to LinuxCNC Functions

**Final vi editing**:

1. Reopen file:
   ```bash
   vi ~/linuxcnc/configs/your_config/custom.hal
   ```

2. Press `Shift+G` to go to end

3. Press `o` to add new lines

4. Add connection configuration:
   ```hal
   # Connect start button to cycle start
   net start-cycle-btn => halui.program.run

   # Connect stop button to cycle stop
   net stop-cycle-btn => halui.program.stop

   # Optional: Add debounce
   loadrt debounce cfg=2
   addf debounce.0 servo-thread
   setp debounce.0.delay 50

   net start-cycle-btn-raw hal_pi_gpio.pin-17-in => debounce.0.0.in
   net start-cycle-btn debounce.0.0.out => halui.program.run

   net stop-cycle-btn-raw hal_pi_gpio.pin-27-in => debounce.0.1.in
   net stop-cycle-btn debounce.0.1.out => halui.program.stop
   ```

5. Press `Esc`, type `:wq`, press Enter

## Complete HAL Configuration Example

```hal
# ===================================
# GPIO Button Configuration
# ===================================

# Load GPIO component
loadrt hal_pi_gpio dir=17:in,27:in

# Load debounce component (optional but recommended)
loadrt debounce cfg=2

# Add to servo thread
addf hal_pi_gpio.read servo-thread
addf debounce.0 servo-thread

# Configure debounce (50ms delay)
setp debounce.0.delay 50

# Invert inputs (buttons pull to ground)
setp hal_pi_gpio.pin-17-in-invert true
setp hal_pi_gpio.pin-27-in-invert true

# Start Cycle Button (GPIO 17)
net start-btn-raw hal_pi_gpio.pin-17-in => debounce.0.0.in
net start-btn debounce.0.0.out => halui.program.run

# Stop Cycle Button (GPIO 27)
net stop-btn-raw hal_pi_gpio.pin-27-in => debounce.0.1.in
net stop-btn debounce.0.1.out => halui.program.stop

# Optional: LED indicators
# net start-btn => hal_pi_gpio.pin-22-out  # LED on GPIO 22
# net stop-btn => hal_pi_gpio.pin-23-out   # LED on GPIO 23
```

## Testing

### Step 1: Test GPIO Pins

Before starting LinuxCNC, test GPIO:

```bash
# Install GPIO tools if not present
sudo apt-get install gpiod

# Test GPIO 17 (Start button)
gpioget gpiochip0 17

# Press button and test again
gpioget gpiochip0 17
```

Expected output:
- Button not pressed: `0`
- Button pressed: `1`

### Step 2: Test in HAL

Start LinuxCNC and open HAL meter:

```bash
# In LinuxCNC, open a terminal
halcmd show pin hal_pi_gpio.pin-17-in
halcmd show pin hal_pi_gpio.pin-27-in
```

Press buttons and verify pin states change.

### Step 3: Test Cycle Start/Stop

1. Load a G-code program
2. Press Start Cycle button → Program should start
3. Press Stop Cycle button → Program should stop

## Troubleshooting

### Buttons Don't Work

**Check GPIO permissions**:
```bash
sudo usermod -a -G gpio $USER
sudo reboot
```

**Verify GPIO pins are configured as inputs**:
```bash
gpioinfo gpiochip0 | grep -E "17|27"
```

### Buttons Trigger Randomly

**Add hardware debouncing**:
- Add 0.1µF capacitor across button terminals
- Increase software debounce delay:
  ```hal
  setp debounce.0.delay 100  # Increase to 100ms
  ```

### Wrong Pin States

**Check invert setting**:
```hal
# If button pressed shows 0, invert is wrong
setp hal_pi_gpio.pin-17-in-invert false
```

### LinuxCNC Won't Start

**Check HAL syntax**:
```bash
halrun -I -f ~/linuxcnc/configs/your_config/custom.hal
```

Look for error messages.

## Advanced Features

### Add E-Stop Button

```hal
# E-Stop on GPIO 22
loadrt hal_pi_gpio dir=17:in,27:in,22:in

net estop-btn hal_pi_gpio.pin-22-in => iocontrol.0.emc-enable-in
setp hal_pi_gpio.pin-22-in-invert true
```

### Add Status LEDs

```hal
# Configure GPIO 23 and 24 as outputs for LEDs
loadrt hal_pi_gpio dir=17:in,27:in,23:out,24:out

# Running LED (GPIO 23)
net program-running halui.program.is-running => hal_pi_gpio.pin-23-out

# Paused LED (GPIO 24)  
net program-paused halui.program.is-paused => hal_pi_gpio.pin-24-out
```

### Add Jog Buttons

```hal
# Jog X+ on GPIO 5
# Jog X- on GPIO 6
loadrt hal_pi_gpio dir=5:in,6:in,17:in,27:in

net jog-x-plus hal_pi_gpio.pin-5-in => halui.jog.0.plus
net jog-x-minus hal_pi_gpio.pin-6-in => halui.jog.0.minus
```

## Safety Considerations

⚠️ **Important Safety Notes**:

1. **E-Stop**: Always have a hardware E-stop that cuts power
2. **Debouncing**: Use debounce to prevent false triggers
3. **Testing**: Test thoroughly before using on actual machine
4. **Isolation**: Consider optoisolators for noisy environments
5. **Backup**: Keep backup of working configuration

## Pin Reference

### Raspberry Pi 5 GPIO Pinout

```
     3.3V  (1) (2)  5V
    GPIO2  (3) (4)  5V
    GPIO3  (5) (6)  GND
    GPIO4  (7) (8)  GPIO14
      GND  (9) (10) GPIO15
   GPIO17 (11) (12) GPIO18
   GPIO27 (13) (14) GND
   GPIO22 (15) (16) GPIO23
     3.3V (17) (18) GPIO24
   GPIO10 (19) (20) GND
    GPIO9 (21) (22) GPIO25
   GPIO11 (23) (24) GPIO8
      GND (25) (26) GPIO7
```

### Safe GPIO Pins for Buttons

✅ Safe to use:
- GPIO 17, 27, 22, 23, 24, 25, 5, 6, 12, 13, 16, 19, 20, 21, 26

❌ Avoid (used by system):
- GPIO 0, 1 (I2C)
- GPIO 14, 15 (UART)
- GPIO 2, 3 (I2C with pull-ups)

## Vi Editor Quick Reference

### Essential Vi Commands

#### Opening and Saving Files
```bash
vi filename              # Open file
vi +10 filename         # Open file at line 10
:w                      # Save (write)
:wq                     # Save and quit
:q                      # Quit (fails if unsaved changes)
:q!                     # Quit without saving
:x                      # Save and quit (same as :wq)
```

#### Modes
```
Normal Mode             # Default mode (press Esc to return here)
Insert Mode             # Press 'i' to enter
Command Mode            # Press ':' to enter
```

#### Navigation (Normal Mode)
```
h, j, k, l             # Left, Down, Up, Right
w                      # Next word
b                      # Previous word
0                      # Start of line
$                      # End of line
gg                     # Start of file
G                      # End of file
:10                    # Go to line 10
```

#### Editing (Normal Mode)
```
i                      # Insert before cursor
a                      # Insert after cursor
o                      # Open new line below
O                      # Open new line above
x                      # Delete character
dd                     # Delete line
yy                     # Copy line
p                      # Paste
u                      # Undo
Ctrl+r                 # Redo
```

#### Search and Replace
```
/text                  # Search forward for "text"
?text                  # Search backward for "text"
n                      # Next search result
N                      # Previous search result
:%s/old/new/g          # Replace all "old" with "new"
:s/old/new/g           # Replace in current line
```

### Step-by-Step: Editing HAL File with Vi

**Complete workflow**:

1. **Open file**:
   ```bash
   vi ~/linuxcnc/configs/your_config/custom.hal
   ```

2. **Go to end of file**:
   - Press `Shift+G`

3. **Start adding content**:
   - Press `o` (opens new line and enters INSERT mode)

4. **Type your configuration**:
   ```hal
   # Load GPIO component
   loadrt hal_pi_gpio dir=17:in,27:in
   addf hal_pi_gpio.read servo-thread
   
   # Configure buttons
   setp hal_pi_gpio.pin-17-in-invert true
   setp hal_pi_gpio.pin-27-in-invert true
   
   # Connect to LinuxCNC
   net start-cycle-btn hal_pi_gpio.pin-17-in => halui.program.run
   net stop-cycle-btn hal_pi_gpio.pin-27-in => halui.program.stop
   ```

5. **Save and exit**:
   - Press `Esc`
   - Type `:wq`
   - Press `Enter`

### Common Vi Mistakes and Fixes

**Problem**: Stuck in INSERT mode
- **Fix**: Press `Esc` to return to Normal mode

**Problem**: Can't save (file is read-only)
- **Fix**: Use `:w !sudo tee %` then `:q!`

**Problem**: Accidentally deleted something
- **Fix**: Press `u` to undo (in Normal mode)

**Problem**: Don't know what mode you're in
- **Fix**: Press `Esc` twice (always returns to Normal mode)

**Problem**: Want to quit without saving changes
- **Fix**: Type `:q!` and press Enter

### Alternative: Using Nano Instead

If vi is too complex, use nano instead:

```bash
nano ~/linuxcnc/configs/your_config/custom.hal
```

Nano commands (shown at bottom of screen):
- `Ctrl+O` - Save
- `Ctrl+X` - Exit
- `Ctrl+K` - Cut line
- `Ctrl+U` - Paste
- `Ctrl+W` - Search

## Resources

- [LinuxCNC HAL Documentation](http://linuxcnc.org/docs/html/hal/intro.html)
- [Raspberry Pi GPIO Documentation](https://www.raspberrypi.com/documentation/computers/raspberry-pi.html)
- [LinuxCNC Forum](https://forum.linuxcnc.org/)
- [Vi Cheat Sheet](https://www.fprintf.net/vimCheatSheet.html)

---

**Note**: This guide is for LinuxCNC on Raspberry Pi 5. This is completely separate from the FRC robot simulation project in this repository.

**Last Updated**: 2026-03-12  
**Version**: 1.0