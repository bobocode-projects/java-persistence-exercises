package com.bobocode.dao;

import com.bobocode.model.Product;
import com.bobocode.util.ExerciseNotCompletedException;
import java.util.List;
import javax.sql.DataSource;

public class ProductDaoImpl implements ProductDao {

    private final DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {
        throw new ExerciseNotCompletedException();// todo
    }

    @Override
    public List<Product> findAll() {
        throw new ExerciseNotCompletedException();// todo
    }

    @Override
    public Product findOne(Long id) {
        throw new ExerciseNotCompletedException();// todo
    }

    @Override
    public void update(Product product) {
        throw new ExerciseNotCompletedException();// todo
    }

    @Override
    public void remove(Product product) {
        throw new ExerciseNotCompletedException();// todo
    }

}
