package com.capstone.emergency.pharmacy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableJpaRepositories(basePackages = "com.capstone.emergency.pharmacy")
@EnableJpaAuditing
@EnableMongoRepositories(basePackages = "com.capstone.emergency.pharmacy.**.repository.mongo")
@Configuration
public class DatabaseConfig {
}
