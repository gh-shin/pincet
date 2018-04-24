/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet;

import lombok.ToString;
import pincet.exception.InitializeException;
import pincet.model.VmArguments;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 */
@ToString
public final class PincetConfig implements Serializable, Cloneable {
  private static final long serialVersionUID = 3629596366788936664L;
  private final String APP_ID;
  private final VmArguments vmArgs;
  private final List<PincetArgs> args;
  private long pid;

  PincetConfig(List<PincetArgs> args) throws InitializeException {
    this.APP_ID = args.stream().filter(arg -> arg.equals(PincetArgs.ID))
        .findFirst().orElseGet(()->{
          PincetArgs id = PincetArgs.ID;
          id.setValue(UUID.randomUUID().toString());
          return id;
        }).getValue();
    RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    List<String> arguments = runtimeMxBean.getInputArguments();
    vmArgs = VmArguments.create(arguments);
    this.args = args;
  }

  private PincetConfig(PincetConfig config) {
    this.APP_ID = config.APP_ID;
    this.vmArgs = VmArguments.duplicate(config.vmArgs);
    this.args = new ArrayList<>(config.args);
  }

  public String getId() {
    return APP_ID;
  }

  public VmArguments getVmArgs() {
    return vmArgs;
  }

  public List<PincetArgs> getPincetArgs() {
    return args;
  }

  PincetConfig get() {
    return new PincetConfig(this);
  }

  public void pid(long pid) {
    this.pid = pid;
  }

  public long pid() {
    return this.pid;
  }
}
