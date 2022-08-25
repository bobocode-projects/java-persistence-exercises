package com.bobocode;

import com.bobocode.exception.DaoOperationException;
import com.bobocode.model.Product;
import com.bobocode.util.FileReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

abstract class AbstractDaoTest {

    static void createTable(DataSource dataSource) throws SQLException {
        String createTablesSql = FileReader.readWholeFileFromResources("db/init.sql");

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute(createTablesSql);
            statement.close();
        }
    }

    Product findProductById(Long id, Connection connection) throws SQLException {
        PreparedStatement selectByIdStatement = prepareSelectByIdStatement(id, connection);
        ResultSet resultSet = selectByIdStatement.executeQuery();
        if (resultSet.next()) {
            return parseRow(resultSet);
        } else {
            throw new DaoOperationException(String.format("Product with id = %d does not exist", id));
        }
    }

    private Product parseRow(ResultSet resultSet) {
        try {
            return createFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot parse row to create product instance", e);
        }
    }

    private Product createFromResultSet(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setId(resultSet.getLong("id"));
        product.setName(resultSet.getString("name"));
        product.setProducer(resultSet.getString("producer"));
        product.setPrice(resultSet.getBigDecimal("price").stripTrailingZeros());
        product.setExpirationDate(resultSet.getDate("expiration_date").toLocalDate());
        product.setCreationTime(resultSet.getTimestamp("creation_time").toLocalDateTime());
        return product;
    }

    List<Product> collectToList(ResultSet resultSet) throws SQLException {
        List<Product> products = new ArrayList<>();
        while (resultSet.next()) {
            Product product = parseRow(resultSet);
            products.add(product);
        }
        return products;
    }

    void saveProduct(Product product, Connection connection) throws SQLException {
        PreparedStatement insertStatement = prepareInsertStatement(product, connection);
        insertStatement.executeUpdate();
        Long id = fetchGeneratedId(insertStatement);
        product.setId(id);
    }

    private PreparedStatement prepareSelectByIdStatement(Long id, Connection connection) {
        try {
            PreparedStatement selectByIdStatement = connection
                    .prepareStatement("SELECT * FROM products WHERE id = ?;");
            selectByIdStatement.setLong(1, id);
            return selectByIdStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot prepare select by id statement for id = %d", id), e);
        }
    }

    private PreparedStatement prepareInsertStatement(Product product, Connection connection) {
        try {
            PreparedStatement insertStatement = connection
                    .prepareStatement("INSERT INTO products(name, producer, price, expiration_date) VALUES (?, ?, ?, ?);",
                            PreparedStatement.RETURN_GENERATED_KEYS);
            fillProductStatement(product, insertStatement);
            return insertStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot prepare statement for product: %s", product), e);
        }
    }

    private void fillProductStatement(Product product, PreparedStatement updateStatement) throws SQLException {
        updateStatement.setString(1, product.getName());
        updateStatement.setString(2, product.getProducer());
        updateStatement.setBigDecimal(3, product.getPrice());
        updateStatement.setDate(4, Date.valueOf(product.getExpirationDate()));
    }

    private Long fetchGeneratedId(PreparedStatement insertStatement) throws SQLException {
        ResultSet generatedKeys = insertStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getLong(1);
        } else {
            throw new DaoOperationException("Can not obtain product ID");
        }
    }

    List<Product> createTestProductList() {
        return List.of(
                Product.builder()
                        .name("Sprite")
                        .producer("The Coca-Cola Company")
                        .price(BigDecimal.valueOf(18))
                        .expirationDate(LocalDate.of(2020, Month.MARCH, 24)).build(),
                Product.builder()
                        .name("Cola light")
                        .producer("The Coca-Cola Company")
                        .price(BigDecimal.valueOf(21))
                        .expirationDate(LocalDate.of(2020, Month.JANUARY, 11)).build(),
                Product.builder()
                        .name("Snickers")
                        .producer("Mars Inc.")
                        .price(BigDecimal.valueOf(16))
                        .expirationDate(LocalDate.of(2019, Month.DECEMBER, 3)).build()
        );
    }
}
