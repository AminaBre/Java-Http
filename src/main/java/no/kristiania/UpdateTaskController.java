package no.kristiania;

import no.kristiania.database.*;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class UpdateTaskController implements HttpController {

    private TaskDao taskDao;


    public UpdateTaskController(TaskDao taskDao) {
        this.taskDao = taskDao;

    }

    @Override
    public HttpMessage handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        HttpMessage response = handle(request);
        response.write(clientSocket);
        return response;

    }

    public HttpMessage handle(HttpMessage request) throws SQLException {
        QueryString requestParameter = new QueryString(request.getBody());

        Integer taskStatus = Integer.valueOf(requestParameter.getParameter("taskId2"));
        String taskStatusColor = String.valueOf(requestParameter.getParameter("statusCode2"));

        Task task = taskDao.retreive(taskStatus);
        task.setStatus(taskStatusColor);

        taskDao.update(task);

        HttpMessage redirect = new HttpMessage();
        redirect.setStartLine("HTTP/1.1 302 redirect");
        redirect.getHeaders().put("Location", "http://localhost:8080/editStatus.html");
        return redirect;
    }
}

