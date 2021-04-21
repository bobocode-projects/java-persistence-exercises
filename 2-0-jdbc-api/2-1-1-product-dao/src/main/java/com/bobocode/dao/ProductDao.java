package com.bobocode.dao;

import com.bobocode.model.Product;

import java.util.List;

/**
 * {@link ProductDao} is an Data Access Object pattern (DAO) that encapsulates all database access and manipulation logic.
 * It provides a convenient API that allow to store, access, update and remove data working with object OO style.
 */
public interface ProductDao {
    /**
     * Stores a new product into the database. Sets generated id to the {@link Product} instance
     *
     * @param product new product
     */
    void save(Product product);

    /**
     * Retrieves and returns all producrs from the database
     *
     * @return list of all products
     */
    List<Product> findAll();

    /**
     * Returns a product object by its id
     *
     * @param id product identifier (primary key)
     * @return one product by its id
     */
    Product findOne(Long id);

    /**
     * Updates existing product.
     *
     * @param product stored product with updated fields
     */
    void update(Product product);

    /**
     * Removes an existing product from the database
     *
     * @param product stored product
     */
    void remove(Product product);
}
