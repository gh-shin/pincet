package pincet.util.db;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static epo.types.DataColumn.Summary;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DataTable {
  private static final int LOCK_TIMEOUT = 10;
  private static final Summary EMPTY_SUMMARY = new Summary(0, 0, 0, 0);
  private DataColumn<?>[] columns;
  private Summary[] summaries;
  private int size = 0;
  private Map<String, Integer> columnIndex;
  private Map<Integer, String> columnValueIndexLabel = Collections.emptyMap();
  private ReentrantLock lock = new ReentrantLock(true);
  private boolean calcurated = false;

  public DataTable() {
    this(10);
  }

  public DataTable(int columnLength) {
    this.columnIndex = Maps.newHashMapWithExpectedSize(columnLength);
    this.columns = new DataColumn[columnLength];
    this.summaries = new Summary[columnLength];
  }

  public static <T> DataColumn<T> column(String key, T... element) {
    return new DataColumn<>(key, element);
  }

  public static <T> DataColumn<T> column(String key) {
    return new DataColumn<>(key);
  }

  public int indexLabel(String... labels) {
    for (int i = 0; i < labels.length; i++) {
      String label = labels[i];
      this.columnValueIndexLabel.put(i, label);
    }
    return columnValueIndexLabel.size();
  }

  public DataColumn<?> get(String key) {
    if (!columnIndex.containsKey(key))
      return null;
    return columns[columnIndex.get(key)];
  }

  public Set<String> keys() {
    return columnIndex.keySet();
  }

  public int index(String key) {
    return columnIndex.get(key);
  }

  public String valueIndexLabel(int index) {
    String label = columnValueIndexLabel.get(index);
    return label != null ? label : index + "";
  }

  /**
   * 현재 사이즈와 데이터 배열 비교 후 배열의 사이즈를 증가
   *
   * @return
   */
  private boolean _checkLength() {
    try {
      lock.tryLock(LOCK_TIMEOUT, SECONDS);
      if (size >= (columns.length * 0.7)) {
        DataColumn<?>[] newColumns = Arrays.copyOf(columns, (int) Math.ceil(columns.length * 1.1));
        columns = newColumns;
      }
      return true;
    } catch (InterruptedException e) {
      e.printStackTrace();
      return false;
    } finally {
      lock.unlock();
    }
  }

  private int _insert(DataColumn<?> column) {
    int newSize = ++size;
    columns[newSize - 1] = column;
    columnIndex.put(column.key, newSize - 1);
    size = newSize;
    return size;
  }

  /**
   * DataColumn 객체를 인자로 하여 DataTable에 키가 존재할 경우 add, 아닐 경우 새로운 컬럼을 생성한다.
   *
   * @param column
   * @return 해당 컬럼의 크기
   */
  public int append(DataColumn<?> column) {
    if (_checkLength()) {
      String key = column.key;
      if (!columnIndex.containsKey(key)) {
        return _insert(column);
      } else {
        DataColumn<?> c = (DataColumn<?>) get(key).clone();
        c.addAll(column);
        try {
          lock.tryLock();
          columns[index(key)] = c;
        } finally {
          lock.unlock();
        }
        return c.size();
      }
    } else {
      return -1;
    }
  }

  public Summary summary(String key) {
    if (!calcurated)
      summary();
    Summary summary = summaries[columnIndex.get(key)];
    return summary != null ? summary : EMPTY_SUMMARY;
  }

  public int size() {
    return size;
  }

  public DataTable summary() {
    for (DataColumn<?> column : columns) {
      summaries[index(column.key)] = Summary.calc(column);
    }
    calcurated = true;
    return this;
  }

}
