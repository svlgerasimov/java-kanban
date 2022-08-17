package ru.yandex.practicum.kanban.tasks;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private final LocalDateTime DEFAULT_TIME =
            LocalDateTime.of(2022, Month.JANUARY, 1, 1, 1, 30);

    @Test
    void getEndTimeTest() {
        Task task = new Task(1, "name", "description", TaskStatus.NEW);
        assertNull(task.getEndTime(), "Время окончания не null при времени начала null");
        final int duration = 10;
        task = new Task(1, "name", "description", TaskStatus.NEW, DEFAULT_TIME, duration);
        assertEquals(duration, Duration.between(task.getStartTime(), task.getEndTime()).toMinutes(),
                "Неверно рассчитано время окончания");
    }
}