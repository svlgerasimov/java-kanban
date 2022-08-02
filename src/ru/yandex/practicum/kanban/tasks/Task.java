package ru.yandex.practicum.kanban.tasks;

import java.util.Objects;

public class Task {

    private String name;
    private String description;
    private int id;
    private TaskStatus status;

    public Task(int id, String name, String description, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Task task = (Task) o;
//        return id == task.id && Objects.equals(name, task.name) && Objects.equals(description, task.description)
//                && status == task.status;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(name, description, id, status);
//    }
}
