package com.bobocode;

import com.bobocode.exception.QueryHelperException;
import com.bobocode.model.Account;
import com.bobocode.util.EntityManagerUtil;
import com.bobocode.util.TestDataGenerator;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QueryHelperTest {
    private static EntityManagerFactory entityManagerFactory;
    private static EntityManagerUtil emUtil;
    private static QueryHelper queryHelper;

    @BeforeAll
    public static void setup() {
        entityManagerFactory = Persistence.createEntityManagerFactory("Account");
        emUtil = new EntityManagerUtil(entityManagerFactory);
        queryHelper = new QueryHelper(entityManagerFactory);
    }

    @AfterAll
    static void destroy() {
        entityManagerFactory.close();
    }

    @Test
    @Order(1)
    @DisplayName("Query helper returns a result")
    public void queryHelperReturnsResult() {
        Account account = saveRandomAccount();
        Long accountId = account.getId();

        Account foundAccount = queryHelper.readWithinTx(entityManager -> entityManager.find(Account.class, accountId));

        assertThat(foundAccount).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("Query helper uses \"Read Only\"")
    public void queryHelperUsesReadOnly() {
        Account account = saveRandomAccount();
        Long accountId = account.getId();

        tryToUpdateFirstName(accountId);
        Account foundAccount = queryHelper.readWithinTx(entityManager -> entityManager.find(Account.class, accountId));

        assertThat(foundAccount.getFirstName()).isNotEqualTo("XXX");
        assertThat(foundAccount.getFirstName()).isEqualTo(account.getFirstName());
    }

    @Test
    @Order(3)
    @DisplayName("Query helper throws exception")
    public void queryHelperThrowsException() {
        Account account = TestDataGenerator.generateAccount();
        emUtil.performWithinTx(entityManager -> entityManager.persist(account));
        try {
            queryHelper.readWithinTx(entityManager -> {
                Account managedAccount = entityManager.find(Account.class, account.getId());
                throwException();
                return managedAccount;
            });
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e.getClass()).isEqualTo(QueryHelperException.class);
            assertThat(e.getMessage()).contains("Transaction is rolled back");
        }
    }

    private Account saveRandomAccount() {
        Account account = TestDataGenerator.generateAccount();
        emUtil.performWithinTx(entityManager -> entityManager.persist(account));
        return account;
    }

    private void tryToUpdateFirstName(Long accountId) {
        queryHelper.readWithinTx(entityManager -> {
            Account managedAccount = entityManager.find(Account.class, accountId);
            managedAccount.setFirstName("XXX");
            return managedAccount;
        });
    }

    private void throwException() {
        throw new RuntimeException("Runtime error");
    }
}
