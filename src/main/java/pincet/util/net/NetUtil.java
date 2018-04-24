package pincet.util.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;

public class NetUtil {
  private static final NetUtil o = new NetUtil();

  private int PORT_START = 1;
  private int PORT_END = 65534;

  private NetUtil() {
  }

  public static NetUtil get() {
    return o;
  }

  public int freePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
  }

  public int freePort(int start, int end) {
    int[] arr = freePorts(start, end, 0);
    if (arr.length > 0)
      return arr[0];
    return -1;
  }

  public int[] freePorts(int start, int end) {
    return freePorts(start, end, end - start);
  }

  public int[] freePorts(int start, int end, int count) {
    if (start < PORT_START || end > PORT_END)
      return new int[0];

    int[] result = new int[end - start];

    int index = 0;
    for (int i = start; i <= end; i++) {
      try (ServerSocket socket = new ServerSocket(i)) {
        result[index] = socket.getLocalPort();
        if (index++ == count) break;
      } catch (IOException ignored) {
      }
    }
    if (index == 0) return new int[0];
    result = Arrays.copyOfRange(result, 0, index);
    return result;
  }
}
