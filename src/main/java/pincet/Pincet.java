package pincet;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pincet.exception.InitializeException;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.util.List;

public class Pincet implements Closeable {
  public static final String PINCET_LOGGER = "pincet-logger";
  private static final Pincet o = new Pincet();
  private ThreadLocal<Thread> holder = new ThreadLocal<>();
  private PincetContext context;

  private Pincet() {
  }

  public static void exit() {
    o.holder.get().interrupt();
    System.exit(0);
  }

  @Override
  public void close() throws IOException {
    exit();
  }

  public static PincetContext context() {
    return o.context;
  }

  private void _applyContext(PincetContext context) {
    this.context = context;
  }

  public static Logger logger() {
    return LoggerFactory.getLogger(PINCET_LOGGER);
  }

  public static void main(String[] notuse) throws InterruptedException {
    logger().info("Pincet is ready!");
    List<PincetArgs> args = Lists.newArrayListWithExpectedSize(PincetArgs.values().length);

    for (PincetArgs pArgs : PincetArgs.values()) {
      if (System.getProperty(pArgs.getKey()) != null) {
        pArgs.setValue(System.getProperty(pArgs.getKey()));
        args.add(pArgs);
      }
    }
    SecurityManager securityManager = new PincetSecurityManager();
    System.setSecurityManager(securityManager);
    o._main(args);
  }

  private void _main(List<PincetArgs> args) {
    try {
      PincetContext context = new PincetContext(args);
      _applyContext(context);

      PincetFeatureActivator activator = new PincetFeatureActivator(args);
      activator.activate(context);
      new PhantomReference<>(activator, null);
    } catch (InitializeException e) {
      logger().error("Pincet context creation failed.", e);
    }
  }

  public static void hold() {

    if (o.holder.get() != null) {
      try {
        o.holder.get().join();
      } catch (InterruptedException e) {
        logger().info("Pincet goes down.");
      }
    } else {
      logger().error("Pincet type is not a daemon mode.");
    }
  }

  public static boolean isGcHandlerRegistered() {
    return o.gcDataHandler != null;
  }

  private VmDataHandler gcDataHandler;

  static VmDataHandler getGcHandler() {
    return o.gcDataHandler;
  }

  public static void registGcDataHandler(VmDataHandler handler) {
    if(isGcHandlerRegistered()){
      logger().warn("GcHandler already registered. new handler will be override.");
    }
    o.gcDataHandler = handler;
  }

  public static void usingWaiter(Thread waiter) {
    o.holder.set(waiter);
    waiter.start();
  }

}
