package no.kristiania;

import no.kristiania.database.*;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class UpdateEmployeeController implements HttpController {
    private EmployeeDao employeeDao;
    private TaskDao taskDao;
    private EmployeesTaskDao employeesTaskDao;


    public UpdateEmployeeController(EmployeeDao employeeDao, TaskDao taskDao, EmployeesTaskDao employeesTaskDao) {
        this.employeeDao = employeeDao;
        this.taskDao = taskDao;
        this.employeesTaskDao = employeesTaskDao;

    }

    @Override
    public HttpMessage handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        HttpMessage response = handle(request);
        response.write(clientSocket);
        return response;

    }

    public HttpMessage handle(HttpMessage request) throws SQLException {
        QueryString requestParameter = new QueryString(request.getBody());

        Integer employeeId = Integer.valueOf(requestParameter.getParameter("employeeId"));
        Integer taskId = Integer.valueOf(requestParameter.getParameter("taskId"));
        Employee employee = employeeDao.retrieve(employeeId);
        Task task = taskDao.retreive(taskId);


        employeesTaskDao.insert(employee, task);

        HttpMessage redirect = new HttpMessage();
        redirect.setStartLine("HTTP/1.1 302 redirect");
        redirect.getHeaders().put("Location", "http://localhost:8080/editTask.html");
        return redirect;
    }
}
