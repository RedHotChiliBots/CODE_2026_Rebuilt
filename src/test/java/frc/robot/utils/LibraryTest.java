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
        // 1. Given (preconditions): Inputs 5 and 3
        int num1 = 5;
        int num2 = 3;
        int expectedResult = 8;

        // 2. When (action): Call the method under test
        int actualResult = 0;

        // 3. Then (assertion): Verify the result
        Assertions.assertEquals(expectedResult, actualResult, "The add method should return the sum of its arguments");
    }

    @Test
    @DisplayName("Test if encoder is not moving towards setpoint")
    void testIsNotMoving() {
        int num1 = 5;
        int num2 = 10;
        int expectedResult = -5;

        int actualResult = 0;

        Assertions.assertEquals(expectedResult, actualResult, "The subtract method should return a negative result when the second number is larger");
    }

    @Test
    @DisplayName("Test if encoder is moving away from setpoint")
    void testIsMovingAway() {
        int num1 = 5;
        int num2 = 10;
        int expectedResult = -5;

        int actualResult = 0;

        Assertions.assertEquals(expectedResult, actualResult, "The subtract method should return a negative result when the second number is larger");
    }
}
