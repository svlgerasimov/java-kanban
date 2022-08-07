package ru.yandex.practicum.kanban.tasks;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIds;
    private LocalDateTime endTime;

    // В конструкторе нет статуса, т.к. он рассчитывается на основе подзадач,
    // конструктора с временем тоже нет, по той же причине
    public Epic(int id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
        subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(int subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    public void removeSubtask(Integer subtaskId) {
        subtaskIds.remove(subtaskId);
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                super.toString() +
                ", subtaskIds=" + subtaskIds + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIds, epic.subtaskIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds);
    }
}
