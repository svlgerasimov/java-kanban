package ru.yandex.practicum.kanban.managers;

import ru.yandex.practicum.kanban.managers.backed.filebacked.FileBackedTaskManager;
import ru.yandex.practicum.kanban.managers.backed.ManagerLoadException;
import ru.yandex.practicum.kanban.managers.backed.http.HttpTaskManager;
import ru.yandex.practicum.kanban.managers.inmemory.InMemoryHistoryManager;
import ru.yandex.practicum.kanban.util.KVServer;

import java.nio.file.Path;

public final class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new HttpTaskManager("http://localhost:" + KVServer.PORT);
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

    public static TaskManager getHttp(String uri, boolean load) {
        HttpTaskManager httpTaskManager = new HttpTaskManager(uri);
        if (load) {
            try {
                httpTaskManager.load();
            } catch (ManagerLoadException e) {
                return httpTaskManager;
            }
        } else {
            httpTaskManager.save();
        }
        return httpTaskManager;
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
