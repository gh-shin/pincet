package pincet.util;


import pincet.util.db.DbUtil;
import pincet.util.lang.*;
import pincet.util.net.ConnectionPool;
import pincet.util.net.NetUtil;
import pincet.util.reflection.ClassUtil;
import pincet.util.schedule.ScheduleUtil;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility end-point
 */
public class UT {
  public static final DateUtil date = DateUtil.get();
  public static final NumericUtil numeric = NumericUtil.get();
  public static final ObjectUtil object = ObjectUtil.get();
  public static final StringUtil string = StringUtil.get();
  public static final ClassUtil reflection = ClassUtil.get();
  public static final ScheduleUtil schedule = ScheduleUtil.get();
  public static final ConcurrentUtil concurrent = ConcurrentUtil.get();
  public static final DbUtil db = DbUtil.get();
  public static final RandomUtil random = RandomUtil.get();
  public static final NetUtil net = NetUtil.get();
  public static final HashUtil hash = HashUtil.get();
  public static final ClassUtil clz = ClassUtil.get();

  /**
   * print with whitespace
   *
   * @param args - print objects
   */
  public static void print(Object... args) {
    print(null, args);
  }

  /**
   * print with delimiter
   *
   * @param delimiter - join character
   * @param args      - print objects
   */
  public static void print(String delimiter, Object... args) {
    String joinStr = Stream.of(args).map(Object::toString).collect(
        Collectors.joining(delimiter == null ? " " : (!delimiter.endsWith(" ") ? delimiter + " " : delimiter))
    );
    System.out.println(joinStr);
  }

  public ConnectionPool.Builder hikariBuilder() {
    return new ConnectionPool.Builder();
  }
}
