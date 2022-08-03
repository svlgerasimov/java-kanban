package ru.yandex.practicum.kanban.managers.inmemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kanban.managers.Managers;
import ru.yandex.practicum.kanban.managers.TaskManager;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
    }


    // Тесты addTask()

    //Так как add меняет id, то может быть реализация, в которой equals будет false,
    //поэтому сравнение по не изменяющимся полям
    @Test
    public void addNewTaskTest() {
        Task task = new Task(0, "name", "description", TaskStatus.NEW);
        Task returnedTask = taskManager.addTask(task);
        assertNotNull(returnedTask, "Возвращается null");
        assertEquals(task.getName(), returnedTask.getName(), "Не совпадает имя");
        assertEquals(task.getDescription(), returnedTask.getDescription(), "Не совпадает описание");
        assertEquals(task.getStatus(), returnedTask.getStatus(), "Не совпадает статус");
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
        assertNull(taskManager.getTask(id + 1));
    }

    @Test
    public void getTaskWithCorrectIdTest() {
        Task task = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        taskManager.addTask(new Task(0, "name1", "description1", TaskStatus.IN_PROGRESS));
        final int id = task.getId();
        Task returnedTask = taskManager.getTask(id);

        assertNotNull(returnedTask, "Задача не найдена по id");
        assertEquals(task, returnedTask, "Задачи не совпадают");
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
        taskManager.updateTask(task);
        assertEquals(0, taskManager.getTasks().size());
    }

    @Test
    public void updateNullTaskTest() {
        Task task = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        final int id = task.getId();
        taskManager.updateTask(null);
        assertEquals(task, taskManager.getTask(id));
    }

    @Test
    public void updateTaskWithIncorrectIdTest() {
        Task task = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        final int id = task.getId();
        Task updatedTask = new Task(id + 1, "new name", "new description", TaskStatus.IN_PROGRESS);
        taskManager.updateTask(updatedTask);
        assertEquals(task, taskManager.getTask(id));
    }

    @Test
    public void updateTaskWithCorrectIdTest() {
        Task task = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        final int id = task.getId();
        taskManager.addTask(new Task(0, "name1", "description1", TaskStatus.IN_PROGRESS));
        Task updatedTask = new Task(id, "new name", "new description", TaskStatus.IN_PROGRESS);
        taskManager.updateTask(updatedTask);
        assertEquals(updatedTask, taskManager.getTask(id));
    }

    // Тесты removeTask()

    @Test
    public void removeTaskFromEmptyListTest() {
        assertDoesNotThrow(() -> taskManager.removeTask(1));
    }

    @Test
    public void removeTaskWithIncorrectIdTest() {
        Task task = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        final int id = task.getId();
        taskManager.removeTask(id + 1);
        assertEquals(1, taskManager.getTasks().size());
    }

    @Test
    public void removeTaskWithCorrectIdTest() {
        Task task = taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        final int id = task.getId();
        Task anotherTask = taskManager.addTask(
                new Task(0, "name1", "description1", TaskStatus.IN_PROGRESS));
        taskManager.removeTask(id);
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
        assertNotNull(returnedEpic, "Возвращается null");
        assertEquals(epic.getName(), returnedEpic.getName(), "Не совпадает имя");
        assertEquals(epic.getDescription(), returnedEpic.getDescription(), "Не совпадает описание");
        assertEquals(epic.getStatus(), TaskStatus.NEW, "Неверно рассчитан статус для нового эпика");
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
        assertNull(taskManager.getEpic(id + 1));
    }

    @Test
    public void getEpicWithCorrectIdTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        taskManager.addEpic(new Epic(0, "name1", "description1"));
        final int id = epic.getId();
        Epic returnedEpic = taskManager.getEpic(id);

        assertNotNull(returnedEpic, "Задача не найдена по id");
        assertEquals(epic, returnedEpic, "Задачи не совпадают");
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
        taskManager.updateEpic(epic);
        assertEquals(0, taskManager.getEpics().size());
    }

    @Test
    public void updateNullEpicTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int id = epic.getId();
        taskManager.updateEpic(null);
        assertEquals(epic, taskManager.getEpic(id));
    }

    @Test
    public void updateEpicWithIncorrectIdTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int id = epic.getId();
        Epic updatedEpic = new Epic(id + 1, "new name", "new description");
        taskManager.updateEpic(updatedEpic);
        assertEquals(epic, taskManager.getEpic(id));
    }

    @Test
    public void updateEpicWithCorrectIdTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int id = epic.getId();
        taskManager.addEpic(new Epic(0, "name1", "description1"));
        Epic updatedEpic = new Epic(id, "new name", "new description");
        taskManager.updateEpic(updatedEpic);
        assertEquals(updatedEpic, taskManager.getEpic(id));
    }

    @Test
    public void updateEpicWithIncorrectSubtasksTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "epic name", "epic description"));
        final int epicId = epic.getId();
        taskManager.addSubtask(new Subtask(0, "name1", "description1", TaskStatus.NEW, epicId));
        taskManager.addSubtask(new Subtask(0, "name2", "description2", TaskStatus.DONE, epicId));
        List<Integer> epicSubtasks = taskManager.getEpic(epicId).getSubtaskIds();
        Epic updatedEpic = new Epic(epicId, "new name", "new description");
        taskManager.updateEpic(updatedEpic);
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
    }

    @Test
    public void removeEpicWithIncorrectIdTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int id = epic.getId();
        taskManager.removeEpic(id + 1);
        assertEquals(1, taskManager.getEpics().size());
    }

    @Test
    public void removeEpicWithCorrectIdTest() {
        Epic epic = taskManager.addEpic(new Epic(0, "name", "description"));
        final int id = epic.getId();
        Epic anotherEpic = taskManager.addEpic(new Epic(0, "name1", "description1"));
        taskManager.addSubtask(new Subtask(0, "name1", "description1", TaskStatus.NEW, id));
        taskManager.addSubtask(new Subtask(0, "name2", "description2", TaskStatus.NEW, id));
        taskManager.removeEpic(id);
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
        Epic epic = taskManager.addEpic(new Epic(0, "epic name", "epic description"));
        final int epicId = epic.getId();
        final int anotherEpicId = taskManager.addEpic(
                new Epic(0, "another epic name", "another epic description")).getId();
        taskManager.addSubtask(new Subtask(0, "another subtask name",
                "another subtask description", TaskStatus.NEW, anotherEpicId));
        Subtask subtask = new Subtask(0, "name", "description", TaskStatus.NEW, epicId);
        Subtask returnedSubtask = taskManager.addSubtask(subtask);
        assertNotNull(returnedSubtask, "Возвращается null");
        assertEquals(subtask.getName(), returnedSubtask.getName(), "Не совпадает имя");
        assertEquals(subtask.getDescription(), returnedSubtask.getDescription(), "Не совпадает описание");
        assertEquals(subtask.getStatus(), returnedSubtask.getStatus(), "Не совпадает статус");
        assertEquals(subtask.getEpicId(), returnedSubtask.getEpicId(), "Не совпадает id эпика");

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
        assertNull(taskManager.getSubtask(id + 1));
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
        taskManager.updateTask(subtask);
        assertEquals(0, taskManager.getSubtasks().size());
    }

    @Test
    public void updateNullSubtaskTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        Subtask subtask = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        final int id = subtask.getId();
        taskManager.updateTask(null);
        assertEquals(subtask, taskManager.getSubtask(id));
    }

    @Test
    public void updateSubtaskWithIncorrectEpicTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        Subtask subtask = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        final int id = subtask.getId();
        taskManager.updateSubtask(
                new Subtask(id, "new name", "new description", TaskStatus.NEW, epicId + 1));
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
        taskManager.updateTask(updatedSubtask);
        assertEquals(subtask, taskManager.getSubtask(id));
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
        taskManager.updateSubtask(updatedSubtask);
        assertEquals(updatedSubtask, taskManager.getSubtask(id));
    }

    // Тесты removeSubtask()

    @Test
    public void removeSubtaskFromEmptyListTest() {
        assertDoesNotThrow(() -> taskManager.removeSubtask(1));
    }

    @Test
    public void removeSubtaskWithIncorrectIdTest() {
        final int epicId = taskManager.addEpic(
                new Epic(0, "epic name", "epic description")).getId();
        Subtask subtask = taskManager.addSubtask(
                new Subtask(0, "name", "description", TaskStatus.NEW, epicId));
        final int id = subtask.getId();
        taskManager.removeSubtask(id + 1);
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
        taskManager.removeSubtask(id);
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

    // Тесты обновления статуса эпика - для разных сценариев;
    // недостаточно тестировать updateEpicStatus, т.к. нет гарантий, что при рефакторинге он не пропадёт откуда-нибудь

    // проверка на правильный статус при создании эпика - в addNewEpicTest
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

//    @Test
//    void getHistory() {
//    }
}