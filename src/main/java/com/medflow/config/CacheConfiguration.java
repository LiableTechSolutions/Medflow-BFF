package com.medflow.config;

import java.time.Duration;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Redis-backed caching for read-heavy aggregates (dashboard KPIs). The logging error
 * handler makes the cache a soft dependency: if Redis is unavailable the application
 * degrades to computing values on every request instead of failing.
 */
@Configuration
@EnableCaching
class CacheConfiguration implements CachingConfigurer {

  static final String DASHBOARD_CACHE = "dashboard";

  @Bean
  RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    var defaults = RedisCacheConfiguration.defaultCacheConfig()
        .prefixCacheNameWith("medflow::")
        .entryTtl(Duration.ofSeconds(60));
    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaults)
        .withCacheConfiguration(DASHBOARD_CACHE, defaults.entryTtl(Duration.ofSeconds(30)))
        .build();
  }

  @Override
  public CacheErrorHandler errorHandler() {
    return new LoggingCacheErrorHandler();
  }
}
