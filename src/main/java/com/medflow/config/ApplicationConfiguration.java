package com.medflow.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Application-wide infrastructure: async execution for Spring Modulith event listeners
 * and an injectable {@link Clock} so time-dependent logic stays testable.
 */
@Configuration
@EnableAsync
class ApplicationConfiguration {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }
}
