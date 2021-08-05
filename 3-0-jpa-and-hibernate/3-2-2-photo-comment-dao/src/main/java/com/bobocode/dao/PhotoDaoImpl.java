package com.bobocode.dao;

import com.bobocode.model.Photo;
import com.bobocode.model.PhotoComment;
import com.bobocode.util.EntityManagerUtil;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Objects;

public class PhotoDaoImpl implements PhotoDao {
    private EntityManagerUtil emUtil;

    public PhotoDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.emUtil = new EntityManagerUtil(entityManagerFactory);
    }

    @Override
    public void save(Photo photo) {
        Objects.requireNonNull(photo);
        emUtil.performWithinTx(entityManager -> entityManager.persist(photo));
    }

    @Override
    public Photo findById(long id) {
        return emUtil.performReturningWithinTx(entityManager -> entityManager.find(Photo.class, id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Photo> findAll() {
        return emUtil.performReturningWithinTx(
                entityManager -> entityManager.createQuery("select p from Photo p").getResultList()
        );
    }

    @Override
    public void remove(Photo photo) {
        Objects.requireNonNull(photo);
        emUtil.performWithinTx(entityManager -> {
            Photo managedPhoto = entityManager.merge(photo);
            entityManager.remove(managedPhoto);
        });
    }

    @Override
    public void addComment(long photoId, String comment) {
        emUtil.performWithinTx(entityManager -> {
            Photo photoReference = entityManager.getReference(Photo.class, photoId);// does not call database
            PhotoComment photoComment = new PhotoComment(comment, photoReference);
            entityManager.persist(photoComment);
        });
    }
}
