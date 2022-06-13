package ru.yandex.practicum.kanban.managers;

import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private Map<Integer, Task> tasks;
    private Map<Integer, Epic> epics;
    private Map<Integer, Subtask> subtasks;
    private HistoryManager historyManager;
    private int nextId;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
    }

    private int generateNextId() {
        return nextId++;
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    //возвращает задачу по идентификатору или null, если задачи с таким идентификатором нет
    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    //добавляет задачу, назначая ей id
    @Override
    public Task addTask(Task task) {
        if (task == null) {
            return null;
        }
        int id = generateNextId();
        task.setId(id);
        tasks.put(id, task);
        return task;
    }

    //обновляет задачу, если задача с таким id есть
    @Override
    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        int id = task.getId();
        tasks.replace(id, task);
    }

    @Override
    public void removeTask(int id) {
        tasks.remove(id);
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    //возвращает подзадачу по идентификатору или null, если задачи с таким идентификатором нет
    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    //добавляет подзадачу, если есть эпик, в который её нужно добавить
    @Override
    public Subtask addSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        int id = generateNextId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addSubtask(id);
        updateEpicStatus(epic);
        return subtask;
    }

    //обновляет подзадачу, если подзадача с таким id есть, и она относится к тому же эпику
    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        int id = subtask.getId();
        Subtask previous = subtasks.get(id);
        if (previous == null || subtask.getEpicId() != previous.getEpicId()) {
            //подзадачи с таким id нет или эпик в новой версии отличается
            return;
        }
        subtasks.put(id, subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            return;
        }
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.removeSubtask(id);
            updateEpicStatus(epic);
        }
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    //возвращает эпик по идентификатору или null, если эпика с таким идентификатором нет
    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Epic addEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        int id = generateNextId();
        epic.setId(id);
        epics.put(id, epic);
        updateEpicStatus(epic);
        return epic;
    }

    //обновляет эпик, если эпик с таким id есть
    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        int id = epic.getId();
        epics.replace(id, epic);
        updateEpicStatus(epic);
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }
        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
    }

    @Override
    public ArrayList<Subtask> getEpicsSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        ArrayList<Subtask> result = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    private void updateEpicStatus(Epic epic) {
        if (epic == null) {
            return;
        }
        int validSubtasks = 0;
        int newSubtasks = 0;
        int doneSubtasks = 0;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if(subtask == null) {
                continue;
            }
            validSubtasks++;
            TaskStatus subtaskStatus = subtask.getStatus();
            if(TaskStatus.IN_PROGRESS.equals(subtaskStatus)) {
                epic.setStatus(TaskStatus.IN_PROGRESS);
                return;
            }
            if(TaskStatus.NEW.equals(subtaskStatus)) {
                newSubtasks++;
            } else if(TaskStatus.DONE.equals(subtaskStatus)) {
                doneSubtasks++;
            }
        }

        if(validSubtasks == 0 || newSubtasks == validSubtasks) {
            epic.setStatus(TaskStatus.NEW);
        } else if(doneSubtasks == validSubtasks) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

}
