package pincet.model;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shingh on 2017-02-03.
 */
@Slf4j
@ToString
public final class VmArguments implements Serializable {

  private static transient final String EMPTY = "";
  private static final long serialVersionUID = 3714135252333233640L;

  @Getter
  private Map<String, String> argsMap;

  private VmArguments() {
  }

  private VmArguments(VmArguments vmArguments) {
    this.argsMap = vmArguments.argsMap;
  }

  private VmArguments(int length) {
    argsMap = new HashMap<>(length);
  }

  public static VmArguments duplicate(VmArguments vmArguments) {
    return new VmArguments(vmArguments);
  }

  public static VmArguments create(List<String> args) {
    VmArguments arguments = new VmArguments(args.size());
    log.info("args size={}", args.size());
    for (String arg : args) {
      log.info("{}", arg);
    }

    args.forEach(e -> {
      if (e.startsWith("-D")) e = e.substring(2);
      if (e.startsWith("-")) e = e.substring(1);

      if (e.contains("=")) {
        String[] kv = e.split("=");
        if (kv.length > 1) {
          arguments.argsMap.put(kv[0], kv[1]);
        } else {
          arguments.argsMap.put(kv[0], EMPTY);
        }
      } else {
        arguments.argsMap.put(e, EMPTY);
      }

    });

    return arguments;
  }
}
