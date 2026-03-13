package frc.robot.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LibraryTest {

    @Test
    @DisplayName("Test if encoder is moveing towards setpoint")
    void testIsMoving() {
    
        Library.isMoving(5.0, 3.0);

        Assertions.assertEquals(true, Library.isMoving(4.0, 3.0), "Moving towards");
    }

    @Test
    @DisplayName("Test if encoder is not moving towards setpoint")
    void testIsNotMoving() {
         Library.isMoving(5.0, 3.0);

        Assertions.assertEquals(false, Library.isMoving(5.0, 3.0), "Not moving");
    }

    @Test
    @DisplayName("Test if encoder is moving away from setpoint")
    void testIsMovingAway() {
         Library.isMoving(5.0, 3.0);

        Assertions.assertEquals(false, Library.isMoving(6.0, 3.0), "Moving away");
     }
}
