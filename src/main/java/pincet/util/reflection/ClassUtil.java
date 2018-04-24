/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.util.reflection;

import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by Shingh on 2016-04-28.
 */
public class ClassUtil {
  private static final ClassUtil o = new ClassUtil();

  private ClassUtil() {
  }

  public static ClassUtil get() {
    return o;
  }


  public List<Field> allFields(Class<?> type) {
    final List<Field> result = new ArrayList<>();
    Class<?> i = type;
    while (i != null && i != Object.class) {
      result.addAll(Arrays.asList(i.getDeclaredFields()));
      i = i.getSuperclass();
    }
    return result;
  }

  public Field field(Class<?> type, String fieldName) {
    Field result = null;
    Class<?> i = type;
  Find:
    do {
      List<Field> fields = allFields(type);
      for (Field f : fields) {
        if (f.getName().equals(fieldName)) {
          result = f;
          break Find;
        }
      }
      i = i.getSuperclass();
    } while (i != null && i != Object.class);

    return result;
  }

  public Set<Member> methodUsingMembers(String fullPath, String methodName) throws ClassNotFoundException {
    final Class<?> c = Class.forName(fullPath);
    return methodUsingMembers(c, methodName);
  }

  public Set<Member> methodUsingMembers(Class<?> clz, String methodName) throws ClassNotFoundException {
    Reflections ref = new Reflections(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forClass(ClassUtil.class))
        .setScanners(
            new SubTypesScanner(),
            new TypeAnnotationsScanner(),
            new FieldAnnotationsScanner(),
            new MethodAnnotationsScanner(),
            new MethodParameterScanner(),
            new MethodParameterNamesScanner(),
            new MemberUsageScanner())
    );

    Set<Member> result = null;
    for (final Method m : clz.getDeclaredMethods()) {
      if (m.getName().equals(methodName)) {
        result = ref.getMethodUsage(m);
        break;
      }
    }
    return result;
  }
}
