package com.bobocode;

import lombok.SneakyThrows;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class Task2DemoApp {
    @SneakyThrows
    public static void main(String[] args) {
        DataSource dataSource = initDb();
        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                connection.setAutoCommit(false);
                statement.setFetchSize(100);
                var rs = statement.executeQuery("select * from performance_demo.reminders");
                while (rs.next()) {
                    System.out.println(rs.getString("todo"));
                }
                connection.rollback();
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
