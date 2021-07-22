package com.bobocode;

import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JpaPersistenceUnitTest {
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
    public void persistenceUnit() {
        assertThat(persistenceUnit).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("Connection URL has proper value")
    public void connectionUrl() {
        Properties properties = persistenceUnit.getProperties();

        assertThat(properties.containsKey("hibernate.connection.url")).isTrue();
        assertThat(properties
                .containsValue("jdbc:h2:mem:tutti_frutti_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false")).isTrue();
    }

    @Test
    @Order(3)
    @DisplayName("Connection driver has proper value")
    public void driverClass() {
        Properties properties = persistenceUnit.getProperties();

        assertThat(properties.containsKey("hibernate.connection.driver_class")).isTrue();
        assertThat(properties.containsValue("org.h2.Driver")).isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("Username has proper value")
    public void username() {
        Properties properties = persistenceUnit.getProperties();

        assertThat(properties.containsKey("hibernate.connection.username")).isTrue();
        assertThat(properties.containsValue("little_richard")).isTrue();
    }

    @Test
    @Order(5)
    @DisplayName("Password has proper value")
    public void password() {
        Properties properties = persistenceUnit.getProperties();

        assertThat(properties.containsKey("hibernate.connection.password")).isTrue();
        assertThat(properties.containsValue("rock_n_roll_is_alive")).isTrue();
    }

    @Test
    @Order(6)
    @DisplayName("Dialect has proper value")
    public void dialect() {
        Properties properties = persistenceUnit.getProperties();

        assertThat(properties.containsKey("hibernate.dialect")).isTrue();
        assertThat(properties.containsValue("org.hibernate.dialect.H2Dialect")).isTrue();
    }

    @Test
    @Order(7)
    @DisplayName("DDL generation and database creation are configured")
    public void ddlAndDatabaseCreation() {
        Properties properties = persistenceUnit.getProperties();

        assertThat(properties.containsKey("hibernate.hbm2ddl.auto")).isTrue();
        assertThat(properties.containsValue("create")).isTrue();
    }
}
