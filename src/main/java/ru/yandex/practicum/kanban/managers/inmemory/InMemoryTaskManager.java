package ru.yandex.practicum.kanban.managers.inmemory;

import ru.yandex.practicum.kanban.managers.HistoryManager;
import ru.yandex.practicum.kanban.managers.Managers;
import ru.yandex.practicum.kanban.managers.TaskManager;
import ru.yandex.practicum.kanban.managers.TimeManager;
import ru.yandex.practicum.kanban.tasks.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    protected final HistoryManager historyManager;
    protected final TimeManager timeManager;
    protected int nextId;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
        timeManager = new TimeManager();
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
        for (Task task : tasks.values()) {
            timeManager.removeTask(task);
        }
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
        if (task == null || !timeManager.validateTask(task)) {
            return null;
        }
        int id = generateNextId();
        task.setId(id);
        tasks.put(id, task);
        timeManager.addTask(task);
        return task;
    }

    //обновляет задачу, если задача с таким id есть и если новые временнЫе параметры позволяют
    @Override
    public boolean updateTask(Task task) {
        if (task == null) {
            return false;
        }
        int id = task.getId();
        Task previous = tasks.get(id);
        if (previous == null) {
            return false;     // задачи с таким id не было
        }
        // Чтобы проверить, что задача с новым временем подходит,
        // нужно проверить, что она не пересекается с остальными.
        // Поэтому придется её убрать, но если она не проходит, вернуть обратно
        timeManager.removeTask(previous);
        if (timeManager.validateTask(task)) {
            tasks.put(id, task);
            historyManager.update(task);
            timeManager.addTask(task);
        } else {
            timeManager.addTask(previous);
            return false;
        }
        return true;
    }

    @Override
    public boolean removeTask(int id) {
        Task removed = removeTaskFromMap(tasks, id);
        timeManager.removeTask(removed);
        return removed != null;
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicFromSubtasks(epic);
        }
        for (Subtask subtask : subtasks.values()) {
            timeManager.removeTask(subtask);
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
        if (subtask == null || !timeManager.validateTask(subtask)) {
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
        updateEpicFromSubtasks(epic);
        timeManager.addTask(subtask);
        return subtask;
    }

    //обновляет подзадачу, если подзадача с таким id есть, и она относится к тому же эпику
    @Override
    public boolean updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return false;
        }
        int id = subtask.getId();
        Subtask previous = subtasks.get(id);
        if (previous == null || subtask.getEpicId() != previous.getEpicId()) {
            //подзадачи с таким id нет или эпик в новой версии отличается
            return false;
        }
        timeManager.removeTask(previous);
        if (timeManager.validateTask(subtask)) {
            subtasks.put(id, subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicFromSubtasks(epic);
            }
            historyManager.update(subtask);
            timeManager.addTask(subtask);
        } else {
            timeManager.addTask(previous);
            return false;
        }
        return true;
    }

    @Override
    public boolean removeSubtask(int id) {
        Subtask subtask = removeTaskFromMap(subtasks, id);
        if (subtask == null) {
            return false;
        }
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.removeSubtask(id);
            updateEpicFromSubtasks(epic);
        }
        timeManager.removeTask(subtask);
        return true;
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void clearEpics() {
        clearTasksFromMap(epics);
        for (Subtask subtask : subtasks.values()) {
            timeManager.removeTask(subtask);
        }
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
        updateEpicFromSubtasks(epic);
        return epic;
    }

    //обновляет эпик, если эпик с таким id есть
    //если передаётся эпик с неправильными подзадачами, заменяет на правильные
    @Override
    public boolean updateEpic(Epic epic) {
        if (epic == null) {
            return false;
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
            updateEpicFromSubtasks(epic);
            historyManager.update(epic);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeEpic(int id) {
        Epic epic = removeTaskFromMap(epics, id);
        if (epic == null) {
            return false;
        }
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = removeTaskFromMap(subtasks, subtaskId);
            timeManager.removeTask(subtask);
        }
        return true;
    }

    @Override
    public List<Subtask> getEpicsSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
//            return new ArrayList<>();
            return null;
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

    protected void updateEpicFromSubtasks(Epic epic) {
        TaskStatus epicStatus = calculateEpicStatus(epic);
        if (epicStatus != null) {
            epic.setStatus(epicStatus);
        }
        updateEpicTime(epic);
    }

    private TaskStatus calculateEpicStatus(Epic epic) {
        if (epic == null) {
            return null;
        }
        List<TaskStatus> taskStatuses = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(Subtask::getStatus)
                .collect(Collectors.toList());
        if (taskStatuses.isEmpty()) {
            return TaskStatus.NEW;
        }
        if (taskStatuses.stream().anyMatch(TaskStatus.IN_PROGRESS::equals)) {
            return TaskStatus.IN_PROGRESS;
        }
        if (taskStatuses.stream().allMatch(TaskStatus.NEW::equals)) {
            return TaskStatus.NEW;
        }
        if (taskStatuses.stream().allMatch(TaskStatus.DONE::equals)) {
            return TaskStatus.DONE;
        }
        return TaskStatus.IN_PROGRESS;
    }

    private void updateEpicTime(Epic epic) {
        if (epic == null) {
            return;
        }
        List<Subtask> validSubtasks = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Optional<LocalDateTime> startTime = validSubtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);
        Optional<LocalDateTime> endTime = validSubtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);
        int duration = validSubtasks.stream()
                .mapToInt(Subtask::getDuration)
                .sum();

        epic.setStartTime(startTime.orElse(null));
        epic.setEndTime(endTime.orElse(null));
        epic.setDuration(duration);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return timeManager.getPrioritizedTasks();
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