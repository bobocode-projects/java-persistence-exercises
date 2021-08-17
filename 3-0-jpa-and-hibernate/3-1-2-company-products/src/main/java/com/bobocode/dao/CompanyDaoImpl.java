package com.bobocode.dao;

import com.bobocode.model.Company;
import com.bobocode.util.ExerciseNotCompletedException;

import javax.persistence.EntityManagerFactory;

public class CompanyDaoImpl implements CompanyDao {
    private EntityManagerFactory entityManagerFactory;

    public CompanyDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Company findByIdFetchProducts(Long id) {
        throw new ExerciseNotCompletedException(); // todo
    }
}
