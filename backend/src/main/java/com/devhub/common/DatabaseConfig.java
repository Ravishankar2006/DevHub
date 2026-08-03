package com.devhub.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String defaultDriverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        // If it's an H2 database url, don't parse it as Postgres
        if (databaseUrl != null && databaseUrl.startsWith("jdbc:h2:")) {
            return DataSourceBuilder.create()
                    .url(databaseUrl)
                    .driverClassName("org.h2.Driver")
                    .username("sa")
                    .password("")
                    .build();
        }

        String cleanUrl = databaseUrl;
        String username = null;
        String password = null;

        if (databaseUrl != null && (databaseUrl.startsWith("postgresql://") || databaseUrl.startsWith("jdbc:postgresql://"))) {
            try {
                String uriString = databaseUrl.startsWith("jdbc:") ? databaseUrl.substring(5) : databaseUrl;
                URI uri = new URI(uriString);
                
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }

                String host = uri.getHost();
                int port = uri.getPort();
                String path = uri.getPath();
                String query = uri.getQuery();

                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
                jdbcUrl.append(host);
                if (port != -1) {
                    jdbcUrl.append(":").append(port);
                }
                jdbcUrl.append(path);
                if (query != null) {
                    jdbcUrl.append("?").append(query);
                }

                cleanUrl = jdbcUrl.toString();
            } catch (URISyntaxException e) {
                // Fall back
            }
        }

        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create()
                .url(cleanUrl)
                .driverClassName(defaultDriverClassName);

        if (username != null) {
            dataSourceBuilder.username(username);
        }
        if (password != null) {
            dataSourceBuilder.password(password);
        }

        return dataSourceBuilder.build();
    }
}
