package no.kristiania;

import no.kristiania.database.Employee;
import no.kristiania.database.EmployeeDao;
import no.kristiania.database.EmployeesTaskDao;
import no.kristiania.database.TaskDao;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private Map<String, HttpController> controllers;

    private EmployeeDao employeeDao;
    private final ServerSocket serverSocket;
    private TaskDao taskDao;

    public HttpServer(int port, DataSource dataSource) throws IOException {

        employeeDao = new EmployeeDao(dataSource);
        taskDao = new TaskDao(dataSource);
        EmployeesTaskDao employeesTaskDao = new EmployeesTaskDao(dataSource);
        controllers = Map.of(
                "/api/newTask", new TaskPostController(taskDao),
                "/api/tasks", new TaskGetController(taskDao, employeeDao, employeesTaskDao),
                "/api/taskOptions", new TaskOptionsController(taskDao),
                "/api/employeeOptions", new EmployeeOptionsController(employeeDao),
                "/api/updateTask", new UpdateEmployeeController(employeeDao, taskDao, employeesTaskDao),
                "/api/updateTaskStatus", new UpdateTaskController(taskDao)


        );

        serverSocket = new ServerSocket(port);
        logger.info("Server started on port {}", serverSocket.getLocalPort());

        new Thread(() -> {
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleRequest(clientSocket);
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    private void handleRequest(Socket clientSocket) throws IOException, SQLException {
        HttpMessage request = new HttpMessage(clientSocket);
        String requestLine = request.getStartLine();
        System.out.println("REQUEST" + requestLine);

        String requestMethod = requestLine.split(" ")[0];

        String requestTarget = requestLine.split(" ")[1];


        int questionPos = requestTarget.indexOf('?');

        String requestPath = (questionPos != -1) ? requestTarget.substring(0, questionPos) : requestTarget;

        if(requestMethod.equals("POST")){
            if(requestPath.equals("/api/newWorker")){
                handlePostEmployee(clientSocket, request);
            }else{
                getController(requestPath).handle(request, clientSocket);
            }
        } else {
            if (requestPath.equals("/echo")) {
                handleEchoRequest(clientSocket, requestTarget, questionPos);

            } else if (requestPath.equals("/api/workers" +
                    "")) {
                handleGetWorkers(clientSocket, requestTarget, questionPos);
            } else {
                HttpController controller = controllers.get(requestPath);
                if(controller != null){
                    controller.handle(request, clientSocket);
                }else{
                    handleFileRequest(clientSocket, requestPath); 
                    
                }
            }

        }

    }

    private HttpController  getController(String requestPath) {
        return controllers.get(requestPath);
    }

    private void handlePostEmployee(Socket clientSocket, HttpMessage request) throws SQLException, IOException {
        QueryString requestedParameter = new QueryString(request.getBody());
        String decodedOutput = URLDecoder.decode(requestedParameter.getParameter("email"), StandardCharsets.UTF_8);

        Employee employee = new Employee();
        employee.setFirstName(requestedParameter.getParameter("first_name"));
        employee.setLastName(requestedParameter.getParameter("last_name"));
        employee.setEmail(decodedOutput);
        employeeDao.insert(employee);
        String body = "Redirecting";
        String response = "HTTP/1.1 302 REDIRECT\r\n" +
                "Location: http://localhost:8080/newWorker.html\r\n" +
                "Connection: close\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;

        clientSocket.getOutputStream().write(response.getBytes());
    }

    private void handleFileRequest(Socket clientSocket, String requestPath) throws IOException {

        try (InputStream inputStream = getClass().getResourceAsStream(requestPath)) {
            if(inputStream == null){
                String body = requestPath + " does not exist";
                String response = "HTTP/1.1 404 Not found\r\n" +
                        "Content-Length: " + body.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        body;

                clientSocket.getOutputStream().write(response.getBytes());
                return;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            inputStream.transferTo(buffer);

            String contentType = "text/plain";
            if (requestPath.endsWith(".html")) {
                contentType = "text/html";
            }
            if(requestPath.endsWith(".css")){
                contentType = "text/css";
            }

            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Length: " + buffer.toByteArray().length + "\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            clientSocket.getOutputStream().write(response.getBytes());
            clientSocket.getOutputStream().write(buffer.toByteArray());
        }
    }

    private void handleGetWorkers(Socket clientSocket, String requestTarget, int questionPos) throws IOException, SQLException {

        String body = "<ul>";
        for (Employee employee : employeeDao.list()) {
                body += "<li>" + employee.getFirstName() + " " + employee.getLastName() + " " + employee.getEmail() + "</li>";

        }
        body += "</ul>";
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;

        clientSocket.getOutputStream().write(response.getBytes());
    }

    private void handleEchoRequest(Socket clientSocket, String requestTarget, int questionPos) throws IOException {
        String statusCode = "200";
        String body = "Hello <strong>World</strong>!";
        if (questionPos != -1) {

            QueryString queryString = new QueryString(requestTarget.substring(questionPos + 1));
            if (queryString.getParameter("status") != null) {
                statusCode = queryString.getParameter("status");
            }

            if (queryString.getParameter("body") != null) {
                body = queryString.getParameter("body");
            }

        }
        String response = "HTTP/1.1 " + statusCode + " OK\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;

        clientSocket.getOutputStream().write(response.getBytes());
    }


    public static void main (String[]args) throws IOException {
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader("pgr203.properties")) {
            properties.load(fileReader);
        }catch (Exception e){
            System.out.println(e);
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(properties.getProperty("dataSource.url"));
        dataSource.setUser(properties.getProperty("dataSource.username"));
        dataSource.setPassword(properties.getProperty("dataSource.password"));
        logger.info("Using database {}", dataSource.getUrl());
        Flyway.configure().dataSource(dataSource).load().migrate();

        HttpServer server = new HttpServer(8080, dataSource);
        logger.info("Started on http://localhost:{}/index.html", 8080);
    }

    public List<Employee> getWorker() throws SQLException {
        return employeeDao.list();
    }
}