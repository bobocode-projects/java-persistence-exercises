package com.bobocode;

import com.bobocode.model.Employee;
import com.bobocode.model.EmployeeProfile;
import com.bobocode.util.EntityManagerUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;

import javax.persistence.*;
import java.lang.reflect.Field;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeProfileMappingTest {
    private static EntityManagerUtil emUtil;
    private static EntityManagerFactory entityManagerFactory;

    @BeforeAll
    static void setup() {
        entityManagerFactory = Persistence.createEntityManagerFactory("Employees");
        emUtil = new EntityManagerUtil(entityManagerFactory);
    }

    @AfterAll
    static void destroy() {
        entityManagerFactory.close();
    }

    @Test
    @Order(1)
    @DisplayName("The employee table has a correct name")
    void employeeTableHasCorrectName() {
        Table table = Employee.class.getAnnotation(Table.class);
        String tableName = table.name();

        assertThat(tableName, equalTo("employee"));
    }

    @Test
    @Order(2)
    @DisplayName("Save an employee only")
    void saveEmployeeOnly() {
        Employee employee = createRandomEmployee();

        emUtil.performWithinTx(entityManager -> entityManager.persist(employee));

        assertThat(employee.getId()).isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("Saving an employee throws an exception when the email is null")
    void saveEmployeeWithoutEmail() {
        Employee employee = createRandomEmployee();
        employee.setEmail(null);

        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
                emUtil.performWithinTx(entityManager -> entityManager.persist(employee)));
    }

    @Test
    @Order(4)
    @DisplayName("Saving an employee throws an exception when the first name is null")
    void saveEmployeeWithoutFirstName() {
        Employee employee = createRandomEmployee();
        employee.setFistName(null);

        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
                emUtil.performWithinTx(entityManager -> entityManager.persist(employee)));
    }

    @Test
    @Order(5)
    @DisplayName("Saving an employee throws an exception when the last name is null")
    void testSaveEmployeeWithoutLastName() {
        Employee employee = createRandomEmployee();
        employee.setLastName(null);

        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
                emUtil.performWithinTx(entityManager -> entityManager.persist(employee)));
    }

    @Test
    @Order(6)
    @DisplayName("The employee profile table has a correct name")
    void employeeProfileTableHasCorrectName() {
        Table table = EmployeeProfile.class.getAnnotation(Table.class);
        String tableName = table.name();

        assertThat(tableName).isEqualTo("employee_profile");
    }

    @Test
    @Order(7)
    @DisplayName("Save only an employee profile")
    void saveEmployeeProfileOnly() {
        EmployeeProfile employeeProfile = createRandomEmployeeProfile();
        employeeProfile.setId(666L);

        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
                emUtil.performWithinTx(entityManager -> entityManager.persist(employeeProfile)));
    }

    @Test
    @Order(8)
    @DisplayName("The foreign key column has a correct name")
    void foreignKeyColumnHasCorrectName() throws NoSuchFieldException {
        Field employee = EmployeeProfile.class.getDeclaredField("employee");
        JoinColumn joinColumn = employee.getAnnotation(JoinColumn.class);
        String foreignKeyColumnName = joinColumn.name();

        assertThat(foreignKeyColumnName).isEqualTo("employee_id");
    }

    @Test
    @Order(9)
    @DisplayName("Save both employee and employee profile")
    void saveBothEmployeeAndEmployeeProfile() {
        Employee employee = createRandomEmployee();
        EmployeeProfile employeeProfile = createRandomEmployeeProfile();

        emUtil.performWithinTx(entityManager -> {
            entityManager.persist(employee);
            employeeProfile.setEmployee(employee);
            entityManager.persist(employeeProfile);
        });

        assertThat(employee.getId()).isNotNull();
        assertThat(employeeProfile.getId()).isNotNull();
        assertThat(employeeProfile.getId()).isEqualTo(employee.getId());
    }

    @Test
    @Order(10)
    @DisplayName("Add an employee profile")
    void addEmployeeProfile() {
        Employee employee = createRandomEmployee();
        emUtil.performWithinTx(entityManager -> entityManager.persist(employee));
        long employeeId = employee.getId();

        EmployeeProfile employeeProfile = createRandomEmployeeProfile();
        emUtil.performWithinTx(entityManager -> {
            Employee managedEmployee = entityManager.find(Employee.class, employeeId);
            employeeProfile.setEmployee(managedEmployee);
            entityManager.persist(employeeProfile);
        });

        assertThat(employee.getId()).isNotNull();
        assertThat(employeeProfile.getId()).isNotNull();
        assertThat(employeeProfile.getId()).isEqualTo(employee.getId());
    }

    @Test
    @Order(11)
    @DisplayName("Adding an employee profile throws an exception when the position is null")
    void addEmployeeWithoutPosition() {
        Employee employee = createRandomEmployee();
        emUtil.performWithinTx(entityManager -> entityManager.persist(employee));
        long employeeId = employee.getId();

        EmployeeProfile profileWithoutPosition = createRandomEmployeeProfile();
        profileWithoutPosition.setPosition(null);

        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
                emUtil.performWithinTx(entityManager -> {
                    Employee managedEmployee = entityManager.find(Employee.class, employeeId);
                    profileWithoutPosition.setEmployee(managedEmployee);
                    entityManager.persist(profileWithoutPosition);
                }));
    }

    @Test
    @Order(12)
    @DisplayName("Adding an employee profile throws an exception when the department is null")
    void addEmployeeWithoutDepartment() {
        Employee employee = createRandomEmployee();
        emUtil.performWithinTx(entityManager -> entityManager.persist(employee));
        long employeeId = employee.getId();

        EmployeeProfile profileWithoutDepartment = createRandomEmployeeProfile();
        profileWithoutDepartment.setDepartment(null);

        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
                emUtil.performWithinTx(entityManager -> {
                    Employee managedEmployee = entityManager.find(Employee.class, employeeId);
                    profileWithoutDepartment.setEmployee(managedEmployee);
                    entityManager.persist(profileWithoutDepartment);
                }));
    }

    private Employee createRandomEmployee() {
        Employee employee = new Employee();
        employee.setEmail(RandomStringUtils.randomAlphabetic(15));
        employee.setFistName(RandomStringUtils.randomAlphabetic(15));
        employee.setLastName(RandomStringUtils.randomAlphabetic(15));
        return employee;
    }

    private EmployeeProfile createRandomEmployeeProfile() {
        EmployeeProfile employeeProfile = new EmployeeProfile();
        employeeProfile.setDepartment(RandomStringUtils.randomAlphabetic(15));
        employeeProfile.setPosition(RandomStringUtils.randomAlphabetic(15));
        return employeeProfile;
    }
}
