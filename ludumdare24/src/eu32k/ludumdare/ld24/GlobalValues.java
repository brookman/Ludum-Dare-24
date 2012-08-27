package eu32k.ludumdare.ld24;

import eu32k.libgdx.math.InterpolatedAnimation;
import eu32k.libgdx.math.Interpolation.Type;

public class GlobalValues {

   public static long resetTime;
   public static long timeScore;
   public static long score;
   public static float DNA_MUTATION;
   public static boolean GAME_OVER;

   public static int RAYS;
   public static int RAY_SLEEP_MIN;
   public static int RAY_SLEEP_MAX;

   public static int GACT_SLEEP_MIN;
   public static int GACT_SLEEP_MAX;
   public static int GACT_SHOWN;

   private static int minutes = 6;
   private static InterpolatedAnimation rays = new InterpolatedAnimation(2.0, 15.0, Type.LINEAR, minutes * 60 * 1000);
   private static InterpolatedAnimation rayMin = new InterpolatedAnimation(2000.0, 0.0, Type.LINEAR, minutes * 60 * 1000);
   private static InterpolatedAnimation rayMax = new InterpolatedAnimation(5000.0, 0.0, Type.LINEAR, minutes * 60 * 1000);

   private static InterpolatedAnimation gactMin = new InterpolatedAnimation(2000.0, 100.0, Type.LINEAR, minutes * 60 * 1000);
   private static InterpolatedAnimation gactMax = new InterpolatedAnimation(5000.0, 100.0, Type.LINEAR, minutes * 60 * 1000);
   private static InterpolatedAnimation gactShown = new InterpolatedAnimation(2000.0, 300.0, Type.LINEAR, minutes * 60 * 1000);

   public static void reset() {
      resetTime = System.currentTimeMillis();
      score = 0;
      timeScore = 0;
      DNA_MUTATION = 0.0f;
      GAME_OVER = false;
      rays.reset();
      rayMin.reset();
      rayMax.reset();

      gactMin.reset();
      gactMax.reset();
      gactShown.reset();
   }

   public static void update() {
      if (!GAME_OVER) {
         timeScore = (System.currentTimeMillis() - resetTime) / 100L;
      }

      RAYS = (int) rays.getValue();
      RAY_SLEEP_MIN = (int) rayMin.getValue();
      RAY_SLEEP_MAX = (int) rayMax.getValue();

      GACT_SLEEP_MIN = (int) gactMin.getValue();
      GACT_SLEEP_MAX = (int) gactMin.getValue();
      GACT_SHOWN = (int) gactShown.getValue();

      if (DNA_MUTATION > 1.0f) {
         GAME_OVER = true;
      }

      if (DNA_MUTATION < 0.0f) {
         DNA_MUTATION = 0.0f;
      }

   }

   static {
      reset();
      rays.start();
      rayMin.start();
      rayMax.start();

      gactMin.start();
      gactMax.start();
      gactShown.start();
   }
}
