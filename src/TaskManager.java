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

    public int generateNextId() {
        return nextId++;
    }

    public ArrayList<Task> getTasks() {
        ArrayList<Task> result = new ArrayList<>();
        for(Task task : tasks.values()) {
            result.add(task);
        }
        return result;
    }

    public void clearTasks() {
        tasks.clear();
    }

    //возвращает задачу по идентификатору или null, если задачи с таким идентификатором нет
    public Task getTask(int id) {
        return tasks.get(id);
    }

    //добавляет задачу, если такой ещё нет
    public void addTask(Task task) {
        if(task == null) {
            return;
        }
        int id = task.getId();
        tasks.putIfAbsent(id, task);
    }

    //обновляет задачу, если такая есть
    public void updateTask(Task task) {
        int id = task.getId();
        tasks.replace(id, task);
    }

    public void removeTask(int id) {
        tasks.remove(id);
    }

    public ArrayList<Subtask> getSubtasks() {
        ArrayList<Subtask> result = new ArrayList<>();
        for(Subtask subtask : subtasks.values()) {
            result.add(subtask);
        }
        return result;
    }

    public void clearSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            Epic epic = subtask.getEpic();
            if(epic != null) {
                epic.removeSubtask(subtask);
            }
        }
        subtasks.clear();
    }

    //возвращает задачу по идентификатору или null, если задачи с таким идентификатором нет
    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    //добавляет задачу, если такой ещё нет
    public void addSubtask(Subtask subtask) {
        if(subtask == null) {
            return;
        }
        int id = subtask.getId();
        subtasks.putIfAbsent(id, subtask);
    }

    //обновляет задачу, если такая есть
    public void updateSubtask(Subtask subtask) {
        int id = subtask.getId();
        subtasks.replace(id, subtask);
        Epic epic = subtask.getEpic();
        if(epic != null) {
            epic.updateSubtask(subtask);
        }
    }

    public void removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if(subtask == null) {
            return;
        }
        Epic epic = subtask.getEpic();
        if(epic != null) {
            epic.removeSubtask(subtask);
        }
    }

    public ArrayList<Epic> getEpics() {
        ArrayList<Epic> result = new ArrayList<>();
        for(Epic epic : epics.values()) {
            result.add(epic);
        }
        return result;
    }

    public void clearEpics() {
        for (Epic epic : epics.values()) {
            tryRemoveEpicsSubtasks(epic);
        }
        epics.clear();
    }

    //возвращает задачу по идентификатору или null, если задачи с таким идентификатором нет
    public Epic getEpic(int id) {
        return epics.get(id);
    }

    //добавляет задачу, если такой ещё нет
    public void addEpic(Epic epic) {
        if(epic == null) {
            return;
        }
        int id = epic.getId();
        epics.putIfAbsent(id, epic);
    }

    //обновляет задачу, если такая есть
    //TODO обновить ссылки на эпик в подзадачах
    public void updateEpic(Epic epic) {
        int id = epic.getId();
        epics.replace(id, epic);
        //for()
    }

    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        tryRemoveEpicsSubtasks(epic);
    }

    public ArrayList<Subtask> getEpicsSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if(epic == null) {
            return new ArrayList<>();
        }
        return epic.getSubtasks();
    }

    //TODO надо подумать, нужно ли обнулять ссылку на эпик у подзадачи
    private void tryRemoveEpicsSubtasks(Epic epic) {
        if(epic == null) {
            return;
        }
        for (Subtask subtask : epic.getSubtasks()) {
            //subtask.setEpic(null);
            subtasks.remove(subtask.getId());
        }
    }

//    @Override
//    public String toString() {
//        String result =  "TaskManager{" +
//                "\ntasks=[";
//        for (Task task : tasks.values()) {
//            result += '\n' + Objects.toString(task);
//        }
//        result += "]\nsubtasks=[";
//        for (Subtask subtask : subtasks.values()) {
//            result += '\n' + Objects.toString(subtask);
//        }
//        result += "]\nepics=[";
//        for (Epic epic : epics.values()) {
//            result += '\n' + Objects.toString(epic);
//        }
//        result += "]\n}";
//        return result;
//    }
}
