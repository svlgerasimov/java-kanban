package ru.yandex.practicum.kanban.managers;

import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;

import java.util.List;

public interface TaskManager {
    List<Task> getTasks();

    void clearTasks();

    //возвращает задачу по идентификатору или null, если задачи с таким идентификатором нет
    Task getTask(int id);

    //добавляет задачу, назначая ей id
    Task addTask(Task task);

    //обновляет задачу, если задача с таким id есть
    void updateTask(Task task);

    void removeTask(int id);

    List<Subtask> getSubtasks();

    void clearSubtasks();

    //возвращает подзадачу по идентификатору или null, если задачи с таким идентификатором нет
    Subtask getSubtask(int id);

    //добавляет подзадачу, если есть эпик, в который её нужно добавить
    Subtask addSubtask(Subtask subtask);

    //обновляет подзадачу, если подзадача с таким id есть, и она относится к тому же эпику
    void updateSubtask(Subtask subtask);

    void removeSubtask(int id);

    List<Epic> getEpics();

    void clearEpics();

    //возвращает эпик по идентификатору или null, если эпика с таким идентификатором нет
    Epic getEpic(int id);

    Epic addEpic(Epic epic);

    //обновляет эпик, если эпик с таким id есть
    void updateEpic(Epic epic);

    void removeEpic(int id);

    List<Subtask> getEpicsSubtasks(int epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
