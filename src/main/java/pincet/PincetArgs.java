package pincet;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * APP_NAME("node.app.name") --> 앱 이름 설정
 * APP_TYPE("node.app.type") --> 기동 모드 설정 FIRE/DAEMON
 * SCAN_PKG("node.scan") --> @PincetObject 스캔 패키지명
 * LOG_LEVEL("node.log.level") --> NodeBuilder 전용 로그 레벨 설정 (default : DEBUG)
 * STAT_METHOD("node.log.stat_method") --> @Component의 methodTracing=true로 설정한 컴포넌트의 각 메서드 수행 시간 로그 출력 여부 설정 true/false
 * PID_PATH("node.path.pid") --> 기동 시 pid파일 생성할 위치 지정
 * CONFIG_PATH("node.path.conf") --> NodeBuilder 설정파일의 위치를 절대경로 지정. 확장자까지 필요
 * RVD_PATH("node.path.rv") --> RV기동 시 RV -logfile 위치 지정. 절대경로
 * GC_LOOKUP("node.gc.lookup") --> JVM GC 로그 출력 여부 설정. true/false
 *
 * @author shingh on 2017-01-26.
 */
public enum PincetArgs {
  ID("p.id"),
  TYPE("p.type"),
  SCAN_PKG("p.scan"),
  LOG_LEVEL("p.log.level"),
  STAT_METHOD("p.log.stat"),
  PID_PATH("p.path.pid"),
  CONFIG_PATH("p.path.conf"),
  LOOKUP_GC("p.lookup.gc"),
  LOOKUP_VM("p.lookup.vm");
  @Getter
  private String key;
  @Setter
  @Getter
  private String value;

  PincetArgs(String key) {
    this.key = key;
  }
}
