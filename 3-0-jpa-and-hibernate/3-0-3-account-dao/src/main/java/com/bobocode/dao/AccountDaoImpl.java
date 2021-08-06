package com.bobocode.dao;

import com.bobocode.model.Account;
import com.bobocode.util.ExerciseNotCompletedException;

import javax.persistence.EntityManagerFactory;
import java.util.List;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {
        throw new ExerciseNotCompletedException(); // todo
    }

    @Override
    public Account findById(Long id) {
        throw new ExerciseNotCompletedException(); // todo
    }

    @Override
    public Account findByEmail(String email) {
        throw new ExerciseNotCompletedException(); // todo
    }

    @Override
    public List<Account> findAll() {
        throw new ExerciseNotCompletedException(); // todo
    }

    @Override
    public void update(Account account) {
        throw new ExerciseNotCompletedException(); // todo
    }

    @Override
    public void remove(Account account) {
        throw new ExerciseNotCompletedException(); // todo
    }
}

