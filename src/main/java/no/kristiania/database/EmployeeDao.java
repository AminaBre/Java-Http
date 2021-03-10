
package no.kristiania.database;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDao extends AbstractDao<Employee> {

    public EmployeeDao(DataSource dataSource) {
        super(dataSource);
    }

    public void insert(Employee employee) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "insert into employees (first_name, last_name, email) values (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                statement.setString(1, employee.getFirstName());
                statement.setString(2, employee.getLastName());
                statement.setString(3, employee.getEmail());
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    employee.setId(generatedKeys.getInt("id"));
                }
            }
        }
    }

    public Employee retrieve(Integer id) throws SQLException {
        return retrieve(id, "SELECT * FROM employees WHERE id = ?");
    }

    public List<Employee> list() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(" SELECT * FROM employees")) {
                try (ResultSet rs = statement.executeQuery()) {
                    List<Employee> employees = new ArrayList<>();
                    while (rs.next()) {

                        employees.add(mapRow(rs));
                    }
                    return employees;
                }

            }
        }
    }
    @Override
    protected Employee mapRow(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getInt("id"));
        employee.setFirstName(rs.getString("first_Name"));
        employee.setLastName(rs.getString("last_Name"));
        employee.setEmail(rs.getString("email"));
        return employee;
    }
}
