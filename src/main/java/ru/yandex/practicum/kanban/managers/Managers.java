package ru.yandex.practicum.kanban.managers;

import ru.yandex.practicum.kanban.managers.backed.filebacked.FileBackedTaskManager;
import ru.yandex.practicum.kanban.managers.backed.http.HttpTaskManager;
import ru.yandex.practicum.kanban.managers.inmemory.InMemoryHistoryManager;
import ru.yandex.practicum.kanban.util.kvstorage.ClientBadResponseException;
import ru.yandex.practicum.kanban.util.kvstorage.KVServer;

import java.nio.file.Path;

public final class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new HttpTaskManager("http://localhost:" + KVServer.PORT);
    }

    public static TaskManager getFileBacked(Path path, boolean load) {
        return new FileBackedTaskManager(path.toString(), load);
    }

    public static TaskManager getHttp(String uri, boolean load) {
        return new HttpTaskManager(uri, load);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
