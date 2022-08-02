package ru.yandex.practicum.kanban.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private Epic epic;
    private static final int epicId = 1;
    private static final String epicName = "name";
    private static final String epicDescription = "description";

    @BeforeEach
    public void beforeEach() {
        epic = new Epic(epicId, epicName, epicDescription);
    }

    @Test
    public void addSubtaskTest() {
        int subtaskId = epicId + 1;
        epic.addSubtask(subtaskId);

        List<Integer> subtaskIds = epic.getSubtaskIds();

        assertNotNull(subtaskIds, "Не возвращается список подзадач");
        assertEquals(1, subtaskIds.size(), "Неверное количество подзадач");
        assertEquals(subtaskId, subtaskIds.get(0), "ID Подзадач не совпадают");
    }

    @Test
    public void removeSubtaskTest() {


    }

}