// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.Vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;

public class VisionConstants {
    // AprilTag layout
    public static AprilTagFieldLayout aprilTagLayout = AprilTagFieldLayout.loadField(AprilTagFields.kDefaultField);

    // Camera names, must match names configured on coprocessor
    public static String camera0Name = "rear-right";
    public static String camera1Name = "rear-left";
    public static String camera2Name = "front-right";
    public static String camera3Name = "front-left";

    // Robot to camera transforms
    // (Not used by Limelight, configure in web UI instead)
    // Robot to camera transforms (Where the camera is relative to front left corner
    // of the robot)
    // These will need to be adjusted as the robot is constructed
    public static Transform3d robotToCamera0 = new Transform3d(new Translation3d( // rear right
            Units.inchesToMeters(10.4574), // x
            Units.inchesToMeters(-10.9114), // y
            Units.inchesToMeters(3.6433)), // z
            new Rotation3d(0.0, // roll
                    Units.degreesToRadians(-15.0), // pitch
                    Units.degreesToRadians(30.0))); // yaw

    public static Transform3d robotToCamera1 = new Transform3d(new Translation3d( // rear left
            Units.inchesToMeters(10.4574), // x
            Units.inchesToMeters(-10.9114), // z
            Units.inchesToMeters(3.6433)), // y
            new Rotation3d(0.0, // roll
                    Units.degreesToRadians(-15.0), // pitch
                    Units.degreesToRadians(30.0))); // yaw

    public static Transform3d robotToCamera2 = new Transform3d(new Translation3d( // front right
            Units.inchesToMeters(11.05), // x
            Units.inchesToMeters(-7.3158), // z
            Units.inchesToMeters(25.5050)), // z
            new Rotation3d(0.0, // roll
                    Units.degreesToRadians(-15.0), // pitch
                    Units.degreesToRadians(30.0))); // yaw

    public static Transform3d robotToCamera3 = new Transform3d(new Translation3d( // front left
            Units.inchesToMeters(11.05), // x
            Units.inchesToMeters(25.5050), // y
            Units.inchesToMeters(-7.3158)), // z
            new Rotation3d(0.0, // roll
                    Units.degreesToRadians(-15.0), // pitch
                    Units.degreesToRadians(-30.0))); // yaw

    // Basic filtering thresholds
    public static double maxAmbiguity = 0.3;
    public static double maxZError = 0.75;

    // Standard deviation baselines, for 1 meter distance and 1 tag
    // (Adjusted automatically based on distance and # of tags)
    public static double linearStdDevBaseline = 0.02; // Meters
    public static double angularStdDevBaseline = 0.06; // Radians

    // Standard deviation multipliers for each camera
    // (Adjust to trust some cameras more than others)
    public static double[] cameraStdDevFactors = new double[] {
            1.0, // Camera 0
            1.0, // Camera 1
            1.0, // Camera 2
            1.0 // Camera 3
    };

    // Multipliers to apply for MegaTag 2 observations
    public static double linearStdDevMegatag2Factor = 0.5; // More stable than full 3D solve
    public static double angularStdDevMegatag2Factor = Double.POSITIVE_INFINITY; // No rotation data available
}
