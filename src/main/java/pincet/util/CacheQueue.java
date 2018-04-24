/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Shingh on 2016-06-14.
 */
@ThreadSafe
public class CacheQueue<K extends Serializable, V extends Serializable> implements Serializable {
  protected static final int DEFAULT_CONCURRENT = Runtime.getRuntime().availableProcessors();
  protected static final long DEFAULT_CACHE_SIZE = 100000L;// max 100,000 objects
  protected static final long DEFAULT_EXPIRE_MILLIS = 100000L;// 100 secs
  private static final long serialVersionUID = 8482518215371428806L;
  protected final Logger log = LoggerFactory.getLogger(CacheQueue.class);
  private final Cache<K, V> cache;
  private final Runnable cleanupTask = new Runnable() {
    @Override
    public void run() {
      cache.cleanUp();
    }
  };
  private transient ScheduledExecutorService cacheCleaner;

  public CacheQueue() {
    this(DEFAULT_CONCURRENT, DEFAULT_CACHE_SIZE, DEFAULT_EXPIRE_MILLIS);
  }

  protected CacheQueue(int concurrentLevel, long cacheSize, long expireMilliseconds) {
    if (log.isTraceEnabled()) {
      log.trace("CacheQueue initialize. concurrentLevel : {}, cacheSize : {}, expireMilliseconds : {}"
          , concurrentLevel, cacheSize, expireMilliseconds);
    }
    this.cache = CacheBuilder.newBuilder()
        .concurrencyLevel(concurrentLevel)
        .maximumSize(cacheSize)
        .expireAfterWrite(expireMilliseconds, TimeUnit.MILLISECONDS)//TTL
        .build();
    cacheCleaner = Executors.newScheduledThreadPool(1);
  }

  public void put(final K key, final V element) {
    cache.put(key, element);
  }

  public void putOverwrite(final K key, final V element) {
    cache.put(key, element);
  }

  public V get(final K key, Callable<V> callable) throws ExecutionException {
    return cache.get(key, callable);
  }

  public V get(final K key) {
    return cache.getIfPresent(key);
  }

  public V remove(final K key) {
    V result = get(key);
    cache.invalidate(key);
    return result;
  }

  public void clear() {
    cache.invalidateAll();
  }

  @SafeVarargs
  public final void removeAll(final K... keys) {
    cache.invalidateAll(Arrays.asList(keys));
  }

  public void removeAll(final Collection<K> keys) {
    cache.invalidateAll(keys);
  }

  public Map<K, V> getAll() {
    return cache.getAllPresent(cache.asMap().keySet());
  }

  public long size() {
    return cache.size();
  }

  public void lookup() {
    cacheCleaner.scheduleWithFixedDelay(cleanupTask, 0, 200, TimeUnit.MILLISECONDS);
  }

  public void expire() {
    cacheCleaner.shutdown();
    cache.invalidateAll();
  }
}
