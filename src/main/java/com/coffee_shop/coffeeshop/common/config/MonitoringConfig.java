package com.coffee_shop.coffeeshop.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.metrics.prometheus.PrometheusMetricsTrackerFactory;

import javax.sql.DataSource;

@Profile({"local", "prod"})
@Configuration
public class MonitoringConfig {
	@Value("${spring.datasource.username}")
	private String username;

	@Value("${spring.datasource.password}")
	private String password;

	@Value("${spring.datasource.url}")
	private String url;

	@Bean
	public DataSource dataSource() {
		HikariConfig config = new HikariConfig();
		config.setUsername(username);
		config.setPassword(password);
		config.setJdbcUrl(url);
		config.setMetricsTrackerFactory(metricsTrackerFactory());
		return new HikariDataSource(config);
	}

	@Bean
	public MetricsTrackerFactory metricsTrackerFactory() {
		return new PrometheusMetricsTrackerFactory();
	}
}