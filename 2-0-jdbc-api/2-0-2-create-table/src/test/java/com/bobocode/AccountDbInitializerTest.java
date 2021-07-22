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
public class AccountDbInitializerTest {
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
            String tableName = resultSet.getString("table_name");

            assertThat(tableName).isEqualTo("account");
        }
    }

    @Test
    @Order(2)
    @DisplayName("The table has a primary key")
    void tableHasPrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'account' AND constraint_type = 'PRIMARY_KEY';");

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
                    " WHERE table_name = 'account';");

            List<String> columns = fetchColumnsNames(resultSet);

            assertThat(columns.size()).isEqualTo(8);
            assertTrue(columns.containsAll(List.of("id", "first_name", "last_name", "email", "gender", "balance", "birthday", "creation_time")));
        }
    }

    @Test
    @Order(4)
    @DisplayName("Id column has a type of BIGINT")
    void idHasTypeBigInteger() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account' AND column_name = 'id';");

            resultSet.next();
            String idTypeName = resultSet.getString("type_name");

            assertThat(idTypeName).isEqualTo("BIGINT");
        }
    }

    @Test
    @Order(5)
    @DisplayName("String columns have correct type and length")
    void stringColumnsHaveCorrectTypeAndLength() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account' AND type_name = 'VARCHAR' AND character_maximum_length = 255;");

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
                    " WHERE table_name = 'account' AND column_name = 'birthday';");

            resultSet.next();
            String birthdayColumnType = resultSet.getString("type_name");

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
                    " WHERE table_name = 'account' AND column_name = 'balance';");

            resultSet.next();
            String balanceColumnType = resultSet.getString("type_name");
            int balanceColumnPrecision = resultSet.getInt("numeric_precision");
            int balanceColumnScale = resultSet.getInt("numeric_scale");

            assertThat(balanceColumnType).isEqualTo("DECIMAL");
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
                    " WHERE table_name = 'account' AND column_name = 'balance';");

            resultSet.next();
            boolean balanceColumnIsNullable = resultSet.getBoolean("nullable");

            assertTrue(balanceColumnIsNullable);
        }
    }

    @Test
    @Order(9)
    @DisplayName("Creation time column has type TIMESTAMP")
    void creationTimeHasTypeTimestamp() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account' AND column_name = 'creation_time';");

            resultSet.next();
            String creationTimeColumnType = resultSet.getString("type_name");

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
                    " WHERE table_name = 'account' AND column_name = 'creation_time';");

            resultSet.next();
            String creationTimeColumnDefault = resultSet.getString("column_default");

            assertThat(creationTimeColumnDefault).isEqualTo("NOW()");
        }
    }

    @Test
    @Order(11)
    @DisplayName("The required columns have NOT NULL constraint")
    void requiredColumnsHaveNotNullConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account' AND nullable = false;");

            List<String> notNullColumns = fetchColumnsNames(resultSet);

            assertThat(notNullColumns.size()).isEqualTo(7);
            assertTrue(notNullColumns.containsAll(List.of("id", "first_name", "last_name", "email", "gender", "birthday", "creation_time")));
        }
    }

    @Test
    @Order(12)
    @DisplayName("The primary key has a correct name")
    void primaryKeyHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'account' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String pkConstraintName = resultSet.getString("constraint_name");

            assertThat(pkConstraintName).isEqualTo("account_pk");
        }
    }

    @Test
    @Order(13)
    @DisplayName("The primary key id based on the Id field")
    void primaryKeyBasedOnIdField() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'account' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String pkColumn = resultSet.getString("column_list");

            assertThat(pkColumn).isEqualTo("id");
        }
    }

    @Test
    @Order(14)
    @DisplayName("The table has a correct alternative key")
    void tableHasCorrectAlternativeKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'account' AND constraint_type = 'UNIQUE';");

            resultSet.next();
            String uniqueConstraintName = resultSet.getString("constraint_name");
            String uniqueConstraintColumn = resultSet.getString("column_list");

            assertThat(uniqueConstraintName).isEqualTo("account_email_uq");
            assertThat(uniqueConstraintColumn).isEqualTo("email");
        }
    }


    private List<String> fetchColumnsNames(ResultSet resultSet) throws SQLException {
        List<String> columns = new ArrayList<>();
        while (resultSet.next()) {
            String columnName = resultSet.getString("column_name");
            columns.add(columnName);
        }
        return columns;
    }
}
