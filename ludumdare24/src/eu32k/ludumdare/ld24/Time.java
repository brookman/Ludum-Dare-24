package eu32k.ludumdare.ld24;

public class Time {

   public static long time = System.currentTimeMillis();

   public static float getTime() {
      long diff = System.currentTimeMillis() - time;
      double d = diff / 1000.0;
      return (float) d;
   }

}
