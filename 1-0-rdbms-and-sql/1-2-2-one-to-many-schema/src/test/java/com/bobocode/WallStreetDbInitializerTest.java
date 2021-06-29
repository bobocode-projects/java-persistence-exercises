package com.bobocode;

import com.bobocode.util.JdbcUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


public class WallStreetDbInitializerTest {
    private static DataSource dataSource;

    @BeforeAll
    static void init() throws SQLException {
        dataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
        WallStreetDbInitializer dbInitializer = new WallStreetDbInitializer(dataSource);
        dbInitializer.init();
    }

    // table broker tests

    @Test
    void testTablesHaveCorrectNames() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SHOW TABLES");
            List<String> tableNames = fetchTableNames(resultSet);

            assertThat(tableNames, containsInAnyOrder("broker", "sales_group", "broker_sales_group"));
        }
    }

    private List<String> fetchTableNames(ResultSet resultSet) throws SQLException {
        List<String> tableNamesList = new ArrayList<>();
        while (resultSet.next()) {
            String tableName = resultSet.getString("table_name");
            tableNamesList.add(tableName);
        }
        return tableNamesList;
    }


    @Test
    void testBrokerTablesHasPrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'broker' AND constraint_type = 'PRIMARY_KEY';");

            boolean resultIsNotEmpty = resultSet.next();

            assertThat(resultIsNotEmpty, is(true));
        }
    }

    @Test
    void testBrokerTablePrimaryKeyHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'broker' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String pkConstraintName = resultSet.getString("constraint_name");

            assertThat(pkConstraintName, equalTo("PK_broker"));
        }
    }

    @Test
    void testBrokerTablePrimaryKeyBasedOnIdField() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'broker' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String pkColumn = resultSet.getString("column_list");

            assertThat("id", equalTo(pkColumn));
        }
    }

    @Test
    void testBrokerTableHasCorrectUniqueConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'broker' AND constraint_type = 'UNIQUE';");

            resultSet.next();
            String uniqueConstraintName = resultSet.getString("constraint_name");
            String uniqueConstraintColumn = resultSet.getString("column_list");

            assertThat(uniqueConstraintName, equalTo("UQ_broker_username"));
            assertThat(uniqueConstraintColumn, equalTo("username"));
        }
    }

    @Test
    void testBrokerTableHasAllRequiredColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'broker';");

            List<String> columns = fetchColumnValues(resultSet, "column_name");

            assertThat(columns.size(), equalTo(4));
            assertThat(columns, containsInAnyOrder("id", "username", "first_name", "last_name"));
        }
    }

    private List<String> fetchColumnValues(ResultSet resultSet, String resultColumnName) throws SQLException {
        List<String> columns = new ArrayList<>();
        while (resultSet.next()) {
            String columnName = resultSet.getString(resultColumnName);
            columns.add(columnName);
        }
        return columns;
    }

    @Test
    void testBrokerTableRequiredColumnsHaveHaveNotNullConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'broker' AND nullable = false;");

            List<String> notNullColumns = fetchColumnValues(resultSet, "column_name");

            assertThat(notNullColumns.size(), is(4));
            assertThat(notNullColumns, containsInAnyOrder("id", "username", "first_name", "last_name"));
        }
    }

    @Test
    void testBrokerIdTypeIsBigint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'broker' AND column_name = 'id';");

            resultSet.next();
            String idTypeName = resultSet.getString("type_name");

            assertThat(idTypeName, is("BIGINT"));
        }
    }

    @Test
    void testBrokerTableStringColumnsHaveCorrectTypeAndLength() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'broker' AND type_name = 'VARCHAR' AND character_maximum_length = 255;");

            List<String> stringColumns = fetchColumnValues(resultSet, "column_name");

            assertThat(stringColumns.size(), is(3));
            assertThat(stringColumns, containsInAnyOrder("username", "first_name", "last_name"));
        }
    }

    // table sale_group test

    @Test
    void testSaleGroupTablesHasPrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'sales_group' AND constraint_type = 'PRIMARY_KEY';");

            boolean resultIsNotEmpty = resultSet.next();

            assertThat(resultIsNotEmpty, is(true));
        }
    }

    @Test
    void testSaleGroupTablePrimaryKeyHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'sales_group' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String pkConstraintName = resultSet.getString("constraint_name");

            assertThat(pkConstraintName, equalTo("PK_sales_group"));
        }
    }

    @Test
    void testSaleGroupTablePrimaryKeyBasedOnIdField() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'sales_group' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String pkColumn = resultSet.getString("column_list");

            assertThat("id", equalTo(pkColumn));
        }
    }

    @Test
    void testSaleGroupTableHasCorrectUniqueConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'sales_group' AND constraint_type = 'UNIQUE';");

            resultSet.next();
            String uniqueConstraintName = resultSet.getString("constraint_name");
            String uniqueConstraintColumn = resultSet.getString("column_list");

            assertThat(uniqueConstraintName, equalTo("UQ_sales_group_name"));
            assertThat(uniqueConstraintColumn, equalTo("name"));
        }
    }

    @Test
    void testSaleGroupTableHasAllRequiredColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'sales_group';");

            List<String> columns = fetchColumnValues(resultSet, "column_name");

            assertThat(columns.size(), equalTo(4));
            assertThat(columns, containsInAnyOrder("id", "name", "transaction_type", "max_transaction_amount"));
        }
    }

    @Test
    void testSaleGroupTableRequiredColumnsHaveHaveNotNullConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'sales_group' AND nullable = false;");

            List<String> notNullColumns = fetchColumnValues(resultSet, "column_name");

            assertThat(notNullColumns.size(), is(4));
            assertThat(notNullColumns, containsInAnyOrder("id", "name", "transaction_type", "max_transaction_amount"));
        }
    }

    @Test
    void testSaleGroupIdTypeIsBigint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'sales_group' AND column_name = 'id';");

            resultSet.next();
            String idTypeName = resultSet.getString("type_name");

            assertThat(idTypeName, is("BIGINT"));
        }
    }

    @Test
    void testSaleGroupTableStringColumnsHaveCorrectTypeAndLength() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'sales_group' AND type_name = 'VARCHAR' AND character_maximum_length = 255;");

            List<String> stringColumns = fetchColumnValues(resultSet, "column_name");

            assertThat(stringColumns.size(), is(2));
            assertThat(stringColumns, containsInAnyOrder("name", "transaction_type"));
        }
    }

    // table broker_sales_group tests

    @Test
    void testBrokerSaleGroupTablesHasForeignKeyToBroker() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'broker_sales_group' AND constraint_type = 'REFERENTIAL' AND column_list = 'broker_id';");

            boolean resultIsNotEmpty = resultSet.next();

            assertThat(resultIsNotEmpty, is(true));
        }
    }

    @Test
    void testBrokerSaleGroupTableForeignKeyToBrokerHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'broker_sales_group' AND constraint_type = 'REFERENTIAL' AND column_list = 'broker_id';");

            resultSet.next();
            String fkConstraintName = resultSet.getString("constraint_name");

            assertThat(fkConstraintName, equalTo("FK_broker_sales_group_broker"));
        }
    }

    @Test
    void testBrokerSaleGroupTablesHasForeignKeyToSalesGroup() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'broker_sales_group' AND constraint_type = 'REFERENTIAL' AND column_list = 'sales_group_id';");

            boolean resultIsNotEmpty = resultSet.next();

            assertThat(resultIsNotEmpty, is(true));
        }
    }

    @Test
    void testBrokerSaleGroupTableForeignKeyToSalesGroupHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'broker_sales_group' AND constraint_type = 'REFERENTIAL' AND column_list = 'sales_group_id';");

            resultSet.next();
            String fkConstraintName = resultSet.getString("constraint_name");

            assertThat(fkConstraintName, equalTo("FK_broker_sales_group_sales_group"));
        }
    }

    @Test
    void testBrokerSaleGroupTableHasNotNullConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'broker_sales_group' AND nullable = false;");

            List<String> notNullColumns = fetchColumnValues(resultSet, "column_name");

            assertThat(notNullColumns.size(), is(2));
            assertThat(notNullColumns, containsInAnyOrder("broker_id", "sales_group_id"));
        }
    }

    @Test
    void testBrokerSaleGroupForeignKeysTypeAreBigint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'broker_sales_group';");

            List<String> columnTypes = fetchColumnValues(resultSet, "type_name");

            assertThat(columnTypes, contains("BIGINT", "BIGINT"));
        }
    }

    @Test
    void testBrokerSaleGroupTableHasCompositePrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'broker_sales_group' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String uniqueConstraintName = resultSet.getString("constraint_name");
            String uniqueConstraintColumn = resultSet.getString("column_list");

            assertThat(uniqueConstraintName, equalTo("PK_broker_sales_group"));
            assertThat(uniqueConstraintColumn, equalTo("broker_id,sales_group_id"));
        }
    }
}
