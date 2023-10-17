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
public class UserProfileDbInitializerTest {
    private static DataSource dataSource;

    @BeforeAll
    static void init() throws SQLException {
        dataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
        UserProfileDbInitializer dbInitializer = new UserProfileDbInitializer(dataSource);
        dbInitializer.init();
    }

    @Test
    @Order(1)
    @DisplayName("The users table has correct name")
    void usersTableHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SHOW TABLES");
            Iterable<String> tableNames = fetchTableNames(resultSet);

            assertThat(tableNames).contains("users");
        }
    }

    @Test
    @Order(2)
    @DisplayName("User Id type is Bigint")
    void userIdTypeIsBigint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'id';");

            resultSet.next();
            String idTypeName = resultSet.getString("DATA_TYPE");

            assertThat(idTypeName).isEqualTo("BIGINT");
        }
    }

    @Test
    @Order(3)
    @DisplayName("The users table has all the required columns")
    void usersTableHasAllRequiredColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'users';");

            List<String> columns = fetchColumnValues(resultSet, "COLUMN_NAME");

            assertThat(columns.size()).isEqualTo(5);
            assertThat(columns).containsExactlyInAnyOrder("id", "email", "first_name", "last_name", "birthday");
        }
    }

    @Test
    @Order(4)
    @DisplayName("The users table String columns have correct type and length")
    void testUsersTableStringColumnsHaveCorrectTypeAndLength() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'users' AND DATA_TYPE = 'CHARACTER VARYING' AND CHARACTER_MAXIMUM_LENGTH = 255;");

            List<String> stringColumns = fetchColumnValues(resultSet, "COLUMN_NAME");

            assertThat(stringColumns.size()).isEqualTo(3);
            assertThat(stringColumns).containsExactlyInAnyOrder("email", "first_name", "last_name");
        }
    }

    @Test
    @Order(5)
    @DisplayName("User birthday type is Date")
    void userBirthdayTypeIsDate() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'birthday';");

            resultSet.next();
            String idTypeName = resultSet.getString("DATA_TYPE");

            assertThat(idTypeName).isEqualTo("DATE");
        }
    }

    @Test
    @Order(6)
    @DisplayName("The users table required columns have Not Null constrains")
    void usersTableRequiredColumnsHaveHaveNotNullConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'users' AND IS_NULLABLE = 'NO';");

            List<String> notNullColumns = fetchColumnValues(resultSet, "COLUMN_NAME");

            assertThat(notNullColumns).contains("email", "first_name", "last_name", "birthday");
        }
    }

    @Test
    @Order(7)
    @DisplayName("The users table has primary key")
    void usersTableHasPrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS" +
                                                         " WHERE TABLE_NAME = 'users' AND CONSTRAINT_TYPE = 'PRIMARY KEY';");

            boolean resultIsNotEmpty = resultSet.next();


            assertTrue(resultIsNotEmpty);
        }
    }

    @Test
    @Order(8)
    @DisplayName("The users table primary key has correct name")
    void usersTablePrimaryKeyHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS" +
                                                         " WHERE TABLE_NAME = 'users' AND CONSTRAINT_TYPE = 'PRIMARY KEY';");

            resultSet.next();
            String pkConstraintName = resultSet.getString(1);

            assertThat(pkConstraintName).isEqualTo("users_PK");
        }
    }

    @Test
    @Order(9)
    @DisplayName("The users table primary key based on id field")
    void usersTablePrimaryKeyBasedOnIdField() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT ccu.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'users' AND tc.CONSTRAINT_TYPE = 'PRIMARY KEY';
                    """);

            resultSet.next();
            String pkColumn = resultSet.getString(1);

            assertThat("id").isEqualTo(pkColumn);
        }
    }

    @Test
    @Order(10)
    @DisplayName("The users table has correct alternative key")
    void testUsersTableHasCorrectAlternativeKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT tc.CONSTRAINT_NAME, ccu.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'users' AND tc.CONSTRAINT_TYPE = 'UNIQUE';
                    """);

            resultSet.next();
            String uniqueConstraintName = resultSet.getString(1);
            String uniqueConstraintColumn = resultSet.getString(2);

            assertThat(uniqueConstraintName).isEqualTo("users_email_AK");
            assertThat(uniqueConstraintColumn).isEqualTo("email");
        }
    }

    @Test
    @Order(11)
    @DisplayName("User profile table has correct name")
    void userProfileTableHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SHOW TABLES");
            List<String> tableNames = fetchTableNames(resultSet);

            assertThat(tableNames).contains("profiles");
        }
    }

    @Test
    @Order(12)
    @DisplayName("The profiles table group id is Bigint")
    void testProfilesGroupIdTypeIsBigint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'profiles' AND COLUMN_NAME = 'user_id';");

            resultSet.next();
            String idTypeName = resultSet.getString("DATA_TYPE");

            assertThat(idTypeName).isEqualTo("BIGINT");
        }
    }

    @Test
    @Order(13)
    @DisplayName("The profiles table has all the required columns")
    void testProfilesTableHasAllRequiredColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'profiles';");

            List<String> columns = fetchColumnValues(resultSet, "COLUMN_NAME");

            assertThat(columns.size()).isEqualTo(5);
            assertThat(columns).containsExactlyInAnyOrder("user_id", "job_position", "company", "education", "city");
        }
    }

    @Test
    @Order(14)
    @DisplayName("The profiles table string columns have correct type and length")
    void profilesTableStringColumnsHaveCorrectTypeAndLength() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS" +
                                                         " WHERE TABLE_NAME = 'profiles' AND DATA_TYPE = 'CHARACTER VARYING' AND CHARACTER_MAXIMUM_LENGTH = 255;");

            List<String> stringColumns = fetchColumnValues(resultSet, "COLUMN_NAME");

            assertThat(stringColumns.size()).isEqualTo(4);
            assertThat(stringColumns).containsExactlyInAnyOrder("job_position", "company", "education", "city");
        }
    }

    @Test
    @Order(15)
    @DisplayName("The profiles table has primary key")
    void profilesTablesHasPrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS" +
                                                         " WHERE TABLE_NAME = 'profiles' AND CONSTRAINT_TYPE = 'PRIMARY KEY';");

            boolean resultIsNotEmpty = resultSet.next();

            assertThat(resultIsNotEmpty).isEqualTo(true);
        }
    }

    @Test
    @Order(16)
    @DisplayName("The profiles table primary key has correct name")
    void profilesTablePrimaryKeyHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS" +
                                                         " WHERE TABLE_NAME = 'profiles' AND CONSTRAINT_TYPE = 'PRIMARY KEY';");

            resultSet.next();
            String pkConstraintName = resultSet.getString("CONSTRAINT_NAME");

            assertThat(pkConstraintName).isEqualTo("profiles_PK");
        }
    }

    @Test
    @Order(17)
    @DisplayName("The profiles table primary key based on foreign key column")
    void profilesTablePrimaryKeyBasedOnForeignKeyColumn() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT ccu.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'profiles' AND tc.CONSTRAINT_TYPE = 'PRIMARY KEY';
                    """);

            resultSet.next();
            String pkColumn = resultSet.getString(1);

            assertThat("user_id", equalTo(pkColumn));
        }
    }

    @Test
    @Order(18)
    @DisplayName("The profiles table has foreign key to users")
    void testProfilesHasForeignKeyToUsers() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'profiles' AND tc.CONSTRAINT_TYPE = 'FOREIGN KEY' AND ccu.COLUMN_NAME = 'user_id';
                    """);

            boolean resultIsNotEmpty = resultSet.next();

            assertThat(resultIsNotEmpty).isEqualTo(true);
        }
    }

    @Test
    @Order(19)
    @DisplayName("The profiles table foreign key to users has correct name")
    void profilesForeignKeyToUsersHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("""
                    SELECT tc.CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc 
                    INNER JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu 
                    ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME 
                    WHERE tc.TABLE_NAME = 'profiles' AND tc.CONSTRAINT_TYPE = 'FOREIGN KEY' AND ccu.COLUMN_NAME = 'user_id';
                    """);

            resultSet.next();
            String fkConstraintName = resultSet.getString(1);

            assertThat(fkConstraintName).isEqualTo("profiles_users_FK");
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
