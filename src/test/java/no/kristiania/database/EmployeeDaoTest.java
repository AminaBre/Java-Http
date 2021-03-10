package no.kristiania.database;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class EmployeeDaoTest {

    private EmployeeDao employeeDao;
    private Random random;
    private TaskDao taskDao;
    private Task defaultTask;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        Flyway.configure().dataSource(dataSource).load().migrate();
        employeeDao = new EmployeeDao(dataSource);
        taskDao = new TaskDao(dataSource);

        defaultTask = TaskDaoTest.exampleTask();
        taskDao.insert(defaultTask);
        
    }

    @Test
    void shouldSaveAndRetrieveAllEmployeeProperties() throws SQLException {
        employeeDao.insert(exampleEmployee());
        employeeDao.insert(exampleEmployee());
        Employee employee = exampleEmployee();
        employeeDao.insert(employee);
        assertThat(employee).hasNoNullFieldsOrPropertiesExcept("taskId");
        assertThat(employeeDao.retrieve(employee.getId()))
                .usingRecursiveComparison()
                .isEqualTo(employee);
    }
    @Test
    void shouldListInsertedEmployees() throws SQLException {
        Employee employee1 = exampleEmployee();
        Employee employee2 = exampleEmployee();
        employeeDao.insert(employee1);
        employeeDao.insert(employee2);
        assertThat(employeeDao.list())
                .extracting(Employee::getFirstName)
                .contains(employee1.getFirstName(), employee2.getFirstName());
    }

    private Employee exampleEmployee() {
        Employee employee = new Employee();
        employee.setEmail(exampleEmail());
        employee.setFirstName(exampleFirstName());
        employee.setLastName(exampleLastName());
        return employee;
    }

    private static String exampleFirstName() {
        String[] options = {"Elise", "Amina", "Trond", "Sarah"};
        Random random = new Random();
        return options[random.nextInt(options.length)];
    }

    private static String exampleLastName() {
        String[] options = {"Berntssen", "Olsen", "Trolsen", "Elf"};
        Random random = new Random();
        return options[random.nextInt(options.length)];
    }

    private static String exampleEmail() {
        String[] options = {"hei@hå", "ko@ko", "tro@lo", "hade@hei"};
        Random random = new Random();
        return options[random.nextInt(options.length)];
    }
}