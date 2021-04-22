package com.bobocode;

import com.bobocode.dao.ProductDao;
import com.bobocode.dao.ProductDaoImpl;
import com.bobocode.exception.DaoOperationException;
import com.bobocode.model.Product;
import com.bobocode.util.JdbcUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductDaoTest {
    private static ProductDao productDao;

    @BeforeAll
    static void init() throws SQLException {
        DataSource h2DataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
        createAccountTable(h2DataSource);
        productDao = new ProductDaoImpl(h2DataSource);
    }

    private static void createAccountTable(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement createTableStatement = connection.createStatement();
            createTableStatement.execute("CREATE TABLE IF NOT EXISTS products (\n" +
                    "  id            SERIAL NOT NULL,\n" +
                    "  name     VARCHAR(255) NOT NULL,\n" +
                    "  producer     VARCHAR(255) NOT NULL,\n" +
                    "  price       DECIMAL(19, 4),\n" +
                    "  expiration_date      TIMESTAMP NOT NULL,\n" +
                    "  creation_time TIMESTAMP NOT NULL DEFAULT now(),\n" +
                    "\n" +
                    "  CONSTRAINT products_pk PRIMARY KEY (id)\n" +
                    ");\n" +
                    "\n");
        }
    }

    private Product generateTestProduct() {
        return Product.builder()
                .name(RandomStringUtils.randomAlphabetic(10))
                .producer(RandomStringUtils.randomAlphabetic(20))
                .price(BigDecimal.valueOf(RandomUtils.nextInt(10, 100)))
                .expirationDate(LocalDate.ofYearDay(LocalDate.now().getYear() + RandomUtils.nextInt(1, 5),
                        RandomUtils.nextInt(1, 365)))
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Save a product")
    void save() {
        Product fanta = createTestFantaProduct();

        int productsCountBeforeInsert = productDao.findAll().size();
        productDao.save(fanta);
        List<Product> products = productDao.findAll();

        assertNotNull(fanta.getId());
        assertThat(productsCountBeforeInsert + 1).isEqualTo(products.size());
        assertTrue(products.contains(fanta));
    }

    @Test
    @Order(2)
    @DisplayName("Save throws an exception when product ID is invalid")
    void saveInvalidProduct() {
        Product invalidTestProduct = createInvalidTestProduct();

        try {
            productDao.save(invalidTestProduct);
            fail("Exception wasn't thrown");
        } catch (Exception e) {
            assertThat(DaoOperationException.class).isEqualTo(e.getClass());
            assertThat(String.format("Error saving product: %s", invalidTestProduct)).isEqualTo(e.getMessage());
        }
    }

    private Product createTestFantaProduct() {
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


    @Test
    @Order(3)
    @DisplayName("Find all the products")
    void findAll() {
        List<Product> newProducts = createTestProductList();
        List<Product> oldProducts = productDao.findAll();
        newProducts.forEach(productDao::save);

        List<Product> products = productDao.findAll();

        assertTrue(products.containsAll(newProducts));
        assertTrue(products.containsAll(oldProducts));
        assertThat(oldProducts.size() + newProducts.size()).isEqualTo(products.size());

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

    @Test
    @Order(4)
    @DisplayName("Find a product by ID")
    void findById() {
        Product testProduct = generateTestProduct();
        productDao.save(testProduct);

        Product product = productDao.findOne(testProduct.getId());

        assertThat(testProduct).isEqualTo(product);
        assertThat(testProduct.getName()).isEqualTo(product.getName());
        assertThat(testProduct.getProducer()).isEqualTo(product.getProducer());
        assertThat(testProduct.getPrice().setScale(2)).isEqualTo(product.getPrice().setScale(2));
        assertThat(testProduct.getExpirationDate()).isEqualTo(product.getExpirationDate());
    }

    @Test
    @Order(5)
    @DisplayName("Find throws an exception when a product ID doesn't exist")
    void findByNotExistingId() {
        long invalidId = -1L;
        try {
            productDao.findOne(invalidId);
            fail("Exception was't thrown");
        } catch (Exception e) {
            assertThat(DaoOperationException.class).isEqualTo(e.getClass());
            assertThat(String.format("Product with id = %d does not exist", invalidId)).isEqualTo(e.getMessage());
        }
    }

    @Test
    @Order(6)
    @DisplayName("Update a product")
    void update() {
        Product testProduct = generateTestProduct();
        productDao.save(testProduct);
        List<Product> productsBeforeUpdate = productDao.findAll();

        testProduct.setName("Updated name");
        testProduct.setProducer("Updated producer");
        testProduct.setPrice(BigDecimal.valueOf(666));
        testProduct.setExpirationDate(LocalDate.of(2020, Month.JANUARY, 1));
        productDao.update(testProduct);
        List<Product> products = productDao.findAll();
        Product updatedProduct = productDao.findOne(testProduct.getId());

        assertThat(productsBeforeUpdate.size()).isEqualTo(products.size());
        assertTrue(completelyEquals(testProduct, updatedProduct));
        productsBeforeUpdate.remove(testProduct);
        products.remove(testProduct);
        assertTrue(deepEquals(productsBeforeUpdate, products));
    }

    private boolean completelyEquals(Product productBeforeUpdate, Product productAfterUpdate) {
        return productBeforeUpdate.getName().equals(productAfterUpdate.getName())
                && productBeforeUpdate.getProducer().equals(productAfterUpdate.getProducer())
                && productBeforeUpdate.getPrice().setScale(2).equals(productAfterUpdate.getPrice().setScale(2))
                && productBeforeUpdate.getExpirationDate().equals(productAfterUpdate.getExpirationDate());
    }

    private boolean deepEquals(List<Product> productsBeforeUpdate, List<Product> productsAfterUpdate) {
        return productsAfterUpdate.stream()
                .allMatch(product -> remainedTheSame(product, productsBeforeUpdate));
    }

    private boolean remainedTheSame(Product productAfterUpdate, List<Product> productsBeforeUpdate) {
        Product productBeforeUpdate = findById(productsBeforeUpdate, productAfterUpdate.getId());
        return completelyEquals(productAfterUpdate, productBeforeUpdate);
    }

    private Product findById(List<Product> products, Long id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst().get();
    }

    @Test
    @Order(7)
    @DisplayName("Update throws an exception when the operation is not stored")
    void updateNotStored() {
        Product notStoredProduct = generateTestProduct();

        try {
            productDao.update(notStoredProduct);
            fail("Exception wasn't thrown");
        } catch (Exception e) {
            assertThat(DaoOperationException.class).isEqualTo(e.getClass());
            assertThat("Cannot find a product without ID").isEqualTo(e.getMessage());
        }
    }

    @Test
    @Order(8)
    @DisplayName("Update throws an exception when a product ID is invalid")
    void updateProductWithInvalidId() {
        Product testProduct = generateTestProduct();
        long invalidId = -1L;
        testProduct.setId(invalidId);

        try {
            productDao.update(testProduct);
            fail("Exception wasn't thrown");
        } catch (Exception e) {
            assertThat(DaoOperationException.class).isEqualTo(e.getClass());
            assertThat(String.format("Product with id = %d does not exist", invalidId)).isEqualTo(e.getMessage());
        }
    }

    @Test
    @Order(9)
    @DisplayName("Remove a product")
    void remove() {
        Product testProduct = generateTestProduct();
        productDao.save(testProduct);
        List<Product> productsBeforeRemove = productDao.findAll();

        productDao.remove(testProduct);
        List<Product> products = productDao.findAll();

        assertThat(productsBeforeRemove.size() - 1).isEqualTo(products.size());
        assertFalse(products.contains(testProduct));
    }

    @Test
    @Order(10)
    @DisplayName("Remove throws an exception when the operation is not stored")
    void removeNotStored() {
        Product notStoredProduct = generateTestProduct();

        try {
            productDao.remove(notStoredProduct);
            fail("Exception wasn't thrown");
        } catch (Exception e) {
            assertThat(DaoOperationException.class).isEqualTo(e.getClass());
            assertThat("Cannot find a product without ID").isEqualTo(e.getMessage());
        }
    }

    @Test
    @Order(11)
    @DisplayName("Remove throws an exception when a product ID is invalid")
    void removeProductWithInvalidId() {
        Product testProduct = generateTestProduct();
        long invalidId = -1L;
        testProduct.setId(invalidId);

        try {
            productDao.remove(testProduct);
            fail("Exception wasn't thrown");
        } catch (Exception e) {
            assertThat(DaoOperationException.class).isEqualTo(e.getClass());
            assertThat(String.format("Product with id = %d does not exist", invalidId)).isEqualTo(e.getMessage());
        }
    }
}