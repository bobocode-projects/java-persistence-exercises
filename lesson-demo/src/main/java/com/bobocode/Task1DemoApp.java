package com.bobocode;

import lombok.SneakyThrows;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class Task1DemoApp {
    @SneakyThrows
    public static void main(String[] args) {
        DataSource dataSource = initDb();
        try (var connection = dataSource.getConnection()) {
            try (var insertStatement = connection.prepareStatement("insert into notes(title, body, person_id) values(?,?,?);")) {
                var start = System.nanoTime();
                // todo: insert 1000 notes
                var end = System.nanoTime();
                System.out.println((end - start) / 1000_000 + "ms");
            }
        }

    }

    private static DataSource initDb() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setURL("jdbc:postgresql://93.175.203.215:5432/postgres");
        dataSource.setUser("ju23user");
        dataSource.setPassword("ju23pass");
        return dataSource;
    }

}
