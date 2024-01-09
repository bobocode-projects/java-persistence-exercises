package com.bobocode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;

public class Task1DemoApp {
    private static EntityManagerFactory emf;

    public static void main(String[] args) {
        emf = Persistence.createEntityManagerFactory("default");

        doInTx(em -> {

        });


        emf.close();
    }

    private static void doInTx(Consumer<EntityManager> entityManagerConsumer) {
        var em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            entityManagerConsumer.accept(em);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
