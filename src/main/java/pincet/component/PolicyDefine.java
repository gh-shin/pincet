/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.component;

import lombok.ToString;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Component 객체의 lifecycle 정책에 대한 정의를 담당
 *
 * @author Shingh on 2016-06-16.
 */
@ToString
public class PolicyDefine implements Externalizable {

  private static final long serialVersionUID = -3455112963421975435L;
  private String className;
  private transient Class<?> clz;
  private String objectName;

  PolicyDefine(final String objectName, final Class<?> clz) {
    if (objectName.trim().length() < 1) {
      String clzName = clz.getSimpleName();
      String pre = clzName.substring(0, 1).toLowerCase();
      this.objectName = pre + clzName.substring(0, 1);
    } else {
      this.objectName = objectName;
    }
    this.clz = clz;
    this.className = clz.getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PolicyDefine that = (PolicyDefine) o;

    if (objectName != null ? !objectName.equals(that.objectName) : that.objectName != null) return false;
    return (clz != null ? clz.equals(that.clz) : that.clz == null) && !(className != null ? !className.equals(that.className) : that.className != null);

  }

  public Class<?> getClz() {
    return clz;
  }

  public String getObjectName() {
    return objectName;
  }

  @Override
  public int hashCode() {
    int result = objectName != null ? objectName.hashCode() : 0;
    result = 31 * result + (clz != null ? clz.hashCode() : 0);
    result = 31 * result + (className != null ? className.hashCode() : 0);
    return result;
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.objectName = (String) in.readObject();
    this.className = (String) in.readObject();
    if (this.className != null)
      this.clz = Class.forName(this.className);
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(objectName);
    out.writeObject(className);
  }
}
