package pincet.exception;

/**
 * D/I 수행 시 injection 정의가 불일치 할 경우 발생
 *
 * @author shingh on 2016-12-29.
 */
public class UnMatchedInjectionDefine extends RuntimeException {

  private static final String msg = "Object Injection define is not valid. ";

  public UnMatchedInjectionDefine(String message) {
    super(msg + message);
  }

  public UnMatchedInjectionDefine() {
    super(msg);
  }

  public UnMatchedInjectionDefine(Throwable e) {
    super(msg, e);
  }

  public UnMatchedInjectionDefine(String message, Throwable e) {
    super(msg + message, e);
  }
}
