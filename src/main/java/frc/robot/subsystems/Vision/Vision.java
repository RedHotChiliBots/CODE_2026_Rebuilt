// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.Vision;

import static frc.robot.subsystems.Vision.VisionConstants.*;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.constants.ValidationConstants;
import frc.robot.subsystems.Vision.VisionIO.PoseObservationType;
import frc.robot.validation.SubsystemValidation;
import frc.robot.validation.ValidationResult;
import frc.robot.validation.ValidationStatus;
import frc.robot.validation.ValidationSupport;
import frc.robot.validation.ValidationUtils;
import java.util.LinkedList;
import java.util.List;
import org.littletonrobotics.junction.Logger;

public class Vision extends SubsystemBase implements SubsystemValidation {
  private final ValidationSupport validation = new ValidationSupport("Vision");
  private final VisionConsumer consumer;
  private final VisionIO[] io;
  private final VisionIOInputsAutoLogged[] inputs;
  private final Alert[] disconnectedAlerts;
  private int currentConnectedCameraCount = 0;
  private int currentTagCount = 0;
  private int currentObservationCount = 0;
  private int currentAcceptedObservationCount = 0;
  private int currentRejectedObservationCount = 0;

  public Vision(VisionConsumer consumer, VisionIO... io) {
    this.consumer = consumer;
    this.io = io;

    // Initialize inputs
    this.inputs = new VisionIOInputsAutoLogged[io.length];
    for (int i = 0; i < inputs.length; i++) {
      inputs[i] = new VisionIOInputsAutoLogged();
    }

    // Initialize disconnected alerts
    this.disconnectedAlerts = new Alert[io.length];
    for (int i = 0; i < inputs.length; i++) {
      disconnectedAlerts[i] =
          new Alert(
              "Vision camera " + Integer.toString(i) + " is disconnected.", AlertType.kWarning);
    }
  }

  /**
   * Returns the X angle to the best target, which can be used for simple servoing with vision.
   *
   * @param cameraIndex The index of the camera to use.
   */
  public Rotation2d getTargetX(int cameraIndex) {
    return inputs[cameraIndex].latestTargetObservation.tx();
  }

  @Override
  public void periodic() {
    currentConnectedCameraCount = 0;
    currentTagCount = 0;
    currentObservationCount = 0;
    currentAcceptedObservationCount = 0;
    currentRejectedObservationCount = 0;

    for (int i = 0; i < io.length; i++) {
      io[i].updateInputs(inputs[i]);
      Logger.processInputs("Vision/Camera" + Integer.toString(i), inputs[i]);
    }

    // Initialize logging values
    List<Pose3d> allTagPoses = new LinkedList<>();
    List<Pose3d> allRobotPoses = new LinkedList<>();
    List<Pose3d> allRobotPosesAccepted = new LinkedList<>();
    List<Pose3d> allRobotPosesRejected = new LinkedList<>();

    // Loop over cameras
    for (int cameraIndex = 0; cameraIndex < io.length; cameraIndex++) {
      // Update disconnected alert
      disconnectedAlerts[cameraIndex].set(!inputs[cameraIndex].connected);
      if (inputs[cameraIndex].connected) {
        currentConnectedCameraCount++;
      }
      currentTagCount += inputs[cameraIndex].tagIds.length;

      // Initialize logging values
      List<Pose3d> tagPoses = new LinkedList<>();
      List<Pose3d> robotPoses = new LinkedList<>();
      List<Pose3d> robotPosesAccepted = new LinkedList<>();
      List<Pose3d> robotPosesRejected = new LinkedList<>();

      // Add tag poses
      for (int tagId : inputs[cameraIndex].tagIds) {
        var tagPose = aprilTagLayout.getTagPose(tagId);
        if (tagPose.isPresent()) {
          tagPoses.add(tagPose.get());
        }
      }

      // Loop over pose observations
      for (var observation : inputs[cameraIndex].poseObservations) {
        currentObservationCount++;
        // Check whether to reject pose
        boolean rejectPose = shouldRejectObservation(observation);

        // Add pose to log
        robotPoses.add(observation.pose());
        if (rejectPose) {
          currentRejectedObservationCount++;
          robotPosesRejected.add(observation.pose());
        } else {
          currentAcceptedObservationCount++;
          robotPosesAccepted.add(observation.pose());
        }

        // Skip if rejected
        if (rejectPose) {
          continue;
        }

        // Calculate standard deviations
        double stdDevFactor =
            Math.pow(observation.averageTagDistance(), 2.0) / observation.tagCount();
        double linearStdDev = linearStdDevBaseline * stdDevFactor;
        double angularStdDev = angularStdDevBaseline * stdDevFactor;
        if (observation.type() == PoseObservationType.MEGATAG_2) {
          linearStdDev *= linearStdDevMegatag2Factor;
          angularStdDev *= angularStdDevMegatag2Factor;
        }
        if (cameraIndex < cameraStdDevFactors.length) {
          linearStdDev *= cameraStdDevFactors[cameraIndex];
          angularStdDev *= cameraStdDevFactors[cameraIndex];
        }

        // Send vision observation
        consumer.accept(
            observation.pose().toPose2d(),
            observation.timestamp(),
            VecBuilder.fill(linearStdDev, linearStdDev, angularStdDev));
      }

      // Log camera metadata
      Logger.recordOutput(
          "Vision/Camera" + Integer.toString(cameraIndex) + "/TagPoses",
          tagPoses.toArray(new Pose3d[0]));
      Logger.recordOutput(
          "Vision/Camera" + Integer.toString(cameraIndex) + "/RobotPoses",
          robotPoses.toArray(new Pose3d[0]));
      Logger.recordOutput(
          "Vision/Camera" + Integer.toString(cameraIndex) + "/RobotPosesAccepted",
          robotPosesAccepted.toArray(new Pose3d[0]));
      Logger.recordOutput(
          "Vision/Camera" + Integer.toString(cameraIndex) + "/RobotPosesRejected",
          robotPosesRejected.toArray(new Pose3d[0]));
      allTagPoses.addAll(tagPoses);
      allRobotPoses.addAll(robotPoses);
      allRobotPosesAccepted.addAll(robotPosesAccepted);
      allRobotPosesRejected.addAll(robotPosesRejected);
    }

    // Log summary data
    Logger.recordOutput("Vision/Summary/TagPoses", allTagPoses.toArray(new Pose3d[0]));
    Logger.recordOutput("Vision/Summary/RobotPoses", allRobotPoses.toArray(new Pose3d[0]));
    Logger.recordOutput(
        "Vision/Summary/RobotPosesAccepted", allRobotPosesAccepted.toArray(new Pose3d[0]));
    Logger.recordOutput(
        "Vision/Summary/RobotPosesRejected", allRobotPosesRejected.toArray(new Pose3d[0]));
  }

  public static boolean shouldRejectObservation(VisionIO.PoseObservation observation) {
    return observation.tagCount() == 0
        || (observation.tagCount() == 1 && observation.ambiguity() > maxAmbiguity)
        || Math.abs(observation.pose().getZ()) > maxZError
        || observation.pose().getX() < 0.0
        || observation.pose().getX() > aprilTagLayout.getFieldLength()
        || observation.pose().getY() < 0.0
        || observation.pose().getY() > aprilTagLayout.getFieldWidth();
  }

  public int getConnectedCameraCount() {
    return currentConnectedCameraCount;
  }

  public int getCurrentTagCount() {
    return currentTagCount;
  }

  public int getCurrentObservationCount() {
    return currentObservationCount;
  }

  public int getCurrentAcceptedObservationCount() {
    return currentAcceptedObservationCount;
  }

  public int getCurrentRejectedObservationCount() {
    return currentRejectedObservationCount;
  }

  private ValidationResult cameraConnectivityResult() {
    boolean passed = currentConnectedCameraCount == io.length;
    return ValidationResult.of(
        "Vision",
        "Camera Connectivity",
        passed,
        ValidationUtils.measurements(
            "connected", Integer.toString(currentConnectedCameraCount),
            "total", Integer.toString(io.length)),
        "all cameras connected",
        passed ? "" : "One or more cameras are disconnected");
  }

  private ValidationResult observationResult() {
    boolean passed = currentObservationCount == 0 || currentAcceptedObservationCount > 0;
    return ValidationResult.of(
        "Vision",
        "Observation Sanity",
        passed,
        ValidationUtils.measurements(
            "observations", Integer.toString(currentObservationCount),
            "accepted", Integer.toString(currentAcceptedObservationCount),
            "rejected", Integer.toString(currentRejectedObservationCount),
            "tags", Integer.toString(currentTagCount)),
        "if observations exist, at least one should be accepted",
        passed ? "" : "Vision observations were present but none were accepted");
  }

  @Override
  public Command validateCommand() {
    return Commands.sequence(
            Commands.runOnce(validation::start),
            Commands.waitSeconds(ValidationConstants.Common.kVisionSampleDelaySec),
            Commands.runOnce(() -> validation.addResult(cameraConnectivityResult())),
            Commands.runOnce(() -> validation.addResult(observationResult())),
            Commands.runOnce(validation::finish))
        .finallyDo(interrupted -> {
          if (interrupted && validation.status() == ValidationStatus.RUNNING) {
            validation.fail("Validation Interrupted", "Vision validation was interrupted");
            validation.finish();
          }
        });
  }

  @Override
  public List<ValidationResult> validationResults() {
    return validation.results();
  }

  @Override
  public String validationSummary() {
    return validation.summary();
  }

  @Override
  public ValidationStatus validationStatus() {
    return validation.status();
  }

  @FunctionalInterface
  public static interface VisionConsumer {
    public void accept(
        Pose2d visionRobotPoseMeters,
        double timestampSeconds,
        Matrix<N3, N1> visionMeasurementStdDevs);
  }
}
