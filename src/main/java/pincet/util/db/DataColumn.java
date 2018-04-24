package pincet.util.db;

import com.google.common.collect.Lists;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@NotThreadSafe
public class DataColumn<T> extends ArrayList<T> {
  final String key;
  private Class<T> type;

  DataColumn(String key) {
    if (key == null) throw new NullPointerException("Column key is null");
    this.key = key;
  }

  DataColumn(String key, T[] element) {
    this(key);
    addAll(Arrays.asList(element));
  }

  @Override
  public void add(int index, T element) {
    if (type == null) type = (Class<T>) element.getClass();
    super.add(index, element);
  }

  @Override
  public boolean add(T element) {
    if (type == null) type = (Class<T>) element.getClass();
    return super.add(element);
  }

  public boolean addAll(DataColumn<?> c) {
    if (c.type.isAssignableFrom(type)) {
      for (Object v : c) {
        this.add((T) v);
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean add(T... element) {
    return Collections.addAll(this, element);
  }

  public Class<T> getElementType() {
    return type;
  }

  public boolean isNumber() {
    return type.isAssignableFrom(Number.class);
  }

  public static final class Summary {
    public final double min;
    public final double max;
    public final double count;
    public final double avg;

    Summary(double min, double max, double count, double avg) {
      this.min = min;
      this.max = max;
      this.count = count;
      this.avg = avg;
    }

    static Summary calc(DataColumn<?> columns) {
      int count = columns.size();
      double min = 0, max = 0, avg = 0;
      if (columns.isNumber()) {
        List<Double> numValues = Lists.newArrayListWithExpectedSize(columns.size());
        double sum = 0d;
        for (Object c : columns) {
          Double v = (Double) c;
          min = min <= v ? min : v;
          max = max >= v ? max : v;
          sum += v;
        }
        avg = sum / count;
      }
      return new Summary(min, max, count, avg);
    }
  }
}