package pincet.util;

import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class ConcurrentUtil {
  private static final String PREFIX = "JWS-";
  private static final ConcurrentUtil o = new ConcurrentUtil();

  private ConcurrentUtil() {
  }

  public static ConcurrentUtil get() {
    return o;
  }

  public ForkJoinPool newNamedForkJoinPool(final String name, final int poolSize) {
    return newNamedForkJoinPool(name, poolSize, false);
  }

  public ForkJoinPool newNamedForkJoinPool(final String name, final int poolSize, final boolean isAsync) {
    final ForkJoinPool.ForkJoinWorkerThreadFactory factory = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
      @Override
      public ForkJoinWorkerThread newThread(final ForkJoinPool pool) {
        final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName(_createPoolName(name, worker.getPoolIndex(), poolSize));
        return worker;
      }
    };
    final Thread.UncaughtExceptionHandler exceptionHandler = _exceptionHandler(true);
    return new ForkJoinPool(poolSize, factory, exceptionHandler, isAsync);
  }

  public ThreadFactory newNamedThreadFactory(final String name) {
    return newNamedThreadFactory(name, false, Thread.NORM_PRIORITY);
  }

  public ThreadFactory newNamedThreadFactory(final String name, final boolean isDaemon) {
    return newNamedThreadFactory(name, isDaemon, Thread.NORM_PRIORITY);
  }

  public ThreadFactory newNamedThreadFactory(final String name, final boolean isDaemon, final int priority) {
    return new ThreadFactory() {
      private final ThreadGroup group = System.getSecurityManager() != null ? System.getSecurityManager().getThreadGroup() : new ThreadGroup(PREFIX + name + "GRP");
      private final AtomicInteger number = new AtomicInteger(0);

      @Override
      public Thread newThread(final Runnable r) {
        Objects.requireNonNull(r, "Runnable object is null");
        String clzName;
        if (!r.getClass().getSuperclass().equals(Object.class)) {
          clzName = r.getClass().getSuperclass().getSimpleName().toUpperCase();
        } else {
          clzName = r.getClass().getSimpleName().toUpperCase();
        }
        Thread t = new Thread(group, r,
            PREFIX + name + "-" + number.incrementAndGet() + "-" + clzName, 0);
        t.setUncaughtExceptionHandler(_exceptionHandler(true));
        if (isDaemon) t.setDaemon(true);
        if (priority != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
        return t;
      }
    };
  }

  public Thread newNamedThread(final Runnable runnable, final String name) {
    return newNamedThread(runnable, name, false);
  }

  public Thread newNamedThread(final Runnable runnable, final String name, final boolean isDaemon) {
    Thread t = new Thread(runnable, name);
    t.setUncaughtExceptionHandler(_exceptionHandler(true));
    if (isDaemon)
      t.setDaemon(true);
    return t;
  }

  private String _createPoolName(final String name, final int index, final int max) {
    return PREFIX + name + "-" + index + "/" + max;
  }

  private Thread.UncaughtExceptionHandler _exceptionHandler(boolean inturrept) {
    return new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        LoggerFactory.getLogger(this.getClass()).error("JWS Thread got exception. {}", t.getName(), e);
        try {
          throw new Exception("JWS Thread caught Exception.", e);
        } catch (final Exception e1) {
          LoggerFactory.getLogger(this.getClass()).error("Exception throw failed. process goes down.", e1);
        } finally {
          if (inturrept) {
            t.interrupt();
          }
        }
      }
    };
  }
}
