package ru.yandex.practicum.kanban.managers;

import ru.yandex.practicum.kanban.managers.filebacked.FileBackedTaskManager;
import ru.yandex.practicum.kanban.managers.inmemory.InMemoryHistoryManager;
import ru.yandex.practicum.kanban.managers.inmemory.InMemoryTaskManager;

import java.nio.file.Path;

public final class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getFileBacked(Path path) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(path);
        fileBackedTaskManager.save();
        return new FileBackedTaskManager(path);
    }

    public static TaskManager restoreFileBacked(Path path) {
        return FileBackedTaskManager.loadFromFile(path);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
