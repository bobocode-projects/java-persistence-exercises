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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountDbInitializerTest {

    private static DataSource dataSource;

    @BeforeAll
    static void init() throws SQLException {
        dataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
        AccountDbInitializer dbInitializer = new AccountDbInitializer(dataSource);
        dbInitializer.init();
    }

    @Test
    @Order(1)
    @DisplayName("The table has a correct name")
    void tableHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SHOW TABLES");
            resultSet.next();
            String tableName = resultSet.getString("TABLE_NAME");

            assertThat(tableName).isEqualTo("account");
        }
    }

    @Test
    @Order(2)
    @DisplayName("The table has a primary key")
    void tableHasPrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS" +
                                                         " WHERE TABLE_NAME = 'account' AND CONSTRAINT_TYPE = 'PRIMARY KEY';");

            boolean resultIsNotEmpty = resultSet.next();

            assertTrue(resultIsNotEmpty);
        }
    }

    @Test
    @Order(3)
    @DisplayName("The table has all the required columns")
    void tableHasAllRequiredColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'account';");

            List<String> columns = fetchColumnsNames(resultSet);

            assertThat(columns.size()).isEqualTo(8);
            assertTrue(columns.containsAll(
                    List.of("id", "first_name", "last_name", "email", "gender", "balance", "birthday", "creation_time")));
        }
    }

    @Test
    @Order(4)
    @DisplayName("Id column has a type of BIGINT")
    void idHasTypeBigInteger() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'account' AND COLUMN_NAME = 'id';");

            resultSet.next();
            String idTypeName = resultSet.getString(1);

            assertThat(idTypeName).isEqualTo("BIGINT");
        }
    }

    @Test
    @Order(5)
    @DisplayName("String columns have correct type and length")
    void stringColumnsHaveCorrectTypeAndLength() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'account' AND DATA_TYPE = 'CHARACTER VARYING' AND CHARACTER_MAXIMUM_LENGTH = 255;");

            List<String> stringColumns = fetchColumnsNames(resultSet);

            assertThat(stringColumns.size()).isEqualTo(4);
            assertTrue(stringColumns.containsAll(List.of("first_name", "last_name", "email", "gender")));
        }
    }

    @Test
    @Order(6)
    @DisplayName("Birthday column has a correct type")
    void birthdayColumnHasCorrectType() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'account' AND COLUMN_NAME = 'birthday';");

            resultSet.next();
            String birthdayColumnType = resultSet.getString("DATA_TYPE");

            assertThat(birthdayColumnType).isEqualTo("DATE");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Balance column has a correct type")
    void balanceColumnHasCorrectType() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'account' AND COLUMN_NAME = 'balance';");

            resultSet.next();
            String balanceColumnType = resultSet.getString("DATA_TYPE");
            int balanceColumnPrecision = resultSet.getInt("NUMERIC_PRECISION");
            int balanceColumnScale = resultSet.getInt("NUMERIC_SCALE");

            assertThat(balanceColumnType).isEqualTo("NUMERIC");
            assertThat(balanceColumnPrecision).isEqualTo(19);
            assertThat(balanceColumnScale).isEqualTo(4);
        }
    }

    @Test
    @Order(8)
    @DisplayName("Balance column is not mandatory")
    void balanceIsNotMandatory() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'account' AND COLUMN_NAME = 'balance';");

            resultSet.next();
            String isNullable = resultSet.getString("IS_NULLABLE");

            assertThat(isNullable).isEqualTo("YES");
        }
    }

    @Test
    @Order(9)
    @DisplayName("Creation time column has type TIMESTAMP")
    void creationTimeHasTypeTimestamp() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'account' AND COLUMN_NAME = 'creation_time';");

            resultSet.next();
            String creationTimeColumnType = resultSet.getString("DATA_TYPE");

            assertThat(creationTimeColumnType).isEqualTo("TIMESTAMP");
        }
    }

    @Test
    @Order(10)
    @DisplayName("Creation time has a default value")
    void creationTimeHasDefaultValue() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'account' AND COLUMN_NAME = 'creation_time';");

            resultSet.next();
            String creationTimeColumnDefault = resultSet.getString("COLUMN_DEFAULT");

            assertThat(creationTimeColumnDefault).isEqualTo("LOCALTIMESTAMP");
        }
    }

    @Test
    @Order(11)
    @DisplayName("The required columns have NOT NULL constraint")
    void requiredColumnsHaveNotNullConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'account' AND IS_NULLABLE = 'NO';");

            List<String> notNullColumns = fetchColumnsNames(resultSet);

            assertThat(notNullColumns.size()).isEqualTo(7);
            assertTrue(notNullColumns.containsAll(
                    List.of("id", "first_name", "last_name", "email", "gender", "birthday", "creation_time")));
        }
    }

    @Test
    @Order(12)
    @DisplayName("The primary key has a correct name")
    void primaryKeyHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS" +
                                                         " WHERE TABLE_NAME = 'account' AND CONSTRAINT_TYPE = 'PRIMARY KEY';");

            resultSet.next();
            String pkConstraintName = resultSet.getString("CONSTRAINT_NAME");

            assertThat(pkConstraintName).isEqualTo("account_pk");
        }
    }

    @Test
    @Order(13)
    @DisplayName("The primary key id based on the Id field")
    void primaryKeyBasedOnIdField() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT ccu.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'account' AND tc.CONSTRAINT_TYPE = 'PRIMARY KEY';
                    """);

            resultSet.next();
            String pkColumn = resultSet.getString(1);

            assertThat(pkColumn).isEqualTo("id");
        }
    }

    @Test
    @Order(14)
    @DisplayName("The table has a correct alternative key")
    void tableHasCorrectAlternativeKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT ccu.CONSTRAINT_NAME, ccu.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'account' AND tc.CONSTRAINT_TYPE = 'UNIQUE';
                    """);

            resultSet.next();
            String uniqueConstraintName = resultSet.getString(1);
            String uniqueConstraintColumn = resultSet.getString(2);

            assertThat(uniqueConstraintName).isEqualTo("account_email_uq");
            assertThat(uniqueConstraintColumn).isEqualTo("email");
        }
    }


    private List<String> fetchColumnsNames(ResultSet resultSet) throws SQLException {
        List<String> columns = new ArrayList<>();
        while (resultSet.next()) {
            String columnName = resultSet.getString("COLUMN_NAME");
            columns.add(columnName);
        }
        return columns;
    }
}
