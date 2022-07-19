package ru.yandex.practicum.kanban.managers;

import ru.yandex.practicum.kanban.managers.inmemory.InMemoryHistoryManager;
import ru.yandex.practicum.kanban.managers.inmemory.InMemoryTaskManager;

public final class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
