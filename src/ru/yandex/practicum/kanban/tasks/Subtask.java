package ru.yandex.practicum.kanban.tasks;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        String result = "ru.yandex.practicum.kanban.Task.Subtask{" +
                super.toString() +
                ", epicId=" + epicId;
        result += '}';
        return result;
    }
}
