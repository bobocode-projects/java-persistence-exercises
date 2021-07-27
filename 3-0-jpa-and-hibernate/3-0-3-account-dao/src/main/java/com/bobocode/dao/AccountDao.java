package com.bobocode.dao;

import com.bobocode.model.Account;

import java.util.List;

/**
 * {@link AccountDao} provides an API to access {@link Account} data.
 */
public interface AccountDao {
    /**
     * Receives a new instance of {@link Account} and stores it into database. Sets a generated id to account.
     *
     * @param account new instance of account
     */
    void save(Account account);

    /**
     * Returns an {@link Account} instance by its id
     *
     * @param id account id in the database
     * @return account instance
     */
    Account findById(Long id);

    /**
     * Returns {@link Account} instance by its email
     *
     * @param email account emails
     * @return account instance
     */
    Account findByEmail(String email);

    /**
     * Returns all accounts stored in the database.
     *
     * @return account list
     */
    List<Account> findAll();

    /**
     * Receives stored {@link Account} instance and updates it in the database
     *
     * @param account stored account with updated fields
     */
    void update(Account account);

    /**
     * Removes the stored account from the database.
     *
     * @param account stored account instance
     */
    void remove(Account account);
}
