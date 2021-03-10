package no.kristiania;

import no.kristiania.database.*;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

public class TaskGetController implements HttpController {
    private TaskDao taskDao;
    private EmployeeDao employeeDao;
    private EmployeesTaskDao employeesTaskDao;

    public TaskGetController(TaskDao taskDao, EmployeeDao  employeeDao, EmployeesTaskDao employeesTaskDao) {
        this.taskDao = taskDao;
        this.employeeDao = employeeDao;
        this.employeesTaskDao = employeesTaskDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {

        String body = "";
        StringBuilder body2 = new StringBuilder();

        List<Task> tasks = taskDao.list();
        for(int i=0; i<tasks.size(); i++){
            String taskId = "" + tasks.get(i).getId();
            String sql = "select employees.* from employees_task " +
                    "join employees on employees_task.employee_id = employees.id where employees_task.task_id = " + taskId;

            String employeeName = "";

            String status = tasks.get(i).getStatus();
            if(status == null){
                status = "No status here";
            }

            List<Employee> employees = employeeDao.list(sql);
            System.out.println("size: " + employees.size());
            for(int j = 0; j<employees.size(); j++){
                employeeName = employeeName + employees.get(j).getFirstName() + ", ";
            }
            body2.append("<hr> <article>\n" +
                    "<h1>" + tasks.get(i).getName() + "</h1>\n" +
                    "<h4> Status: </h4>\n" +
                    "<p>" + status + "</p>\n" +
                    "<h4> Employees: </h4>\n" +
                    "<div>" + employeeName +"</div>\n" +
                    "\n" +
                    " </article>");
        }


        body += body2;
        body += "<hr>";

        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;

        clientSocket.getOutputStream().write(response.getBytes());
        return request;

    }
}
