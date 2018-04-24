/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.util.lang;

import com.google.common.base.CaseFormat;

import java.util.regex.Pattern;

/**
 * Created by SteveShin on 2015-09-25.
 */
public class StringUtil {
  private static final String isInt = "^[0-9]*$";
  private static final String isDouble = "^[0-9]*\\.[0-9]*$";
  private static final String specificMatcher = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
  private static final StringUtil o = new StringUtil();

  private StringUtil() {
  }

  public static StringUtil get() {
    return o;
  }


  public String removeSpecific(String str) {
    return str.replaceAll(specificMatcher, "");
  }

  public String removeEmpty(String str) {
    return str.trim().replaceAll(" ", "");
  }

  public String charactersOnly(String str) {
    return removeSpecific(removeEmpty(str));
  }

  public boolean isEqualCharacters(String str1, String str2) {
    return charactersOnly(str1).equalsIgnoreCase(charactersOnly(str2));
  }

  public boolean checkNull(String str) {
    return str == null || str.trim().equals("");
  }

  public Class<?> type(String str) {
    if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
      return Boolean.class;
    } else if (Pattern.compile(isInt + "|" + isDouble).matcher(str).find()) {
      return Number.class;
    } else {
      return String.class;
    }
  }

  public String camelToUpperWithUnderscore(String str) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, str).toUpperCase();
  }

  public String upperWithUnderscoreToCamel(String str) {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, str).toUpperCase();
  }

  public String convertHexIpToString(String hexValue) {
    if (hexValue == null) {
      return null;
    }
    StringBuilder ip = new StringBuilder();
    for (int j = 0; j < hexValue.length(); j += 2) {
      String sub = hexValue.substring(j, j + 2);
      int num = Integer.parseInt(sub, 16);
      ip.append(num);
      if (j < hexValue.length() - 2) {
        ip.append(".");
      }
    }
    return ip.toString();
  }

  public String bytesToString(byte[] bytes) {
    StringBuilder buffer = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) buffer.append('0');
      buffer.append(hex);
    }
    return buffer.toString();
  }
}
