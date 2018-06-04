/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.exception;

/**
 * Component 오브젝트의 중복생성 시 발생
 *
 * @author Shingh on 2016-07-13.
 */
public class DuplicateObjectException extends RuntimeException {

  private static final String msg = "Object already exist. ";

  public DuplicateObjectException(String message) {
    super(msg + message);
  }

  public DuplicateObjectException() {
    super(msg);
  }

  public DuplicateObjectException(Throwable e) {
    super(msg, e);
  }

  public DuplicateObjectException(String message, Throwable e) {
    super(msg + message, e);
  }

}
