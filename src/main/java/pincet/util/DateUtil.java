package pincet.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtil {

  private static final DateUtil o = new DateUtil();
  private static final String FULL_FORMAT = "yyyyMMddHHmmssSSS";

  private DateUtil() {
  }

  public static DateUtil get() {
    return o;
  }

  public long nano(String time) {
    time = UT.string.charactersOnly(time);
    return Long.parseLong(time.substring("yyyyMMddHHmmssSSS".length() + 1, time.length()));
  }

  public long toUTC(LocalDateTime dateTime) {
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }

  public long toUTC(java.sql.Date date) {
    return toUTC((Date) date);
  }

  public long toUTC(Date date) {
    return date.getTime();
  }

  public String toStringFromLocalDateTime(LocalDateTime localDateTime, String format) {
    return localDateTime.format(DateTimeFormatter.ofPattern(format));
  }

  public LocalDateTime toLocalDateTimeFromString(String date, String format) {
    return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
  }

  public LocalDateTime toLocalDateTimeFromStringWithMillis(String date) {
    String dateOnly = UT.string.charactersOnly(date);
    String format = "yyyyMMddHHmmssSSS";
    if (dateOnly.length() > format.length()) {
      dateOnly = date.substring(0, format.length());
    }
    return LocalDateTime.parse(dateOnly, DateTimeFormatter.ofPattern(format));
  }

  public LocalDateTime toLocalDateTimeFromUTC(long utc) {
    return Instant.ofEpochMilli(utc).atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  public long duration(LocalDateTime before, LocalDateTime after) {
    return ChronoUnit.MILLIS.between(before, after);
  }

  public long truncatedToUTC(LocalDateTime localDateTime, long value, TimeUnit unit) {
    long utc = toUTC(localDateTime);
    return utc - (utc % unit.toMillis(value));
  }

  public LocalDateTime truncatedToLocalDateTime(LocalDateTime localDateTime, long value, TimeUnit unit) {
    return toLocalDateTimeFromUTC(truncatedToUTC(localDateTime, value, unit));
  }

  public String toStringFromDate(Date date, String format) {
    DateFormat df = new SimpleDateFormat(format);
    return df.format(date);
  }

  public Date toDateFromString(String date, String format) throws ParseException {
    DateFormat df = new SimpleDateFormat(format);
    return df.parse(date);
  }

  public Date toDateFromStringWithMillis(String date) throws ParseException {
    date = UT.string.charactersOnly(date);
    String format = "yyyyMMddHHmmssSSS";
    DateFormat df = new SimpleDateFormat();
    if (date.length() > format.length()) {
      date = date.substring(0, format.length());
    }
    return df.parse(date);
  }

  public Date toDateFromUTC(long utc) {
    return new Date(utc);
  }

  public long duration(Date before, Date after) {
    return after.getTime() - before.getTime();
  }

  public long truncatedToUTC(Date date, long value, TimeUnit unit) {
    long utc = toUTC(date);
    return utc - (utc % unit.toMillis(value));
  }

  public Date truncatedToDate(Date date, long value, TimeUnit unit) {
    return toDateFromUTC(truncatedToUTC(date, value, unit));
  }

  public Date convertToDate(Object date) {
    Date result;
    Class<?> dateClass = date.getClass();
    if (checkType(dateClass)) {
      if (Date.class.isAssignableFrom(dateClass)) {
        result = (Date) date;
      } else if (LocalDateTime.class.isAssignableFrom(dateClass)) {
        result = Date.from(((LocalDateTime) date).atZone(ZoneId.systemDefault()).toInstant());
      } else {
        result = new Date();
      }
    } else {
      result = new Date();
    }
    return result;
  }

  public boolean checkType(Class<?> dateClass) {
    return Date.class.isAssignableFrom(dateClass) || LocalDateTime.class.isAssignableFrom(dateClass);
  }
}
