package com.bobocode;

import lombok.SneakyThrows;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class Task4DemoApp {
    @SneakyThrows
    public static void main(String[] args) {
        DataSource dataSource = initDb();
        var start = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            try (var connection = dataSource.getConnection()) {
                try (var selectStatement = connection.createStatement()) {
                    selectStatement.executeQuery("select random()"); // just to call the DB
                }
            }
        }
        
        var end = System.nanoTime();
        System.out.println((end - start) / 1000_000 + "ms");
    }

    private static DataSource initDb() { // todo: refactor to use custom pooled data source
        var dataSource = new PGSimpleDataSource();
        dataSource.setURL("jdbc:postgresql://93.175.203.215:5432/postgres");
        dataSource.setUser("ju23user");
        dataSource.setPassword("ju23pass");
        return dataSource;
    }

}
