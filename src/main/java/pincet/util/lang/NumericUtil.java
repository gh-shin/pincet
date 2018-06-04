/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.util.lang;

import pincet.util.UT;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Created by Shingh on 2016-09-27.
 */
public class NumericUtil {
  private static final NumericUtil o = new NumericUtil();

  private NumericUtil() {
  }

  public static NumericUtil get() {
    return o;
  }

  @Deprecated
  public static Integer parseIntSafely(final String str) {
    return Integer.parseInt(UT.string.checkNull(str) ? "0" : str);
  }

  @Deprecated
  public static long parseLongSafely(final String str) {
    return Long.parseLong(UT.string.checkNull(str) ? "0" : str);
  }

  @Deprecated
  public static String toStringWithPoint(final Number number, final int countAfterPoint) {
    StringBuilder pattern = new StringBuilder("#.");
    for (int i = 0; i < countAfterPoint; i++) {
      pattern.append("0");
    }
    DecimalFormat decimalFormat = new DecimalFormat(pattern.toString());
    decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
    return decimalFormat.format(number);
  }

  @Deprecated
  public static Number solveNumber(String numberStr) {
    if (numberStr.contains(".")) {
      return Double.valueOf(numberStr);
    } else {
      Long l = Long.valueOf(numberStr);
      if (l <= Byte.MAX_VALUE) {
        return l.byteValue();
      } else if (l <= Short.MAX_VALUE) {
        return l.shortValue();
      } else if (l <= Integer.MAX_VALUE) {
        return l.intValue();
      } else {
        return l;
      }
    }
  }

  @Deprecated
  public static Number minValue(final Number value) {
    Class<? extends Number> clz = value.getClass();
    if (Integer.class.isAssignableFrom(clz)) {
      return 0;
    } else if (Long.class.isAssignableFrom(clz)) {
      return 0L;
    } else if (Float.class.isAssignableFrom(clz)) {
      return 0F;
    } else if (Double.class.isAssignableFrom(clz)) {
      return 0D;
    } else {
      return null;
    }
  }

  @Deprecated
  public static Number maxValue(final Number value) {
    Class<? extends Number> clz = value.getClass();
    if (Integer.class.isAssignableFrom(clz)) {
      return Integer.MAX_VALUE;
    } else if (Long.class.isAssignableFrom(clz)) {
      return Long.MAX_VALUE;
    } else if (Float.class.isAssignableFrom(clz)) {
      return Float.MAX_VALUE;
    } else if (Double.class.isAssignableFrom(clz)) {
      return Double.MAX_VALUE;
    } else {
      return null;
    }
  }

  public Integer parseInt(String str) {
    return parseIntSafely(str);
  }

  public Integer parseInt(Object object) {
    return parseIntSafely(Objects.toString(object, null));
  }

  public Long parseLong(String str) {
    return parseLongSafely(str);
  }

  public Long parseLong(Object object) {
    return parseLongSafely(Objects.toString(object, null));
  }

  public String toString(Number number, int count) {
    return toStringWithPoint(number, count);
  }

  public Number solve(String numberStr) {
    return solveNumber(numberStr);
  }

  public Number min(Number value) {
    return minValue(value);
  }

  public Number max(Number value) {
    return maxValue(value);
  }
}
