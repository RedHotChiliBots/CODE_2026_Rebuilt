package frc.robot.subsystems.Vision;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class VisionConstantsTest {

    static class NumVal {
        public boolean isPositive(double number) {
            return number > 0.0;
        }
        public boolean isNegative(double number) {
            return number < 0.0;
        }
    }

    NumVal val = new NumVal();

    @Test
    @DisplayName("Front Left")
    void testFrontLeft() {
        Assertions.assertEquals(VisionConstants.camera3Name, "front-left", 
        "X");
        Assertions.assertTrue(val.isNegative(VisionConstants.robotToCamera3.getTranslation().getX()), 
        "X");
        Assertions.assertTrue(val.isNegative(VisionConstants.robotToCamera3.getTranslation().getY()), 
        "Y");
        Assertions.assertTrue(val.isPositive(VisionConstants.robotToCamera3.getTranslation().getZ()), 
        "Z");
    }

    @Test
    @DisplayName("Front Right")
    void testFrontRight() {
        Assertions.assertEquals(VisionConstants.camera2Name, "front-right", 
        "X");
        Assertions.assertTrue(val.isPositive(VisionConstants.robotToCamera2.getTranslation().getX()), 
        "X");
        Assertions.assertTrue(val.isNegative(VisionConstants.robotToCamera2.getTranslation().getY()), 
        "Y");
        Assertions.assertTrue(val.isPositive(VisionConstants.robotToCamera2.getTranslation().getZ()), 
        "Z");
    }

    @Test
    @DisplayName("Rear Left")
    void testRearLeft() {
        Assertions.assertEquals(VisionConstants.camera1Name, "rear-left", 
        "X");
        Assertions.assertTrue(val.isNegative(VisionConstants.robotToCamera1.getTranslation().getX()), 
        "X");
        Assertions.assertTrue(val.isNegative(VisionConstants.robotToCamera1.getTranslation().getY()), 
        "Y");
        Assertions.assertTrue(val.isPositive(VisionConstants.robotToCamera1.getTranslation().getZ()), 
        "Z");
    }

    @Test
    @DisplayName("Rear Right")
    void testRearRight() {
        Assertions.assertEquals(VisionConstants.camera0Name, "rear-right", 
        "X");
        Assertions.assertTrue(val.isPositive(VisionConstants.robotToCamera0.getTranslation().getX()), 
        "X");
        Assertions.assertTrue(val.isNegative(VisionConstants.robotToCamera0.getTranslation().getY()), 
        "Y");
        Assertions.assertTrue(val.isPositive(VisionConstants.robotToCamera0.getTranslation().getZ()), 
        "Z");
    }
}

