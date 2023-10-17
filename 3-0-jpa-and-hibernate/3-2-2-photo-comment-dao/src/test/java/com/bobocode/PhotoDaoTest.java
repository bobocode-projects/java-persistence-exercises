package com.bobocode;

import static com.bobocode.util.PhotoTestDataGenerator.createListOfRandomPhotos;
import static com.bobocode.util.PhotoTestDataGenerator.createRandomPhoto;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bobocode.dao.PhotoDao;
import com.bobocode.dao.PhotoDaoImpl;
import com.bobocode.model.Photo;
import com.bobocode.util.EntityManagerUtil;
import java.util.List;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PhotoDaoTest {

    private EntityManagerUtil emUtil;
    private PhotoDao photoDao;
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setup() {
        entityManagerFactory = Persistence.createEntityManagerFactory("PhotoComments");
        emUtil = new EntityManagerUtil(entityManagerFactory);
        photoDao = new PhotoDaoImpl(entityManagerFactory);
    }

    @AfterEach
    void destroy() {
        entityManagerFactory.close();
    }

    @Test
    @Order(1)
    @DisplayName("Save a photo")
    void savePhoto() {
        Photo photo = createRandomPhoto();

        photoDao.save(photo);

        Photo fountPhoto = emUtil.performReturningWithinTx(entityManager -> entityManager.find(Photo.class, photo.getId()));
        assertThat(fountPhoto).isEqualTo(photo);
    }

    @Test
    @Order(3)
    @DisplayName("Find a photo by Id")
    void findPhotoById() {
        Photo photo = createRandomPhoto();
        emUtil.performWithinTx(entityManager -> entityManager.persist(photo));

        Photo foundPhoto = photoDao.findById(photo.getId());

        assertThat(foundPhoto).isEqualTo(photo);
    }

    @Test
    @Order(3)
    @DisplayName("Find all photos")
    void findAllPhotos() {
        List<Photo> listOfRandomPhotos = createListOfRandomPhotos(5);
        emUtil.performWithinTx(entityManager -> listOfRandomPhotos.forEach(entityManager::persist));

        List<Photo> foundPhotos = photoDao.findAll();

        assertThat(foundPhotos).containsExactlyInAnyOrderElementsOf(listOfRandomPhotos);
    }

    @Test
    @Order(4)
    @DisplayName("Remove a photo")
    void removePhoto() {
        Photo photo = createRandomPhoto();
        emUtil.performWithinTx(entityManager -> entityManager.persist(photo));

        photoDao.remove(photo);

        Photo removedPhoto = emUtil.performReturningWithinTx(entityManager -> entityManager.find(Photo.class, photo.getId()));
        assertThat(removedPhoto).isNull();
    }

    @Test
    @Order(5)
    @DisplayName("Add a photo comment")
    void addPhotoComment() {
        Photo photo = createRandomPhoto();
        emUtil.performWithinTx(entityManager -> entityManager.persist(photo));

        photoDao.addComment(photo.getId(), "Nice picture!");

        emUtil.performWithinTx(entityManager -> {
            Photo managedPhoto = entityManager.find(Photo.class, photo.getId());
            assertThat(managedPhoto.getComments()).extracting("text").contains("Nice picture!");
        });
    }
}
