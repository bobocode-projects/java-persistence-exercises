package com.bobocode;

import com.bobocode.entity.Product;
import com.bobocode.orm.Session;
import com.bobocode.orm.SessionImpl;
import com.bobocode.service.PrinterService;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class DemoApp {
    public static void main(String[] args) {
        var dataSource = initializeDataSource();
        Session session = new SessionImpl(dataSource);
        Product product = session.find(Product.class, 8L);
        System.out.println(product);
    }

    private static DataSource initializeDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL("jdbc:postgresql://93.175.204.87:5432/postgres");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        return dataSource;
    }
}
