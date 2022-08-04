package ru.yandex.practicum.kanban.tasks;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void getEndTime() {
        Task task = new Task(1, "name", "description", TaskStatus.NEW);
        assertNull(task.getEndTime(), "Время окончания не null при времени начала null");
        final int duration = 10;
        task = new Task(1, "name", "description", TaskStatus.NEW, LocalDateTime.now(), duration);
        assertEquals(duration, Duration.between(task.getStartTime(), task.getEndTime()).toMinutes(),
                "Неверно рассчитано время окончания");
    }
}