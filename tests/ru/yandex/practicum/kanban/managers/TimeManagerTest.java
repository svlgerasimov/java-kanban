package ru.yandex.practicum.kanban.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeManagerTest {

    private TimeManager timeManager;
    private final LocalDateTime DEFAULT_TIME =
            LocalDateTime.of(2022, Month.JANUARY, 1, 1, 1, 30);

    @BeforeEach
    public void beforeEach() {
        timeManager = new TimeManager();
    }

    @Test
    public void getPrioritizedTasksTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME;
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

    @Test
    public void validateTaskOnEmptyTimeListTest() {
        assertFalse(timeManager.validateTask(null), "null проходит проверку");

        Task task = new Task(1, "", "", TaskStatus.NEW, DEFAULT_TIME, 10);
        assertTrue(timeManager.validateTask(task), "Неверная проверка при пустом списке задач");

        timeManager.addTask(new Task(2, "", "", TaskStatus.NEW));
        assertTrue(timeManager.validateTask(task), "Неверная проверка при отсутствии задач с меткой времени");
    }

    @Test
    public void validateTaskWithoutTimeTest() {
        timeManager.addTask(new Task(1, "", "", TaskStatus.NEW, DEFAULT_TIME, 10));
        assertTrue(timeManager.validateTask(new Task(2, "", "", TaskStatus.NEW)),
                "Задача без временной метки не проходит проверку");
    }

    @Test
    public void validateTaskInterceptingIntervalsTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME;
        final int duration1 = 5 * 24 * 60;
        timeManager.addTask(new Task(1, "", "", TaskStatus.NEW, startTime1, duration1));

        final int duration2 = 2 * 24 * 60;
        final int intercept = 5;
        LocalDateTime startTime2 = startTime1.minusMinutes(intercept);
        assertFalse(timeManager.validateTask(
                new Task(2, "", "", TaskStatus.NEW, startTime2, duration2)));

        startTime2 = startTime1.plusMinutes(duration1 - intercept);
        assertFalse(timeManager.validateTask(
                new Task(2, "", "", TaskStatus.NEW, startTime2, duration2)));

        startTime2 = startTime1.plusMinutes(intercept);
        assertFalse(timeManager.validateTask(
                new Task(2, "", "", TaskStatus.NEW, startTime2, duration2)));

        startTime2 = startTime1;
        assertFalse(timeManager.validateTask(
                new Task(2, "", "", TaskStatus.NEW, startTime2, duration2)));

        startTime2 = startTime1.plusMinutes(duration1 - duration2);
        assertFalse(timeManager.validateTask(
                new Task(2, "", "", TaskStatus.NEW, startTime2, duration2)));
    }

    @Test
    public void validateTaskNotInterceptingIntervalsTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME;
        final int duration1 = 5 * 24 * 60;
        timeManager.addTask(new Task(1, "", "", TaskStatus.NEW, startTime1, duration1));

        final int duration2 = 2 * 24 * 60;
        final int distance = 5;
        LocalDateTime startTime2 = startTime1.minusMinutes(duration2 + distance);
        assertTrue(timeManager.validateTask(
                new Task(2, "", "", TaskStatus.NEW, startTime2, duration2)));

        startTime2 = startTime1.plusMinutes(duration1 + distance);
        assertTrue(timeManager.validateTask(
                new Task(2, "", "", TaskStatus.NEW, startTime2, duration2)));
    }

    @Test
    public void validateTaskCloseIntervalsTest() {
        final LocalDateTime startTime1 = DEFAULT_TIME;
        final int duration1 = 5 * 24 * 60;
        timeManager.addTask(new Task(1, "", "", TaskStatus.NEW, startTime1, duration1));

        final int duration2 = 2 * 24 * 60;
        LocalDateTime startTime2 = startTime1.minusMinutes(duration2);
        assertTrue(timeManager.validateTask(
                new Task(2, "", "", TaskStatus.NEW, startTime2, duration2)));

        startTime2 = startTime1.plusMinutes(duration1);
        assertTrue(timeManager.validateTask(
                new Task(2, "", "", TaskStatus.NEW, startTime2, duration2)));
    }
}