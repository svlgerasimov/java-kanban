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
    public void getSubtaskIdsFromEmptyListTest() {
        assertNotNull(epic.getSubtaskIds(), "Не возвращается список подзадач");
        assertEquals(0, epic.getSubtaskIds().size(), "Возвращается непустой список подзадач");
    }

    @Test
    public void addSubtaskTest() {
        int subtaskId = epicId + 1;
        epic.addSubtask(subtaskId);
        epic.addSubtask(subtaskId + 1);

        List<Integer> subtaskIds = epic.getSubtaskIds();

        assertNotNull(subtaskIds, "Не возвращается список подзадач");
        assertEquals(2, subtaskIds.size(), "Неверное количество подзадач");
        assertEquals(subtaskId, subtaskIds.get(0), "Возвращается неверный id подзадачи");
    }

    @Test
    public void removeNullSubtaskTest() {
        epic.addSubtask(epicId + 1);
        assertDoesNotThrow(() -> epic.removeSubtask(null));
    }

    @Test
    public void removeSubtaskFromEmptyListTest() {
        assertDoesNotThrow(() -> epic.removeSubtask(1));
    }

    @Test
    public void removeIncorrectSubtaskTest() {
        int subtaskId = epicId + 1;
        epic.addSubtask(subtaskId);
        epic.removeSubtask(subtaskId + 1);
        assertEquals(1, epic.getSubtaskIds().size());
    }

    @Test
    public void removeCorrectSubtaskTest() {
        int subtaskId = epicId + 1;
        epic.addSubtask(subtaskId);
        int subtaskId2 = subtaskId + 1;
        epic.addSubtask(subtaskId2);
        epic.removeSubtask(subtaskId2);
        assertEquals(1, epic.getSubtaskIds().size(), "Не удаляется подзадача");
        assertEquals(subtaskId, epic.getSubtaskIds().get(0), "Удаляется дргая подзадача");
    }

    @Test
    public void clearSubtasksWithEmptyListTest() {
        assertDoesNotThrow(epic::clearSubtasks);
    }

    @Test
    public void clearSubtasksCorrectTest() {
        int subtaskId = epicId + 1;
        epic.addSubtask(subtaskId);
        epic.addSubtask(subtaskId + 1);
        epic.clearSubtasks();
        assertEquals(0, epic.getSubtaskIds().size());
    }
}