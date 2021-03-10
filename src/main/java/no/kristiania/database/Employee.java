package no.kristiania.database;

public class Employee {

    private String email;
    private String firstName;
    private String lastName;
    private Integer id;
    private Integer taskId;

    public String getEmail(){return email;}

    public void setEmail(String email){this.email = email;}

    public String getFirstName(){return firstName;}

    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    public String getLastName(){return lastName;}

    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    public Integer getId(){return id;}

    public void setId(Integer id){
        this.id = id;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }
}
