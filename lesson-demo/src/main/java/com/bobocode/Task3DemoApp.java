package com.bobocode;

import lombok.SneakyThrows;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class Task3DemoApp {
    @SneakyThrows
    public static void main(String[] args) {
        DataSource dataSource = initDb();
        try (var connection = dataSource.getConnection()) {
            try (var selectStatement = connection.prepareStatement("select * from notes where id = ?;")) {
                
                connection.setAutoCommit(false);
                
                selectStatement.setInt(1, 54114);
                var rs = selectStatement.executeQuery();
                rs.next();
                System.out.println(rs.getString("body"));
                
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
