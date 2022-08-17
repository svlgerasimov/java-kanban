package ru.yandex.practicum.kanban.managers.filebacked;

import ru.yandex.practicum.kanban.managers.inmemory.InMemoryTaskManager;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path path;

    public FileBackedTaskManager(Path path) {
        this.path = path;
    }

    private void save() {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            bufferedWriter.write(CSVUtil.FILE_HEADER + "\n");
            for (Task task : tasks.values()) {
                bufferedWriter.write(CSVUtil.taskToString(task) + "\n");
            }
            // Сначала сохраняем эпики, потом подзадачи.
            // Иначе при восстановлении подзадачи без эпиков не добавятся
            for (Epic epic : epics.values()) {
                bufferedWriter.write(CSVUtil.taskToString(epic) + "\n");
            }
            for (Subtask subtask : subtasks.values()) {
                bufferedWriter.write(CSVUtil.taskToString(subtask) + "\n");
            }
            bufferedWriter.write(String.format("%n%s", CSVUtil.historyToString(historyManager)));
        } catch (IOException e) {
            throw new ManagerSaveException("Manager save to file error: " + e.getMessage());
        }
    }

    public static FileBackedTaskManager loadFromFile(Path path) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(path);
        try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
            bufferedReader.readLine();  // Считываем и игнорируем заголовок
            while (bufferedReader.ready()) {
                String taskLine = bufferedReader.readLine();
                if (taskLine.isEmpty()) {
                    break;
                }
                taskManager.addTaskFromString(taskLine);
            }
            String historyLine = bufferedReader.readLine();
            if (historyLine != null) {
                for (Integer taskId : CSVUtil.historyIdsFromString(historyLine)) {
                    Task task = taskManager.getAnyTaskById(taskId);
                    if (task != null) {
                        taskManager.historyManager.add(task);
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Manager load from file exception: " + e.getMessage());
        }
        return taskManager;
    }

    // Здесь нельзя использовать родительские addTask и т.д., т.к. они подменяют id
    private void addTaskFromString(String line) {
        Task task = CSVUtil.taskFromString(line);
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
