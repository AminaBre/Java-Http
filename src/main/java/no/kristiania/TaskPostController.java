package no.kristiania;

import no.kristiania.database.Task;
import no.kristiania.database.TaskDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class TaskPostController implements HttpController {
    private TaskDao taskDao;

    public TaskPostController(TaskDao taskDao) {

        this.taskDao = taskDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        QueryString requestedParameter = new QueryString(request.getBody());


        Task task = new Task();
        task.setName(requestedParameter.getParameter("taskName"));
        task.setStatus(requestedParameter.getParameter("statusCode"));
        taskDao.insert(task);

        String body = "Redirecting";
        String response = "HTTP/1.1 302 redirecting\r\n" +
                "Location: http://localhost:8080/newTask.html\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;

        clientSocket.getOutputStream().write(response.getBytes());
        return request;

    }
}
