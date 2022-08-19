package ru.yandex.practicum.kanban.managers.http;

import ru.yandex.practicum.kanban.managers.filebacked.FileBackedTaskManager;

import java.net.URI;
import java.nio.file.Path;

public class HttpTaskManager extends FileBackedTaskManager {

    public HttpTaskManager(String serverUrl) {
        super(serverUrl);
    }
}
