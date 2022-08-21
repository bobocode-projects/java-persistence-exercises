package com.bobocode.pool;

import lombok.SneakyThrows;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PooledDataSource extends PGSimpleDataSource {
    private Queue<Connection> pool;
    private final int poolSize = 10;

    public PooledDataSource(String url, String userName, String pass) {
        this.pool = new ConcurrentLinkedQueue<>();
        this.setUrl(url);
        this.setUser(userName);
        this.setPassword(pass);
        initPool();
    }

    @SneakyThrows
    private void initPool() {
        for (int i = 0; i < poolSize; i++) {
            Connection connection = super.getConnection();
            pool.add(new ConnectionProxy(connection, pool));
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return pool.poll();
    }
}
