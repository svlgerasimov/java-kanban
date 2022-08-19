package ru.yandex.practicum.kanban.managers;

import ru.yandex.practicum.kanban.managers.filebacked.FileBackedTaskManager;
import ru.yandex.practicum.kanban.managers.filebacked.ManagerLoadException;
import ru.yandex.practicum.kanban.managers.inmemory.InMemoryHistoryManager;
import ru.yandex.practicum.kanban.managers.inmemory.InMemoryTaskManager;

import java.nio.file.Path;

public final class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getFileBacked(Path path, boolean load) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(path.toString());
        if (load) {
            try {
                fileBackedTaskManager.load();
            } catch (ManagerLoadException e) {
                return fileBackedTaskManager;
            }
        } else {
            fileBackedTaskManager.save();
        }
        return fileBackedTaskManager;
    }

//    public static TaskManager restoreFileBacked(String path) {
//        return FileBackedTaskManager.loadFromFile(path);
//    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
