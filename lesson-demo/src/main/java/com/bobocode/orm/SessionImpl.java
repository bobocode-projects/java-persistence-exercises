package com.bobocode.orm;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.sql.DataSource;

@RequiredArgsConstructor
public class SessionImpl implements Session{
    private final DataSource dataSource;

    @Override
    @SneakyThrows
    public <T> T find(Class<T> type, Object id) {
        // todo: 1. Open JDBC Connection
        // todo: 2. Create SELECT BY ID SQL statement ("select * from products where id = ?")
        // todo: 3. Send query to the DB
        // todo: 4. Receive result from the DB
        // todo: 5. Create entity object (product object)
        // todo: 6. Parse the response and set entity object fields
        // todo: 7. Return entity object
        return null;
    }
}
