package ru.yandex.practicum.kanban.managers.backed.filebacked;

import ru.yandex.practicum.kanban.managers.backed.ManagerLoadException;
import ru.yandex.practicum.kanban.managers.backed.ManagerSaveException;
import ru.yandex.practicum.kanban.managers.inmemory.InMemoryTaskManager;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;


public class FileBackedTaskManager extends InMemoryTaskManager {

    private final String path;

    public FileBackedTaskManager(String path) {
        this.path = path;
    }

    public FileBackedTaskManager(String path, boolean loadAtStart) {
        this(path);
        if (loadAtStart) {
            load();
        }
    }

    public void save() {
        String serialized = serialize();
        saveToTarget(serialized);
    }

    protected String serialize() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CSVUtil.FILE_HEADER + "\n");
        for (Task task : tasks.values()) {
            stringBuilder.append(CSVUtil.taskToString(task)).append("\n");
        }
        // Сначала сохраняем эпики, потом подзадачи.
        // Иначе при восстановлении подзадачи без эпиков не добавятся
        for (Epic epic : epics.values()) {
            stringBuilder.append(CSVUtil.taskToString(epic)).append("\n");
        }
        for (Subtask subtask : subtasks.values()) {
            stringBuilder.append(CSVUtil.taskToString(subtask)).append("\n");
        }
        stringBuilder.append(String.format("%n%s", CSVUtil.historyToString(historyManager)));
        return stringBuilder.toString();
    }

    protected void saveToTarget(String content) {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(path))) {
            bufferedWriter.write(content);
        } catch (IOException e) {
            throw new ManagerSaveException("Manager save to file exception: " + e.getMessage());
        }
    }

    public void load() {
        String serialized = loadFromTarget();
        super.clearTasks();
        super.clearEpics();
        deserialize(serialized);
    }

    protected void deserialize(String serialized) {
        Iterator<String> linesIterator = Arrays.stream(serialized.split("\r?\n")).iterator();
        if (linesIterator.hasNext()) {
            linesIterator.next();  // Считываем и игнорируем заголовок
        }
        while (linesIterator.hasNext()) {
            String taskLine = linesIterator.next();
            if (taskLine.isEmpty()) {
                break;
            }
            Task task = CSVUtil.taskFromString(taskLine);
            addDeserializedTask(task);
        }
        if (linesIterator.hasNext()) {
            String historyLine = linesIterator.next();
            deserializeHistory(CSVUtil.historyIdsFromString(historyLine).stream());
        }
    }

    protected void deserializeHistory(Stream<Integer> ids) {
        ids.map(this::getAnyTaskById)
                .filter(Objects::nonNull)
                .forEach(historyManager::add);
    }

    protected String loadFromTarget() {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new ManagerLoadException("Manager load from file exception: " + e.getMessage());
        }
    }

    // Здесь нельзя использовать родительские addTask и т.д., т.к. они подменяют id
    protected void addDeserializedTask(Task task) {
        switch (task.getType()) {
            case TASK:
                if (timeManager.validateTask(task)) {
                    tasks.put(task.getId(), task);
                    timeManager.addTask(task);
                }
                break;
            case EPIC:
                epics.put(task.getId(), (Epic) task);
                break;
            case SUBTASK:
                Subtask subtask = (Subtask) task;
                Epic parentEpic = epics.get(subtask.getEpicId());
                if (parentEpic != null && timeManager.validateTask(subtask)) {
                    subtasks.put(subtask.getId(), subtask);
                    parentEpic.addSubtask(subtask.getId());
                    updateEpicFromSubtasks(parentEpic);
                    timeManager.addTask(subtask);
                }
                break;
        }
        int taskId = task.getId();
        if (taskId >= nextId) {
            nextId = taskId + 1;
        }
    }


    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public Task getTask(int id) {
        Task result = super.getTask(id);
        save();
        return result;
    }

    @Override
    public Task addTask(Task task) {
        Task result = super.addTask(task);
        save();
        return result;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean result = super.updateTask(task);
        save();
        return result;
    }

    @Override
    public boolean removeTask(int id) {
        boolean result = super.removeTask(id);
        save();
        return result;
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask result = super.getSubtask(id);
        save();
        return result;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Subtask result = super.addSubtask(subtask);
        save();
        return result;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        boolean result = super.updateSubtask(subtask);
        save();
        return result;
    }

    @Override
    public boolean removeSubtask(int id) {
        boolean result = super.removeSubtask(id);
        save();
        return result;
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public Epic getEpic(int id) {
        Epic result = super.getEpic(id);
        save();
        return result;
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic result = super.addEpic(epic);
        save();
        return result;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean result = super.updateEpic(epic);
        save();
        return result;
    }

    @Override
    public boolean removeEpic(int id) {
        boolean result = super.removeEpic(id);
        save();
        return result;
    }
}
