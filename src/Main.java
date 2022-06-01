import java.util.ArrayList;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();


        int taskId1 = taskManager.generateNextId();
        taskManager.addTask(new Task(taskId1,
                "Задача 1","Описание задачи 1", Task.STATUS_NEW));
        int taskId2 = taskManager.generateNextId();
        taskManager.addTask(new Task(taskId2,
                "Задача 2","Описание задачи 2", Task.STATUS_NEW));
        int taskId3 = taskManager.generateNextId();
        taskManager.addTask(new Task(taskId3,
                "Задача 3","Описание задачи 3", Task.STATUS_DONE));

        printAllTasks(taskManager);

        Task task = taskManager.getTask(taskId2);
        taskManager.updateTask(new Task(task.getId(),
                task.getName() + " updated", task.getDescription(), Task.STATUS_IN_PROGRESS));
        taskManager.removeTask(taskId1);
        printAllTasks(taskManager);

        taskManager.clearTasks();
        printAllTasks(taskManager);

        int subtaskId1 = taskManager.generateNextId();
        taskManager.addSubtask(new Subtask(subtaskId1,
                "Подзадача 1", "Описание подзадачи 1", Task.STATUS_NEW, null));
        int subtaskId2 = taskManager.generateNextId();
        taskManager.addSubtask(new Subtask(subtaskId2,
                "Подзадача 2", "Описание подзадачи 2", Task.STATUS_DONE, null));
        int subtaskId3 = taskManager.generateNextId();
        taskManager.addSubtask(new Subtask(subtaskId3,
                "Подзадача 3", "Описание подзадачи 3", Task.STATUS_DONE, null));
        int subtaskId4 = taskManager.generateNextId();
        taskManager.addSubtask(new Subtask(subtaskId4,
                "Подзадача 4", "Описание подзадачи 4", Task.STATUS_DONE, null));

        int epicId1 = taskManager.generateNextId();
        taskManager.addEpic(new Epic(epicId1,
                "Эпик 1", "Описание Эпика 1", null));
        int epicId2 = taskManager.generateNextId();
        taskManager.addEpic(new Epic(epicId2,
                "Эпик 2", "Описание Эпика 2", null));
        int epicId3 = taskManager.generateNextId();
        taskManager.addEpic(new Epic(epicId3,
                "Эпик 3", "Описание Эпика 3", null));
        int epicId4 = taskManager.generateNextId();
        taskManager.addEpic(new Epic(epicId4,
                "Эпик 4", "Описание Эпика 4", null));

        printAllTasks(taskManager);

        taskManager.getEpic(epicId1).addSubtask(taskManager.getSubtask(subtaskId1));
        taskManager.getEpic(epicId2).addSubtask(taskManager.getSubtask(subtaskId2));
        taskManager.getEpic(epicId2).addSubtask(taskManager.getSubtask(subtaskId3));
        taskManager.getEpic(epicId3).addSubtask(taskManager.getSubtask(subtaskId4));

        printAllTasks(taskManager);

        Subtask subtask = taskManager.getSubtask(subtaskId1);
        subtask = new Subtask(subtask.getId(), subtask.getName() + " (updated)",
                subtask.getDescription(), Task.STATUS_NEW, subtask.getEpic());
        taskManager.updateSubtask(subtask);

        subtask = taskManager.getSubtask(subtaskId3);
        subtask = new Subtask(subtask.getId(), subtask.getName() + " (updated)",
                subtask.getDescription(), Task.STATUS_IN_PROGRESS, subtask.getEpic());
        taskManager.updateSubtask(subtask);

        printAllTasks(taskManager);

        Epic epic = taskManager.getEpic(epicId1);
        epic = new Epic(epic.getId(), epic.getName() + " (updated)",
                epic.getDescription(), epic.getSubtasks());
        epic.addSubtask(new Subtask(taskManager.generateNextId(), "NEW SUBTASK", "", Task.STATUS_DONE, null));
        taskManager.updateEpic(epic);
        epic = taskManager.getEpic(epicId2);
        epic = new Epic(epic.getId(), epic.getName() + " (updated)",
                epic.getDescription(), epic.getSubtasks());
        taskManager.updateEpic(epic);
        printAllTasks(taskManager);



//        taskManager.removeSubtask(subtaskId1);
//        taskManager.removeEpic(epicId2);

//        taskManager.clearSubtasks();

//        taskManager.clearEpics();
//        taskManager.removeSubtask();

        taskManager.clearEpics();
        printAllTasks(taskManager);

    }

    private static void printAllTasks(TaskManager taskManager) {
        ArrayList<Task> tasks = taskManager.getTasks();
        ArrayList<Subtask> subtasks = taskManager.getSubtasks();
        ArrayList<Epic> epics = taskManager.getEpics();

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
}
