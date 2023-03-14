package com.bobocode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Properties;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaPersistenceUnitTest {

    private static ParsedPersistenceXmlDescriptor persistenceUnit;

    @BeforeAll
    public static void beforeAll() {
        List<ParsedPersistenceXmlDescriptor> persistenceUnits = PersistenceXmlParser
                .locatePersistenceUnits(new Properties());

        persistenceUnit = persistenceUnits.stream()
                .filter(unit -> unit.getName().equals("TuttiFrutti"))
                .findAny().get();
    }

    @Test
    @Order(1)
    @DisplayName("Persistence unit has a proper name")
    void persistenceUnit() {
        assertThat(persistenceUnit).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("Connection URL has proper value")
    void connectionUrl() {
        Properties properties = persistenceUnit.getProperties();

        assertThat(properties)
                .containsKey("hibernate.connection.url")
                .containsValue("jdbc:h2:mem:tutti_frutti_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false");
    }

    @Test
    @Order(3)
    @DisplayName("Connection driver has proper value")
    void driverClass() {
        Properties properties = persistenceUnit.getProperties();

        assertThat(properties)
                .containsKey("hibernate.connection.driver_class")
                .containsValue("org.h2.Driver");
    }

    @Test
    @Order(4)
    @DisplayName("Username has proper value")
    void username() {
        Properties properties = persistenceUnit.getProperties();

        assertThat(properties)
                .containsKey("hibernate.connection.username")
                .containsValue("little_richard");
    }

    @Test
    @Order(5)
    @DisplayName("Password has proper value")
    void password() {
        Properties properties = persistenceUnit.getProperties();

        assertThat(properties)
                .containsKey("hibernate.connection.password")
                .containsValue("rock_n_roll_is_alive");
    }

    @Test
    @Order(6)
    @DisplayName("Dialect has proper value")
    void dialect() {
        Properties properties = persistenceUnit.getProperties();

        assertThat(properties)
                .containsKey("hibernate.dialect")
                .containsValue("org.hibernate.dialect.H2Dialect");
    }

    @Test
    @Order(7)
    @DisplayName("DDL generation and database creation are configured")
    void ddlAndDatabaseCreation() {
        Properties properties = persistenceUnit.getProperties();

        assertThat(properties)
                .containsKey("hibernate.hbm2ddl.auto")
                .containsValue("create");
    }
}
