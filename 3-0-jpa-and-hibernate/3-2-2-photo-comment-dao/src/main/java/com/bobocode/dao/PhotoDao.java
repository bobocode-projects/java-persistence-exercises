package com.bobocode.dao;

import com.bobocode.model.Photo;

import java.util.List;

/**
 * {@link PhotoDao} defines and API of Data-Access Object for entity {@link Photo}
 */
public interface PhotoDao {

    /**
     * Saves photo into db and sets an id
     *
     * @param photo new photo
     */
    void save(Photo photo);

    /**
     * Retrieves a photo from the database by its id
     *
     * @param id photo id
     * @return photo instance
     */
    Photo findById(long id);

    /**
     * Returns a list of all stored photos
     *
     * @return list of stored photos
     */
    List<Photo> findAll();

    /**
     * Removes a photo from the database
     *
     * @param photo an instance of stored photo
     */
    void remove(Photo photo);

    /**
     * Adds a new comment to an existing photo. This method does not require additional SQL select methods to load
     * {@link Photo}.
     *
     * @param photoId
     * @param comment
     */
    void addComment(long photoId, String comment);
}
