package ru.yandex.practicum.kanban.managers.filebacked;

import ru.yandex.practicum.kanban.tasks.*;

public class TaskFieldsCSV {
    private final static String delimiter = ",";

    private final int id;
    private final TaskType type;
    private final String name;
    private final TaskStatus status;
    private final String description;
    private final Integer epic;

    public TaskFieldsCSV(String csvLine) {
        try {
            String[] words = csvLine.split(delimiter);
            id = Integer.parseInt(words[0]);
            type = TaskType.valueOf(words[1]);
            name = words[2];
            status = TaskStatus.valueOf(words[3]);
            description = words[4];
            if (TaskType.SUBTASK.equals(type)) {
                epic = Integer.valueOf(words[5]);
            } else {
                epic = null;
            }
        } catch (Exception e) {
            throw new WrongCSVFormatException("CSV format error in line {" + csvLine + "}");
        }
    }

    public TaskFieldsCSV(Task task) {
        this.id = task.getId();
        this.type = TaskType.TASK;
        this.name = task.getName();
        this.status = task.getStatus();
        this.description = task.getDescription();
        this.epic = null;
    }

    public TaskFieldsCSV(Epic epic) {
        this.id = epic.getId();
        this.type = TaskType.EPIC;
        this.name = epic.getName();
        this.status = epic.getStatus();
        this.description = epic.getDescription();
        this.epic = null;
    }

    public TaskFieldsCSV(Subtask subtask){
        this.id = subtask.getId();
        this.type = TaskType.SUBTASK;
        this.name = subtask.getName();
        this.status = subtask.getStatus();
        this.description = subtask.getDescription();
        this.epic = subtask.getEpicId();
    }

    @Override
    public String toString() {
        String result = id + delimiter +
                type + delimiter +
                name + delimiter +
                status + delimiter +
                description + delimiter;
        if (TaskType.SUBTASK.equals(type)) {
            result += epic;
        }
        return result;
    }

    public int getId() {
        return id;
    }

    public TaskType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public Integer getEpic() {
        return epic;
    }
}
