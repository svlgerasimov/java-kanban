package ru.yandex.practicum.kanban.managers.inmemory;

import ru.yandex.practicum.kanban.managers.HistoryManager;
import ru.yandex.practicum.kanban.managers.Managers;
import ru.yandex.practicum.kanban.managers.TaskManager;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    protected final HistoryManager historyManager;
    protected int nextId;

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
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void clearTasks() {
        clearTasksFromMap(tasks);
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
        historyManager.update(task);
    }

    @Override
    public void removeTask(int id) {
        removeTaskFromMap(tasks, id);
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }
        clearTasksFromMap(subtasks);
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
        historyManager.update(subtask);
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = removeTaskFromMap(subtasks, id);
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
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void clearEpics() {
        clearTasksFromMap(epics);
        clearTasksFromMap(subtasks);
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
    //если передаётся эпик с неправильными подзадачами, заменяет на правильные
    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        int id = epic.getId();
        Epic previous = epics.replace(id, epic);
        if (previous != null) {
            List<Integer> previousSubtaskIds = previous.getSubtaskIds();
            // если у нового эпика другой список подзадач, подменяем на правильный;
            // сравнивать нужно, иначе всё сломается:
            // если передать тот же объект - список очистится, и подзадачи потеряются
            if (!Objects.equals(previousSubtaskIds, epic.getSubtaskIds())) {
                epic.clearSubtasks();
                for (Integer subtaskId : previousSubtaskIds) {
                    epic.addSubtask(subtaskId);
                }
            }
            updateEpicStatus(epic);
            historyManager.update(epic);
        }
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = removeTaskFromMap(epics, id);
        if (epic == null) {
            return;
        }
        for (Integer subtaskId : epic.getSubtaskIds()) {
            removeTaskFromMap(subtasks, subtaskId);
        }
    }

    @Override
    public List<Subtask> getEpicsSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        List<Subtask> result = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    protected void updateEpicStatus(Epic epic) {
        if (epic == null) {
            return;
        }
        int validSubtasks = 0;
        int newSubtasks = 0;
        int doneSubtasks = 0;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }
            validSubtasks++;
            TaskStatus subtaskStatus = subtask.getStatus();
            if (TaskStatus.IN_PROGRESS.equals(subtaskStatus)) {
                epic.setStatus(TaskStatus.IN_PROGRESS);
                return;
            }
            if (TaskStatus.NEW.equals(subtaskStatus)) {
                newSubtasks++;
            } else if (TaskStatus.DONE.equals(subtaskStatus)) {
                doneSubtasks++;
            }
        }

        if (validSubtasks == 0 || newSubtasks == validSubtasks) {
            epic.setStatus(TaskStatus.NEW);
        } else if (doneSubtasks == validSubtasks) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private <V extends Task> V removeTaskFromMap(Map<Integer, V> map, int id) {
        historyManager.remove(id);
        return map.remove(id);
    }

    private <V extends Task> void clearTasksFromMap(Map<Integer, V> map) {
        for (Integer id : map.keySet()) {
            historyManager.remove(id);
        }
        map.clear();
    }

    //Ищет задачу любого типа по id; если не нашел, возвращает null
    protected Task getAnyTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            task = epics.get(id);
        }
        if (task == null) {
            task = subtasks.get(id);
        }
        return task;
    }


}
