package no.kristiania;

import no.kristiania.database.Employee;
import no.kristiania.database.EmployeeDao;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class HttpServerTest {

    private JdbcDataSource dataSource;
    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        Flyway.configure().dataSource(dataSource).load().migrate();
        server = new HttpServer(0, dataSource);
    }

    @Test
    void shouldReturnSuccessfulErrorCode() throws IOException {
        HttpClient client = new HttpClient("/echo", "localhost", server.getPort());
        assertEquals(200, client.getStatusCode());
    }

    @Test
    void shouldReturnUnsuccessfulErrorCode() throws IOException {
        HttpClient client = new HttpClient("/echo?status=404", "localhost", server.getPort());
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldReturnContentLength() throws IOException {
        HttpClient client = new HttpClient("/echo?body=HelloWorld", "localhost", server.getPort());
        assertEquals("10", client.getResponseHeader("Content-Length"));
    }

    @Test
    void shouldReturnResponseBody() throws IOException {
        HttpClient client = new HttpClient("/echo?body=HelloWorld", "localhost", server.getPort());
        assertEquals("HelloWorld", client.getResponseBody());
    }

    @Test
    void shouldReturnFileFromDesk() throws IOException {
        File contentRoot = new File("target/test-classes");

        String fileContent = "Hello World" + new Date();
        Files.writeString(new File(contentRoot, "test.txt").toPath(), fileContent);

        HttpClient client = new HttpClient("/test.txt", "localhost", server.getPort());
        assertEquals(fileContent, client.getResponseBody());
        assertEquals("text/plain", client.getResponseHeader("Content-Type"));
    }

    @Test
    void shouldReturnCorrectContentType() throws IOException {
        File contentRoot = new File("target/test-classes");

        Files.writeString(new File(contentRoot, "index.html").toPath(), "<h2>Hello World</h2>");

        HttpClient client = new HttpClient("/index.html", "localhost", server.getPort());
        assertEquals("text/html", client.getResponseHeader("Content-Type"));
    }

    @Test
    void shouldReturn404IfFileNotFound() throws IOException {
        File contentRoot = new File("target/test-classes");

        HttpClient client = new HttpClient("/notFound.txt", "localhost", server.getPort());
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldPostNewWorker() throws IOException, SQLException {
        String requestBody = "first_name=amina&email=amina@gmail";
        HttpClient client = new HttpClient("/api/newWorker", "localhost", server.getPort(), "POST", requestBody);
        assertEquals(302, client.getStatusCode());
        assertThat(server.getWorker())
                .filteredOn(employee -> employee.getFirstName().equals("amina"))
                .isNotEmpty()
                .satisfies(employee -> assertThat(employee.get(0).getEmail()).isEqualTo("amina@gmail"));

    }

    @Test
    void shouldReturnExistingWorker() throws IOException, SQLException {
        EmployeeDao employeeDao = new EmployeeDao(dataSource);
        Employee employee = new Employee();
        employee.setFirstName("Elise");
        employee.setLastName("Easter");
        employee.setEmail("elise@mail");
        employeeDao.insert(employee);
        HttpClient client = new HttpClient("/api/workers", "localhost", server.getPort());
        assertThat(client.getResponseBody()).contains("<li>Elise Easter elise@mail</li>");
    }
}
