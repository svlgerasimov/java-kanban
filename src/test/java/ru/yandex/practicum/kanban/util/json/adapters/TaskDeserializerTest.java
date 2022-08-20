package ru.yandex.practicum.kanban.util.json.adapters;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;
import ru.yandex.practicum.kanban.util.json.GsonBuilders;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class TaskDeserializerTest {

    private final static LocalDateTime DEFAULT_TIME =
            LocalDateTime.of(2022, Month.JANUARY, 1, 1, 1, 0);

    @Test
    public void taskDeserializationTest() {
        Gson gson = GsonBuilders.getBuilderSeparateTaskTypes().create();

        Task task = new Task(1, "task name", "task description", TaskStatus.IN_PROGRESS,
                DEFAULT_TIME, 10);
        Epic epic = new Epic(2, "epic name", "epic description");
        Subtask subtask = new Subtask(3, "subtask name", "subtask description", TaskStatus.DONE,
                epic.getId(), DEFAULT_TIME, 20);

        String taskJson = gson.toJson(task);
        String epicJson = gson.toJson(epic);
        String subtaskJson = gson.toJson(subtask);

        System.out.println(taskJson);
        System.out.println(epicJson);
        System.out.println(subtaskJson);

        assertEquals(task, gson.fromJson(taskJson, Task.class));
        assertEquals(epic, gson.fromJson(epicJson, Epic.class));
        assertEquals(subtask, gson.fromJson(subtaskJson, Subtask.class));
    }
}