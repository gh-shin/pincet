package pincet;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pincet.model.VmGcData;
import pincet.model.VmStat;
import pincet.model.VmStatBuilder;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.lang.management.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class PincetFeatureActivator {

  private final List<PincetArgs> args;

  PincetFeatureActivator(List<PincetArgs> args) {
    this.args = new ArrayList<>(args);
  }

  void activate(PincetContext context) {
    for (PincetArgs arg : args) {
      switch (arg) {
        case LOG_LEVEL:
          _activateLogFileAppender((LoggerContext) LoggerFactory.getILoggerFactory(), Level.toLevel(arg.getValue(), Level.INFO));
          break;
        case LOOKUP_GC:
          _activateGCLogger();
          break;
        case LOOKUP_VM:
          Callable<VmStat> worker = _createVmStatWorker();
          context.usingStatWorker(worker);
          break;
        case PID_PATH:
          long pid = _createPidFile(arg.getValue());
          context.pid(pid);
          break;
        case TYPE:
          Thread waiter = _createHolder();
          waiter.setDaemon(true);
          Pincet.usingWaiter(waiter);
          break;
      }
    }
  }

  private Thread _createHolder() {
    return new Thread("WAITER") {
      public void run() {
        while (true) {
          try {
            Thread.sleep(30 * 1000);
          } catch (InterruptedException e) {
            Pincet.logger().info("DAEMON thread interrupted. Node goes down.");
            break;
          }
        }
      }
    };
  }

  private Callable<VmStat> _createVmStatWorker() {
    return () -> {
      VmStatBuilder stat = VmStat.builder();

      CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
      stat.compiler(compilationMXBean.getName());
      MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
      MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
      stat.heapInit(heapUsage.getInit()).heapCommitted(heapUsage.getCommitted()).heapMax(heapUsage.getMax()).heapUsed(heapUsage.getUsed());

      MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
      stat.nonHeapInit(nonHeapUsage.getInit()).nonHeapCommitted(nonHeapUsage.getCommitted()).nonHeapMax(nonHeapUsage.getMax()).nonHeapUsed(nonHeapUsage.getUsed());

      if (System.getProperty("java.vendor") != null && !System.getProperty("java.vendor").toLowerCase().contains("oracle")) {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        stat.osName(operatingSystemMXBean.getName()).version(operatingSystemMXBean.getVersion()).processors(operatingSystemMXBean.getAvailableProcessors())
            .loadAvg(operatingSystemMXBean.getSystemLoadAverage());

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        stat.totalThreads(threadMXBean.getThreadCount()).totalDaemonThreads(threadMXBean.getDaemonThreadCount()).deadLocks(threadMXBean.findDeadlockedThreads());
      } else {
        com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        stat.osName(operatingSystemMXBean.getName()).version(operatingSystemMXBean.getVersion()).processors(operatingSystemMXBean.getAvailableProcessors())
            .loadAvg(operatingSystemMXBean.getSystemLoadAverage())
            .committedVMsize(operatingSystemMXBean.getCommittedVirtualMemorySize()).totalSwapSize(operatingSystemMXBean.getTotalSwapSpaceSize())
            .freeSwapSize(operatingSystemMXBean.getFreeSwapSpaceSize()).freePhysicalMemSize(operatingSystemMXBean.getFreePhysicalMemorySize())
            .totalPhysicalMemSize(operatingSystemMXBean.getTotalPhysicalMemorySize()).sysCpuLoad(operatingSystemMXBean.getSystemCpuLoad())
            .procCpuLoad(operatingSystemMXBean.getProcessCpuLoad());

        com.sun.management.ThreadMXBean threadMXBean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        stat.totalThreads(threadMXBean.getThreadCount()).totalDaemonThreads(threadMXBean.getDaemonThreadCount()).deadLocks(threadMXBean.findDeadlockedThreads());
      }
      return stat.success(true).build();
    };
  }

  private long _createPidFile(String path) {
    String name = ManagementFactory.getRuntimeMXBean().getName();
    String pid;
    try {
      Path pidFile = Files.createFile(Paths.get(path + "/.pid"));
      pid = name.split("@")[0];
      Files.write(pidFile, pid.getBytes(Charset.forName("UTF-8")));
    } catch (IOException e) {
      log.error("PID file creation failed", e);
      pid = "-1";
    }
    return Long.parseLong(pid);
  }

  private void _activateLogFileAppender(LoggerContext loggerContext, Level logLevel) {
    RollingFileAppender rfAppender = new RollingFileAppender();
    rfAppender.setName("nodeFileAppender");
    rfAppender.setContext(loggerContext);
    rfAppender.setFile("logs/pincet.log");
    rfAppender.setAppend(true);
    FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
    rollingPolicy.setContext(loggerContext);
    rollingPolicy.setMaxIndex(9);
    rollingPolicy.setMinIndex(0);
    rollingPolicy.setParent(rfAppender);
    rollingPolicy.setFileNamePattern("logs/pincet.%i.log");
    rollingPolicy.start();

    SizeBasedTriggeringPolicy triggeringPolicy = new ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy();
    triggeringPolicy.setMaxFileSize(FileSize.valueOf("10MB"));
    triggeringPolicy.start();

    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(loggerContext);
    encoder.setCharset(Charset.forName("UTF-8"));
    encoder.setPattern("[%d{yyyy-MM-dd HH:mm:ss.SSS}][%thread] %msg%n");
    encoder.start();

    rfAppender.setEncoder(encoder);
    rfAppender.setRollingPolicy(rollingPolicy);
    rfAppender.setTriggeringPolicy(triggeringPolicy);
    rfAppender.start();

    ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    Optional<ConsoleAppender> consoleAppender = Optional.empty();
    for (Iterator<Appender<ILoggingEvent>> it = rootLogger.iteratorForAppenders(); it.hasNext(); ) {
      Appender appender = it.next();
      if (ConsoleAppender.class.isAssignableFrom(appender.getClass())) {
        consoleAppender = Optional.of((ConsoleAppender) appender);
        break;
      }
    }
    ch.qos.logback.classic.Logger nodeLogger = loggerContext.getLogger(Pincet.PINCET_LOGGER);
    consoleAppender.ifPresent(nodeLogger::addAppender);
    nodeLogger.addAppender(rfAppender);
    nodeLogger.setAdditive(false);
    nodeLogger.setLevel(Level.TRACE);

    ch.qos.logback.classic.Logger frameworkLogger = loggerContext.getLogger("com.jws.framework");
    frameworkLogger.addAppender(rfAppender);
    frameworkLogger.setLevel(logLevel);
  }

  private static void _activateGCLogger() {
    List<GarbageCollectorMXBean> gcbeans = ManagementFactory.getGarbageCollectorMXBeans();
    for (GarbageCollectorMXBean gcbean : gcbeans) {
      NotificationEmitter emitter = (NotificationEmitter) gcbean;
      NotificationListener listener = new NotificationListener() {
        Logger logger = Pincet.logger();
        long totalGcDuration = 0;

        @Override
        public void handleNotification(Notification notification, Object handback) {
          if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
            GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
            GcInfo gcInfo = info.getGcInfo();
            long duration = gcInfo.getDuration();
            String gctype = info.getGcAction();
            logger.warn("GC LOGGING ------------------------------");
            logger.warn("  INFO: ID={}, NAME={}, TYPE={} occurred from {} during. {} to {} milliseconds", gcInfo.getId(), info.getGcName(), gctype, info.getGcCause(), duration, gcInfo.getStartTime(), gcInfo.getEndTime());
            logger.warn("  CompositeType: {}", gcInfo.getCompositeType());
            logger.warn("  MemoryUsageAfterGc: {}", gcInfo.getMemoryUsageAfterGc());
            logger.warn("  MemoryUsageBeforeGc: {}", gcInfo.getMemoryUsageBeforeGc());

            Map<String, MemoryUsage> membefore = gcInfo.getMemoryUsageBeforeGc();
            Map<String, MemoryUsage> mem = gcInfo.getMemoryUsageAfterGc();
            logger.warn("  Memory Usage:");
            final VmGcData gcData = new VmGcData(gcInfo.getId(), info.getGcName(), gctype, info.getGcCause(), duration, gcInfo.getStartTime(), gcInfo.getEndTime());

            for (Map.Entry<String, MemoryUsage> entry : mem.entrySet()) {
              String name = entry.getKey();
              MemoryUsage memdetail = entry.getValue();
              long memInit = memdetail.getInit();
              long memCommitted = memdetail.getCommitted();
              long memMax = memdetail.getMax();
              long memUsed = memdetail.getUsed();
              MemoryUsage before = membefore.get(name);
              long beforepercent = ((before.getUsed() * 1000L) / before.getCommitted());
              long percent = ((memUsed * 1000L) / before.getCommitted()); //>100% when it gets expanded
              logger.warn("    {}({}): ", name, memCommitted == memMax ? "reached full Memory" : "still expandable");
              logger.warn("        used: {}.{}% -> {}.{}%. ({}/{} MB)", beforepercent / 10, beforepercent % 10, percent / 10, percent % 10, (memUsed / 1048576) + 1, (memInit / 1048576) + 1);
              if (Pincet.isGcHandlerRegistered()) {
                VmGcData.GcMemoryData memoryData = new VmGcData.GcMemoryData(memInit, memCommitted, memMax, memUsed, ((before.getUsed() * 1000L) / before.getCommitted()), ((memUsed * 1000L) / before.getCommitted()));
                gcData.getMemData().put(name, memoryData);
              }
            }
            totalGcDuration += gcInfo.getDuration();
            long percent = totalGcDuration * 1000L / gcInfo.getEndTime();
            logger.warn("  overhead: " + (percent / 10) + "." + (percent % 10) + "%");
            logger.warn("------------------------------ GC LOGGING");
            if (Pincet.isGcHandlerRegistered()) {
              logger.info("GC Handling worker execute.");
              CompletableFuture.supplyAsync(() -> Pincet.getGcHandler().handle(gcData));
            }
          }
        }
      };

      //Add the listener
      emitter.addNotificationListener(listener, null, null);
    }
  }
}
