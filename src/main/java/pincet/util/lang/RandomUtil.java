package pincet.util.lang;

import java.util.Random;
import java.util.stream.IntStream;

public class RandomUtil {
  private static final RandomUtil o = new RandomUtil();

  private RandomUtil() {
  }

  public static RandomUtil get() {
    return o;
  }

  public String str(int length) {
    StringBuffer buf = new StringBuffer();
    Random random = new Random();

    IntStream.range(0, length).forEach(
        i -> buf.append((char) random.nextInt(Character.MAX_VALUE))
    );
    return buf.toString();
  }

  public int num(int max) {
    Random random = new Random();
    return random.nextInt(max);
  }

  public int num() {
    Random random = new Random();
    return random.nextInt();
  }
}
