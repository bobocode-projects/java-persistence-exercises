package com.bobocode.pool;

import org.postgresql.ds.PGSimpleDataSource;

public class PooledDataSource extends PGSimpleDataSource {
    // todo: 1. store a queue of connections (this is a pool)
    // todo: 2. initialize a datasource with 10 physical connection
    // todo: 3. override method getConnection so it uses a pool
}
