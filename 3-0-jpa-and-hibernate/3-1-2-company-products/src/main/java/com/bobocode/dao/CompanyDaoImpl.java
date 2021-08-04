package com.bobocode.dao;

import com.bobocode.exception.CompanyDaoException;
import com.bobocode.model.Company;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.function.Function;

public class CompanyDaoImpl implements CompanyDao {
    private EntityManagerFactory entityManagerFactory;

    public CompanyDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Company findByIdFetchProducts(Long id) {
        return readWithinTx(entityManager ->
                entityManager
                        .createQuery("select c from Company c join fetch c.products where c.id = :id", Company.class)
                        .setParameter("id", id)
                        .getSingleResult()
        );
    }

    private <T> T readWithinTx(Function<EntityManager, T> entityManagerFunction) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.unwrap(Session.class).setDefaultReadOnly(true);
        entityManager.getTransaction().begin();
        try {
            T queryResult = entityManagerFunction.apply(entityManager);
            entityManager.getTransaction().commit();
            return queryResult;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new CompanyDaoException("Error performing read operation", e);
        } finally {
            entityManager.close();
        }
    }
}

