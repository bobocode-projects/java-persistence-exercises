package com.bobocode;

import com.bobocode.util.FileReader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * {@link WallStreetDbInitializer} is an API that has only one method. It allow to create a database tables to store
 * information about brokers and its sales groups.
 */
public class WallStreetDbInitializer {
    private final static String TABLE_INITIALIZATION_SQL_FILE = "db/migration/table_initialization.sql"; // todo: see the file
    private DataSource dataSource;

    public WallStreetDbInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Reads the SQL script form the file and executes it
     *
     * @throws SQLException
     */
    public void init() throws SQLException {
        String createTablesSql = FileReader.readWholeFileFromResources(TABLE_INITIALIZATION_SQL_FILE);

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute(createTablesSql);
        }
    }

}
