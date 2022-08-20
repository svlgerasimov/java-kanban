package ru.yandex.practicum.kanban.managers.backed.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kanban.managers.backed.BackedTaskManagerTest;
import ru.yandex.practicum.kanban.managers.backed.ManagerLoadException;
import ru.yandex.practicum.kanban.managers.backed.ManagerSaveException;
import ru.yandex.practicum.kanban.managers.backed.filebacked.FileBackedTaskManager;
import ru.yandex.practicum.kanban.util.KVServer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class HttpTaskManagerTest extends BackedTaskManagerTest {

    private final String kvServerUri = "http://localhost:" + KVServer.PORT;
    private static KVServer kvServer;

    @BeforeAll
    static void beforeAll() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
    }

    @AfterAll
    static void afterAll() {
        kvServer.stop(0);
    }

    @BeforeEach
    void setUp() {
        taskManager = new HttpTaskManager(kvServerUri);
        taskManager.save(); // чтобы сохранить новый пустой менеджер в файл, если в нём что-то было
    }

    @Override
    protected FileBackedTaskManager createNewManagerOfSamePath() {
        return new HttpTaskManager(kvServerUri);
    }

    @Test
    public void incorrectUrlTest() {
        taskManager = new HttpTaskManager("http://localhost:77");
        assertThrows(ManagerSaveException.class, taskManager::save);
        assertThrows(ManagerLoadException.class, taskManager::load);
    }
}
