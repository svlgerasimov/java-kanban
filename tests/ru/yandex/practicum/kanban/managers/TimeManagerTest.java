package ru.yandex.practicum.kanban.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeManagerTest {

    private TimeManager timeManager;

    @BeforeEach
    public void beforeEach() {
        timeManager = new TimeManager();
    }

    @Test
    public void getPrioritizedTasksTest() {
        final LocalDateTime startTime1 = LocalDateTime.now().withSecond(0).withNano(0);
        final int duration1 = 10;
        final LocalDateTime startTime2 = startTime1.plusMinutes(duration1 + 10);
        final int duration2 = 20;
        final LocalDateTime startTime3 = startTime2.plusMinutes(duration2 + 10);
        final int duration3 = 30;
        final LocalDateTime startTime4 = startTime3.plusMinutes(duration3 + 10);
        final int duration4 = 40;

        assertNotNull(timeManager.getPrioritizedTasks(), "Не возвращается список задач");
        timeManager.addTask(null);
        List<Task> prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(0, prioritizedTasks.size(), "null добавлен в список задач");

        final int taskId0 = 10;
        Task task0 = new Task(taskId0, "", "", TaskStatus.NEW);
        timeManager.addTask(task0);
        prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(1, prioritizedTasks.size(), "Задача не добавлена");
        assertEquals(task0, prioritizedTasks.get(0), "Добавлена неверная задача");

        final int taskId1 = 1;
        Task task1 = new Task(taskId1, "", "", TaskStatus.NEW, startTime2, duration2);
        timeManager.addTask(task1);
        prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.size(), "Задача не добавлена");
        assertArrayEquals(new Task[] {task1, task0}, prioritizedTasks.toArray(),
                "Неверный порядок задач");

        final int taskId2 = 2;
        Task task2 = new Task(taskId2, "", "", TaskStatus.NEW, startTime1, duration1);
        timeManager.addTask(task2);
        prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(3, prioritizedTasks.size(), "Задача не добавлена");
        assertArrayEquals(new Task[] {task2, task1, task0}, prioritizedTasks.toArray(),
                "Неверный порядок задач");

        final int taskId3 = 3;
        Task task3 = new Task(taskId3, "", "", TaskStatus.NEW, startTime4, duration4);
        timeManager.addTask(task3);
        prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(4, prioritizedTasks.size(), "Задача не добавлена");
        assertArrayEquals(new Task[] {task2, task1, task3, task0}, prioritizedTasks.toArray(),
                "Неверный порядок задач");

        final int taskId4 = 4;
        Task task4 = new Task(taskId4, "", "", TaskStatus.NEW, startTime3, duration3);
        timeManager.addTask(task4);
        prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(5, prioritizedTasks.size(), "Задача не добавлена");
        assertArrayEquals(new Task[] {task2, task1, task4, task3, task0}, prioritizedTasks.toArray(),
                "Неверный порядок задач");

        final int taskId5 = 5;
        Task task5 = new Task(taskId5, "", "", TaskStatus.NEW);
        timeManager.addTask(task5);
        prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(6, prioritizedTasks.size(), "Задача не добавлена");
        assertArrayEquals(new Task[] {task2, task1, task4, task3, task5, task0}, prioritizedTasks.toArray(),
                "Неверный порядок задач");

        timeManager.removeTask(task0);
        prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(5, prioritizedTasks.size(), "Задача не удалена");
        assertArrayEquals(new Task[] {task2, task1, task4, task3, task5}, prioritizedTasks.toArray(),
                "Неверный порядок задач");

        timeManager.removeTask(task5);
        timeManager.removeTask(task3);
        prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(3, prioritizedTasks.size(), "Задача не удалена");
        assertArrayEquals(new Task[] {task2, task1, task4}, prioritizedTasks.toArray(),
                "Неверный порядок задач");

        timeManager.removeTask(task1);
        prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.size(), "Задача не удалена");
        assertArrayEquals(new Task[] {task2, task4}, prioritizedTasks.toArray(),
                "Неверный порядок задач");

        timeManager.removeTask(task1);
        prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.size(), "Задача не удалена");
        assertArrayEquals(new Task[] {task2, task4}, prioritizedTasks.toArray(),
                "Неверный порядок задач");

        timeManager.removeTask(task2);
        prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(1, prioritizedTasks.size(), "Задача не удалена");
        assertEquals(task4, prioritizedTasks.get(0), "Удалена не та задача");

        timeManager.removeTask(task4);
        prioritizedTasks = timeManager.getPrioritizedTasks();
        assertEquals(0, prioritizedTasks.size(), "Задача не удалена");
    }
}