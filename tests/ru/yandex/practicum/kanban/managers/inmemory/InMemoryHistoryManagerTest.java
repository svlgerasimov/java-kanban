package ru.yandex.practicum.kanban.managers.inmemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    public void getEmptyHistoryTest() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "Не возвращается история");
        assertEquals(0, history.size(), "Возвращается не пустая история");
    }

    @Test
    public void addNewTaskTest() {
        Task task1 = new Task(1, "name", "description", TaskStatus.NEW);
        Task task2 = new Task(2, "name", "description", TaskStatus.NEW);
        Task task3 = new Task(3, "name", "description", TaskStatus.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "Не возвращается история");
        assertEquals(3, history.size(), "Неверное количество задач");
        assertEquals(task1, history.get(0), "Неверная последовательность задач");
        assertEquals(task2, history.get(1), "Неверная последовательность задач");
        assertEquals(task3, history.get(2), "Неверная последовательность задач");
    }

    @Test
    public void addNullTaskTest() {
        historyManager.add(new Task(1, "name", "description", TaskStatus.NEW));
        historyManager.add(null);
        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    public void addSameTaskTest() {
        Task task1 = new Task(1, "name", "description", TaskStatus.NEW);
        Task task2 = new Task(2, "name", "description", TaskStatus.NEW);
        Task task3 = new Task(3, "name", "description", TaskStatus.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.add(task3);  // дублирование в конце
        List<Task> history = historyManager.getHistory();
        assertEquals(task1, history.get(0), "Неверная последовательность задач");
        assertEquals(task2, history.get(1), "Неверная последовательность задач");
        assertEquals(task3, history.get(2), "Неверная последовательность задач");

        historyManager.add(task1);  // дублирование в начале
        history = historyManager.getHistory();
        assertEquals(task2, history.get(0), "Неверная последовательность задач");
        assertEquals(task3, history.get(1), "Неверная последовательность задач");
        assertEquals(task1, history.get(2), "Неверная последовательность задач");

        historyManager.add(task3);  // дублирование в середине
        history = historyManager.getHistory();
        assertEquals(task2, history.get(0), "Неверная последовательность задач");
        assertEquals(task1, history.get(1), "Неверная последовательность задач");
        assertEquals(task3, history.get(2), "Неверная последовательность задач");
    }

    @Test
    public void removeFromEmptyHistoryTest() {
        assertDoesNotThrow(() -> historyManager.remove(1));
    }

    @Test
    public void removeIncorrectTaskTest() {
        final int id = 1;
        Task task1 = new Task(id, "name", "description", TaskStatus.NEW);
        historyManager.add(task1);
        historyManager.remove(id + 1);
        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    public void removeCorrectTaskTest() {
        Task task1 = new Task(1, "name", "description", TaskStatus.NEW);
        Task task2 = new Task(2, "name", "description", TaskStatus.NEW);
        Task task3 = new Task(3, "name", "description", TaskStatus.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId()); // удаление в конце
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Задача не удалена");
        assertEquals(task1, history.get(0), "Удалена другая задача");
        assertEquals(task2, history.get(1), "Удалена другая задача");

        historyManager.add(task3);
        historyManager.remove(task1.getId()); // удаление в начале
        history = historyManager.getHistory();
        assertEquals(2, history.size(), "Задача не удалена");
        assertEquals(task2, history.get(0), "Удалена другая задача");
        assertEquals(task3, history.get(1), "Удалена другая задача");

        historyManager.add(task1);
        historyManager.remove(task3.getId()); // удаление в начале
        history = historyManager.getHistory();
        assertEquals(2, history.size(), "Задача не удалена");
        assertEquals(task2, history.get(0), "Удалена другая задача");
        assertEquals(task1, history.get(1), "Удалена другая задача");
    }

    @Test
    public void updateTaskWithEmptyHistoryTest() {
        historyManager.update(new Task(1, "name", "description", TaskStatus.NEW));
        assertEquals(0, historyManager.getHistory().size());
    }

    @Test
    public void updateNullTask() {
        assertDoesNotThrow(() -> historyManager.update(null));
    }

    @Test
    public void updateIncorrectTask() {
        final int id = 1;
        Task task1 = new Task(id, "name", "description", TaskStatus.NEW);
        historyManager.add(task1);
        historyManager.update(new Task(id + 1, "new name", "new description", TaskStatus.NEW));
        assertEquals(task1, historyManager.getHistory().get(0));
    }

    @Test
    public void updateCorrectTask() {
        final int id1 = 1;
        historyManager.add(new Task(id1, "name", "description", TaskStatus.NEW));
        final int id2 = 2;
        Task task2 = new Task(id2, "name 2", "description 2", TaskStatus.NEW);
        historyManager.add(task2);

        Task updatedTask = new Task(id1, "updated name", "updated description", TaskStatus.DONE);
        historyManager.update(updatedTask);

        List<Task> history = historyManager.getHistory();
        assertEquals(updatedTask, history.get(0), "Задача не обновлена");
        assertEquals(task2, history.get(1), "Обновлена не та задача");
    }
}
