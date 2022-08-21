package ru.yandex.practicum.kanban.managers.backed.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kanban.managers.backed.BackedTaskManagerTest;
import ru.yandex.practicum.kanban.util.kvstorage.ClientSendException;
import ru.yandex.practicum.kanban.util.kvstorage.KVServer;

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
    protected HttpTaskManager loadStateInNewManager() {
        return new HttpTaskManager(kvServerUri, true);
    }

    @Test
    public void incorrectUrlTest() {
        assertThrows(ClientSendException.class, () -> new HttpTaskManager("http://localhost:77"));
    }
}
