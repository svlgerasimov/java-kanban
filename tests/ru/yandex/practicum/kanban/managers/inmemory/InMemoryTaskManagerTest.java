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


//    @Test
//    void addTask() {
//    }
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

//    @Test
//    void getTask() {
//    }

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
        Task task = new Task(0, "name", "description", TaskStatus.NEW);
        task = taskManager.addTask(task);
        taskManager.addTask(new Task(0, "name1", "description1", TaskStatus.IN_PROGRESS));
        final int id = task.getId();
        Task returnedTask = taskManager.getTask(id);

        assertNotNull(returnedTask, "Задача не найдена по id");
        assertEquals(task, returnedTask, "Задачи не совпадают");
    }


//    @Test
//    void getTasks() {
//    }

    @Test
    public void getTasksFromEmptyListTest() {
        List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Не возвращается список задач");
        assertEquals(0, tasks.size(), "Возвращается не пустой список задач");
    }

    @Test
    public void getTasksCorrectTest() {
        Task task = new Task(0, "name", "description", TaskStatus.NEW);
        task = taskManager.addTask(task);
        List<Task> returnedTasks = taskManager.getTasks();

        assertNotNull(returnedTasks, "Не возвращается список задач");
        assertEquals(1, returnedTasks.size(), "Возвращается неверное количество задач");
        assertEquals(task, returnedTasks.get(0), "Задачи не совпадают");
    }

//    @Test
//    void updateTask() {
//    }

    @Test
    public void updateTaskInEmptyListTest() {
        Task task = new Task(0, "name", "description", TaskStatus.NEW);
        taskManager.updateTask(task);
        assertEquals(0, taskManager.getTasks().size());
    }

    @Test
    public void updateNullTaskTest() {
        Task task = new Task(0, "name", "description", TaskStatus.NEW);
        final int id = taskManager.addTask(task).getId();
        taskManager.updateTask(null);
        assertEquals(task, taskManager.getTask(id));
    }

    @Test
    public void updateTaskWithIncorrectIdTest() {
        Task task = new Task(0, "name", "description", TaskStatus.NEW);
        task = taskManager.addTask(task);
        final int id = task.getId();
        Task updatedTask = new Task(id + 1, "new name", "new description", TaskStatus.IN_PROGRESS);
        taskManager.updateTask(updatedTask);
        assertEquals(task, taskManager.getTask(id));
    }

    @Test
    public void updateTaskWithCorrectIdTest() {
        Task task = new Task(0, "name", "description", TaskStatus.NEW);
        final int id = taskManager.addTask(task).getId();
        taskManager.addTask(new Task(0, "name1", "description1", TaskStatus.IN_PROGRESS));
        Task updatedTask = new Task(id, "new name", "new description", TaskStatus.IN_PROGRESS);
        taskManager.updateTask(updatedTask);
        assertEquals(updatedTask, taskManager.getTask(id));
    }

//    @Test
//    void removeTask() {
//    }

    @Test
    public void removeTaskFromEmptyListTest() {
        assertDoesNotThrow(() -> taskManager.removeTask(1));
    }

    @Test
    public void removeTaskWithIncorrectIdTest() {
        Task task = new Task(0, "name", "description", TaskStatus.NEW);
        final int id = taskManager.addTask(task).getId();
        taskManager.removeTask(id + 1);
        assertEquals(1, taskManager.getTasks().size());
    }

    @Test
    public void removeTaskWithCorrectIdTest() {
        Task task = new Task(0, "name", "description", TaskStatus.NEW);
        final int id = taskManager.addTask(task).getId();
        Task anotherTask = new Task(0, "name1", "description1", TaskStatus.IN_PROGRESS);
        anotherTask = taskManager.addTask(anotherTask);
        taskManager.removeTask(id);
        List<Task> tasks = taskManager.getTasks();
        assertEquals(1, tasks.size(), "Задача не удалена");
        assertEquals(anotherTask, tasks.get(0), "Удалена не та задача");
    }

//    @Test
//    void clearTasks() {
//    }

    @Test
    public void clearTasksFromEmptyListTest() {
        assertDoesNotThrow(() -> taskManager.clearTasks());
    }

    @Test
    public void clearTasksCorrectTest() {
        taskManager.addTask(new Task(0, "name", "description", TaskStatus.NEW));
        taskManager.addTask(new Task(0, "name1", "description1", TaskStatus.IN_PROGRESS));
        taskManager.clearTasks();
        assertEquals(0, taskManager.getTasks().size());
    }

//    @Test
//    void addEpic() {
//    }
    @Test
    public void addNewEpicTest() {
        Epic epic = new Epic(0, "name", "description");
        Epic returnedEpic = taskManager.addEpic(epic);
        assertNotNull(returnedEpic, "Возвращается null");
        assertEquals(epic.getName(), returnedEpic.getName(), "Не совпадает имя");
        assertEquals(epic.getDescription(), returnedEpic.getDescription(), "Не совпадает описание");
        assertEquals(epic.getStatus(), TaskStatus.NEW, "Неверно рассчитан статус для нового эпика");
    }

    @Test
    public void addNullEpicTest() {
        assertNull(taskManager.addEpic(null));
    }

//    @Test
//    void getEpic() {
//    }

    @Test
    public void getEpicFromEmptyListTest() {
        assertNull(taskManager.getEpic(1));
    }

    @Test
    public void getEpicWithIncorrectIdTest() {
        Epic epic = new Epic(0, "name", "description");
        final int id = taskManager.addEpic(epic).getId();
        assertNull(taskManager.getEpic(id + 1));
    }

    @Test
    public void getEpicWithCorrectIdTest() {
        Epic epic = new Epic(0, "name", "description");
        epic = taskManager.addEpic(epic);
        taskManager.addEpic(new Epic(0, "name1", "description1"));
        final int id = epic.getId();
        Epic returnedEpic = taskManager.getEpic(id);

        assertNotNull(returnedEpic, "Задача не найдена по id");
        assertEquals(epic, returnedEpic, "Задачи не совпадают");
    }


//    @Test
//    void getEpics() {
//    }

    @Test
    public void getEpicsFromEmptyListTest() {
        List<Epic> epics = taskManager.getEpics();
        assertNotNull(epics, "Не возвращается список задач");
        assertEquals(0, epics.size(), "Возвращается не пустой список задач");
    }

    @Test
    public void getEpicsCorrectTest() {
        Epic epic = new Epic(0, "name", "description");
        epic = taskManager.addEpic(epic);
        List<Epic> returnedEpics = taskManager.getEpics();

        assertNotNull(returnedEpics, "Не возвращается список задач");
        assertEquals(1, returnedEpics.size(), "Возвращается неверное количество задач");
        assertEquals(epic, returnedEpics.get(0), "Задачи не совпадают");
    }

//    @Test
//    void updateEpic() {
//    }

    @Test
    public void updateEpicInEmptyListTest() {
        Epic epic = new Epic(0, "name", "description");
        taskManager.updateEpic(epic);
        assertEquals(0, taskManager.getEpics().size());
    }

    @Test
    public void updateNullEpicTest() {
        Epic epic = new Epic(0, "name", "description");
        final int id = taskManager.addEpic(epic).getId();
        taskManager.updateEpic(null);
        assertEquals(epic, taskManager.getEpic(id));
    }

    @Test
    public void updateEpicWithIncorrectIdTest() {
        Epic epic = new Epic(0, "name", "description");
        epic = taskManager.addEpic(epic);
        final int id = epic.getId();
        Epic updatedEpic = new Epic(id + 1, "new name", "new description");
        taskManager.updateEpic(updatedEpic);
        assertEquals(epic, taskManager.getEpic(id));
    }

    @Test
    public void updateEpicWithCorrectIdTest() {
        Epic epic = new Epic(0, "name", "description");
        final int id = taskManager.addEpic(epic).getId();
        taskManager.addEpic(new Epic(0, "name1", "description1"));
        Epic updatedEpic = new Epic(id, "new name", "new description");
        taskManager.updateEpic(updatedEpic);
        assertEquals(updatedEpic, taskManager.getEpic(id));
    }

    @Test
    public void updateEpicWithIncorrectSubtasksTest() {
        Epic epic = new Epic(0, "epic name", "epic description");
        epic = taskManager.addEpic(epic);
        final int epicId = epic.getId();
        taskManager.addSubtask(new Subtask(0, "name1", "description1", TaskStatus.NEW, epicId));
        taskManager.addSubtask(new Subtask(0, "name2", "description2", TaskStatus.NEW, epicId));
        List<Integer> epicSubtasks = taskManager.getEpic(epicId).getSubtaskIds();
        Epic updatedEpic = new Epic(epicId, "new name", "new description");
        taskManager.updateEpic(updatedEpic);
        assertEquals(epicSubtasks, taskManager.getEpic(epicId).getSubtaskIds());
    }

    @Test
    public void updateEpicWithSameObjectTest() {
        Epic epic = new Epic(0, "epic name", "epic description");
        epic = taskManager.addEpic(epic);
        final int epicId = epic.getId();
        taskManager.addSubtask(new Subtask(0, "name1", "description1", TaskStatus.NEW, epicId));
        taskManager.addSubtask(new Subtask(0, "name2", "description2", TaskStatus.NEW, epicId));
        taskManager.updateEpic(epic);
        Epic returnedEpic = taskManager.getEpic(epicId);
        assertEquals(epic, returnedEpic, "Возвращается другой эпик");
        assertEquals(2, epic.getSubtaskIds().size(), "Изменилось количество подзадач эпика");
    }

//    @Test
//    void removeEpic() {
//    }

    @Test
    public void removeEpicFromEmptyListTest() {
        assertDoesNotThrow(() -> taskManager.removeEpic(1));
    }

    @Test
    public void removeEpicWithIncorrectIdTest() {
        Epic epic = new Epic(0, "name", "description");
        final int id = taskManager.addEpic(epic).getId();
        taskManager.removeEpic(id + 1);
        assertEquals(1, taskManager.getEpics().size());
    }

    @Test
    public void removeEpicWithCorrectIdTest() {
        Epic epic = new Epic(0, "name", "description");
        final int id = taskManager.addEpic(epic).getId();
        Epic anotherEpic = new Epic(0, "name1", "description1");
        anotherEpic = taskManager.addEpic(anotherEpic);
        taskManager.addSubtask(new Subtask(0, "name1", "description1", TaskStatus.NEW, id));
        taskManager.addSubtask(new Subtask(0, "name2", "description2", TaskStatus.NEW, id));
        taskManager.removeEpic(id);
        List<Epic> epics = taskManager.getEpics();
        assertEquals(1, epics.size(), "Задача не удалена");
        assertEquals(anotherEpic, epics.get(0), "Удалена не та задача");
        assertEquals(0, taskManager.getSubtasks().size(), "Не удалены подзадачи эпика");
    }

//    @Test
//    void clearTasks() {
//    }
//
    @Test
    public void clearEpicsFromEmptyListTest() {
        assertDoesNotThrow(() -> taskManager.clearEpics());
    }

    @Test
    public void clearEpicsCorrectTest() {
        Epic epic = new Epic(0, "name", "description");
        final int id = taskManager.addEpic(epic).getId();
        taskManager.addSubtask(new Subtask(0, "name1", "description1", TaskStatus.NEW, id));
        taskManager.addSubtask(new Subtask(0, "name2", "description2", TaskStatus.NEW, id));
        taskManager.addEpic(new Epic(0, "name3", "description3"));
        taskManager.clearEpics();
        assertEquals(0, taskManager.getEpics().size(), "Не удалены эпики");
        assertEquals(0, taskManager.getSubtasks().size(), "Не удалены подзадачи");
    }

//    @Test
//    void getSubtasks() {
//    }
//
//    @Test
//    void clearSubtasks() {
//    }
//
//    @Test
//    void getSubtask() {
//    }
//
//    @Test
//    void addSubtask() {
//    }
//
//    @Test
//    void updateSubtask() {
//    }
//
//    @Test
//    void removeSubtask() {
//    }
//

//
//    @Test
//    void clearEpics() {
//    }
//

//

//

//

//
//    @Test
//    void getEpicsSubtasks() {
//    }
//
//    @Test
//    void getHistory() {
//    }
}