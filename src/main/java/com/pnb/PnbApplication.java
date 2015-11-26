package com.pnb;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.pnb")
@EnableJpaRepositories("com.pnb.repo.jpa")
@SpringBootApplication
public class PnbApplication {

    private static final Logger log = LoggerFactory.getLogger(PnbApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PnbApplication.class);
        System.out.println("_______________");
        System.out.println(args);
        System.out.println("_______________");
        SimpleCommandLinePropertySource source = new SimpleCommandLinePropertySource(args);
        TimeZone.setDefault(TimeZone.getTimeZone("US/Eastern"));

        // Check if the selected profile has been set as argument.
        // if not the development profile will be added
        addDefaultProfile(app, source);

        app.run(args);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * If the following has not been set in the program argument: "--spring.profiles.active=sit"
     * Set a default profile if it has not been set
     */
    private static void addDefaultProfile(SpringApplication app, SimpleCommandLinePropertySource source) {
        if (!source.containsProperty("spring.profiles.active")) {
            app.setAdditionalProfiles(CommonConstants.SPRING_PROFILE_DEVELOPMENT);
        }
    }

}