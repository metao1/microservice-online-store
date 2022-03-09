package com.metao.product.application.config;

import com.metao.ddd.shared.SharedConfiguration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

// @Configuration
// @EntityScan(basePackages = { "com.metao.product.domain" })
// @EnableJpaRepositories
// @EnableScheduling
// @Import(SharedConfiguration.class)
// @ComponentScan(basePackages = { "com.metao.product.domain" })
public class RepositoryConfig {

}
