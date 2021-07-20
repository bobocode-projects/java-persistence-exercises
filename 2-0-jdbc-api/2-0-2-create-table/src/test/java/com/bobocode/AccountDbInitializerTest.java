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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class AccountDbInitializerTest {
    private static DataSource dataSource;

    @BeforeAll
    static void init() throws SQLException {
        dataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
        AccountDbInitializer dbInitializer = new AccountDbInitializer(dataSource);
        dbInitializer.init();
    }

    @Test
    void testTableHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SHOW TABLES");
            resultSet.next();
            String tableName = resultSet.getString("table_name");

            assertEquals("account", tableName);
        }
    }

    @Test
    void testTableHasPrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'account' AND constraint_type = 'PRIMARY_KEY';");

            boolean resultIsNotEmpty = resultSet.next();

            assertTrue(resultIsNotEmpty);
        }
    }

    @Test
    void testPrimaryKeyHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'account' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String pkConstraintName = resultSet.getString("constraint_name");

            assertEquals("account_pk", pkConstraintName);
        }
    }

    @Test
    void testPrimaryKeyBasedOnIdField() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'account' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String pkColumn = resultSet.getString("column_list");

            assertEquals("id", pkColumn);
        }
    }

    @Test
    void testTableHasCorrectAlternativeKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'account' AND constraint_type = 'UNIQUE';");

            resultSet.next();
            String uniqueConstraintName = resultSet.getString("constraint_name");
            String uniqueConstraintColumn = resultSet.getString("column_list");

            assertEquals("account_email_uq", uniqueConstraintName);
            assertEquals("email", uniqueConstraintColumn);
        }
    }

    @Test
    void testTableHasAllRequiredColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account';");

            List<String> columns = fetchColumnsNames(resultSet);

            assertEquals(8, columns.size());
            assertTrue(columns.containsAll(List.of("id", "first_name", "last_name", "email", "gender", "balance", "birthday", "creation_time")));
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


    @Test
    void testRequiredColumnsHaveHaveNotNullConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account' AND nullable = false;");

            List<String> notNullColumns = fetchColumnsNames(resultSet);

            assertEquals(7, notNullColumns.size());
            assertTrue(notNullColumns.containsAll(List.of("id", "first_name", "last_name", "email", "gender", "birthday", "creation_time")));
        }
    }

    @Test
    void testIdHasTypeBiInteger() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account' AND column_name = 'id';");

            resultSet.next();
            String idTypeName = resultSet.getString("type_name");

            assertEquals("BIGINT", idTypeName);
        }
    }

    @Test
    void testCreationTimeHasTypeTimestamp() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account' AND column_name = 'creation_time';");

            resultSet.next();
            String creationTimeColumnType = resultSet.getString("type_name");

            assertEquals("TIMESTAMP", creationTimeColumnType);
        }
    }

    @Test
    void testCreationTimeHasDefaultValue() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account' AND column_name = 'creation_time';");

            resultSet.next();
            String creationTimeColumnDefault = resultSet.getString("column_default");

            assertEquals("NOW()", creationTimeColumnDefault);
        }
    }

    @Test
    void testEmailColumnHasCorrectSize() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account' AND column_name = 'email';");

            resultSet.next();
            String emailColumnType = resultSet.getString("type_name");
            int emailColumnMaxLength = resultSet.getInt("character_maximum_length");

            assertEquals("VARCHAR", emailColumnType);
            assertEquals(255, emailColumnMaxLength);
        }
    }

    @Test
    void testBirthdayColumnHasCorrectType() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account' AND column_name = 'birthday';");

            resultSet.next();
            String birthdayColumnType = resultSet.getString("type_name");

            assertEquals("DATE", birthdayColumnType);
        }
    }

    @Test
    void testBalanceColumnHasCorrectType() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account' AND column_name = 'balance';");

            resultSet.next();
            String balanceColumnType = resultSet.getString("type_name");
            int balanceColumnPrecision = resultSet.getInt("numeric_precision");
            int balanceColumnScale = resultSet.getInt("numeric_scale");

            assertEquals("DECIMAL", balanceColumnType);
            assertEquals(19, balanceColumnPrecision);
            assertEquals(4, balanceColumnScale);
        }
    }

    @Test
    void testBalanceIsNotMandatory() throws SQLException {
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
    void testStringColumnsHaveCorrectTypeAndLength() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'account' AND type_name = 'VARCHAR' AND character_maximum_length = 255;");

            List<String> stringColumns = fetchColumnsNames(resultSet);

            assertEquals(4, stringColumns.size());
            assertTrue(stringColumns.containsAll(List.of("first_name", "last_name", "email", "gender")));
        }
    }


}
