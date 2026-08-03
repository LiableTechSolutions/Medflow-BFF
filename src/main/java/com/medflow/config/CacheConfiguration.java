package com.medflow.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Caching for read-heavy aggregates (dashboard KPIs).
 *
 * <p>Local development defaults to in-memory caches ({@code medflow.cache.provider=simple})
 * so PostgreSQL is the only infrastructure the application needs. Switching the property
 * to {@code redis} uses a Redis-backed manager instead; the logging error handler keeps
 * that cache a soft dependency, so a Redis outage degrades to computing values per
 * request rather than failing them.
 */
@Configuration
@EnableCaching
class CacheConfiguration implements CachingConfigurer {

  static final String DASHBOARD_CACHE = "dashboard";
  static final String MODULE_CATALOG_CACHE = "module-catalog";

  @Bean
  @ConditionalOnProperty(name = "medflow.cache.provider", havingValue = "redis")
  RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    var defaults = RedisCacheConfiguration.defaultCacheConfig()
        .prefixCacheNameWith("medflow::")
        .entryTtl(Duration.ofSeconds(60));
    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaults)
        .withCacheConfiguration(DASHBOARD_CACHE, defaults.entryTtl(Duration.ofSeconds(30)))
        .build();
  }

  /**
   * Entries expire on the same short horizon as the Redis ones: a dashboard that keeps
   * showing yesterday's counts is worse than one that recomputes a few cheap aggregates.
   */
  @Bean
  @ConditionalOnProperty(name = "medflow.cache.provider", havingValue = "simple",
      matchIfMissing = true)
  CacheManager inMemoryCacheManager() {
    var manager = new CaffeineCacheManager(DASHBOARD_CACHE, MODULE_CATALOG_CACHE);
    manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(30)));
    return manager;
  }

  @Override
  public CacheErrorHandler errorHandler() {
    return new LoggingCacheErrorHandler();
  }
}
