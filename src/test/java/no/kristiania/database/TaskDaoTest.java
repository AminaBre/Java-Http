package no.kristiania.database;

import no.kristiania.HttpServer;
import no.kristiania.TaskOptionsController;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskDaoTest {

    private TaskDao taskDao;
    private static Random random = new Random();
    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        Flyway.configure().dataSource(dataSource).load().migrate();
        taskDao = new TaskDao(dataSource);
        server = new HttpServer(0, dataSource);
    }

    @Test
    void shouldListAllTasks() throws SQLException {
        Task task1 = exampleTask();
        Task task2 = exampleTask();
        taskDao.insert(task1);
        taskDao.insert(task2);
        assertThat(taskDao.list())
                .extracting(Task::getName)
                .contains(task1.getName(), task2.getName());
    }

    @Test
    void shouldRetrieveAllTaskProperties() throws SQLException {
        taskDao.insert(exampleTask());
        taskDao.insert(exampleTask());
        Task task = exampleTask();
        taskDao.insert(task);
        assertThat(task).hasNoNullFieldsOrProperties();

        assertThat(taskDao.retreive(task.getId()))
                .usingRecursiveComparison()
                .isEqualTo(task);

    }

    @Test
    void shouldReturnTasksAsOptions() throws SQLException {
        TaskOptionsController controller = new TaskOptionsController(taskDao);
        Task task = exampleTask();
        taskDao.insert(task);

        assertThat(controller.getBody())
                .contains("<option value=" + task.getId() + ">" + task.getName() + "</option>");
    }

    public static Task exampleTask() {

        Task task = new Task();
        task.setName(exampleTaskName());
        task.setStatus("green");
        return task;
    }


    private static String exampleTaskName() {
        String[] options = {"clean", "write", "make", "save"};
        return options[random.nextInt(options.length)];
    }

}
