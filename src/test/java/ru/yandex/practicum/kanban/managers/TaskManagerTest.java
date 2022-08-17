package ru.yandex.practicum.kanban.managers;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;
    protected final LocalDateTime DEFAULT_TIME =
            LocalDateTime.of(2022, Month.JANUARY, 1, 1, 1, 30);

    // Тесты addTask()

    // Так как add меняет id, то может быть реализация, в которой equals будет false
    // (например возвращается копия, дополненная правильным id),
    // поэтому сравнение по не изменяющимся полям
    @Test
    public void addNewTaskTest() {
        final int duration = 10;
        Task task = new Task(0, "name", "description", TaskStatus.NEW, DEFAULT_TIME, duration);
        Task returnedTask = taskManager.addTask(task);
        assertNotNull(returnedTask, "Не возвращается задача");
        assertEquals(task.getName(), returnedTask.getName(), "Не совпадает имя");
        assertEquals(task.getDescription(), returnedTask.getDescription(), "Не совпадает описание");
        assertEquals(task.getStatus(), returnedTask.getStatus(), "Не совпадает статус");
        assertEquals(task.getStartTime(), returnedTask.getStartTime(), "Не совпадает время начала");
        assertEquals(task.getDuration(), returnedTask.getDuration(), "Не совпадает продолжительность");
    }

    @Test
    public void addNullTaskTest() {
        assertNull(taskManager.addTask(null));
    }

    // Тесты getTask()

    @Test
    public void getTaskFromEmptyListTest() {
        assertNull(taskManager.getTask(1));
    }

    @Test
    public void getTaskWithIncorrectIdTest() {
        Task task = new Task(0, "name", "description", TaskStatus.NEW);
        final int id = taskManager.addTask(task).getId();
        assertNull(taskManager.getTask(id + 1), "Задача добавлена");

        List<Task> history = taskManager.getHistory();
        assertEquals(0, history.size(), "Задача добавлена в историю");
    }

    @Test
    public void getTaskWithCorrectIdTest() {
        Task task = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        taskManager.addTask(new Task(0, "name1", "description1", TaskStatus.IN_PROGRESS));
        final int id = task.getId();
        Task returnedTask = taskManager.getTask(id);

        assertNotNull(returnedTask, "Задача не найдена по id");
        assertEquals(task, returnedTask, "Задачи не совпадают");

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "Задача не добавлена в историю");
        assertEquals(task, history.get(0), "В историю добавлена неверная задача");
    }

    // Тесты getTasks()

    @Test
    public void getTasksFromEmptyListTest() {
        List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Не возвращается список задач");
        assertEquals(0, tasks.size(), "Возвращается не пустой список задач");
    }

    @Test
    public void getTasksCorrectTest() {
        Task task = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        List<Task> returnedTasks = taskManager.getTasks();

        assertNotNull(returnedTasks, "Не возвращается список задач");
        assertEquals(1, returnedTasks.size(), "Возвращается неверное количество задач");
        assertEquals(task, returnedTasks.get(0), "Задачи не совпадают");
    }

    // Тесты updateTask()

    @Test
    public void updateTaskInEmptyListTest() {
        Task task = new Task(0, "name", "description", TaskStatus.NEW);
        assertFalse(taskManager.updateTask(task));
        assertEquals(0, taskManager.getTasks().size());
    }

    @Test
    public void updateNullTaskTest() {
        assertFalse(taskManager.updateTask(null));
        assertDoesNotThrow(() -> taskManager.updateTask(null));
    }

    @Test
    public void updateTaskWithIncorrectIdTest() {
        Task task = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        final int id = task.getId();
        Task updatedTask = new Task(id + 1, "new name", "new description", TaskStatus.IN_PROGRESS);
        assertFalse(taskManager.updateTask(updatedTask));
        assertEquals(task, taskManager.getTask(id));
    }

    @Test
    public void updateTaskWithCorrectIdTest() {
        Task task = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        final int id = task.getId();
        taskManager.addTask(new Task(0, "name1", "description1", TaskStatus.IN_PROGRESS));
        Task updatedTask = new Task(id, "new name", "new description", TaskStatus.IN_PROGRESS);
        taskManager.getTask(id);    // для добавления в историю
        assertTrue(taskManager.updateTask(updatedTask));

        List<Task> history = taskManager.getHistory();
        // в такой последовательности, потому что taskManager.getTask(id) сломает проверку истории
        assertEquals(updatedTask, history.get(0), "Задача не обновлена в истории");
        assertEquals(updatedTask, taskManager.getTask(id), "Задача не обновлена");
    }

    // Тесты removeTask()

    @Test
    public void removeTaskFromEmptyListTest() {
        assertDoesNotThrow(() -> taskManager.removeTask(1));
        assertFalse(taskManager.removeTask(1));
    }

    @Test
    public void removeTaskWithIncorrectIdTest() {
        Task task = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        final int id = task.getId();
        assertFalse(taskManager.removeTask(id + 1));
        assertEquals(1, taskManager.getTasks().size());
    }

    @Test
    public void removeTaskWithCorrectIdTest() {
        Task task = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        final int id = task.getId();
        Task anotherTask = taskManager.addTask(
                new Task(0, "name1", "description1", TaskStatus.IN_PROGRESS));
        assertTrue(taskManager.removeTask(id));
        List<Task> tasks = taskManager.getTasks();
        assertEquals(1, tasks.size(), "Задача не удалена");
        assertEquals(anotherTask, tasks.get(0), "Удалена не та задача");
    }

    // Тесты clearTasks()

    @Test
    public void clearTasksFromEmptyListTest() {
        assertDoesNotThrow(taskManager::clearTasks);
    }

    @Test
    public void clearTasksCorrectTest() {
        taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        taskManager.addTask(new Task(0, "name1", "description1", TaskStatus.IN_PROGRESS));
        taskManager.clearTasks();
        assertEquals(0, taskManager.getTasks().size());
    }

    // Тесты addEpic()
    @Test
    public void addNewEpicTest() {
        Epic epic = new Epic(0, "name", "description");
        Epic returnedEpic = taskManager.addEpic(epic);
        assertNotNull(returnedEpic, "Не возвращается задача");
        assertEquals(epic.getName(), returnedEpic.getName(), "Не совпадает имя");
        assertEquals(epic.getDescription(), returnedEpic.getDescription(), "Не совпадает описание");
        assertEquals(TaskStatus.NEW, returnedEpic.getStatus(), "Неверно рассчитан статус для нового эпика");
        assertNull(returnedEpic.getStartTime(), "Неверно инициализировано время начала");
        assertEquals(0, returnedEpic.getDuration(), "Неверно инициализирована продолжительность");
        assertEquals(0, epic.getSubtaskIds().size(), "Не пустой список подзадач у нового эпика");
    }

    @Test
    public void addNullEpicTest() {
        assertNull(taskManager.addEpic(null));
    }

    // Тесты getEpic()

    @Test
    public void getEpicFromEmptyListTest() {
        assertNull(taskManager.getEpic(1));
    }

    @Test
    public void getEpicWithIncorrectIdTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int id = epic.getId();
        assertNull(taskManager.getEpic(id + 1), "Задача добавлена");

        List<Task> history = taskManager.getHistory();
        assertEquals(0, history.size(), "Задача добавлена в историю");
    }

    @Test
    public void getEpicWithCorrectIdTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        taskManager.addEpic(new Epic(0, "name1", "description1"));
        final int id = epic.getId();
        Epic returnedEpic = taskManager.getEpic(id);

        assertNotNull(returnedEpic, "Задача не найдена по id");
        assertEquals(epic, returnedEpic, "Задачи не совпадают");

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "Задача не добавлена в историю");
        assertEquals(epic, history.get(0), "В историю добавлена неверная задача");
    }

    // Тесты getEpics()

    @Test
    public void getEpicsFromEmptyListTest() {
        List<Epic> epics = taskManager.getEpics();
        assertNotNull(epics, "Не возвращается список задач");
        assertEquals(0, epics.size(), "Возвращается не пустой список задач");
    }

    @Test
    public void getEpicsCorrectTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        List<Epic> returnedEpics = taskManager.getEpics();

        assertNotNull(returnedEpics, "Не возвращается список задач");
        assertEquals(1, returnedEpics.size(), "Возвращается неверное количество задач");
        assertEquals(epic, returnedEpics.get(0), "Задачи не совпадают");
    }

    // Тесты updateEpic()

    @Test
    public void updateEpicInEmptyListTest() {
        Epic epic = new Epic(0, "name", "description");
        assertFalse(taskManager.updateEpic(epic));
        assertEquals(0, taskManager.getEpics().size());
    }

    @Test
    public void updateNullEpicTest() {
        assertFalse(taskManager.updateEpic(null));
        assertDoesNotThrow(() -> taskManager.updateEpic(null));
    }

    @Test
    public void updateEpicWithIncorrectIdTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int id = epic.getId();
        Epic updatedEpic = new Epic(id + 1, "new name", "new description");
        assertFalse(taskManager.updateEpic(updatedEpic));
        assertEquals(epic, taskManager.getEpic(id));
    }

    @Test
    public void updateEpicWithCorrectIdTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int id = epic.getId();
        taskManager.addEpic(new Epic(0, "name1", "description1"));
        Epic updatedEpic = new Epic(id, "new name", "new description");
        taskManager.getEpic(id);
        assertTrue(taskManager.updateEpic(updatedEpic));

        List<Task> history = taskManager.getHistory();
        assertEquals(updatedEpic, history.get(0), "Задача не обновлена в истории");
        assertEquals(updatedEpic, taskManager.getEpic(id), "Задача не обновлена");
    }

    @Test
    public void updateEpicWithIncorrectSubtasksTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "epic name", "epic description"));
        final int epicId = epic.getId();
        taskManager.addSubtask(new Subtask(0, "name1", "description1", TaskStatus.NEW, epicId));
        taskManager.addSubtask(new Subtask(0, "name2", "description2", TaskStatus.DONE, epicId));
        List<Integer> epicSubtasks = taskManager.getEpic(epicId).getSubtaskIds();
        Epic updatedEpic = new Epic(epicId, "new name", "new description");
        assertTrue(taskManager.updateEpic(updatedEpic));
        assertEquals(epicSubtasks, taskManager.getEpic(epicId).getSubtaskIds(),
                "После обновления эпик не дополнился правильным списком подзадач");
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus(),
                "После обновления не восстановился статус эпика");
    }

    @Test
    public void updateEpicWithSameObjectTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "epic name", "epic description"));
        final int epicId = epic.getId();
        taskManager.addSubtask(new Subtask(0, "name1", "description1", TaskStatus.NEW, epicId));
        taskManager.addSubtask(new Subtask(0, "name2", "description2", TaskStatus.NEW, epicId));
        taskManager.updateEpic(epic);
        Epic returnedEpic = taskManager.getEpic(epicId);
        assertEquals(epic, returnedEpic, "Возвращается другой эпик");
        assertEquals(2, epic.getSubtaskIds().size(), "Изменилось количество подзадач эпика");
    }

    // Тесты removeEpic()

    @Test
    public void removeEpicFromEmptyListTest() {
        assertDoesNotThrow(() -> taskManager.removeEpic(1));
        assertFalse(taskManager.removeEpic(1));
    }

    @Test
    public void removeEpicWithIncorrectIdTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int id = epic.getId();
        assertFalse(taskManager.removeEpic(id + 1));
        assertEquals(1, taskManager.getEpics().size());
    }

    @Test
    public void removeEpicWithCorrectIdTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int id = epic.getId();
        Epic anotherEpic = taskManager.addEpic(new Epic(0, "name1", "description1"));
        taskManager.addSubtask(new Subtask(0, "name1", "description1", TaskStatus.NEW, id));
        taskManager.addSubtask(new Subtask(0, "name2", "description2", TaskStatus.NEW, id));
        assertTrue(taskManager.removeEpic(id));
        List<Epic> epics = taskManager.getEpics();
        assertEquals(1, epics.size(), "Задача не удалена");
        assertEquals(anotherEpic, epics.get(0), "Удалена не та задача");
        assertEquals(0, taskManager.getSubtasks().size(), "Не удалены подзадачи эпика");
    }

    // Тесты clearEpics()

    @Test
    public void clearEpicsFromEmptyListTest() {
        assertDoesNotThrow(taskManager::clearEpics);
    }

    @Test
    public void clearEpicsCorrectTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int id = epic.getId();
        taskManager.addSubtask(new Subtask(0, "name1", "description1", TaskStatus.NEW, id));
        taskManager.addSubtask(new Subtask(0, "name2", "description2", TaskStatus.NEW, id));
        taskManager.addEpic(new Epic(0, "name3", "description3"));
        taskManager.clearEpics();
        assertEquals(0, taskManager.getEpics().size(), "Не удалены эпики");
        assertEquals(0, taskManager.getSubtasks().size(), "Не удалены подзадачи");
    }

    // Тесты getEpicsSubtasks()

    @Test
    public void getSubtasksOfAbsentEpicTest() {
        assertNotNull(taskManager.getEpicsSubtasks(1), "Не возвращается список подзадач");
        assertEquals(0, taskManager.getEpicsSubtasks(1).size(),
                "Возвращается не пустой список");
    }

    @Test
    public void getSubtasksOfIncorrectEpicTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int id = epic.getId();
        taskManager.addSubtask(new Subtask(0, "name1", "description1", TaskStatus.NEW, id));
        assertEquals(0, taskManager.getEpicsSubtasks(id + 1).size());
    }

    @Test
    public void getSubtasksOfCorrectEpicTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int epicId = epic.getId();
        Subtask subtask = taskManager.addSubtask(
                new Subtask(0, "name1", "description1", TaskStatus.NEW, epicId));
        taskManager.addSubtask(new Subtask(0, "name2", "description2", TaskStatus.NEW, epicId));
        taskManager.addEpic(new Epic(0, "name3", "description3"));
        List<Subtask> subtasks = taskManager.getEpicsSubtasks(epicId);
        assertNotNull(subtasks, "Не возвращается список подзадач");
        assertEquals(2, subtasks.size(), "Возвращается список неверной длины");
        assertEquals(subtask, subtasks.get(0), "Возвращается неверная подзадача");
    }

    // Тесты addSubtask()

    @Test
    public void addNewSubtaskTest() {
        final int duration = 10;
        Epic epic = taskManager.addEpic(new Epic(0, "epic name", "epic description"));
        final int epicId = epic.getId();
        final int anotherEpicId = taskManager.addEpic(
                new Epic(0, "another epic name", "another epic description")).getId();
        taskManager.addSubtask(new Subtask(0, "another subtask name",
                "another subtask description", TaskStatus.NEW, anotherEpicId, DEFAULT_TIME, duration));
        Subtask subtask = new Subtask(0, "name", "description", TaskStatus.NEW, epicId);
        Subtask returnedSubtask = taskManager.addSubtask(subtask);
        assertNotNull(returnedSubtask, "Не возвращается задача");
        assertEquals(subtask.getName(), returnedSubtask.getName(), "Не совпадает имя");
        assertEquals(subtask.getDescription(), returnedSubtask.getDescription(), "Не совпадает описание");
        assertEquals(subtask.getStatus(), returnedSubtask.getStatus(), "Не совпадает статус");
        assertEquals(subtask.getEpicId(), returnedSubtask.getEpicId(), "Не совпадает id эпика");
        assertEquals(subtask.getStartTime(), returnedSubtask.getStartTime(), "Не совпадает время начала");
        assertEquals(subtask.getDuration(), returnedSubtask.getDuration(), "Не совпадает продолжительность");

        assertEquals(1, taskManager.getEpic(epicId).getSubtaskIds().size(),
                "Id подзадачи не добавлен в эпик");
        assertEquals(returnedSubtask.getId(), taskManager.getEpic(epicId).getSubtaskIds().get(0),
                "В эпик добавлен неверный id подзадачи");
    }

    @Test
    public void addNullSubtaskTest() {
        assertNull(taskManager.addSubtask(null));
    }

    @Test
    public void addSubtaskWithIncorrectEpicIdTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "epic name", "epic description"));
        final int epicId = epic.getId();
        assertNull(taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId + 1)));
    }

    // Тесты getSubtask()

    @Test
    public void getSubtaskFromEmptyListTest() {
        assertNull(taskManager.getSubtask(1));
    }

    @Test
    public void getSubtaskWithIncorrectIdTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        Subtask subtask = new Subtask(0, "name", "description", TaskStatus.NEW, epicId);
        final int id = taskManager.addSubtask(subtask).getId();
        assertNull(taskManager.getSubtask(id + 1), "Задача добавлена");

        List<Task> history = taskManager.getHistory();
        assertEquals(0, history.size(), "Задача добавлена в историю");
    }

    @Test
    public void getSubtaskWithCorrectIdTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        Subtask subtask = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        taskManager.addSubtask(
                new Subtask(0, "name1", "description1", TaskStatus.IN_PROGRESS, epicId));
        final int id = subtask.getId();
        Task returnedSubtask = taskManager.getSubtask(id);

        assertNotNull(returnedSubtask, "Задача не найдена по id");
        assertEquals(subtask, returnedSubtask, "Задачи не совпадают");

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "Задача не добавлена в историю");
        assertEquals(subtask, history.get(0), "В историю добавлена неверная задача");
    }


    // Тесты getSubtasks()

    @Test
    public void getSubtasksFromEmptyListTest() {
        List<Subtask> subtasks = taskManager.getSubtasks();
        assertNotNull(subtasks, "Не возвращается список задач");
        assertEquals(0, subtasks.size(), "Возвращается не пустой список задач");
    }

    @Test
    public void getSubtasksCorrectTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        Subtask subtask = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        List<Subtask> returnedSubtasks = taskManager.getSubtasks();

        assertNotNull(returnedSubtasks, "Не возвращается список задач");
        assertEquals(1, returnedSubtasks.size(), "Возвращается неверное количество задач");
        assertEquals(subtask, returnedSubtasks.get(0), "Задачи не совпадают");
    }

    // Тесты updateSubtask()

    @Test
    public void updateSubtaskInEmptyListTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        Subtask subtask = new Subtask(0, "name", "description", TaskStatus.NEW, epicId);
        assertFalse(taskManager.updateTask(subtask));
        assertEquals(0, taskManager.getSubtasks().size());
    }

    @Test
    public void updateNullSubtaskTest() {
        assertDoesNotThrow(() -> taskManager.updateSubtask(null));
        assertFalse(taskManager.updateSubtask(null));
    }

    @Test
    public void updateSubtaskWithIncorrectEpicTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        Subtask subtask = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        final int id = subtask.getId();
        assertFalse(taskManager.updateSubtask(
                new Subtask(id, "new name", "new description", TaskStatus.NEW, epicId + 1)));
        assertEquals(subtask, taskManager.getSubtask(id));
    }

    @Test
    public void updateSubtaskWithIncorrectIdTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        Subtask subtask = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        final int id = subtask.getId();
        Subtask updatedSubtask =
                new Subtask(id + 1, "new name", "new description", TaskStatus.IN_PROGRESS, epicId);
        assertFalse(taskManager.updateSubtask(updatedSubtask));
        assertEquals(subtask, taskManager.getSubtask(id), "Задача не обновлена");
    }

    @Test
    public void updateSubtaskWithCorrectIdTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        Subtask subtask = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        final int id = subtask.getId();
        taskManager.addSubtask(
                new Subtask(0, "name1", "description1", TaskStatus.IN_PROGRESS, epicId));
        Subtask updatedSubtask =
                new Subtask(id, "new name", "new description", TaskStatus.IN_PROGRESS, epicId);
        taskManager.getSubtask(id);
        assertTrue(taskManager.updateSubtask(updatedSubtask));

        List<Task> history = taskManager.getHistory();
        assertEquals(updatedSubtask, history.get(0), "Задача не обновлена в истории");
        assertEquals(updatedSubtask, taskManager.getSubtask(id));
    }

    // Тесты removeSubtask()

    @Test
    public void removeSubtaskFromEmptyListTest() {
        assertDoesNotThrow(() -> taskManager.removeSubtask(1));
        assertFalse(taskManager.removeSubtask(1));
    }

    @Test
    public void removeSubtaskWithIncorrectIdTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        Subtask subtask = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        final int id = subtask.getId();
        assertFalse(taskManager.removeSubtask(id + 1));
        assertEquals(1, taskManager.getSubtasks().size());
    }

    @Test
    public void removeSubtaskWithCorrectIdTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        Subtask subtask = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        final int id = subtask.getId();
        Subtask anotherSubtask = taskManager.addSubtask(
                new Subtask(0, "name1", "description1", TaskStatus.IN_PROGRESS, epicId));
        assertTrue(taskManager.removeSubtask(id));
        List<Subtask> subtasks = taskManager.getSubtasks();
        assertEquals(1, subtasks.size(), "Задача не удалена");
        assertEquals(anotherSubtask, subtasks.get(0), "Удалена не та задача");
        assertEquals(1, taskManager.getEpic(epicId).getSubtaskIds().size(),
                "Id подзадачи не удален из эпика");
        assertEquals(anotherSubtask.getId(), taskManager.getEpic(epicId).getSubtaskIds().get(0),
                "Из эпика удален id не той подзадачи");
    }

    // Тесты clearSubtasks()

    @Test
    public void clearSubtasksFromEmptyListTest() {
        assertDoesNotThrow(taskManager::clearSubtasks);
    }

    @Test
    public void clearSubtasksCorrectTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        taskManager.addSubtask(
                new Subtask(0, "name1", "description1", TaskStatus.IN_PROGRESS, epicId));
        taskManager.clearSubtasks();
        assertEquals(0, taskManager.getTasks().size(), "Задачи не удалены");
        assertEquals(0, taskManager.getEpic(epicId).getSubtaskIds().size(),
                "id подзадач не удалены из эпика");
    }

    // Тесты обновления статуса эпика - для разных сценариев

    // проверка на правильные статус, время начала и продолжительность при создании эпика - в addNewEpicTest
    @Test
    public void updateEpicStatusOnAddSubtaskTest() {
        int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика при статусах подзадач NEW");

        epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.DONE, epicId));
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.DONE, epicId));
        assertEquals(TaskStatus.DONE, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика при статусах подзадач DONE");

        epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.DONE, epicId));
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика при статусах подзадач NEW и DONE");

        epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.IN_PROGRESS, epicId));
        taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.IN_PROGRESS, epicId));
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика при статусах подзадач IN_PROGRESS");
    }

    @Test
    public void updateEpicTimeOnAddSubtasksTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME.withSecond(0).withNano(0);
        final int duration1 = 10;
        final LocalDateTime startTime2 = startTime1.plusMinutes(duration1 + 10);
        final int duration2 = 20;
        final LocalDateTime startTime3 = startTime2.plusMinutes(duration2 + 10);
        final int duration3 = 30;
        final LocalDateTime startTime4 = startTime3.plusMinutes(duration3 + 10);
        final int duration4 = 40;

        int epicId = taskManager.addEpic(new Epic(0, "name", "description")).getId();

        // добавление одной подзадачи
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId,
                startTime2, duration2));
        Epic epic = taskManager.getEpic(epicId);
        int duration = duration2;
        assertEquals(startTime2, epic.getStartTime(),
                "Неверно рассчитано начало эпика");
        assertEquals(duration, epic.getDuration(),
                "Неверно рассчитана длительность эпика");
        assertEquals(startTime2.plusMinutes(duration2), epic.getEndTime(),
                "Неверно рассчитано окончание эпика");

        // добавление в начало
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId,
                startTime1, duration1));
        epic = taskManager.getEpic(epicId);
        duration += duration1;
        assertEquals(startTime1, epic.getStartTime(),
                "Неверно рассчитано начало эпика");
        assertEquals(duration, epic.getDuration(), "Неверно рассчитана длительность эпика");
        assertEquals(startTime2.plusMinutes(duration2), epic.getEndTime(),
                "Неверно рассчитано окончание эпика");

        // добавление в конец
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId,
                startTime4, duration4));
        epic = taskManager.getEpic(epicId);
        duration += duration4;
        assertEquals(startTime1, epic.getStartTime(),
                "Неверно рассчитано начало эпика");
        assertEquals(duration, epic.getDuration(), "Неверно рассчитана длительность эпика");
        assertEquals(startTime4.plusMinutes(duration4), epic.getEndTime(),
                "Неверно рассчитано окончание эпика");

        // добавление в середину
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId,
                startTime3, duration3));
        epic = taskManager.getEpic(epicId);
        duration += duration3;
        assertEquals(startTime1, epic.getStartTime(),
                "Неверно рассчитано начало эпика");
        assertEquals(duration, epic.getDuration(), "Неверно рассчитана длительность эпика");
        assertEquals(startTime4.plusMinutes(duration4), epic.getEndTime(),
                "Неверно рассчитано окончание эпика");
    }

    @Test
    public void updateEpicStatusOnUpdateSubtaskTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        final int subtaskId1 = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.IN_PROGRESS, epicId)).getId();
        final int subtaskId2 = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.IN_PROGRESS, epicId)).getId();

        taskManager.updateSubtask(
                new Subtask(subtaskId1, "name", "description", TaskStatus.NEW, epicId));
        taskManager.updateSubtask(
                new Subtask(subtaskId2, "name", "description", TaskStatus.NEW, epicId));
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика при статусах подзадач NEW");

        taskManager.updateSubtask(
                new Subtask(subtaskId1, "name", "description", TaskStatus.DONE, epicId));
        taskManager.updateSubtask(
                new Subtask(subtaskId2, "name", "description", TaskStatus.DONE, epicId));
        assertEquals(TaskStatus.DONE, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика при статусах подзадач DONE");

        taskManager.updateSubtask(
                new Subtask(subtaskId1, "name", "description", TaskStatus.NEW, epicId));
        taskManager.updateSubtask(
                new Subtask(subtaskId2, "name", "description", TaskStatus.DONE, epicId));
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика при статусах подзадач NEW и DONE");

        taskManager.updateSubtask(
                new Subtask(subtaskId1, "name", "description", TaskStatus.IN_PROGRESS, epicId));
        taskManager.updateSubtask(
                new Subtask(subtaskId2, "name", "description", TaskStatus.IN_PROGRESS, epicId));
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика при статусах подзадач IN_PROGRESS");
    }

    @Test
    public void updateEpicTimeOnUpdateSubtaskTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME.withSecond(0).withNano(0);
        final int duration1 = 10;
        final LocalDateTime startTime2 = startTime1.plusMinutes(duration1 + 10);
        final int duration2 = 20;
        final LocalDateTime startTime3 = startTime2.plusMinutes(duration2 + 10);
        final int duration3 = 30;
        final LocalDateTime startTime4 = startTime3.plusMinutes(duration3 + 10);
        final int duration4 = 40;

        int epicId = taskManager.addEpic(new Epic(0, "name", "description")).getId();
        final int subtaskId1 = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId)).getId();
        final int subtaskId2 = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId)).getId();
        final int subtaskId3 = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId)).getId();
        final int subtaskId4 = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId)).getId();

        // обновление одной подзадачи
        taskManager.updateSubtask(new Subtask(subtaskId2, "name", "description", TaskStatus.NEW,
                epicId, startTime2, duration2));
        Epic epic = taskManager.getEpic(epicId);
        int duration = duration2;
        assertEquals(startTime2, epic.getStartTime(),
                "Неверно рассчитано начало эпика");
        assertEquals(duration, epic.getDuration(), "Неверно рассчитана длительность эпика");
        assertEquals(startTime2.plusMinutes(duration2), epic.getEndTime(),
                "Неверно рассчитано окончание эпика");

        // обновление в начале
        taskManager.updateSubtask(new Subtask(subtaskId1, "name", "description", TaskStatus.NEW,
                epicId, startTime1, duration1));
        epic = taskManager.getEpic(epicId);
        duration += duration1;
        assertEquals(startTime1, epic.getStartTime(),
                "Неверно рассчитано начало эпика");
        assertEquals(duration, epic.getDuration(), "Неверно рассчитана длительность эпика");
        assertEquals(startTime2.plusMinutes(duration2), epic.getEndTime(),
                "Неверно рассчитано окончание эпика");

        // обновление в конце
        taskManager.updateSubtask(new Subtask(subtaskId4, "name", "description", TaskStatus.NEW,
                epicId, startTime4, duration4));
        epic = taskManager.getEpic(epicId);
        duration += duration4;
        assertEquals(startTime1, epic.getStartTime(),
                "Неверно рассчитано начало эпика");
        assertEquals(duration, epic.getDuration(), "Неверно рассчитана длительность эпика");
        assertEquals(startTime4.plusMinutes(duration4), epic.getEndTime(),
                "Неверно рассчитано окончание эпика");

        // обновление в середине
        taskManager.updateSubtask(new Subtask(subtaskId3, "name", "description", TaskStatus.NEW,
                epicId, startTime3, duration3));
        epic = taskManager.getEpic(epicId);
        duration += duration3;
        assertEquals(startTime1, epic.getStartTime(),
                "Неверно рассчитано начало эпика");
        assertEquals(duration, epic.getDuration(), "Неверно рассчитана длительность эпика");
        assertEquals(startTime4.plusMinutes(duration4), epic.getEndTime(),
                "Неверно рассчитано окончание эпика");

        // обновление на подзадачи без данных о времени
        taskManager.updateSubtask(new Subtask(subtaskId1, "name", "description",
                TaskStatus.NEW, epicId));
        taskManager.updateSubtask(new Subtask(subtaskId2, "name", "description",
                TaskStatus.NEW, epicId));
        taskManager.updateSubtask(new Subtask(subtaskId3, "name", "description",
                TaskStatus.NEW, epicId));
        taskManager.updateSubtask(new Subtask(subtaskId4, "name", "description",
                TaskStatus.NEW, epicId));
        epic = taskManager.getEpic(epicId);
        assertNull(epic.getStartTime(), "Неверно рассчитано начало эпика");
        assertEquals(0, epic.getDuration(),
                "Неверно рассчитана длительность эпика");
        assertNull(epic.getEndTime(),"Неверно рассчитано окончание эпика");
    }

    @Test
    public void updateEpicStatusOnRemoveSubtaskTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        final int subtaskId1 = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId)).getId();
        final int subtaskId2 = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId)).getId();

        int subtaskId3 = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.IN_PROGRESS, epicId)).getId();
        taskManager.removeSubtask(subtaskId3);
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика при статусах подзадач NEW");

        taskManager.updateSubtask(
                new Subtask(subtaskId1, "name", "description", TaskStatus.DONE, epicId));
        taskManager.updateSubtask(
                new Subtask(subtaskId2, "name", "description", TaskStatus.DONE, epicId));
        subtaskId3 = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.IN_PROGRESS, epicId)).getId();
        taskManager.removeSubtask(subtaskId3);
        assertEquals(TaskStatus.DONE, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика при статусах подзадач DONE");

        taskManager.updateSubtask(
                new Subtask(subtaskId1, "name", "description", TaskStatus.NEW, epicId));
        taskManager.updateSubtask(
                new Subtask(subtaskId2, "name", "description", TaskStatus.DONE, epicId));
        subtaskId3 = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.DONE, epicId)).getId();
        taskManager.removeSubtask(subtaskId3);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика при статусах подзадач NEW и DONE");

        taskManager.updateSubtask(
                new Subtask(subtaskId1, "name", "description", TaskStatus.IN_PROGRESS, epicId));
        taskManager.updateSubtask(
                new Subtask(subtaskId2, "name", "description", TaskStatus.IN_PROGRESS, epicId));
        subtaskId3 = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.DONE, epicId)).getId();
        taskManager.removeSubtask(subtaskId3);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика при статусах подзадач IN_PROGRESS");

        taskManager.removeSubtask(subtaskId1);
        taskManager.removeSubtask(subtaskId2);
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика без подзадач");
    }

    @Test
    public void updateEpicTimeOnRemoveSubtaskTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME.withSecond(0).withNano(0);
        final int duration1 = 10;
        final LocalDateTime startTime2 = startTime1.plusMinutes(duration1 + 10);
        final int duration2 = 20;
        final LocalDateTime startTime3 = startTime2.plusMinutes(duration2 + 10);
        final int duration3 = 30;
        final LocalDateTime startTime4 = startTime3.plusMinutes(duration3 + 10);
        final int duration4 = 40;

        int epicId = taskManager.addEpic(new Epic(0, "name", "description")).getId();
        final int subtaskId1 = taskManager.addSubtask(new Subtask(0, "name", "description",
                TaskStatus.NEW, epicId, startTime1, duration1)).getId();
        final int subtaskId2 = taskManager.addSubtask(new Subtask(0, "name", "description",
                TaskStatus.NEW, epicId, startTime2, duration2)).getId();
        final int subtaskId3 = taskManager.addSubtask(new Subtask(0, "name", "description",
                TaskStatus.NEW, epicId, startTime3, duration3)).getId();
        final int subtaskId4 = taskManager.addSubtask(new Subtask(0, "name", "description",
                TaskStatus.NEW, epicId, startTime4, duration4)).getId();

        // удаление из начала
        taskManager.removeSubtask(subtaskId1);
        Epic epic = taskManager.getEpic(epicId);
        int duration = duration2 + duration3 + duration4;
        assertEquals(startTime2, epic.getStartTime(),
                "Неверно рассчитано начало эпика");
        assertEquals(duration, epic.getDuration(), "Неверно рассчитана длительность эпика");
        assertEquals(startTime4.plusMinutes(duration4), epic.getEndTime(),
                "Неверно рассчитано окончание эпика");

        // удаление из середины
        taskManager.removeSubtask(subtaskId3);
        epic = taskManager.getEpic(epicId);
        duration -= duration3;
        assertEquals(startTime2, epic.getStartTime(),
                "Неверно рассчитано начало эпика");
        assertEquals(duration, epic.getDuration(), "Неверно рассчитана длительность эпика");
        assertEquals(startTime4.plusMinutes(duration4), epic.getEndTime(),
                "Неверно рассчитано окончание эпика");

        // удаление в конце
        taskManager.removeSubtask(subtaskId4);
        epic = taskManager.getEpic(epicId);
        duration -= duration4;
        assertEquals(startTime2, epic.getStartTime(),
                "Неверно рассчитано начало эпика");
        assertEquals(duration, epic.getDuration(),"Неверно рассчитана длительность эпика");
        assertEquals(startTime2.plusMinutes(duration2), epic.getEndTime(),
                "Неверно рассчитано окончание эпика");

        // удаление последней подзадачи
        taskManager.removeSubtask(subtaskId2);
        epic = taskManager.getEpic(epicId);
        assertNull(epic.getStartTime(), "Неверно рассчитано начало эпика");
        assertEquals(0, epic.getDuration(),
                "Неверно рассчитана длительность эпика");
        assertNull(epic.getEndTime(), "Неверно рассчитано окончание эпика");
    }

    @Test
    public void updateEpicStatusOnClearSubtasksTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.IN_PROGRESS, epicId));
        taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.DONE, epicId));

        taskManager.clearSubtasks();
        assertEquals(TaskStatus.NEW, taskManager.getEpic(epicId).getStatus(),
                "Неверно рассчитан статус эпика без подзадач");
    }

    @Test
    public void updateEpicTimeOnClearSubtasksTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME;
        final int duration1 = 10;
        final LocalDateTime startTime2 = startTime1.plusMinutes(duration1 + 10);
        final int duration2 = 20;

        int epicId = taskManager.addEpic(new Epic(0, "name", "description")).getId();
        taskManager.addSubtask(new Subtask(0, "name", "description",
                TaskStatus.NEW, epicId, startTime1, duration1));
        taskManager.addSubtask(new Subtask(0, "name", "description",
                TaskStatus.NEW, epicId, startTime2, duration2));

        taskManager.clearSubtasks();
        Epic epic = taskManager.getEpic(epicId);
        assertNull(epic.getStartTime(), "Неверно рассчитано начало эпика");
        assertEquals(0, epic.getDuration(),
                "Неверно рассчитана длительность эпика");
        assertNull(epic.getEndTime(), "Неверно рассчитано окончание эпика");
    }

    @Test
    public void getPrioritizedTasksOnAddAndRemoveTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME;
        final int duration1 = 10;
        final LocalDateTime startTime2 = startTime1.plusMinutes(duration1 + 10);
        final int duration2 = 20;
        final LocalDateTime startTime3 = startTime2.plusMinutes(duration2 + 10);
        final int duration3 = 30;
        final LocalDateTime startTime4 = startTime3.plusMinutes(duration3 + 10);
        final int duration4 = 40;

        assertNotNull(taskManager.getPrioritizedTasks(), "Не возвращается список задач");
        final int epicId = taskManager.addEpic(new Epic(0,"name", "description")).getId();
        // задача без метки времени
        taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        // первая задача с меткой времени
        Task task1 = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW,
                startTime2, duration2));
        // задача в конце
        Task task2 = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW,
                startTime4, duration4));
        // подзадаче без метки времени
        taskManager.addSubtask(new Subtask(0, "name", "description",
                TaskStatus.NEW, epicId));
        // подзадача в начале
        Subtask subtask1 = taskManager.addSubtask(new Subtask(0, "name", "description",
                TaskStatus.NEW, epicId, startTime1, duration1));
        // подзадача в середине
        Subtask subtask2 = taskManager.addSubtask(new Subtask(0, "name", "description",
                TaskStatus.NEW, epicId, startTime3, duration3));

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(6, prioritizedTasks.size(), "Возвращается неверное количество задач");
        assertArrayEquals(new Task[] {subtask1, task1, subtask2, task2},
                Arrays.copyOf(prioritizedTasks.toArray(), 4));

        // удаление задачи в середине
        taskManager.removeTask(task1.getId());
        prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(5, prioritizedTasks.size(), "Возвращается неверное количество задач");
        assertArrayEquals(new Task[] {subtask1, subtask2, task2},
                Arrays.copyOf(prioritizedTasks.toArray(), 3));

        // удаление задачи в конце
        taskManager.removeTask(task2.getId());
        prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(4, prioritizedTasks.size(), "Возвращается неверное количество задач");
        assertArrayEquals(new Task[] {subtask1, subtask2},
                Arrays.copyOf(prioritizedTasks.toArray(), 2));

        // удаление подзадачи в начале
        taskManager.removeSubtask(subtask1.getId());
        prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(3, prioritizedTasks.size(), "Возвращается неверное количество задач");
        assertEquals(subtask2, prioritizedTasks.get(0));

        // удаление последней подзадачи с меткой времени
        taskManager.removeSubtask(subtask2.getId());
        prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.size(), "Возвращается неверное количество задач");
    }

    @Test
    public void getPrioritizedTasksOnUpdateTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME;
        final int duration1 = 10;
        final LocalDateTime startTime2 = startTime1.plusMinutes(duration1 + 10);
        final int duration2 = 20;
        final LocalDateTime startTime3 = startTime2.plusMinutes(duration2 + 10);
        final int duration3 = 30;
        final LocalDateTime startTime4 = startTime3.plusMinutes(duration3 + 10);
        final int duration4 = 40;

        assertNotNull(taskManager.getPrioritizedTasks(), "Не возвращается список задач");
        final int epicId = taskManager.addEpic(new Epic(0,"name", "description")).getId();
        int[] taskIds = new int[3];
        int[] subtaskIds = new int[3];
        for (int i = 0; i < 3; i++) {
            taskIds[i] = taskManager.addTask(
                    new Task(0, "name", "description", TaskStatus.NEW)).getId();
            subtaskIds[i] = taskManager.addSubtask(new Subtask(0, "name", "description",
                    TaskStatus.NEW, epicId)).getId();
        }
        // первая задача с меткой времени
        Task task1 = new Task(taskIds[0], "name", "description", TaskStatus.NEW,
                startTime2, duration2);
        taskManager.updateTask(task1);
        // задача в конце
        Task task2 = new Task(taskIds[1], "name", "description", TaskStatus.NEW,
                startTime4, duration4);
        taskManager.updateTask(task2);
        // подзадача в начале
        Subtask subtask1 = new Subtask(subtaskIds[0], "name", "description",
                TaskStatus.NEW, epicId, startTime1, duration1);
        taskManager.updateSubtask(subtask1);
        // подзадача в середине
        Subtask subtask2 = new Subtask(subtaskIds[1], "name", "description",
                TaskStatus.NEW, epicId, startTime3, duration3);
        taskManager.updateSubtask(subtask2);

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(6, prioritizedTasks.size(), "Возвращается неверное количество задач");
        assertArrayEquals(new Task[] {subtask1, task1, subtask2, task2},
                Arrays.copyOf(prioritizedTasks.toArray(), 4));
    }

    @Test
    public void getPrioritizedTasksOnClearTasksTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME;
        final int duration1 = 10;
        final LocalDateTime startTime2 = startTime1.plusMinutes(duration1 + 10);
        final int duration2 = 20;

        taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW,
                startTime1, duration1));
        taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW,
                startTime2, duration2));
        taskManager.clearTasks();

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(0, prioritizedTasks.size(), "Возвращается неверное количество задач");
    }

    @Test
    public void getPrioritizedTasksOnClearSubtasksTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME;
        final int duration1 = 10;
        final LocalDateTime startTime2 = startTime1.plusMinutes(duration1 + 10);
        final int duration2 = 20;

        final int epicId = taskManager.addEpic(new Epic(0,"name", "description")).getId();
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId,
                startTime1, duration1));
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId,
                startTime2, duration2));
        taskManager.clearSubtasks();

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(0, prioritizedTasks.size(), "Возвращается неверное количество задач");
    }

    @Test
    public void getPrioritizedTasksOnRemoveEpicTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME;
        final int duration1 = 10;
        final LocalDateTime startTime2 = startTime1.plusMinutes(duration1 + 10);
        final int duration2 = 20;

        final int epicId = taskManager.addEpic(new Epic(0,"name", "description")).getId();
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId,
                startTime1, duration1));
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId,
                startTime2, duration2));
        taskManager.removeEpic(epicId);

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(0, prioritizedTasks.size(), "Возвращается неверное количество задач");
    }

    @Test
    public void getPrioritizedTasksOnClearEpicsTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME;
        final int duration1 = 10;
        final LocalDateTime startTime2 = startTime1.plusMinutes(duration1 + 10);
        final int duration2 = 20;

        final int epicId = taskManager.addEpic(new Epic(0,"name", "description")).getId();
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId,
                startTime1, duration1));
        taskManager.addSubtask(new Subtask(0, "name", "description", TaskStatus.NEW, epicId,
                startTime2, duration2));
        taskManager.clearEpics();

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(0, prioritizedTasks.size(), "Возвращается неверное количество задач");
    }
}