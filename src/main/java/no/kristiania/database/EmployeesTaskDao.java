package no.kristiania.database;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeesTaskDao extends AbstractDao<EmployeesTask> {

    public EmployeesTaskDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected EmployeesTask mapRow (ResultSet rs) throws SQLException {
        EmployeesTask employeesTask = new EmployeesTask();
        employeesTask.setEmployeeId(rs.getInt("employee_id"));
        employeesTask.setTaskId((Integer) rs.getObject("task_id"));
        return employeesTask;
    }

    public void insert(Employee employeee, Task task) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "insert into employees_task (employee_id, task_id) values (?, ?)"
            )) {
                statement.setInt(1, employeee.getId());
                statement.setInt(2, task.getId());
                statement.executeUpdate();
            }
        }
    }
}

               /* try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    employeesTask.setEmployeeId(generatedKeys.getInt("employee_id"));
                    employeesTask.setTaskId(generatedKeys.getInt("task_id"));
                }
            }
        }
    }*/

   /* public void update(EmployeesTask employeesTask) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE employees_task SET task_id = ? WHERE id = ?"
            )) {
                statement.setInt(1, employeesTask.getEmployeeId());
                statement.setInt(2, employeesTask.getTaskId());
                statement.executeUpdate();

            }
        }
    }

    public EmployeesTask retrieve(Integer id) throws SQLException {
        return retrieve(id, "SELECT * FROM employees WHERE id = ?");
    }

    public List<EmployeesTask> list() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(" SELECT * FROM employees_task")) {
                try (ResultSet rs = statement.executeQuery()) {
                    List<EmployeesTask> employeesTask = new ArrayList<>();
                    while (rs.next()) {

                        employeesTask.add(mapRow(rs));
                    }
                    return employeesTask;
                }

            }
        }
    }*/

