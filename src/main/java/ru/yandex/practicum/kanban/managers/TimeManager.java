package ru.yandex.practicum.kanban.managers;

import ru.yandex.practicum.kanban.tasks.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class TimeManager {
    private final TreeSet<Task> prioritizedTasks;

    public TimeManager() {
        prioritizedTasks = new TreeSet<>((task1, task2) -> {
            if (Objects.equals(task1, task2)) {
                return 0;
            }
            if (task1 == null) {
                return 1;
            }
            if (task2 == null) {
                return -1;
            }
            LocalDateTime startTime1 = task1.getStartTime();
            LocalDateTime startTime2 = task2.getStartTime();
            // Если время начала двух задач равно (прежде всего случай startTime = null,
            // такие задачи имеют право на существование и должны располагаться в конце),
            // нужно определить заранее ясный порядок. Логично использовать id.
            // Вторая задача с тем же id не добавится, но таких задач быть не должно
            if (Objects.equals(startTime1, startTime2)) {
                return Integer.compare(task1.getId(), task2.getId());
            }
            if (startTime1 == null) {
                return 1;
            }
            if (startTime2 == null) {
                return -1;
            }
            return startTime1.compareTo(startTime2);    // здесь уже не будет равного времени
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





