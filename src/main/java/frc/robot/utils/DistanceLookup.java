package frc.robot.utils;
/**
 * DistanceLookup - A utility class for looking up RPM and Angle values based on distance.
 * Uses a lookup table with linear interpolation for values between table entries.
 */
public class DistanceLookup {
    
    /**
     * Inner class to hold the result of a lookup operation
     */
    public static class LookupResult {
        private final double rpm;
        private final double angle;
        
        public LookupResult(double rpm, double angle) {
            this.rpm = rpm;
            this.angle = angle;
        }
        
        public double getRpm() {
            return rpm;
        }
        
        public double getAngle() {
            return angle;
        }
        
        @Override
        public String toString() {
            return String.format("LookupResult{rpm=%.2f, angle=%.2f}", rpm, angle);
        }
    }
    
    /**
     * Inner class to represent a single entry in the lookup table
     */
    private static class LookupEntry {
        final double distance;
        final double rpm;
        final double angle;
        
        LookupEntry(double distance, double rpm, double angle) {
            this.distance = distance;
            this.rpm = rpm;
            this.angle = angle;
        }
    }
    
    // Lookup table - sorted by distance (ascending order)
    // Modify this table with your actual data
    private static final LookupEntry[] LOOKUP_TABLE = {
        // new LookupEntry(0.0, 0.0, 0.0),
        // new LookupEntry(10.0, 1000.0, 15.0),
        // new LookupEntry(20.0, 1500.0, 20.0),
        // new LookupEntry(30.0, 2000.0, 25.0),
        // new LookupEntry(40.0, 2300.0, 28.0),
        // new LookupEntry(50.0, 2500.0, 30.0),
        // new LookupEntry(75.0, 2800.0, 33.0),
        // new LookupEntry(100.0, 3000.0, 35.0),
        // new LookupEntry(150.0, 3200.0, 38.0),
        // new LookupEntry(200.0, 3400.0, 40.0)
        new LookupEntry(4.5, 3000, -10),
        new LookupEntry(6.5, 3000, -10),
        new LookupEntry(8.5, 3300, -6),
        new LookupEntry(10.5, 3500, -2),
        new LookupEntry(12.5, 3800, 3),
        new LookupEntry(14.5, 4700, 4)
    };
    
    /**
     * Looks up RPM and Angle values for a given distance.
     * Uses linear interpolation for distances between table entries.
     * 
     * @param distance The distance to look up
     * @return LookupResult containing the RPM and Angle values
     * @throws IllegalArgumentException if distance is negative or beyond table range
     */
    public LookupResult lookup(double distance) {
        if (distance < 0) {
            throw new IllegalArgumentException("Distance cannot be negative: " + distance);
        }
        
        // Check if distance is before first entry
        if (distance <= LOOKUP_TABLE[0].distance) {
            return new LookupResult(LOOKUP_TABLE[0].rpm, LOOKUP_TABLE[0].angle);
        }
        
        // Check if distance is beyond last entry
        if (distance >= LOOKUP_TABLE[LOOKUP_TABLE.length - 1].distance) {
            LookupEntry last = LOOKUP_TABLE[LOOKUP_TABLE.length - 1];
            return new LookupResult(last.rpm, last.angle);
        }
        
        // Find the two entries to interpolate between
        for (int i = 0; i < LOOKUP_TABLE.length - 1; i++) {
            LookupEntry lower = LOOKUP_TABLE[i];
            LookupEntry upper = LOOKUP_TABLE[i + 1];
            
            if (distance >= lower.distance && distance <= upper.distance) {
                // Perform linear interpolation
                double ratio = (distance - lower.distance) / (upper.distance - lower.distance);
                double interpolatedRpm = lower.rpm + ratio * (upper.rpm - lower.rpm);
                double interpolatedAngle = lower.angle + ratio * (upper.angle - lower.angle);
                
                return new LookupResult(interpolatedRpm, interpolatedAngle);
            }
        }
        
        // Should never reach here if table is properly sorted
        throw new IllegalStateException("Lookup table error for distance: " + distance);
    }
    
    /**
     * Example usage and testing
     */
    // public static void main(String[] args) {
    //     System.out.println("Distance Lookup Table Demo");
    //     System.out.println("==========================\n");
        
    //     // Test exact table values
    //     double[] testDistances = {0.0, 10.0, 50.0, 100.0, 200.0};
    //     System.out.println("Exact table values:");
    //     for (double dist : testDistances) {
    //         LookupResult result = lookup(dist);
    //         System.out.printf("Distance: %.1f -> RPM: %.2f, Angle: %.2f°%n", 
    //                         dist, result.getRpm(), result.getAngle());
    //     }
        
    //     System.out.println("\nInterpolated values:");
    //     // Test interpolated values
    //     double[] interpolatedDistances = {5.0, 15.0, 35.0, 60.0, 125.0};
    //     for (double dist : interpolatedDistances) {
    //         LookupResult result = lookup(dist);
    //         System.out.printf("Distance: %.1f -> RPM: %.2f, Angle: %.2f°%n", 
    //                         dist, result.getRpm(), result.getAngle());
    //     }
        
    //     System.out.println("\nEdge cases:");
    //     // Test edge cases
    //     try {
    //         LookupResult result1 = lookup(-5.0);
    //         System.out.println("Negative distance: " + result1);
    //     } catch (IllegalArgumentException e) {
    //         System.out.println("Negative distance: " + e.getMessage());
    //     }
        
    //     LookupResult result2 = lookup(250.0);
    //     System.out.printf("Beyond table (250.0): RPM: %.2f, Angle: %.2f°%n", 
    //                     result2.getRpm(), result2.getAngle());
    // }
}
