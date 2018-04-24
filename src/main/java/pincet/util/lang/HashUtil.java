package pincet.util.lang;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

public class HashUtil {
  private static final HashUtil o = new HashUtil();

  private HashUtil() {
  }

  public static HashUtil get() {
    return o;
  }

  public byte[] md5WithRandom(String str) {
    return withAlgorithm(str, "MD5", true);
  }

  public byte[] sha256WithRandom(String str) {
    return withAlgorithm(str, "SHA-256", true);
  }

  public byte[] md5(String str) {
    return withAlgorithm(str, "MD5", false);
  }

  public byte[] sha256(String str) {
    return withAlgorithm(str, "SHA-256", false);
  }

  public byte[] withAlgorithm(String str, String typeName, boolean useRandom) {
    try {
      if (useRandom) {
        str = str.concat(new Date().toString() + UUID.randomUUID().toString());
      }
      MessageDigest digest = MessageDigest.getInstance(typeName);
      return digest.digest(str.getBytes(Charset.forName("UTF-8")));
    } catch (NoSuchAlgorithmException ignore) {
      return str.getBytes(Charset.forName("UTF-8"));
    }
  }
}
