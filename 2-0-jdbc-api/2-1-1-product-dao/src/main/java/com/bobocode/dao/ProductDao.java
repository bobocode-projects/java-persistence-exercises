package com.bobocode.dao;

import com.bobocode.exception.DaoOperationException;
import com.bobocode.model.Product;

import java.util.List;

/**
 * {@link ProductDao} is a Data Access Object pattern (DAO) that encapsulates all database access and manipulation logic.
 * It provides a convenient API that allows to store, access, update and remove data working with object-oriented style.
 */
public interface ProductDao {

    /**
     * Stores a new product into the database. Sets the database-generated ID for to {@link Product} instance
     *
     * @param product new product
     * @throws DaoOperationException in case of database errors
     */
    void save(Product product);

    /**
     * Retrieves and returns all products from the database
     *
     * @return list of all products
     * @throws DaoOperationException in case of database errors
     */
    List<Product> findAll();

    /**
     * Returns a product object by provided id
     *
     * @param id product identifier (primary key)
     * @return single product by its id
     * @throws DaoOperationException in case of database errors
     */
    Product findOne(Long id);

    /**
     * Updates existing product.
     *
     * @param product stored product with updated fields
     * @throws DaoOperationException in case of database errors
     */
    void update(Product product);

    /**
     * Removes an existing product from the database
     *
     * @param product stored product
     * @throws DaoOperationException in case of database errors
     */
    void remove(Product product);
}
