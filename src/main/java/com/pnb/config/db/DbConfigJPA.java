package com.pnb.config.db;

import java.net.URI;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


@Configuration
@EntityScan(basePackages = "com.pnb.domain.jpa")
@DependsOn("propertyPlaceholderConfigurer")
public class DbConfigJPA {

    @Autowired
    private Environment environment;

    @Value("${spring.data.postgres.uri}")
    private String databaseUrl;
    @Value("${spring.data.postgres.dataSourceClassName}")
    private String dataSourceClassName;
    @Value("${spring.data.postgres.user}")
    private String user;
    @Value("${spring.data.postgres.pass}")
    private String pass;

    @Bean
    public DataSource dataSource() throws Exception {
        return new HikariDataSource(parseDatabaseUrl(databaseUrl));
    }

    private HikariConfig parseDatabaseUrl(String url) throws Exception {
        URI dbUri = new URI(url);
        String username = "";
        String password = "";
        if (dbUri.getUserInfo() != null) {
            username = dbUri.getUserInfo().split(":")[0];
            password = dbUri.getUserInfo().split(":")[1];
        }
        String dbUrl;

        if (url.contains("postgres")) {
            dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
        } else if (url.contains("h2")) {
            dbUrl = "jdbc:h2:mem:" + dbUri.getPath();
        } else {
            throw new RuntimeException("No URL known for: [ " + url + " ]");
        }

        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName(dataSourceClassName);
        if (environment.acceptsProfiles("heroku")) {
            config.addDataSourceProperty("ssl", true);
            config.addDataSourceProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
        }
        config.addDataSourceProperty("url", url);
        config.addDataSourceProperty("user", user);
        config.addDataSourceProperty("password", pass);
        config.setConnectionTestQuery("SELECT 1");

        return config;
    }
}
