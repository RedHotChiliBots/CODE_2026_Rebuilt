package frc.robot.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LibraryTest {

    Library lib;

    @BeforeEach
    void setUp() {
        // Initialize a new Library instance before each test method runs
        lib = new Library();
    }

    @Test
    @DisplayName("Test if encoder is moveing towards setpoint")
    void testIsMoving() {
    
        lib.isMoving(5.0, 3.0);

        Assertions.assertEquals(true, lib.isMoving(4.0, 3.0), "Moving towards");
    }

    @Test
    @DisplayName("Test if encoder is not moving towards setpoint")
    void testIsNotMoving() {
         lib.isMoving(5.0, 3.0);

        Assertions.assertEquals(false, lib.isMoving(5.0, 3.0), "Not moving");
    }

    @Test
    @DisplayName("Test if encoder is moving away from setpoint")
    void testIsMovingAway() {
         lib.isMoving(5.0, 3.0);

        Assertions.assertEquals(false, lib.isMoving(6.0, 3.0), "Moving away");
     }
}
