package com.bobocode;

import com.bobocode.domain.Note;
import com.bobocode.domain.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;
import java.util.function.Function;

public class Task2DemoApp {
    private static EntityManagerFactory emf;

    public static void main(String[] args) {
        emf = Persistence.createEntityManagerFactory("default");

        // Transactional write-behind cache
        // Performs write operation asynchronously in a specific order 
        // 1. OPERATION REORDERING
        // 2. INSERT/UPDATE BATCHING
        var start = System.nanoTime();
        doInTx(em -> {
            var person = em.find(Person.class, 230076);

            for (int i = 0; i < 1000; i++) {
                var newNote = new Note();
                newNote.setTitle("Task2 Note " + i);
                newNote.setBody("Test Note" + i);
                newNote.setPerson(person);

                em.persist(newNote);
            }
        });
        System.out.println(((System.nanoTime() - start) / 1000_000) + "ms");

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

    private static <T> T doInTxReturning(Function<EntityManager, T> entityManagerFunction) {
        var em = emf.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            var result = entityManagerFunction.apply(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }


}

