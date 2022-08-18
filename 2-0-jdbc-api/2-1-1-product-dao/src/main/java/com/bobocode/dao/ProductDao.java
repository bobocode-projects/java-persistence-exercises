package com.bobocode.dao;

import com.bobocode.exception.DaoOperationException;
import com.bobocode.model.Product;

import java.util.List;

/**
 * {@link ProductDao} is a Data Access Object pattern (DAO) that encapsulates all database access and manipulation logic.
 * It provides a convenient API that allow to store, access, update and remove data working with object OO style.
 */
public interface ProductDao {
    /**
     * Stores a new product into the database. Sets generated id to the {@link Product} instance
     *
     * @param product new product
     * @throws DaoOperationException with massage: "Error saving product: {@link Product} instance"
     */
    void save(Product product);

    /**
     * Retrieves and returns all products from the database
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
