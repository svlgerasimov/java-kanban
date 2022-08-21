package ru.yandex.practicum.kanban.managers.backed.http;

import com.google.gson.*;
import ru.yandex.practicum.kanban.managers.backed.ManagerLoadException;
import ru.yandex.practicum.kanban.managers.backed.ManagerSaveException;
import ru.yandex.practicum.kanban.managers.backed.filebacked.FileBackedTaskManager;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.util.KVTaskClient;
import ru.yandex.practicum.kanban.util.json.GsonBuilders;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

public class HttpTaskManager extends FileBackedTaskManager {

    private final KVTaskClient taskClient;
    private final String storageKey = "1";
    private final Gson gson;

    // Прежде чем загружать состояние, нужно подготовить клиент. Поэтому нельзя использовать super(String, boolean).
    public HttpTaskManager(String serverUrl) {
        super(serverUrl);
        taskClient = new KVTaskClient(serverUrl);
        gson = GsonBuilders.getBuilderSeparateTaskTypes().create();
    }

    public HttpTaskManager(String serverUrl, boolean loadAtStart) {
        this(serverUrl);
        if (loadAtStart) {
            load();
        }
    }

    @Override
    protected String serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("tasks", gson.toJsonTree(tasks.values()));
        jsonObject.add("epics", gson.toJsonTree(epics.values()));
        jsonObject.add("subtasks", gson.toJsonTree(subtasks.values()));
        jsonObject.add("history", gson.toJsonTree(
                historyManager.getHistory().stream().mapToInt(Task::getId).toArray()));
        return gson.toJson(jsonObject);
    }

    @Override
    protected void saveToTarget(String content) {
        try {
            taskClient.put(storageKey, content);
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Manager save to storage exception: " + e.getMessage());
        }
    }

    @Override
    protected void deserialize(String serialized) {
        JsonObject jsonObject = JsonParser.parseString(serialized).getAsJsonObject();

        Stream.of(gson.fromJson(jsonObject.get("tasks"), Task[].class),
                gson.fromJson(jsonObject.get("epics"), Epic[].class),
                gson.fromJson(jsonObject.get("subtasks"), Subtask[].class))
                .flatMap(Arrays::stream)
                .forEach(this::addDeserializedTask);

        deserializeHistory(Arrays.stream(gson.fromJson(jsonObject.get("history"), Integer[].class)));
    }

    @Override
    protected String loadFromTarget() {
        try {
            return taskClient.load(storageKey);
        } catch (IOException | InterruptedException e) {
            throw new ManagerLoadException("Manager load from storage exception: " + e.getMessage());
        }
    }
}
