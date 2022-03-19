package com.metao.book.retails.application.config;

import com.metao.book.shared.SharedConfiguration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

// @Configuration
// @EntityScan(basePackages = { "com.metao.book.retail.domain" })
// @EnableJpaRepositories
// @EnableScheduling
// @Import(SharedConfiguration.class)
// @ComponentScan(basePackages = { "com.metao.book.retail.domain" })
public class RepositoryConfig {

}
