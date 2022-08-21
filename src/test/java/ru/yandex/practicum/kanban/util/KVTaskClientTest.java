package ru.yandex.practicum.kanban.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kanban.util.kvstorage.ClientBadResponseException;
import ru.yandex.practicum.kanban.util.kvstorage.KVServer;
import ru.yandex.practicum.kanban.util.kvstorage.KVTaskClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class KVTaskClientTest {

    private KVServer kvServer;
    private KVTaskClient kvTaskClient;

    @BeforeEach
    void setUp() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        kvTaskClient = new KVTaskClient("http://localhost:" + KVServer.PORT);
    }

    @AfterEach
    void tearDown() {
        kvServer.stop(0);
    }

    @Test
    void loadTest() {
        String key1 = "key1";
        String value1 = "value1";
        String key2 = "key2";
        String value2 = "value2";
        String key3 = "value3";

        kvTaskClient.put(key1, value1);
        kvTaskClient.put(key2, value2);
        assertEquals(value1, kvTaskClient.load(key1));
        assertEquals(value2, kvTaskClient.load(key2));
//        assertNull(kvTaskClient.load(key3));
        assertThrows(ClientBadResponseException.class, () -> kvTaskClient.load(key3));
    }
}