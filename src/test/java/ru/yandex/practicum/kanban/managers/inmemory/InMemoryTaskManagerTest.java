package ru.yandex.practicum.kanban.managers.inmemory;

import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.kanban.managers.TaskManagerTest;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }

}
