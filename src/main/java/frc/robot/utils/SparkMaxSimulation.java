// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utils;

import com.revrobotics.spark.SparkBase;
import edu.wpi.first.math.system.plant.DCMotor;

/**
 * Utility class for simulating REV SparkMax motor controllers.
 * Provides simple physics-based simulation for velocity and position control.
 * 
 * This is a lightweight simulation that models motor behavior without
 * requiring complex WPILib simulation classes.
 */
public class SparkMaxSimulation {
    private final SparkBase motor;
    private final DCMotor motorType;
    private final double gearRatio;
    private final double moi; // Moment of inertia
    private final boolean isVelocityControl;
    
    // Simulation state
    private double currentVelocityRPM = 0.0;
    private double currentPositionDeg = 0.0;
    private double currentVoltage = 0.0;
    
    // Position control parameters
    private final double minAngleDeg;
    private final double maxAngleDeg;
    private final boolean simulateGravity;
    private final double armLengthMeters;
    
    /**
     * Creates a SparkMax simulation for velocity control (flywheel/roller).
     * 
     * @param motor The SparkMax motor controller to simulate
     * @param motorType The DC motor type (use DCMotor.getNEO(), DCMotor.getNeo550(), or DCMotor.getNeoVortex())
     * @param gearRatio The gear ratio (output/input)
     * @param moi Moment of inertia in kg*m^2 (typical values: 0.001-0.01 for flywheels)
     * @return A new SparkMaxSimulation configured for velocity control
     */
    public static SparkMaxSimulation createVelocitySim(SparkBase motor, DCMotor motorType, double gearRatio, double moi) {
        return new SparkMaxSimulation(motor, motorType, gearRatio, moi, true, 0, 0, 0, false);
    }
    
    /**
     * Creates a SparkMax simulation for position control (arm/tilt mechanism).
     * 
     * @param motor The SparkMax motor controller to simulate
     * @param motorType The DC motor type (use DCMotor.getNEO(), DCMotor.getNeo550(), or DCMotor.getNeoVortex())
     * @param gearRatio The gear ratio (output/input)
     * @param armLengthMeters The length of the arm in meters
     * @param minAngleDeg The minimum angle in degrees
     * @param maxAngleDeg The maximum angle in degrees
     * @param simulateGravity Whether to simulate gravity effects
     * @param startingAngleDeg The starting angle in degrees
     * @return A new SparkMaxSimulation configured for position control
     */
    public static SparkMaxSimulation createPositionSim(SparkBase motor, DCMotor motorType, double gearRatio,
                                                       double armLengthMeters, double minAngleDeg, double maxAngleDeg,
                                                       boolean simulateGravity, double startingAngleDeg) {
        return new SparkMaxSimulation(motor, motorType, gearRatio, armLengthMeters, false,
                                     minAngleDeg, maxAngleDeg, startingAngleDeg, simulateGravity);
    }
    
    /**
     * Private constructor.
     */
    private SparkMaxSimulation(SparkBase motor, DCMotor motorType, double gearRatio, double moi,
                              boolean isVelocityControl, double minAngleDeg, double maxAngleDeg,
                              double startingAngleDeg, boolean simulateGravity) {
        this.motor = motor;
        this.motorType = motorType;
        this.gearRatio = gearRatio;
        this.moi = moi;
        this.isVelocityControl = isVelocityControl;
        this.minAngleDeg = minAngleDeg;
        this.maxAngleDeg = maxAngleDeg;
        this.armLengthMeters = moi; // Reuse moi parameter for arm length in position mode
        this.simulateGravity = simulateGravity;
        this.currentPositionDeg = startingAngleDeg;
    }
    
    /**
     * Updates the simulation with the current setpoint.
     * Call this from your subsystem's simulationPeriodic() method.
     *
     * @param setpoint The current setpoint (RPM for velocity, degrees for position)
     * @param dt The time delta in seconds (typically 0.02 for 20ms)
     */
    public void update(double setpoint, double dt) {
        if (isVelocityControl) {
            updateVelocityControl(setpoint, dt);
            // Update the motor's relative encoder with simulated velocity
            try {
                // Set velocity directly on the encoder (REVLib handles this in sim)
                motor.getEncoder().setPosition(currentVelocityRPM / 60.0); // Convert RPM to rotations
            } catch (Exception e) {
                // Ignore if encoder not available
            }
        } else {
            updatePositionControl(setpoint, dt);
            // Update the motor's encoder with simulated position
            // For position control with absolute encoder, we need to update the relative encoder
            // because that's what the simulation can actually write to
            try {
                // IMPORTANT: If using through-bore encoder on OUTPUT shaft (not motor shaft),
                // the encoder reads output rotations directly, so NO gear ratio conversion needed
                // Convert degrees to output rotations
                double outputRotations = currentPositionDeg / 360.0;
                motor.getEncoder().setPosition(outputRotations);
            } catch (Exception e) {
                // Ignore if encoder not available
            }
        }
    }
    
    /**
     * Updates velocity control simulation.
     */
    private void updateVelocityControl(double targetRPM, double dt) {
        // Simple first-order model: velocity approaches target with time constant
        double error = targetRPM - currentVelocityRPM;
        
        // Calculate required voltage (simplified model)
        double kP = 0.1;
        currentVoltage = Math.max(-12.0, Math.min(12.0, error * kP));
        
        // Motor acceleration based on voltage and load
        // Simplified: accel = (voltage / resistance) * torque_constant / inertia
        double maxAccelRPMPerSec = 10000.0; // Typical for NEO motors
        double accelRPMPerSec = (currentVoltage / 12.0) * maxAccelRPMPerSec;
        
        // Update velocity
        currentVelocityRPM += accelRPMPerSec * dt;
        
        // Apply friction/damping
        currentVelocityRPM *= 0.99;
    }
    
    /**
     * Updates position control simulation.
     */
    private void updatePositionControl(double targetDeg, double dt) {
        // Clamp target to limits
        targetDeg = Math.max(minAngleDeg, Math.min(maxAngleDeg, targetDeg));
        
        // Calculate error
        double errorDeg = targetDeg - currentPositionDeg;
        
        // PD controller
        double kP = 2.0;
        double kD = 0.1;
        double velocityDegPerSec = currentVelocityRPM * 6.0; // Convert RPM to deg/sec (approximate)
        
        currentVoltage = Math.max(-12.0, Math.min(12.0, errorDeg * kP - velocityDegPerSec * kD));
        
        // Gravity compensation (if enabled)
        if (simulateGravity) {
            double gravityTorque = Math.sin(Math.toRadians(currentPositionDeg)) * 0.5;
            currentVoltage += gravityTorque;
            currentVoltage = Math.max(-12.0, Math.min(12.0, currentVoltage));
        }
        
        // Update velocity based on voltage
        double maxVelDegPerSec = 500.0; // Typical max velocity
        double accelDegPerSec2 = (currentVoltage / 12.0) * 2000.0; // Max acceleration
        velocityDegPerSec += accelDegPerSec2 * dt;
        velocityDegPerSec = Math.max(-maxVelDegPerSec, Math.min(maxVelDegPerSec, velocityDegPerSec));
        
        // Update position
        currentPositionDeg += velocityDegPerSec * dt;
        currentPositionDeg = Math.max(minAngleDeg, Math.min(maxAngleDeg, currentPositionDeg));
        
        // Update velocity in RPM for consistency
        currentVelocityRPM = velocityDegPerSec / 6.0;
    }
    
    /**
     * Gets the simulated velocity in RPM.
     * 
     * @return The current velocity in RPM
     */
    public double getVelocityRPM() {
        return currentVelocityRPM;
    }
    
    /**
     * Gets the simulated position in degrees.
     * 
     * @return The current position in degrees
     */
    public double getPositionDegrees() {
        return currentPositionDeg;
    }
    
    /**
     * Sets the simulated position (useful for absolute encoders).
     * 
     * @param degrees The position to set in degrees
     */
    public void setPositionDegrees(double degrees) {
        this.currentPositionDeg = degrees;
    }
    
    /**
     * Gets the current draw of the simulated motor (simplified).
     * 
     * @return The current in amps
     */
    public double getCurrentAmps() {
        // Simplified current model based on voltage and load
        double baseCurrent = Math.abs(currentVoltage) * 0.5; // Base current
        double loadCurrent = Math.abs(currentVelocityRPM) / 1000.0; // Load-dependent current
        return baseCurrent + loadCurrent;
    }
    
    /**
     * Resets the simulation to zero position and velocity.
     */
    public void reset() {
        currentVelocityRPM = 0.0;
        currentPositionDeg = 0.0;
        currentVoltage = 0.0;
    }
    
    /**
     * Resets the simulation to a specific position.
     * 
     * @param positionDeg The position to reset to in degrees
     */
    public void reset(double positionDeg) {
        currentVelocityRPM = 0.0;
        currentPositionDeg = positionDeg;
        currentVoltage = 0.0;
    }
}

// Made with Bob
