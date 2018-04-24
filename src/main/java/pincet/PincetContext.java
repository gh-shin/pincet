package pincet;

import lombok.extern.slf4j.Slf4j;
import pincet.exception.InitializeException;
import pincet.model.VmStat;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

@Slf4j
public class PincetContext {
  private static final Semaphore MUTEX = new Semaphore(1);

  private final PincetConfig config;
  private static WeakReference<VmStat> vmStatCache;
  private static Callable<VmStat> vmStatWorker;

  PincetContext(List<PincetArgs> args) throws InitializeException {
    this.config = new PincetConfig(args);
  }

  void init(String pkg){

  }
  public PincetConfig config() {
    return this.config.get();
  }

  void usingStatWorker(Callable<VmStat> worker) {
    vmStatWorker = worker;
    try {
      vmStatCache = new WeakReference<>(vmStatWorker.call());
    } catch (Exception e) {
      log.error("", e);
    }
  }

  Future<VmStat> stat() {
    if (vmStatWorker == null) {
      throw new NullPointerException("VmStat worker is not initialized.");
    }
    return CompletableFuture.supplyAsync(() -> {
      try {
        if (vmStatCache.get() != null) {
          return vmStatCache.get();
        } else {
          VmStat stat = vmStatWorker.call();
          vmStatCache = new WeakReference<>(stat);
          return stat;
        }
      } catch (Exception e) {
        return VmStat.ERROR(e);
      }
    });
  }

  public void pid(long pid) {
    log.info("Pincet running at {}", pid);
    config.pid(pid);
  }
  public long pid(){
    return config().pid();
  }
}
