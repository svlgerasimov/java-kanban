package ru.yandex.practicum.kanban.managers.filebacked;

import ru.yandex.practicum.kanban.managers.inmemory.InMemoryTaskManager;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.managers.filebacked.CSVUtil.TaskFieldsCSV;

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

    private void save() throws ManagerSaveException {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            for (Task task : tasks.values()) {
                bufferedWriter.write(new TaskFieldsCSV(task) + "\n");
            }
            // Сначала сохраняем эпики, потом подзадачи.
            // Иначе при восстановлении подзадачи без эпиков не добавятся
            for (Epic epic : epics.values()) {
                bufferedWriter.write(new TaskFieldsCSV(epic) + "\n");
            }
            for (Subtask subtask : subtasks.values()) {
                bufferedWriter.write(new TaskFieldsCSV(subtask) + "\n");
            }
            bufferedWriter.write(String.format("%n%s", CSVUtil.historyToString(historyManager)));
        } catch (IOException e) {
            throw new ManagerSaveException("Manager save to file error: " + e.getMessage());
        }
    }

    public static FileBackedTaskManager loadFromFile(Path path) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(path);
        try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
            while (bufferedReader.ready()) {
                String taskLine = bufferedReader.readLine();
                if (taskLine.isEmpty()) {
                    break;
                }
                taskManager.addTaskFromString(taskLine);
            }
            String historyLine = bufferedReader.readLine();
            for (Integer taskId : CSVUtil.historyIdsFromString(historyLine)) {
                Task task = taskManager.getAnyTaskById(taskId);
                if (task != null) {
                    taskManager.historyManager.add(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Manager load from file exception: " + e.getMessage());
        }
        return taskManager;
    }

    //здесь нельзя использовать родительские addTask и т.д., т.к. они подменяют id
    private void addTaskFromString(String line) {
        TaskFieldsCSV taskFieldsCSV = new TaskFieldsCSV(line);
        switch (taskFieldsCSV.getType()) {
            case TASK:
                Task task = new Task(taskFieldsCSV.getId(), taskFieldsCSV.getName(),
                        taskFieldsCSV.getDescription(), taskFieldsCSV.getStatus());
                tasks.put(task.getId(), task);
                break;
            case EPIC:
                Epic epic = new Epic(taskFieldsCSV.getId(), taskFieldsCSV.getName(), taskFieldsCSV.getDescription());
                epics.put(epic.getId(), epic);
                break;
            case SUBTASK:
                Subtask subtask = new Subtask(taskFieldsCSV.getId(), taskFieldsCSV.getName(),
                        taskFieldsCSV.getDescription(), taskFieldsCSV.getStatus(), taskFieldsCSV.getEpic());
                Epic parentEpic = epics.get(subtask.getEpicId());
                if (parentEpic != null) {
                    subtasks.put(subtask.getId(), subtask);
                    parentEpic.addSubtask(subtask.getId());
                    updateEpicStatus(parentEpic);
                }
                break;
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
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
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
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
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
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }
}
