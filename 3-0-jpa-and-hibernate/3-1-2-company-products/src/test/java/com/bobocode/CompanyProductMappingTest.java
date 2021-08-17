package com.bobocode;

import com.bobocode.dao.CompanyDao;
import com.bobocode.dao.CompanyDaoImpl;
import com.bobocode.model.Company;
import com.bobocode.model.Product;
import com.bobocode.util.EntityManagerUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.JoinColumn;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CompanyProductMappingTest {
    private static EntityManagerUtil emUtil;
    private static EntityManagerFactory entityManagerFactory;
    private static CompanyDao companyDao;

    @BeforeAll
    static void setup() {
        entityManagerFactory = Persistence.createEntityManagerFactory("CompanyProducts");
        emUtil = new EntityManagerUtil(entityManagerFactory);
        companyDao = new CompanyDaoImpl(entityManagerFactory);
    }

    @AfterAll
    static void destroy() {
        entityManagerFactory.close();
    }

    @Test
    @Order(1)
    @DisplayName("Save a company")
    void saveCompany() {
        var company = createRandomCompany();

        emUtil.performWithinTx(entityManager -> entityManager.persist(company));

        assertThat(company.getId()).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("Saving a company throws an exception when the name is null")
    void saveCompanyWithNullName() {
        var company = new Company();

        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
                emUtil.performWithinTx(entityManager -> entityManager.persist(company))
        );
    }

    @Test
    @Order(3)
    @DisplayName("Foreign key column is specified")
    void foreignKeyColumnIsSpecified() throws NoSuchFieldException {
        Field company = Product.class.getDeclaredField("company");
        JoinColumn joinColumn = company.getAnnotation(JoinColumn.class);

        assertThat(joinColumn.name()).isEqualTo("company_id");
    }

    @Test
    @Order(4)
    @DisplayName("Save a product")
    void saveProduct() {
        var product = createRandomProduct();

        emUtil.performWithinTx(entityManager -> entityManager.persist(product));

        assertThat(product.getId()).isNotNull();
    }

    @Test
    @Order(5)
    @DisplayName("Saving a product throws an exception when the name is null")
    void saveProductWithNullName() {
        var product = new Product();

        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
                emUtil.performWithinTx(entityManager -> entityManager.persist(product))
        );
    }

    @Test
    @Order(6)
    @DisplayName("Save both a product and a company")
    void saveProductAndCompany() {
        var company = createRandomCompany();
        var product = createRandomProduct();

        emUtil.performWithinTx(entityManager -> entityManager.persist(company));
        emUtil.performWithinTx(entityManager -> {
            var companyProxy = entityManager.getReference(Company.class, company.getId());
            product.setCompany(companyProxy);
            entityManager.persist(product);
        });

        emUtil.performWithinTx(entityManager -> {
            var managedCompany = entityManager.find(Company.class, company.getId());
            var managedProduct = entityManager.find(Product.class, product.getId());

            assertThat(managedCompany.getProducts()).contains(managedProduct);
            assertThat(managedProduct.getCompany()).isEqualTo(managedCompany);
        });
    }

    @Test
    @Order(7)
    @DisplayName("Add a new product to an existing company")
    void addNewProductToExistingCompany() {
        var company = createRandomCompany();
        emUtil.performWithinTx(entityManager -> entityManager.persist(company));

        var product = createRandomProduct();
        emUtil.performWithinTx(entityManager -> {
            entityManager.persist(product);
            var managedCompany = entityManager.merge(company);
            managedCompany.addProduct(product);
            assertThat(managedCompany.getProducts()).contains(product);
        });

        assertThat(product.getCompany()).isEqualTo(company);
        emUtil.performWithinTx(entityManager -> {
            var managedCompany = entityManager.find(Company.class, company.getId());
            assertThat(managedCompany.getProducts()).contains(product);

        });
    }

    @Test
    @Order(8)
    @DisplayName("Remove a product from a company")
    void removeProductFromCompany() {
        var company = createRandomCompany();
        emUtil.performWithinTx(entityManager -> entityManager.persist(company));

        var product = createRandomProduct();
        emUtil.performWithinTx(entityManager -> {
            product.setCompany(company);
            entityManager.persist(product);
        });

        emUtil.performWithinTx(entityManager -> {
            var managedProduct = entityManager.find(Product.class, product.getId());
            var managedCompany = entityManager.find(Company.class, company.getId());
            managedCompany.removeProduct(managedProduct);
            assertThat(managedCompany.getProducts()).doesNotContain(managedProduct);
        });

        emUtil.performWithinTx(entityManager -> {
            var managedCompany = entityManager.find(Company.class, company.getId());
            assertThat(managedCompany.getProducts()).doesNotContain(product);
        });
    }

    @Test
    @Order(9)
    @DisplayName("Field \"products\" is lazy in Company entity")
    void companyToProductsIsLazy() {
        var company = createRandomCompany();
        emUtil.performWithinTx(entityManager -> entityManager.persist(company));

        var product = createRandomProduct();
        emUtil.performWithinTx(entityManager -> {
            product.setCompany(company);
            entityManager.persist(product);
        });

        Company loadedCompany = emUtil.performReturningWithinTx(entityManager -> entityManager.find(Company.class, company.getId()));
        List<Product> products = loadedCompany.getProducts();

        assertThatExceptionOfType(LazyInitializationException.class).isThrownBy(() -> System.out.println(products));
    }

    @Test
    @Order(10)
    @DisplayName("Field \"company\" is lazy in Product entity")
    void productsToCompanyIsLazy() {
        var company = createRandomCompany();
        emUtil.performWithinTx(entityManager -> entityManager.persist(company));

        var product = createRandomProduct();
        emUtil.performWithinTx(entityManager -> {
            product.setCompany(company);
            entityManager.persist(product);
        });

        Product loadedProduct = emUtil.performReturningWithinTx(entityManager -> entityManager.find(Product.class, product.getId()));
        Company loadedCompany = loadedProduct.getCompany();

        assertThatExceptionOfType(LazyInitializationException.class).isThrownBy(() -> System.out.println(loadedCompany));
    }

    @Test
    @Order(11)
    @DisplayName("findByIdFetchProducts() loads company and products all together")
    void findByIdFetchesProducts() {
        var company = createRandomCompany();
        emUtil.performWithinTx(entityManager -> entityManager.persist(company));

        var product = createRandomProduct();
        emUtil.performWithinTx(entityManager -> {
            product.setCompany(company);
            entityManager.persist(product);
        });

        Company foundCompany = companyDao.findByIdFetchProducts(company.getId());
        assertThat(foundCompany).isEqualTo(company);
        assertThat(foundCompany.getProducts()).contains(product);
    }

    @Test
    @Order(12)
    @DisplayName("Setter for field \"products\" is private in Company entity")
    void companySetProductsIsPrivate() throws NoSuchMethodException {
        assertThat(Company.class.getDeclaredMethod("setProducts", List.class).getModifiers()).isEqualTo(Modifier.PRIVATE);
    }

    private Company createRandomCompany() {
        var company = new Company();
        company.setName(RandomStringUtils.randomAlphabetic(20));
        return company;
    }

    private Product createRandomProduct() {
        var product = new Product();
        product.setName(RandomStringUtils.randomAlphabetic(20));
        return product;
    }
}
