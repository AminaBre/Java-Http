package no.kristiania;

import no.kristiania.database.Employee;
import no.kristiania.database.EmployeeDao;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class EmployeeOptionsController implements HttpController {
    private EmployeeDao employeeDao;

    public EmployeeOptionsController(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException {
        HttpMessage response = new HttpMessage(getBody());
        response.write(clientSocket);
        return response;
    }

    public String getBody() throws SQLException {

        String body = "";
        for (Employee employee : employeeDao.list()) {
            body += "<option value=" + employee.getId() + ">" + employee.getFirstName() + employee.getLastName() + "</option>";
            
        }

        return body;
    }


}
