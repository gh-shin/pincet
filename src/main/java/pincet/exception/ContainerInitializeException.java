package pincet.exception;

/**
 * Created by shingh on 2017-07-07.
 */
public class ContainerInitializeException extends Exception {

  private static final String msg = "Container can not initialize. ";

  public ContainerInitializeException(String message) {
    super(msg + message);
  }

  public ContainerInitializeException() {
    super(msg);
  }

  public ContainerInitializeException(Throwable e) {
    super(msg, e);
  }

  public ContainerInitializeException(String message, Throwable e) {
    super(msg + message, e);
  }
}
