package frc.robot.subsystems.Vision;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import frc.robot.subsystems.Vision.VisionIO.PoseObservation;
import frc.robot.subsystems.Vision.VisionIO.PoseObservationType;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class VisionTest {

    static class FakeVisionIO implements VisionIO {
        private final PoseObservation[] observations;

        FakeVisionIO(PoseObservation... observations) {
            this.observations = observations;
        }

        @Override
        public void updateInputs(VisionIOInputs inputs) {
            inputs.connected = true;
            inputs.poseObservations = observations;
            inputs.tagIds = new int[] { 1 };
        }
    }

    @Test
    @DisplayName("Rejects Zero Tag Observation")
    void rejectsZeroTagObservation() {
        AtomicInteger callCount = new AtomicInteger();

        Vision vision = new Vision(
                (Pose2d pose, double timestamp, Matrix<N3, N1> stdDevs) -> callCount.incrementAndGet(),
                new FakeVisionIO(
                        new PoseObservation(
                                1.0,
                                new Pose3d(),
                                0.0,
                                0,
                                1.0,
                                PoseObservationType.PHOTONVISION)));

        vision.periodic();

        assertEquals(0, callCount.get());
    }

    @Test
    @DisplayName("Rejects High Ambiguity Single Tag")
    void rejectsHighAmbiguitySingleTag() {
        AtomicInteger callCount = new AtomicInteger();

        Vision vision = new Vision(
                (Pose2d pose, double timestamp, Matrix<N3, N1> stdDevs) -> callCount.incrementAndGet(),
                new FakeVisionIO(
                        new PoseObservation(
                                1.0,
                                new Pose3d(),
                                VisionConstants.maxAmbiguity + 0.01,
                                1,
                                1.0,
                                PoseObservationType.PHOTONVISION)));

        vision.periodic();

        assertEquals(0, callCount.get());
    }

    @Test
    @DisplayName("Accepts Valid Observation")
    void acceptsValidObservation() {
        AtomicInteger callCount = new AtomicInteger();
        AtomicReference<Pose2d> acceptedPose = new AtomicReference<>();

        Pose3d pose = new Pose3d(1.0, 1.0, 0.0, new edu.wpi.first.math.geometry.Rotation3d());
        Vision vision = new Vision(
                (Pose2d accepted, double timestamp, Matrix<N3, N1> stdDevs) -> {
                    callCount.incrementAndGet();
                    acceptedPose.set(accepted);
                },
                new FakeVisionIO(
                        new PoseObservation(
                                1.0,
                                pose,
                                0.0,
                                2,
                                1.0,
                                PoseObservationType.PHOTONVISION)));

        vision.periodic();

        assertEquals(1, callCount.get());
        assertEquals(pose.toPose2d(), acceptedPose.get());
    }
}
