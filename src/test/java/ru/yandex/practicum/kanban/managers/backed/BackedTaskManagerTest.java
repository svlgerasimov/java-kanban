package ru.yandex.practicum.kanban.managers.backed;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kanban.managers.TaskManagerTest;
import ru.yandex.practicum.kanban.managers.backed.filebacked.FileBackedTaskManager;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    abstract protected FileBackedTaskManager createNewManagerOfSamePath();

    @Test
    public void loadEmptyManagerTest() {
        FileBackedTaskManager restored = createNewManagerOfSamePath();
        restored.load();
        assertEquals(0, restored.getTasks().size(), "Не пустой список задач");
        assertEquals(0, restored.getEpics().size(), "Не пустой список эпиков");
        assertEquals(0, restored.getSubtasks().size(), "Не пустой список подзадач");
        assertEquals(0, restored.getHistory().size(), "Не пустая история");
        assertEquals(0, restored.getPrioritizedTasks().size(),
                "Не пустой список отсортированных задач");
    }

    @Test
    public void loadManagerFilledTest() {
        Task task = taskManager.addTask(
                new Task(0, "task name", "task description", TaskStatus.DONE,
                        DEFAULT_TIME, 10));
        int taskId = task.getId();
        Epic epic1 = taskManager.addEpic(new Epic(0, "epic name 1", "epic with subtasks"));
        int epicId1 = epic1.getId();
        Epic epic2 = taskManager.addEpic(
                new Epic(0, "epic name 2", "epic without subtasks"));
        int epicId2 = epic2.getId();
        Subtask subtask1 = taskManager.addSubtask(
                new Subtask(0, "subtask name 1", "subtask description 1", TaskStatus.NEW, epicId1,
                        DEFAULT_TIME.plusMinutes(10), 5));
        int subtaskId1 = subtask1.getId();
        Subtask subtask2 = taskManager.addSubtask(
                new Subtask(0, "subtask name 2", "subtask description 2", TaskStatus.DONE, epicId1));
        int subtaskId2 = subtask2.getId();

        // формируем историю
        taskManager.getEpic(epicId1);
        taskManager.getSubtask(subtaskId1);
        taskManager.getTask(taskId);
        taskManager.getEpic(epicId2);
        taskManager.getSubtask(subtaskId2);

        FileBackedTaskManager restoredTaskManager = createNewManagerOfSamePath();
        restoredTaskManager.load();
        assertEquals(taskManager.getTasks(), restoredTaskManager.getTasks(),
                "Список задач после выгрузки не совпадает");
        assertEquals(taskManager.getSubtasks(), restoredTaskManager.getSubtasks(),
                "Список подзадач после выгрузки не совпадает");
        assertEquals(taskManager.getEpics(), restoredTaskManager.getEpics(),
                "Список эпиков после выгрузки не совпадает");
        assertEquals(taskManager.getHistory(), restoredTaskManager.getHistory(),
                "История после выгрузки не совпадает");
        assertEquals(taskManager.getPrioritizedTasks(), restoredTaskManager.getPrioritizedTasks(),
                "Отсортированный список задач после выгрузки не совпадает");
    }

    @Test
    public void loadTasksWithBlankFieldsTest() {
        Task task = taskManager.addTask(new Task(0, "", "", TaskStatus.NEW));
        final int taskId = task.getId();
        Epic epic = taskManager.addEpic(new Epic(0,"", ""));
        final int epicId = epic.getId();
        Subtask subtask = taskManager.addSubtask(new Subtask(0, "", "", TaskStatus.NEW, epicId));
        final int subtaskId = subtask.getId();

        FileBackedTaskManager restored = createNewManagerOfSamePath();
        restored.load();
        assertEquals(task, restored.getTask(taskId), "Неверно восстановлена задача");
        assertEquals(subtask, restored.getSubtask(subtaskId), "Неверно восстановлена подзадача");
        assertEquals(epic, restored.getEpic(epicId), "Неверно восстановлен эпик");
    }
}
