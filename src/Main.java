import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new Managers().getDefault();


        int taskId1 = taskManager.addTask(new Task(0,
                "Задача 1", "Описание задачи 1", TaskStatus.NEW)).getId();
        int taskId2 = taskManager.addTask(new Task(0,
                "Задача 2", "Описание задачи 2", TaskStatus.NEW)).getId();
        int taskId3 = taskManager.addTask(new Task(0,
                "Задача 3", "Описание задачи 3", TaskStatus.DONE)).getId();

        printAllTasks(taskManager);

        Task task = taskManager.getTask(taskId2);
        taskManager.updateTask(new Task(task.getId(),
                task.getName() + " updated", task.getDescription(), TaskStatus.IN_PROGRESS));
        taskManager.getTask(taskId1);
        taskManager.getTask(taskId2);
        taskManager.removeTask(taskId1);
        printAllTasks(taskManager);

        taskManager.clearTasks();
        printAllTasks(taskManager);

        int epicId1 = taskManager.addEpic(new Epic(0, "Эпик 1", "Описание Эпика 1")).getId();
        int epicId2 = taskManager.addEpic(new Epic(0, "Эпик 2", "Описание Эпика 2")).getId();
        int epicId3 = taskManager.addEpic(new Epic(0, "Эпик 3", "Описание Эпика 3")).getId();
        int epicId4 = taskManager.addEpic(new Epic(0, "Эпик 4", "Описание Эпика 4")).getId();

        int subtaskId1 = taskManager.addSubtask(new Subtask(0,
                "Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epicId1)).getId();
        int subtaskId2 = taskManager.addSubtask(new Subtask(0,
                "Подзадача 2", "Описание подзадачи 2", TaskStatus.DONE, epicId2)).getId();
        int subtaskId3 = taskManager.addSubtask(new Subtask(0,
                "Подзадача 3", "Описание подзадачи 3", TaskStatus.DONE, epicId2)).getId();
        int subtaskId4 = taskManager.addSubtask(new Subtask(0,
                "Подзадача 4", "Описание подзадачи 4", TaskStatus.DONE, epicId4)).getId();

        printAllTasks(taskManager);

        Subtask subtask = taskManager.getSubtask(subtaskId1);
        subtask = new Subtask(subtask.getId(), subtask.getName() + " (updated)",
                subtask.getDescription(), TaskStatus.NEW, subtask.getEpicId());
        taskManager.updateSubtask(subtask);

        subtask = taskManager.getSubtask(subtaskId2);
        subtask = new Subtask(subtask.getId(), subtask.getName() + " (updated)",
                subtask.getDescription(), subtask.getStatus(), epicId4);
        taskManager.updateSubtask(subtask);

        subtask = taskManager.getSubtask(subtaskId3);
        subtask = new Subtask(subtask.getId(), subtask.getName() + " (updated)",
                subtask.getDescription(), TaskStatus.IN_PROGRESS, subtask.getEpicId());
        taskManager.updateSubtask(subtask);

        taskManager.removeSubtask(subtaskId4);

        printAllTasks(taskManager);

        Epic oldEpic = taskManager.getEpic(epicId1);
        Epic epic = new Epic(oldEpic.getId(), oldEpic.getName() + " (updated)", oldEpic.getDescription());
        for (Integer subtaskId : oldEpic.getSubtaskIds()) {
            epic.addSubtask(subtaskId);
        }
//        epic.addSubtask(11);
        taskManager.updateEpic(epic);

        oldEpic = taskManager.getEpic(epicId2);
        epic = new Epic(oldEpic.getId(), oldEpic.getName() + " (updated)", oldEpic.getDescription());
        for (Integer subtaskId : oldEpic.getSubtaskIds()) {
            epic.addSubtask(subtaskId);
        }
        taskManager.updateEpic(epic);
        printAllTasks(taskManager);

        taskManager.removeEpic(epicId2);
        printAllTasks(taskManager);

        taskManager.addSubtask(new Subtask(0, "NEW SUBTASK", "", TaskStatus.DONE, epicId1));
        printAllTasks(taskManager);

        taskManager.clearSubtasks();
        printAllTasks(taskManager);

        taskManager.addSubtask(new Subtask(0, "NEW SUBTASK", "", TaskStatus.DONE, epicId1));
        printAllTasks(taskManager);

        taskManager.clearEpics();
        printAllTasks(taskManager);

        taskManager.addSubtask(new Subtask(0, "NEW SUBTASK", "", TaskStatus.DONE, epicId1));
        printAllTasks(taskManager);

        epic = new Epic(0, "New Epic", "some description");
        epic.addSubtask(0);
        epic.addSubtask(100);
        epic.addSubtask(101);
        int epicId = taskManager.addEpic(epic).getId();
        taskManager.addSubtask(new Subtask(0, "new subtask 1", "", TaskStatus.DONE, epicId));
        epic.addSubtask(55);
        taskManager.addSubtask(new Subtask(0, "new subtask 2", "", TaskStatus.DONE, epicId));
        taskManager.addSubtask(new Subtask(0, "new subtask 3", "", TaskStatus.DONE, epicId));
        epic.addSubtask(70);

        printAllTasks(taskManager);

        printHistory(taskManager);

        taskManager.clearEpics();

        taskId1 = taskManager.addTask(new Task(0, "new task 1", "", TaskStatus.NEW)).getId();
        epicId1 =  taskManager.addEpic(new Epic(0, "new subtask 1", "")).getId();
        subtaskId1 = taskManager.addSubtask(
                new Subtask(0, "new subtask 1", "", TaskStatus.NEW, epicId1))
                .getId();
        subtaskId2 = taskManager.addSubtask(
                        new Subtask(0, "new subtask 2", "", TaskStatus.NEW, epicId1))
                .getId();

        printAllTasks(taskManager);
        taskManager.getTask(taskId1);
        taskManager.getSubtask(subtaskId1);
        taskManager.getSubtask(subtaskId2);
        taskManager.getEpic(epicId1);
        printHistory(taskManager);

        taskManager.updateTask(new Task(taskId1, "updated task 1", "", TaskStatus.IN_PROGRESS));
        taskManager.updateSubtask(new Subtask(subtaskId1, "updated subtask 1", "",
                TaskStatus.DONE, epicId1));
        taskManager.getTask(taskId1);
        taskManager.getSubtask(subtaskId1);
        taskManager.getSubtask(subtaskId2);
        taskManager.getEpic(epicId1);
        printAllTasks(taskManager);
        printHistory(taskManager);
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

    private static void printHistory(TaskManager taskManager) {
        System.out.println("История просмотров задач");
        List<Task> history = taskManager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println(i + ". " + history.get(i));
        }
        System.out.println("------------------");
    }
}
