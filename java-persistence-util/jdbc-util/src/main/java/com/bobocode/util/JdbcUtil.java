package com.bobocode.util;

import org.h2.jdbcx.JdbcDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.util.Map;

public class JdbcUtil {
    static String DEFAULT_DATABASE_NAME = "bobocode_db";
    static String DEFAULT_USERNAME = "bobouser";
    static String DEFAULT_PASSWORD = "bobodpass";

    public static DataSource createDefaultInMemoryH2DataSource() {
        String url = formatH2ImMemoryDbUrl(DEFAULT_DATABASE_NAME);
        return createInMemoryH2DataSource(url, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public static DataSource createInMemoryH2DataSource(String url, String username, String pass) {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setUser(username);
        h2DataSource.setPassword(pass);
        h2DataSource.setUrl(url);

        return h2DataSource;
    }

    private static String formatH2ImMemoryDbUrl(String databaseName) {
        return String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false;DATABASE_TO_UPPER=false;", databaseName);
    }

    public static DataSource createDefaultPostgresDataSource() {
        String url = formatPostgresDbUrl(DEFAULT_DATABASE_NAME);
        return createPostgresDataSource(url, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public static DataSource createPostgresDataSource(String url, String username, String pass) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(pass);
        return dataSource;
    }

    private static String formatPostgresDbUrl(String databaseName) {
        return String.format("jdbc:postgresql://localhost:5432/%s", databaseName);
    }

    public static Map<String, String> getInMemoryDbPropertiesMap() {
        return Map.of(
                "url", String.format("jdbc:h2:mem:%s", DEFAULT_DATABASE_NAME),
                "username", DEFAULT_USERNAME,
                "password", DEFAULT_PASSWORD);
    }


}
