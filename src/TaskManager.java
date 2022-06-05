import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, Subtask> subtasks;

    private int nextId;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        nextId = 0;
    }

    private int generateNextId() {
        return nextId++;
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void clearTasks() {
        tasks.clear();
    }

    //возвращает задачу по идентификатору или null, если задачи с таким идентификатором нет
    public Task getTask(int id) {
        return tasks.get(id);
    }

    //добавляет задачу, назначая ей id
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
    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        int id = task.getId();
        tasks.replace(id, task);
    }

    public void removeTask(int id) {
        tasks.remove(id);
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    //возвращает подзадачу по идентификатору или null, если задачи с таким идентификатором нет
    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    //добавляет подзадачу, если есть эпик, в который её нужно добавить
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

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    //возвращает эпик по идентификатору или null, если эпика с таким идентификатором нет
    public Epic getEpic(int id) {
        return epics.get(id);
    }

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
    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        int id = epic.getId();
        epics.replace(id, epic);
        updateEpicStatus(epic);
    }

    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }
        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
    }

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
        ArrayList<Integer> subtaskStatuses = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                subtaskStatuses.add(subtask.status);
            }
        }
        epic.updateStatus(subtaskStatuses);
    }
}
