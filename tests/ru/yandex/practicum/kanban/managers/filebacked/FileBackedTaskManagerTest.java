package ru.yandex.practicum.kanban.managers.filebacked;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kanban.managers.TaskManager;
import ru.yandex.practicum.kanban.managers.TaskManagerTest;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private final Path filePath = Path.of("resources", "taskManager.csv");

    @BeforeEach
    public void beforeEach() {
        taskManager = new FileBackedTaskManager(filePath);
        taskManager.addTask(null);  // чтобы сохранить новый пустой менеджер в файл, если в нём что-то было
    }

    @Test
    public void loadEmptyManagerTest() {
        TaskManager restored = FileBackedTaskManager.loadFromFile(filePath);
        assertEquals(0, restored.getTasks().size(), "Не пустой список задач");
        assertEquals(0, restored.getEpics().size(), "Не пустой список эпиков");
        assertEquals(0, restored.getSubtasks().size(), "Не пустой список подзадач");
        assertEquals(0, restored.getHistory().size(), "Не пустая история");
    }

    @Test
    public void loadManagerFilledTest() {
        Task task = taskManager.addTask(
                new Task(0, "task name", "task description", TaskStatus.DONE,
                        LocalDateTime.now(), 10));
        int taskId = task.getId();
        Epic epic1 = taskManager.addEpic(new Epic(0, "epic name 1", "epic with subtasks"));
        int epicId1 = epic1.getId();
        Epic epic2 = taskManager.addEpic(
                new Epic(0, "epic name 2", "epic without subtasks"));
        int epicId2 = epic2.getId();
        Subtask subtask1 = taskManager.addSubtask(
                new Subtask(0, "subtask name 1", "subtask description 1", TaskStatus.NEW, epicId1,
                        LocalDateTime.now(), 5));
        int subtaskId1 = subtask1.getId();
        Subtask subtask2 = taskManager.addSubtask(
                new Subtask(0, "subtask name 2", "subtask description 2", TaskStatus.DONE, epicId1));
        int subtaskId2 = subtask2.getId();

        epic1 = taskManager.getEpic(epicId1);
        taskManager.getSubtask(subtaskId1);
        taskManager.getTask(taskId);
        taskManager.getEpic(epicId2);
        taskManager.getSubtask(subtaskId2);

        TaskManager restored = FileBackedTaskManager.loadFromFile(filePath);
        List<Task> history = restored.getHistory();
        assertEquals(epic1, history.get(0), "Неверно восстановлена история");
        assertEquals(subtask1, history.get(1), "Неверно восстановлена история");
        assertEquals(task, history.get(2), "Неверно восстановлена история");
        assertEquals(epic2, history.get(3), "Неверно восстановлена история");
        assertEquals(subtask2, history.get(4), "Неверно восстановлена история");

        assertEquals(task, restored.getTask(taskId), "Неверно восстановлены задачи");
        assertEquals(epic1, restored.getEpic(epicId1), "Неверно восстановлены задачи");
        assertEquals(epic2, restored.getEpic(epicId2), "Неверно восстановлены задачи");
        assertEquals(subtask1, restored.getSubtask(subtaskId1), "Неверно восстановлены задачи");
        assertEquals(subtask2, restored.getSubtask(subtaskId2), "Неверно восстановлены задачи");
    }

    @Test
    public void loadTasksWithBlankFieldsTest() {
        Task task = taskManager.addTask(new Task(0, "", "", TaskStatus.NEW));
        final int taskId = task.getId();
        Epic epic = taskManager.addEpic(new Epic(0,"", ""));
        final int epicId = epic.getId();
        Subtask subtask = taskManager.addSubtask(new Subtask(0, "", "", TaskStatus.NEW, epicId));
        final int subtaskId = subtask.getId();

        TaskManager restored = FileBackedTaskManager.loadFromFile(filePath);
        assertEquals(task, restored.getTask(taskId), "Неверно восстановлена задача");
        assertEquals(subtask, restored.getSubtask(subtaskId), "Неверно восстановлена подзадача");
        assertEquals(epic, restored.getEpic(epicId), "Неверно восстановлен эпик");
    }
}
