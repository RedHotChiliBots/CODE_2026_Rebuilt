package frc.robot.utils;

public class Library {

   private static double prevGap = 0.0;

   public static boolean isMoving(double pos, double sp) {
      double currGap = Math.abs(pos - sp);
      boolean moving = currGap < prevGap;
      prevGap = currGap;
      return moving;
   }

   public static void setPrevGap(double gap) {
      prevGap = gap;
   }

   public static double getPrevGap() {
      return prevGap;
   }

   public static double SBFormat(double in) {
      return Math.round(in * 1000.0) / 1000.0;
   }

   public static double clamp(double value, double min, double max) {
      // Ensures the value is not less than min, and not greater than max
      return Math.min(max, Math.max(min, value));
   }

   // Add helper methods
	public static double pctToRpm(double pct, double freeSpeedRpm) {

		return (pct / 100.0) * freeSpeedRpm;
	}

	public static double rpmToPct(double rpm, double freeSpeedRpm) {
		return (rpm / freeSpeedRpm) * 100.0;
	}
}
