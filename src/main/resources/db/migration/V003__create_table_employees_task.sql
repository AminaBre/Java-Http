create table employees_task (
    task_id int references tasks (id),
    employee_id int not null,
    foreign key (employee_id) references employees,
    primary key (task_id, employee_id)
)

