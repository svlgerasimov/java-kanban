package ru.yandex.practicum.kanban.managers;

import ru.yandex.practicum.kanban.tasks.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int HISTORY_SIZE = 10;

    private final List<Task> history;

    public InMemoryHistoryManager() {
        history = new LinkedList<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (history.size() >= HISTORY_SIZE) {
            history.remove(0);
        }
        history.add(task);
    }

    @Override
    public void remove(int id) {

    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    private static class Node {
        private Node next;
        private Node prev;
    }
}
