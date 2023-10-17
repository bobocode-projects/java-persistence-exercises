package com.bobocode;

import com.bobocode.util.JdbcUtil;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WallStreetDbInitializerTest {
    private static DataSource dataSource;

    @BeforeAll
    static void init() throws SQLException {
        dataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
        WallStreetDbInitializer dbInitializer = new WallStreetDbInitializer(dataSource);
        dbInitializer.init();
    }

    // table broker tests

    @Test
    @Order(1)
    @DisplayName("The brokers table has a correct name")
    void brokerTableHasCorrectNames() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SHOW TABLES");
            List<String> tableNames = fetchTableNames(resultSet);

            assertThat(tableNames).contains("broker");
        }
    }

    @Test
    @Order(2)
    @DisplayName("The brokers table has all the required columns")
    void brokerTableHasAllRequiredColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'broker';");

            List<String> columns = fetchColumnValues(resultSet, "COLUMN_NAME");

            assertThat(columns.size()).isEqualTo(4);
            assertThat(columns).containsExactlyInAnyOrder("id", "username", "first_name", "last_name");
        }
    }

    @Test
    @Order(3)
    @DisplayName("The brokers id column type is BIGINT")
    void brokerIdTypeIsBigint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'broker' AND COLUMN_NAME = 'id';");

            resultSet.next();
            String idTypeName = resultSet.getString("DATA_TYPE");

            assertThat(idTypeName).isEqualTo("BIGINT");
        }
    }

    @Test
    @Order(4)
    @DisplayName("The brokers table required columns have NOT NULL constraint")
    void brokerTableRequiredColumnsHaveNotNullConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'broker' AND IS_NULLABLE = 'NO';");

            List<String> notNullColumns = fetchColumnValues(resultSet, "COLUMN_NAME");

            assertThat(notNullColumns.size()).isEqualTo(4);
            assertThat(notNullColumns).containsExactlyInAnyOrder("id", "username", "first_name", "last_name");
        }
    }

    @Test
    @Order(5)
    @DisplayName("The brokers table string columns have correct type and length")
    void brokerTableStringColumnsHaveCorrectTypeAndLength() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'broker' AND DATA_TYPE = 'CHARACTER VARYING' AND CHARACTER_MAXIMUM_LENGTH = 255;");

            List<String> stringColumns = fetchColumnValues(resultSet, "COLUMN_NAME");

            assertThat(stringColumns.size()).isEqualTo(3);
            assertThat(stringColumns).containsExactlyInAnyOrder("username", "first_name", "last_name");
        }
    }

    @Test
    @Order(6)
    @DisplayName("The brokers table has a primary key")
    void brokerTableHasPrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS" +
                                                         " WHERE TABLE_NAME = 'broker' AND CONSTRAINT_TYPE = 'PRIMARY KEY';");

            boolean resultIsNotEmpty = resultSet.next();

            assertTrue(resultIsNotEmpty);
        }
    }

    @Test
    @Order(7)
    @DisplayName("The brokers table primary key has a correct name")
    void brokerTablePrimaryKeyHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS" +
                                                         " WHERE TABLE_NAME = 'broker' AND CONSTRAINT_TYPE = 'PRIMARY KEY';");

            resultSet.next();
            String pkConstraintName = resultSet.getString("CONSTRAINT_NAME");

            assertThat(pkConstraintName).isEqualTo("PK_broker");
        }
    }

    @Test
    @Order(8)
    @DisplayName("The brokers table primary key based on the id field")
    void brokerTablePrimaryKeyBasedOnIdField() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT ccu.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'broker' AND tc.CONSTRAINT_TYPE = 'PRIMARY KEY';
                    """);

            resultSet.next();
            String pkColumn = resultSet.getString(1);

            assertThat("id").isEqualTo(pkColumn);
        }
    }

    @Test
    @Order(9)
    @DisplayName("The brokers table has a correct unique constraint")
    void brokerTableHasCorrectUniqueConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT tc.CONSTRAINT_NAME, ccu.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'broker' AND tc.CONSTRAINT_TYPE = 'UNIQUE';
                    """);

            resultSet.next();
            String uniqueConstraintName = resultSet.getString(1);
            String uniqueConstraintColumn = resultSet.getString(2);

            assertThat(uniqueConstraintName).isEqualTo("UQ_broker_username");
            assertThat(uniqueConstraintColumn).isEqualTo("username");
        }
    }

    // table sale_group test

    @Test
    @Order(10)
    @DisplayName("The sales group table has a correct name")
    void salesGroupTableHasCorrectNames() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SHOW TABLES");
            List<String> tableNames = fetchTableNames(resultSet);

            assertThat(tableNames).contains("sales_group");
        }
    }

    @Test
    @Order(11)
    @DisplayName("The sales group table has all the required columns")
    void saleGroupTableHasAllRequiredColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'sales_group';");

            List<String> columns = fetchColumnValues(resultSet, "COLUMN_NAME");

            assertThat(columns.size()).isEqualTo(4);
            assertThat(columns).containsExactlyInAnyOrder("id", "name", "transaction_type", "max_transaction_amount");
        }
    }

    @Test
    @Order(12)
    @DisplayName("The sales group id column type is BIGINT")
    void saleGroupIdTypeIsBigint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'sales_group' AND COLUMN_NAME = 'id';");

            resultSet.next();
            String idTypeName = resultSet.getString("DATA_TYPE");

            assertThat(idTypeName).isEqualTo("BIGINT");
        }
    }

    @Test
    @Order(13)
    @DisplayName("The sales group table required columns have NOT NULL constraint")
    void saleGroupTableRequiredColumnsHaveNotNullConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'sales_group' AND IS_NULLABLE = 'NO';");

            List<String> notNullColumns = fetchColumnValues(resultSet, "column_name");

            assertThat(notNullColumns.size()).isEqualTo(4);
            assertThat(notNullColumns).containsExactlyInAnyOrder("id", "name", "transaction_type", "max_transaction_amount");
        }
    }

    @Test
    @Order(14)
    @DisplayName("The sales group table string columns have correct type and length")
    void saleGroupTableStringColumnsHaveCorrectTypeAndLength() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'sales_group' AND DATA_TYPE = 'CHARACTER VARYING' " +
                                                         "AND CHARACTER_MAXIMUM_LENGTH = 255;");

            List<String> stringColumns = fetchColumnValues(resultSet, "column_name");

            assertThat(stringColumns.size()).isEqualTo(2);
            assertThat(stringColumns).containsExactlyInAnyOrder("name", "transaction_type");
        }
    }

    @Test
    @Order(15)
    @DisplayName("The sales group table has a primary key")
    void saleGroupTablesHasPrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS" +
                                                         " WHERE TABLE_NAME = 'sales_group' AND CONSTRAINT_TYPE = 'PRIMARY KEY';");

            boolean resultIsNotEmpty = resultSet.next();

            assertThat(resultIsNotEmpty).isEqualTo(true);
        }
    }

    @Test
    @Order(16)
    @DisplayName("The sales group table primary key has a correct name")
    void saleGroupTablePrimaryKeyHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS" +
                                                         " WHERE TABLE_NAME = 'sales_group' AND CONSTRAINT_TYPE = 'PRIMARY KEY';");

            resultSet.next();
            String pkConstraintName = resultSet.getString("CONSTRAINT_NAME");

            assertThat(pkConstraintName).isEqualTo("PK_sales_group");
        }
    }

    @Test
    @Order(17)
    @DisplayName("The sales group table primary key based on the id field")
    void saleGroupTablePrimaryKeyBasedOnIdField() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT ccu.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'sales_group' AND tc.CONSTRAINT_TYPE = 'PRIMARY KEY';
                    """);

            resultSet.next();
            String pkColumn = resultSet.getString(1);

            assertThat("id").isEqualTo(pkColumn);
        }
    }

    @Test
    @Order(18)
    @DisplayName("The sales group table has a correct unique constraint")
    void saleGroupTableHasCorrectUniqueConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT tc.CONSTRAINT_NAME, ccu.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'sales_group' AND tc.CONSTRAINT_TYPE = 'UNIQUE';
                    """);

            resultSet.next();
            String uniqueConstraintName = resultSet.getString(1);
            String uniqueConstraintColumn = resultSet.getString(2);

            assertThat(uniqueConstraintName).isEqualTo("UQ_sales_group_name");
            assertThat(uniqueConstraintColumn).isEqualTo("name");
        }
    }

    // table broker_sales_group tests
    @Test
    @Order(19)
    @DisplayName("The relations table has a correct name")
    void relationTableHasCorrectNames() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SHOW TABLES");
            List<String> tableNames = fetchTableNames(resultSet);

            assertThat(tableNames).contains("broker_sales_group");
        }
    }

    @Test
    @Order(20)
    @DisplayName("The relations table has correct columns")
    void brokerSaleGroupTableHasCorrectColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'broker_sales_group';");

            List<String> notNullColumns = fetchColumnValues(resultSet, "COLUMN_NAME");

            assertThat(notNullColumns.size()).isEqualTo(2);
            assertThat(notNullColumns).containsExactlyInAnyOrder("broker_id", "sales_group_id");
        }
    }

    @Test
    @Order(21)
    @DisplayName("The relations table columns have NOT NULL constraint")
    void brokerSaleGroupTableColumnsHaveNotNullConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'broker_sales_group' AND IS_NULLABLE = 'NO';");

            List<String> notNullColumns = fetchColumnValues(resultSet, "COLUMN_NAME");

            assertThat(notNullColumns.size()).isEqualTo(2);
            assertThat(notNullColumns).containsExactlyInAnyOrder("broker_id", "sales_group_id");
        }
    }

    @Test
    @Order(22)
    @DisplayName("The relations table columns are BIGINT")
    void brokerSaleGroupForeignKeysTypeAreBigint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'broker_sales_group';");

            List<String> columnTypes = fetchColumnValues(resultSet, "DATA_TYPE");

            assertThat(columnTypes).containsExactlyInAnyOrder("BIGINT", "BIGINT");
        }
    }

    @Test
    @Order(23)
    @DisplayName("The relations table has a composite primary key")
    void brokerSaleGroupTableHasCompositePrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT tc.CONSTRAINT_NAME, ccu.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'broker_sales_group' AND tc.CONSTRAINT_TYPE = 'PRIMARY KEY';
                    """);

            resultSet.next();
            String uniqueConstraintName = resultSet.getString(1);
            String firstPrimaryKeyColumn = resultSet.getString(2);
            resultSet.next();
            String secondPrimaryKeyColumn = resultSet.getString(2);

            assertThat(uniqueConstraintName).isEqualTo("PK_broker_sales_group");
            assertThat(firstPrimaryKeyColumn).isEqualTo("sales_group_id");
            assertThat(secondPrimaryKeyColumn).isEqualTo("broker_id");
        }
    }

    @Test
    @Order(24)
    @DisplayName("The relations table has a foreign key to the brokers table")
    void brokerSaleGroupTablesHasForeignKeyToBroker() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'broker_sales_group' AND tc.CONSTRAINT_TYPE = 'FOREIGN KEY' AND ccu.COLUMN_NAME = 'broker_id';
                    """);

            boolean resultIsNotEmpty = resultSet.next();

            assertTrue(resultIsNotEmpty);
        }
    }

    @Test
    @Order(25)
    @DisplayName("The relations table foreign key to the brokers table has a correct name")
    void brokerSaleGroupTableForeignKeyToBrokerHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT tc.CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'broker_sales_group' AND tc.CONSTRAINT_TYPE = 'FOREIGN KEY' AND ccu.COLUMN_NAME = 'broker_id';
                    """);

            resultSet.next();
            String fkConstraintName = resultSet.getString(1);

            assertThat(fkConstraintName, equalTo("FK_broker_sales_group_broker"));
        }
    }

    @Test
    @Order(26)
    @DisplayName("The relations table has a foreign key to the sales groups table")
    void brokerSaleGroupTablesHasForeignKeyToSalesGroup() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'broker_sales_group' AND tc.CONSTRAINT_TYPE = 'FOREIGN KEY' AND ccu.COLUMN_NAME = 'sales_group_id';
                    """);

            boolean resultIsNotEmpty = resultSet.next();

            assertTrue(resultIsNotEmpty);
        }
    }

    @Test
    @Order(27)
    @DisplayName("The relations table foreign key to the sales group table has a correct name")
    void brokerSaleGroupTableForeignKeyToSalesGroupHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT tc.CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'broker_sales_group' AND tc.CONSTRAINT_TYPE = 'FOREIGN KEY' AND ccu.COLUMN_NAME = 'sales_group_id';
                    """);

            resultSet.next();
            String fkConstraintName = resultSet.getString(1);

            assertThat(fkConstraintName).isEqualTo("FK_broker_sales_group_sales_group");
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

    private List<String> fetchColumnValues(ResultSet resultSet, String resultColumnName) throws SQLException {
        List<String> columns = new ArrayList<>();
        while (resultSet.next()) {
            String columnName = resultSet.getString(resultColumnName);
            columns.add(columnName);
        }
        return columns;
    }
}
