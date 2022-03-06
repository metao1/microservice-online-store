package com.metao.product.application.config;

import com.metao.SharedConfiguration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = { "com.metao.product.domain" })
@EntityScan
@EnableJpaRepositories
@EnableScheduling
@Import(SharedConfiguration.class)
public class RepositoryConfig {

}
