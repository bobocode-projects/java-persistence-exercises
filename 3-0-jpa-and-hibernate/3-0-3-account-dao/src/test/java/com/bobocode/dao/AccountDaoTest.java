package com.bobocode.dao;

import com.bobocode.exception.AccountDaoException;
import com.bobocode.model.Account;
import com.bobocode.util.TestDataGenerator;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


class AccountDaoTest {
    private static EntityManagerFactory emf;
    private static AccountDao accountDao;

    @BeforeAll
    static void init() {
        emf = Persistence.createEntityManagerFactory("SingleAccountEntityH2");
        accountDao = new AccountDaoImpl(emf);
    }

    @AfterAll
    static void destroy() {
        emf.close();
    }

    @Test
    void testSaveAccount() {
        Account account = TestDataGenerator.generateAccount();

        accountDao.save(account);
        boolean saved = isSaved(account);

        assertThat(account.getId(), notNullValue());
        assertThat(saved, is(true));

    }

    private void saveTestAccount(Account account) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.unwrap(Session.class).doWork(connection -> {
            String insertSql = "INSERT INTO account(first_name, last_name, email, birthday, gender, creation_time, balance) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                insertStatement.setString(1, account.getFirstName());
                insertStatement.setString(2, account.getLastName());
                insertStatement.setString(3, account.getEmail());
                insertStatement.setDate(4, Date.valueOf(account.getBirthday()));
                insertStatement.setString(5, account.getGender().name());
                insertStatement.setTimestamp(6, Timestamp.valueOf(account.getCreationTime()));
                insertStatement.setBigDecimal(7, account.getBalance().setScale(2));
                insertStatement.executeUpdate();
                ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                generatedKeys.next();
                long id = generatedKeys.getLong(1);
                account.setId(id);
            }
        });
        entityManager.close();
    }

    private boolean isSaved(Account account) {
        EntityManager entityManager = emf.createEntityManager();
        boolean isSaved = entityManager.unwrap(Session.class).doReturningWork(connection -> {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM account WHERE id = ?");
            statement.setLong(1, account.getId());
            return statement.executeQuery().next();
        });
        entityManager.close();
        return isSaved;
    }

    @Test
    void testFindAccountById() {
        Account account = TestDataGenerator.generateAccount();
        saveTestAccount(account);

        Account foundAccount = accountDao.findById(account.getId());

        assertEquals(account, foundAccount);
    }

    @Test
    void testFindAccountByEmail() {
        Account account = TestDataGenerator.generateAccount();
        saveTestAccount(account);

        Account foundAccount = accountDao.findByEmail(account.getEmail());

        assertEquals(account, foundAccount);
    }

    @Test
    void testFindAllAccounts() {
        List<Account> accounts = TestDataGenerator.generateAccountList(3);
        accounts.forEach(this::saveTestAccount);

        List<Account> foundAccounts = accountDao.findAll();

        assertThat(accounts, everyItem(isIn(foundAccounts)));
    }

    @Test
    void testUpdateAccount() {
        Account account = TestDataGenerator.generateAccount();
        saveTestAccount(account);

        account.setBalance(account.getBalance().add(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP)));
        accountDao.update(account);
        boolean balanceUpdated = isBalanceUpdated(account);

        assertThat(balanceUpdated, is(true));
    }

    private boolean isBalanceUpdated(Account account) {
        EntityManager entityManager = emf.createEntityManager();
        boolean isUpdated = entityManager.unwrap(Session.class).doReturningWork(connection -> {
            PreparedStatement statement = connection.prepareStatement("SELECT balance = ? FROM account WHERE id = ?");
            statement.setBigDecimal(1, account.getBalance());
            statement.setLong(2, account.getId());
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getBoolean(1);
        });
        entityManager.close();
        return isUpdated;
    }

    @Test
    void testRemoveAccount() {
        Account account = TestDataGenerator.generateAccount();
        saveTestAccount(account);

        accountDao.remove(account);
        boolean saved = isSaved(account);

        assertThat(saved, is(false));
    }

    @Test
    void testSaveInvalidAccount() {
        try {
            Account invalidAccount = TestDataGenerator.generateAccount();
            invalidAccount.setEmail(null);
            accountDao.save(invalidAccount);
            fail("AccountDaoException should be thrown");
        } catch (Exception e) {
            assertEquals(AccountDaoException.class, e.getClass());
        }
    }

    @Test
    void testUpdateInvalidAccount() {
        try {
            Account account = TestDataGenerator.generateAccount();
            saveTestAccount(account);

            account.setFirstName(null);
            accountDao.update(account);

            fail("AccountDaoException should be thrown");
        } catch (Exception e) {
            assertEquals(AccountDaoException.class, e.getClass());
        }
    }

}
