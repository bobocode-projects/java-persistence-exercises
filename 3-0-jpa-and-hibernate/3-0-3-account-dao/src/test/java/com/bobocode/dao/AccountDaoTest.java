package com.bobocode.dao;

import com.bobocode.exception.AccountDaoException;
import com.bobocode.model.Account;
import com.bobocode.util.TestDataGenerator;
import org.hibernate.Session;
import org.junit.jupiter.api.*;

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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    @Order(1)
    @DisplayName("Save account")
    void saveAccount() {
        Account account = TestDataGenerator.generateAccount();

        accountDao.save(account);
        boolean saved = isSaved(account);

        assertThat(account.getId()).isNotNull();
        assertThat(saved).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("Save throws exception when account is invalid")
    void testSaveInvalidAccount() {
        Account invalidAccount = TestDataGenerator.generateAccount();
        invalidAccount.setEmail(null);
        assertThatThrownBy(() -> {
            accountDao.save(invalidAccount);
        }).isInstanceOf(AccountDaoException.class);
    }

    @Test
    @Order(3)
    @DisplayName("Find account by Id")
    void testFindAccountById() {
        Account account = TestDataGenerator.generateAccount();
        saveTestAccount(account);

        Account foundAccount = accountDao.findById(account.getId());

        assertThat(account).isEqualTo(foundAccount);
    }

    @Test
    @Order(4)
    @DisplayName("Find account by email")
    void testFindAccountByEmail() {
        Account account = TestDataGenerator.generateAccount();
        saveTestAccount(account);

        Account foundAccount = accountDao.findByEmail(account.getEmail());

        assertThat(account).isEqualTo(foundAccount);
    }

    @Test
    @Order(5)
    @DisplayName("Find all accounts")
    void testFindAllAccounts() {
        List<Account> accounts = TestDataGenerator.generateAccountList(3);
        accounts.forEach(this::saveTestAccount);

        List<Account> foundAccounts = accountDao.findAll();

        assertThat(foundAccounts).containsAll(accounts);
    }

    @Test
    @Order(6)
    @DisplayName("Update accounts")
    void testUpdateAccount() {
        Account account = TestDataGenerator.generateAccount();
        saveTestAccount(account);

        account.setBalance(account.getBalance().add(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP)));
        accountDao.update(account);
        boolean balanceUpdated = isBalanceUpdated(account);

        assertThat(balanceUpdated).isTrue();
    }

    @Test
    @Order(7)
    @DisplayName("Update throws exception when account is invalid")
    void testUpdateInvalidAccount() {
        Account account = TestDataGenerator.generateAccount();
        saveTestAccount(account);
        account.setFirstName(null);
        assertThatThrownBy(() -> {
            accountDao.update(account);
        }).isInstanceOf(AccountDaoException.class);
    }

    @Test
    @Order(8)
    @DisplayName("Remove account")
    void testRemoveAccount() {
        Account account = TestDataGenerator.generateAccount();
        saveTestAccount(account);

        accountDao.remove(account);
        boolean saved = isSaved(account);

        assertThat(saved).isFalse();
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
}
