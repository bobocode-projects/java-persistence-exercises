package com.bobocode.dao;

import com.bobocode.model.Photo;
import com.bobocode.util.ExerciseNotCompletedException;

import javax.persistence.EntityManagerFactory;
import java.util.List;

/**
 * Please note that you should not use auto-commit mode for your implementation.
 */
public class PhotoDaoImpl implements PhotoDao {
    private EntityManagerFactory entityManagerFactory;

    public PhotoDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void save(Photo photo) {
        throw new ExerciseNotCompletedException(); // todo
    }

    @Override
    public Photo findById(long id) {
        throw new ExerciseNotCompletedException(); // todo
    }

    @Override
    public List<Photo> findAll() {
        throw new ExerciseNotCompletedException(); // todo
    }

    @Override
    public void remove(Photo photo) {
        throw new ExerciseNotCompletedException(); // todo
    }

    @Override
    public void addComment(long photoId, String comment) {
        throw new ExerciseNotCompletedException(); // todo
    }
}
