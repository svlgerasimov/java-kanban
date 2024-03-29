package ru.yandex.practicum.kanban;

import ru.yandex.practicum.kanban.managers.Managers;
import ru.yandex.practicum.kanban.managers.TaskManager;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;
import ru.yandex.practicum.kanban.util.kvstorage.KVServer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

//        Path filePath = Path.of("src","main", "resources", "taskManager.csv");
//        TaskManager taskManager = Managers.getFileBacked(filePath, false);


        KVServer kvServer;
        try {
            kvServer = new KVServer();
        } catch (IOException e) {
            System.out.println("Failed to start KVServer");
            return;
        }

        // Если в вывалится исключение, сервер сам не остановится
        try {
            kvServer.start();
            TaskManager taskManager = Managers.getDefault();

            LocalDateTime minStartTime = LocalDateTime.now();

            int taskId1 = taskManager.addTask(new Task(0,
                    "Задача 1", "Задача 1", TaskStatus.NEW, minStartTime, 10)).getId();
            int taskId2 = taskManager.addTask(new Task(0,
                    "Задача 2", "Задача 2", TaskStatus.DONE)).getId();
            int epicId1 = taskManager.addEpic(new Epic(0,
                    "Эпик 1", "Эпик с 3 подзадачами")).getId();
            int epicId2 = taskManager.addEpic(new Epic(0,
                    "Эпик 2", "Эпик без подзадач")).getId();
            int subtaskId1 = taskManager.addSubtask(new Subtask(0,
                    "Подзадача 1", "Подзадача 1", TaskStatus.NEW, epicId1)).getId();
            int subtaskId2 = taskManager.addSubtask(new Subtask(0,
                    "Подзадача 2", "Подзадача 2", TaskStatus.NEW, epicId1,
                    minStartTime.plusMinutes(20), 20)).getId();
            int subtaskId3 = taskManager.addSubtask(new Subtask(0,
                    "Подзадача 3", "Подзадача 3", TaskStatus.NEW, epicId1,
                    minStartTime.plusMinutes(60), 30)).getId();
            taskManager.addTask(new Task(0, "", "", TaskStatus.NEW, minStartTime, 20));

            printAllTasks(taskManager);
            printHistory(taskManager);

            System.out.println("Get task: " + taskManager.getTask(taskId1));
            System.out.println("Get task: " + taskManager.getEpic(epicId1));
            System.out.println("Get task: " + taskManager.getSubtask(subtaskId1));
            System.out.println("------------------");

            printHistory(taskManager);

            Subtask subtask = taskManager.getSubtask(subtaskId2);
            System.out.println("Get task: " + subtask);
            taskManager.updateSubtask(new Subtask(subtask.getId(), subtask.getName(), subtask.getDescription(),
                    TaskStatus.IN_PROGRESS, subtask.getEpicId(), minStartTime.plusMinutes(25), 25));
            subtask = taskManager.getSubtask(subtaskId1);
            System.out.println("Get task: " + subtask);
            taskManager.updateSubtask(new Subtask(subtask.getId(), subtask.getName(), subtask.getDescription(),
                    TaskStatus.IN_PROGRESS, subtask.getEpicId()));
            Epic epic = taskManager.getEpic(epicId1);
            taskManager.updateEpic(new Epic(epic.getId(), epic.getName() + " updated", epic.getDescription()));
            System.out.println("------------------");

            printHistory(taskManager);

            System.out.println("Get task: " + taskManager.getTask(taskId2));
            System.out.println("Get task: " + taskManager.getSubtask(subtaskId3));
            System.out.println("Get task: " + taskManager.getEpic(epicId2));
            System.out.println("------------------");

            printAllTasks(taskManager);
            printHistory(taskManager);
            printPrioritizedTasks(taskManager);

//        System.out.println("Remove subtask " + subtaskId2);
//        taskManager.removeSubtask(subtaskId2);
//        System.out.println("------------------");
//        printAllTasks(taskManager);
//        printHistory(taskManager);
//
//        System.out.println("Remove epic " + epicId1);
//        taskManager.removeEpic(epicId1);
//        System.out.println("------------------");
//        printAllTasks(taskManager);
//        printHistory(taskManager);
//
//        System.out.println("Remove task " + taskId1);
//        taskManager.removeTask(taskId1);
//        System.out.println("------------------");
//        printAllTasks(taskManager);
//        printHistory(taskManager);

//        filePath = Path.of("resources", "taskManager_.csv");
//        filePath = Path.of("resources", "taskManager1.csv");
//        filePath = Path.of("resources", "taskManager2.csv");
//        taskManager.clearTasks();
//        taskManager.clearEpics();
            TaskManager taskManagerCopy = Managers.getHttp("http://localhost:" + KVServer.PORT, true);
            System.out.println("------------------");
            System.out.println("Task manager from file:");
            printAllTasks(taskManagerCopy);
            printHistory(taskManagerCopy);
            printPrioritizedTasks(taskManagerCopy);
        } finally {
            kvServer.stop(0);
        }
    }

    private static void printAllTasks(TaskManager taskManager) {
        List<Task> tasks = taskManager.getTasks();
        List<Subtask> subtasks = taskManager.getSubtasks();
        List<Epic> epics = taskManager.getEpics();

        System.out.println("Tasks:");
        for (Task task : tasks) {
            System.out.println(task);
        }
        System.out.println("Subtasks:");
        for (Subtask subtask : subtasks) {
            System.out.println(subtask);
        }
        System.out.println("Epics:");
        for (Epic epic : epics) {
            System.out.println(epic);
        }
        System.out.println("------------------");
    }

    private static void printHistory(TaskManager taskManager) {
        System.out.println("История просмотров задач");
        List<Task> history = taskManager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println(i + ". " + history.get(i));
        }
        System.out.println("------------------");
    }

    private static void printPrioritizedTasks(TaskManager taskManager) {
        System.out.println("Задачи в порядке приоритета");
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        for (int i = 0; i < prioritizedTasks.size(); i++) {
            System.out.println(i + ". " + prioritizedTasks.get(i));
        }
        System.out.println("------------------");
    }
}
