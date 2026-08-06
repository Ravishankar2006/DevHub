package com.devhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class DevHubApplication {
    public static void main(String[] args) {
        // Forces JDBC timestamp handling (H2 in particular) to be UTC-consistent
        // regardless of the host OS timezone, instead of relying on the JVM's
        // default timezone during read/write conversion.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(DevHubApplication.class, args);
    }
}
