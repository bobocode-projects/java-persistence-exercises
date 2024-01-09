package com.bobocode;

import com.bobocode.domain.Note;
import com.bobocode.domain.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;

public class DemoApp {
    private static EntityManagerFactory emf;
    public static void main(String[] args) {
        emf = Persistence.createEntityManagerFactory("default");
        var noteTitle = "A strange note 3";
        
        doInTx(em ->{
            var newNote = new Note();
            newNote.setTitle(noteTitle);
            newNote.setBody("I don't know who is my owner");

            var mariana = em.find(Person.class, 230095);
//            newNote.setPerson(mariana);
            mariana.getNotes().add(newNote);
            
//            var serhii = em.find(Person.class, 230096);
//            serhii.getNotes().add(newNote);
//            serhii.addNote(newNote);
            
        });
        
        doInTx(em -> {
            var note = em.createQuery("select n from Note n where n.title = ?1", Note.class)
                    .setParameter(1, noteTitle)
                    .getSingleResult();
//            System.out.println(note.getPerson().getFirstName());
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
