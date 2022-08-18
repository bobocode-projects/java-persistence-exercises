package com.bobocode;

import com.bobocode.dao.ProductDao;
import com.bobocode.dao.ProductDaoImpl;
import com.bobocode.exception.DaoOperationException;
import com.bobocode.model.Product;
import com.bobocode.util.JdbcUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductDaoTest {
    private static ProductDao productDao;
    private static DataSource spyDataSource;
    private static DataSource originalDataSource;

    @BeforeAll
    static void init() {
        originalDataSource = initializeDataSource();
        spyDataSource = Mockito.spy(originalDataSource);
        productDao = new ProductDaoImpl(spyDataSource);
    }

    @AfterEach
    @SneakyThrows
    void clearDb() {
        try (var connection = originalDataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("delete from products;");
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("save generates product id")
    void saveGeneratesId() {
        var product = createTestProduct();
        assertThat(product.getId()).isNull();

        productDao.save(product);

        assertThat(product.getId()).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("save stores a product to the DB")
    void save() {
        var product = createTestProduct();

        productDao.save(product);
        List<Product> products = findAllFromDataBase();

        assertThat(products).contains(product);
    }

    @Test
    @Order(3)
    @DisplayName("save throws an exception when product is not invalid")
    void saveThrowsException() {
        Product invalidTestProduct = createInvalidTestProduct();

        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.save(invalidTestProduct))
                .withMessage(String.format("Error saving product: %s", invalidTestProduct));
    }

    @Test
    @Order(4)
    @DisplayName("save wraps DB errors with a custom exception")
    @SneakyThrows
    void saveWrapsSqlException() {
        givenDatabaseError();
        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.save(new Product()));
    }

    @Test
    @Order(5)
    @DisplayName("findAll loads all products from the DB")
    void findAll() {
        List<Product> products = givenStoredProducts();

        List<Product> foundProducts = productDao.findAll();

        assertThat(foundProducts).isEqualTo(products);
    }

    @Test
    @SneakyThrows
    @Order(6)
    @DisplayName("findAll wraps DB errors with a custom exception")
    void findAllWrapsSqlExceptions() {
        givenDatabaseError();
        assertThatExceptionOfType(DaoOperationException.class).isThrownBy(() -> productDao.findAll());
    }

    @Test
    @Order(7)
    @DisplayName("findOne loads a product by id")
    void findById() {
        Product testProduct = generateTestProduct();
        saveToDB(testProduct);

        Product product = productDao.findOne(testProduct.getId());

        assertThat(testProduct).isEqualTo(product);
        assertThat(testProduct.getName()).isEqualTo(product.getName());
        assertThat(testProduct.getProducer()).isEqualTo(product.getProducer());
        assertThat(testProduct.getPrice().setScale(2)).isEqualTo(product.getPrice().setScale(2));
        assertThat(testProduct.getExpirationDate()).isEqualTo(product.getExpirationDate());
    }

    @Test
    @Order(8)
    @DisplayName("findOne throws an exception when the product is not found")
    void findOneThrowsExceptionWhenNotFound() {
        long productId = 666L;

        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.findOne(productId))
                .withMessage(String.format("Product with id = %d does not exist", productId));
    }

    @Test
    @Order(9)
    @DisplayName("findOne wraps DB errors with a custom exception")
    @SneakyThrows
    void findOneWrapsSqlExceptions() {
        givenDatabaseError();
        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.findOne(1L));
    }

    @Test
    @Order(10)
    @DisplayName("update changes the product in the DB")
    void update() {
        Product testProduct = generateTestProduct();
        saveToDB(testProduct);
        List<Product> productsBeforeUpdate = findAllFromDataBase();
        testProduct.setName("Updated name");
        testProduct.setProducer("Updated producer");
        testProduct.setPrice(BigDecimal.valueOf(666));
        testProduct.setExpirationDate(LocalDate.of(2020, Month.JANUARY, 1));

        productDao.update(testProduct);
        List<Product> products = findAllFromDataBase();
        Product updatedProduct = findOneFromDatabase(testProduct.getId());

        assertThat(productsBeforeUpdate.size()).isEqualTo(products.size());
        RecursiveComparisonConfiguration recursiveComparisonConfiguration = new RecursiveComparisonConfiguration();
        recursiveComparisonConfiguration.setIgnoreAllActualNullFields(true);
        assertThat(testProduct).usingRecursiveComparison(recursiveComparisonConfiguration).isEqualTo(updatedProduct);
        productsBeforeUpdate.remove(testProduct);
        products.remove(testProduct);
        assertThat(productsBeforeUpdate).usingRecursiveComparison().isEqualTo(products);
    }

    @Test
    @Order(11)
    @DisplayName("update throws an exception when a product ID is null")
    void updateNotStored() {
        Product notStoredProduct = generateTestProduct();

        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.update(notStoredProduct))
                .withMessage("Product id cannot be null");
    }

    @Test
    @Order(12)
    @DisplayName("update wraps DB errors with a custom exception")
    @SneakyThrows
    void updateWrapsSqlExceptions() {
        givenDatabaseError();
        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.update(new Product()));
    }

    @Test
    @Order(13)
    @DisplayName("remove deletes the product by id from the DB")
    void remove() {
        var product = givenStoredProduct();

        productDao.remove(product);
        List<Product> allProducts = findAllFromDataBase();

        assertThat(allProducts).doesNotContain(product);
    }
    
    private Product givenStoredProduct(){
        Product product = generateTestProduct();
        saveToDB(product);
        return product;
    }

    @Test
    @Order(14)
    @DisplayName("remove throws an exception when a product ID is null")
    void removeNotStored() {
        Product notStoredProduct = generateTestProduct();

        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.remove(notStoredProduct))
                .withMessage("Product id cannot be null");
    }

    @Test
    @Order(15)
    @DisplayName("remove wraps DB errors with a custom exception")
    @SneakyThrows
    void removeWrapsSqlExceptions() {
        givenDatabaseError();
        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.remove(new Product()));
    }

    private static DataSource initializeDataSource() {
        var dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:demo;INIT=runscript from 'classpath:db/init.sql'");
        dataSource.setUser("sa");
        return dataSource;
    }

    private Product generateTestProduct() {
        return Product.builder()
                .name(RandomStringUtils.randomAlphabetic(10))
                .producer(RandomStringUtils.randomAlphabetic(20))
                .price(BigDecimal.valueOf(RandomUtils.nextInt(10, 100)))
                .expirationDate(LocalDate.ofYearDay(
                        LocalDate.now().getYear() + RandomUtils.nextInt(1, 5),
                        RandomUtils.nextInt(1, 365))
                )
                .build();
    }

    private List<Product> givenStoredProducts() {
        List<Product> products = createTestProductList();
        products.forEach(this::saveToDB);
        return products;
    }

    private void givenDatabaseError() throws SQLException {
        doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
    }

    private List<Product> findAllFromDataBase() {
        try (Connection connection = originalDataSource.getConnection()) {
            return findAllProducts(connection);
        } catch (SQLException e) {
            throw new DaoOperationException("Error finding all products", e);
        }
    }

    private List<Product> findAllProducts(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM products;");
        return collectToList(resultSet);
    }

    private List<Product> collectToList(ResultSet resultSet) throws SQLException {
        List<Product> products = new ArrayList<>();
        while (resultSet.next()) {
            Product product = parseRow(resultSet);
            products.add(product);
        }
        return products;
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

    private Product createTestProduct() {
        return Product.builder()
                .name("Fanta")
                .producer("The Coca-Cola Company")
                .price(BigDecimal.valueOf(22))
                .expirationDate(LocalDate.of(2020, Month.APRIL, 14)).build();
    }

    private Product createInvalidTestProduct() {
        return Product.builder()
                .name("INVALID")
                .price(BigDecimal.valueOf(22))
                .expirationDate(LocalDate.of(2020, Month.APRIL, 14)).build();
    }

    private void saveToDB(Product product) {
        Objects.requireNonNull(product);
        try (Connection connection = originalDataSource.getConnection()) {
            saveProduct(product, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error saving product: %s " + e.getMessage(), product), e);
        }
    }

    private void saveProduct(Product product, Connection connection) throws SQLException {
        PreparedStatement insertStatement = prepareInsertStatement(product, connection);
        insertStatement.executeUpdate();
        Long id = fetchGeneratedId(insertStatement);
        product.setId(id);
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

    private List<Product> createTestProductList() {
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

    private Product findOneFromDatabase(Long id) {
        Objects.requireNonNull(id);
        try (Connection connection = originalDataSource.getConnection()) {
            return findProductById(id, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error finding product by id = %d", id), e);
        }
    }

    private Product findProductById(Long id, Connection connection) throws SQLException {
        PreparedStatement selectByIdStatement = prepareSelectByIdStatement(id, connection);
        ResultSet resultSet = selectByIdStatement.executeQuery();
        if (resultSet.next()) {
            return parseRow(resultSet);
        } else {
            throw new DaoOperationException(String.format("Product with id = %d does not exist", id));
        }
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
}