package pincet.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public final class VmGcData implements Serializable {
  private final String cause;
  private final long duration;
  private final long endTime;
  private final String gcType;
  private final long id;
  private final Map<String, GcMemoryData> memData;
  private final String name;
  private final long startTime;

  public VmGcData(long id, String name, String gcType, String cause, long duration, long startTime, long endTime) {
    this.id = id;
    this.name = name;
    this.gcType = gcType;
    this.cause = cause;
    this.duration = duration;
    this.startTime = startTime;
    this.endTime = endTime;
    memData = new HashMap<>();
  }

  @Data
  @AllArgsConstructor
  public static final class GcMemoryData implements Serializable {
    private final double beforepercent;
    private final long memCommitted;
    private final long memInit;
    private final long memMax;
    private final long memUsed;
    private final double percent;
  }
}
