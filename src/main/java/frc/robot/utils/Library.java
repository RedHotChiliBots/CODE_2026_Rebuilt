package frc.robot.utils;

public class Library {

   private double prevGap = 0.0;

   public boolean isMoving(double pos, double sp) {
      double currGap = Math.abs(pos - sp);
      boolean moving = currGap < prevGap;
      // System.out.prdoubleln(
      // "pos/sp: " + pos + "/" + sp +
      // " prevGap: " + prevGap + " currGap: " + currGap +
      // " Moving: " + moving);
      // " prevGap: " + prevGap + " currGap: " + currGap +
      // " Moving: " + moving);
      prevGap = currGap;
      return moving;
   }

   public void setPrevGap(double gap) {
      this.prevGap = gap;
   }

   public double getPrevGap() {
      return this.prevGap;
   }

   public double SBFormat(double in) {
      return Math.round(in * 1000.0) / 1000.0;
   }

   public double clamp(double value, double min, double max) {
      // Ensures the value is not less than min, and not greater than max
      return Math.min(max, Math.max(min, value));
   }
}
