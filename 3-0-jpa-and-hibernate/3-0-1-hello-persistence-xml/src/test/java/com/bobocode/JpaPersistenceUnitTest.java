package com.bobocode;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class JpaPersistenceUnitTest {

    private static EntityManagerFactory entityManagerFactory;

    @BeforeAll
    static void setup() {
        entityManagerFactory = Persistence.createEntityManagerFactory("TuttiFrutti");
    }

    @AfterAll
    static void destroy() {
        entityManagerFactory.close();
    }

    @Test
    public void testPersistenceEntity() {
        Metamodel metamodel = entityManagerFactory.getMetamodel();
        Set<EntityType<?>> entities = metamodel.getEntities();

        assertThat(entities, hasSize(1));
        assertThat(entities.iterator().next().getName(), is("Song"));
    }

    @Test
    public void testSqlDialect() {
        String hibernateSqlDialect = (String) entityManagerFactory.getProperties().get("hibernate.dialect");

        assertThat(hibernateSqlDialect, is("org.hibernate.dialect.H2Dialect"));
    }

    @Test
    public void testConnectionDriverClass() {
        String driverClass = (String) entityManagerFactory.getProperties().get("hibernate.connection.driver_class");

        assertThat(driverClass, is("org.h2.Driver"));
    }

    @Test
    public void testDbUrl() {
        String url = (String) entityManagerFactory.getProperties().get("hibernate.connection.url");

        assertThat(url, is("jdbc:h2:mem:tutti_frutti_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false"));
    }

    @Test
    public void testDdlGeneration() {
        String ddlGenerationStrategy = (String) entityManagerFactory.getProperties().get("hibernate.hbm2ddl.auto");

        assertThat(ddlGenerationStrategy, is("create"));
    }

}
