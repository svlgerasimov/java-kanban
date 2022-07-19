package ru.yandex.practicum.kanban.managers;

import ru.yandex.practicum.kanban.tasks.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);
    void remove(int id);
    List<Task> getHistory();
    void clearHistory();
}
