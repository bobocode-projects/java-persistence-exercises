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
    @DisplayName("users table has correct name")
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
                    " WHERE table_name = 'users' AND column_name = 'id';");

            resultSet.next();
            String idTypeName = resultSet.getString("type_name");

            assertThat(idTypeName).isEqualTo("BIGINT");
        }
    }

    @Test
    @Order(3)
    @DisplayName("users table has all the required columns")
    void usersTableHasAllRequiredColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'users';");

            List<String> columns = fetchColumnValues(resultSet, "column_name");

            assertThat(columns.size()).isEqualTo(5);
            assertThat(columns).containsExactlyInAnyOrder("id", "email", "first_name", "last_name", "birthday");
        }
    }

    @Test
    @Order(4)
    @DisplayName("users table String columns have correct type and length")
    void testUsersTableStringColumnsHaveCorrectTypeAndLength() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'users' AND type_name = 'VARCHAR' AND character_maximum_length = 255;");

            List<String> stringColumns = fetchColumnValues(resultSet, "column_name");

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
                    " WHERE table_name = 'users' AND column_name = 'birthday';");

            resultSet.next();
            String idTypeName = resultSet.getString("type_name");

            assertThat(idTypeName).isEqualTo("DATE");
        }
    }

    @Test
    @Order(6)
    @DisplayName("users table required columns have Not Null constrains")
    void usersTableRequiredColumnsHaveHaveNotNullConstraint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'users' AND nullable = false;");

            List<String> notNullColumns = fetchColumnValues(resultSet, "column_name");

            assertThat(notNullColumns).contains("email", "first_name", "last_name", "birthday");
        }
    }

    @Test
    @Order(7)
    @DisplayName("users table has primary key")
    void usersTableHasPrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'users' AND constraint_type = 'PRIMARY_KEY';");

            boolean resultIsNotEmpty = resultSet.next();


            assertTrue(resultIsNotEmpty);
        }
    }

    @Test
    @Order(8)
    @DisplayName("users table primary key has correct name")
    void usersTablePrimaryKeyHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'users' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String pkConstraintName = resultSet.getString("constraint_name");

            assertThat(pkConstraintName).isEqualTo("users_PK");
        }
    }

    @Test
    @Order(9)
    @DisplayName("users table primary key based on id field")
    void usersTablePrimaryKeyBasedOnIdField() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'users' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String pkColumn = resultSet.getString("column_list");

            assertThat("id").isEqualTo(pkColumn);
        }
    }

    @Test
    @Order(10)
    @DisplayName("users table has correct alternative key")
    void testUsersTableHasCorrectAlternativeKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'users' AND constraint_type = 'UNIQUE';");

            resultSet.next();
            String uniqueConstraintName = resultSet.getString("constraint_name");
            String uniqueConstraintColumn = resultSet.getString("column_list");

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
    @DisplayName("profiles table group id is Bigint")
    void testProfilesGroupIdTypeIsBigint() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'profiles' AND column_name = 'user_id';");

            resultSet.next();
            String idTypeName = resultSet.getString("type_name");

            assertThat(idTypeName).isEqualTo("BIGINT");
        }
    }

    @Test
    @Order(13)
    @DisplayName("profiles table has all the required columns")
    void testProfilesTableHasAllRequiredColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'profiles';");

            List<String> columns = fetchColumnValues(resultSet, "column_name");

            assertThat(columns.size()).isEqualTo(5);
            assertThat(columns).containsExactlyInAnyOrder("user_id", "job_position", "company", "education", "city");
        }
    }

    @Test
    @Order(14)
    @DisplayName("profiles table string columns have correct type and length")
    void profilesTableStringColumnsHaveCorrectTypeAndLength() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE table_name = 'profiles' AND type_name = 'VARCHAR' AND character_maximum_length = 255;");

            List<String> stringColumns = fetchColumnValues(resultSet, "column_name");

            assertThat(stringColumns.size()).isEqualTo(4);
            assertThat(stringColumns).containsExactlyInAnyOrder("job_position", "company", "education", "city");
        }
    }

    @Test
    @Order(15)
    @DisplayName("profiles table has primary key")
    void profilesTablesHasPrimaryKey() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'profiles' AND constraint_type = 'PRIMARY_KEY';");

            boolean resultIsNotEmpty = resultSet.next();

            assertThat(resultIsNotEmpty).isEqualTo(true);
        }
    }

    @Test
    @Order(16)
    @DisplayName("profiles table primary key has correct name")
    void profilesTablePrimaryKeyHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'profiles' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String pkConstraintName = resultSet.getString("constraint_name");

            assertThat(pkConstraintName).isEqualTo("profiles_PK");
        }
    }

    @Test
    @Order(17)
    @DisplayName("profiles table primary key based on foreign key column")
    void profilesTablePrimaryKeyBasedOnForeignKeyColumn() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'profiles' AND constraint_type = 'PRIMARY_KEY';");

            resultSet.next();
            String pkColumn = resultSet.getString("column_list");

            assertThat("user_id", equalTo(pkColumn));
        }
    }

    @Test
    @Order(18)
    @DisplayName("profiles table has foreign key to users")
    void testProfilesHasForeignKeyToUsers() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'profiles' AND constraint_type = 'REFERENTIAL' AND column_list = 'user_id';");

            boolean resultIsNotEmpty = resultSet.next();

            assertThat(resultIsNotEmpty).isEqualTo(true);
        }
    }

    @Test
    @Order(19)
    @DisplayName("profiles table foreign key to users has correct name")
    void profilesForeignKeyToUsersHasCorrectName() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS" +
                    " WHERE table_name = 'profiles' AND constraint_type = 'REFERENTIAL' AND column_list = 'user_id';");

            resultSet.next();
            String fkConstraintName = resultSet.getString("constraint_name");

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
