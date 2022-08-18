package com.bobocode.dao;

import com.bobocode.exception.DaoOperationException;
import com.bobocode.model.Product;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class ProductDaoImpl implements ProductDao {
    private DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {
        try (var connection = dataSource.getConnection()) {
            try (var insertStatement = connection.prepareStatement(
                    "INSERT INTO products(name, producer, price, expiration_date) VALUES(?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS)
            ) {
                insertStatement.setString(1, product.getName());
                insertStatement.setString(2, product.getProducer());
                insertStatement.setBigDecimal(3, product.getPrice());
                insertStatement.setDate(4, Date.valueOf(product.getExpirationDate()));
                try {
                    insertStatement.executeUpdate();
                } catch (SQLException e) {
                    throw new DaoOperationException("Error saving product: " + product, e);
                }

                var generatedIdResultSet = insertStatement.getGeneratedKeys();
                generatedIdResultSet.next();
                var id = generatedIdResultSet.getLong(1);
                product.setId(id);
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Error saving product " + product, e);
        }
    }

    @Override
    public List<Product> findAll() {
        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                var productList = new ArrayList<Product>();
                var rs = statement.executeQuery("select * from products");
                while (rs.next()) {
                    var product = createProductFromResultSet(rs);
                    productList.add(product);
                }
                return productList;
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Error selecting all products", e);
        }
    }

    @SneakyThrows
    private Product createProductFromResultSet(ResultSet rs) {
        var product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setProducer(rs.getString("producer"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setExpirationDate(rs.getDate("expiration_date").toLocalDate());
        product.setCreationTime(rs.getTimestamp("creation_time").toLocalDateTime());
        return product;
    }

    @Override
    public Product findOne(Long id) {
        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                var rs = statement.executeQuery("select * from products where id = " + id);
                if (rs.next()) {
                    return createProductFromResultSet(rs);
                } else {
                    throw new DaoOperationException("Product with id = " + id + " does not exist");
                }
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Error selecting product by id = " + id, e);
        }
    }

    @Override
    public void update(Product product) {
        verifyProductId(product);
        try (var connection = dataSource.getConnection()) {
            try (var updateStatement = connection.prepareStatement("UPDATE products SET name = ?, producer = ?, price = ?, expiration_date = ? WHERE id = ?")) {
                updateStatement.setString(1, product.getName());
                updateStatement.setString(2, product.getProducer());
                updateStatement.setBigDecimal(3, product.getPrice());
                updateStatement.setDate(4, Date.valueOf(product.getExpirationDate()));
                updateStatement.setLong(5, product.getId());

                updateStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Error updating product " + product, e);
        }
    }

    private static void verifyProductId(Product product) {
        if (product.getId() == null) {
            throw new DaoOperationException("Product id cannot be null");
        }
    }

    @Override
    public void remove(Product product) {
        verifyProductId(product);
        try (var connection = dataSource.getConnection()) {
            try (var deleteStatement = connection.prepareStatement("delete from products where id = ?")) {
                deleteStatement.setLong(1, product.getId());
                deleteStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Error removing product by id = " + product.getId(), e);
        }
    }

}
