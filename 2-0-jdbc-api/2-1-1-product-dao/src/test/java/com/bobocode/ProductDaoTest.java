package com.bobocode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.doThrow;

import com.bobocode.dao.ProductDao;
import com.bobocode.dao.ProductDaoImpl;
import com.bobocode.exception.DaoOperationException;
import com.bobocode.model.Product;
import com.bobocode.util.JdbcUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductDaoTest extends AbstractDaoTest {

    private static ProductDao productDao;
    private static DataSource spyDataSource;
    private static DataSource originalDataSource;

    @BeforeAll
    @SneakyThrows
    static void init() {
        originalDataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
        spyDataSource = Mockito.spy(originalDataSource);
        productDao = new ProductDaoImpl(spyDataSource);
        createTable(originalDataSource);
    }

    @SneakyThrows
    @AfterEach
    void reset() {
        try (var connection = originalDataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("TRUNCATE TABLE products;");
            }
        }
        Mockito.reset(spyDataSource);
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
    @DisplayName("save throws an exception when product is not valid")
    void saveThrowsException() {
        Product invalidTestProduct = createTestProduct();
        invalidTestProduct.setProducer(null);//setting null to mandatory field to make the product entity invalid

        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.save(invalidTestProduct))
                .withMessage(String.format("Error saving product: %s", invalidTestProduct));
    }

    @Test
    @Order(4)
    @DisplayName("save wraps DB errors with a custom exception")
    @SneakyThrows
    void saveWrapsSqlException() {
        mockDataSourceToThrowError();
        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.save(new Product()));
    }

    @Test
    @Order(5)
    @DisplayName("findAll loads all products from the DB")
    void findAll() {
        List<Product> products = givenStoredProductsFromDB();

        List<Product> foundProducts = productDao.findAll();

        assertThat(foundProducts).isEqualTo(products);
    }

    @Test
    @SneakyThrows
    @Order(6)
    @DisplayName("findAll wraps DB errors with a custom exception")
    void findAllWrapsSqlExceptions() {
        mockDataSourceToThrowError();
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
                .isThrownBy(() -> productDao.findOne(productId));
    }

    @Test
    @Order(9)
    @DisplayName("findOne wraps DB errors with a custom exception")
    @SneakyThrows
    void findOneWrapsSqlExceptions() {
        mockDataSourceToThrowError();
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

        assertThat(productsBeforeUpdate).hasSameSizeAs(products);
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
                .isThrownBy(() -> productDao.update(notStoredProduct));
    }

    @Test
    @Order(12)
    @DisplayName("update wraps DB errors with a custom exception")
    @SneakyThrows
    void updateWrapsSqlExceptions() {
        mockDataSourceToThrowError();
        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.update(new Product()));
    }

    @Test
    @Order(13)
    @DisplayName("remove deletes the product by id from the DB")
    void remove() {
        var product = givenStoredProductFromDB();

        productDao.remove(product);
        List<Product> allProducts = findAllFromDataBase();

        assertThat(allProducts).doesNotContain(product);
    }

    @Test
    @Order(14)
    @DisplayName("remove throws an exception when a product ID is null")
    void removeNotStored() {
        Product notStoredProduct = generateTestProduct();

        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.remove(notStoredProduct));
    }

    @Test
    @Order(15)
    @DisplayName("remove wraps DB errors with a custom exception")
    @SneakyThrows
    void removeWrapsSqlExceptions() {
        mockDataSourceToThrowError();
        assertThatExceptionOfType(DaoOperationException.class)
                .isThrownBy(() -> productDao.remove(new Product()));
    }

    private Product givenStoredProductFromDB() {
        Product product = generateTestProduct();
        saveToDB(product);
        return product;
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

    private void mockDataSourceToThrowError() throws SQLException {
        doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
    }

    private List<Product> givenStoredProductsFromDB() {
        List<Product> products = createTestProductList();
        products.forEach(this::saveToDB);
        return products;
    }

    private List<Product> findAllFromDataBase() {
        try (Connection connection = originalDataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM products;");
            return collectToList(resultSet);
        } catch (SQLException e) {
            throw new DaoOperationException("Error finding all products", e);
        }
    }

    private Product findOneFromDatabase(Long id) {
        Objects.requireNonNull(id);
        try (Connection connection = originalDataSource.getConnection()) {
            return findProductById(id, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error finding product by id = %d", id), e);
        }
    }

    private Product createTestProduct() {
        return Product.builder()
                .name("Fanta")
                .producer("The Coca-Cola Company")
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
}