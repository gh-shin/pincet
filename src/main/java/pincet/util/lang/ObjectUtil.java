/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.util.lang;

import java.io.*;

/**
 * Created by Shingh on 2016-05-24.
 */
public class ObjectUtil<T> {
  private static final ObjectUtil o = new ObjectUtil();

  private ObjectUtil() {
  }

  public static ObjectUtil get() {
    return o;
  }

  public byte[] toBytes(Object obj) {
    byte[] bytes = null;
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(obj);
      oos.flush();
      bytes = bos.toByteArray();
    } catch (IOException ex) {
    }
    return bytes;
  }

  public T toObject(byte[] bytes, Class<T> clz) {
    Object obj = null;
    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bis)) {
      obj = ois.readObject();
    } catch (IOException | ClassNotFoundException ex) {
    }
    return clz.cast(obj);
  }

}
