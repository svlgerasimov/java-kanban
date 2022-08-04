package ru.yandex.practicum.kanban.managers;

import ru.yandex.practicum.kanban.tasks.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class TimeManager {
    private final TreeSet<Task> prioritizedTasks;

    public TimeManager() {
        prioritizedTasks = new TreeSet<>((task1, task2) -> {
            LocalDateTime startTime1 = task1.getStartTime();
            LocalDateTime startTime2 = task2.getStartTime();
            if (startTime1 == null) {
                return 1;
            }
            if (startTime2 == null) {
                return -1;
            }
            return startTime1.compareTo(startTime2);    // с одинаковым временем добавится только 1 задача
            // но так как есть проверка на пересечение, ничего страшного
        });
    }

    public void addTask(Task task) {
        if (task == null) {
            return;
        }
        prioritizedTasks.add(task);
    }

//    public void updateTask(Task previous, Task task) {
//        if (previous != null) {
//            prioritizedTasks.remove(previous);
//        }
//        if (task != null) {
//            prioritizedTasks.add(task);
//        }
//    }

    public void removeTask(Task task) {
        if (task == null) {
            return;
        }
        prioritizedTasks.remove(task);
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    public boolean validateTask(Task task) {
        return true;
    }
}





