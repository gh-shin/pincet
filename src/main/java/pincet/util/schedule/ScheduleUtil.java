package pincet.util.schedule;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by shingh on 2017-05-02.
 */
public class ScheduleUtil {
  private static final ScheduleUtil o = new ScheduleUtil();
  private static final SchedulerThreadFactory FACTORY = new SchedulerThreadFactory();

  private ScheduleUtil() {
  }

  public static ScheduleUtil get() {
    return o;
  }

  public ScheduledFuture<?> single(Runnable func, int initDelay, int delay, TimeUnit unit) {
    ScheduledExecutorService pool = _createPool(1);
    return pool.scheduleAtFixedRate(func, initDelay, delay, unit);
  }

  public ScheduledFuture<?> singleAfterDone(Runnable func, int initDelay, int delay, TimeUnit unit) {
    ScheduledExecutorService pool = _createPool(1);
    return pool.scheduleWithFixedDelay(func, initDelay, delay, unit);
  }

  public ScheduledFuture<?> single(Runnable func, int delay, TimeUnit unit) {
    return single(func, delay, delay, unit);
  }

  public ScheduledFuture<?> singleAfterDone(Runnable func, int delay, TimeUnit unit) {
    return singleAfterDone(func, delay, delay, unit);
  }

  public ScheduledFuture<?> singlePerSec(Runnable func, int delay) {
    return single(func, delay, TimeUnit.SECONDS);
  }

  public ScheduledFuture<?> singlePerSecAfterDone(Runnable func, int delay) {
    return singleAfterDone(func, delay, TimeUnit.SECONDS);
  }

  public ScheduledExecutorService pool(Runnable fuc, int delay, TimeUnit unit, int poolCount) {
    ScheduledExecutorService schedulerPool = _createPool(poolCount);
    schedulerPool.scheduleAtFixedRate(fuc, 0, delay, unit);
    return schedulerPool;
  }

  public ScheduledExecutorService poolAfterDone(Runnable fuc, int delay, TimeUnit unit, int poolCount) {
    ScheduledExecutorService schedulerPool = Executors.newScheduledThreadPool(poolCount, new SchedulerThreadFactory());
    schedulerPool.scheduleWithFixedDelay(fuc, 0, delay, unit);
    return schedulerPool;
  }

  public ScheduledExecutorService poolPerSec(Runnable fuc, int delay, int poolCount) {
    return pool(fuc, delay, TimeUnit.SECONDS, poolCount);
  }

  public ScheduledExecutorService poolPerSecAfterDone(Runnable fuc, int delay, int poolCount) {
    return poolAfterDone(fuc, delay, TimeUnit.SECONDS, poolCount);
  }

  private ScheduledExecutorService _createPool(int poolCount) {
    return Executors.newScheduledThreadPool(poolCount, FACTORY);
  }

  private static final AtomicInteger cnt = new AtomicInteger(0);

  private static class SchedulerThreadFactory implements ThreadFactory {
    private final ThreadGroup group;

    SchedulerThreadFactory() {
      SecurityManager s = System.getSecurityManager();
      group = (s != null) ? s.getThreadGroup() : new ThreadGroup("JWS-SCH-GRP");
    }

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(group, r,
          "JWS-SCH-" + cnt.getAndIncrement(), 0);
      if (t.isDaemon()) t.setDaemon(true);
      if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
      return t;
    }
  }

}
