package com.capstone.emergency.pharmacy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.capstone.emergency.pharmacy")
@EnableJpaAuditing
@Configuration
public class PostgresConfig {
}
