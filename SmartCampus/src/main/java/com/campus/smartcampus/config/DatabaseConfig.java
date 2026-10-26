package com.campus.smartcampus.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {
        String password = System.getenv("CAMPUS_DB_PASSWORD");
        if (password == null || password.isBlank()) {
            password = System.getProperty("spring.datasource.password");
        }
        if (password == null || password.isBlank()) {
            password = System.getenv("SPRING_DATASOURCE_PASSWORD");
        }

        log.info("Configuring Oracle HikariCP DataSource (password configured: {})", (password != null && !password.isBlank()));

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:oracle:thin:@//localhost:1521/FREEPDB1");
        config.setUsername("campus_app");
        config.setPassword(password);
        config.setDriverClassName("oracle.jdbc.OracleDriver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(15000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);

        return new HikariDataSource(config);
    }
}
