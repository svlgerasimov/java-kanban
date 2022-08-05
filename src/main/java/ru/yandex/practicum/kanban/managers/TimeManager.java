package ru.yandex.practicum.kanban.managers;

import ru.yandex.practicum.kanban.tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

public class TimeManager {
    // Чтобы не сравнивать пересечение со всеми задачами, время задачи делится на интервалы фиксированной длины.
    // Для проверки пересечения новой задачи с существующими проверяется, свободны ли все её интервалы;
    // если интервал не свободен, проверяется пересечение только с задачами, пересекающимися с этим интервалом.
    // Интервал определяется LocalDateTime его начала.
    private final static int BASE_INTERVAL_MINUTES = 60;    // Длительность интервала
    private final static LocalDateTime BASE_TIME =
            LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0);

    private final Map<LocalDateTime, List<Task>> tasksByInterval;

    private final TreeSet<Task> prioritizedTasks;

    public TimeManager() {
        tasksByInterval = new HashMap<>();
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
            // Если время начала двух задач равно (прежде всего случай startTime == null,
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
        if (task.getStartTime() != null) {
            addTaskToBaseIntervals(task);
        }
    }

    public void removeTask(Task task) {
        if (task == null) {
            return;
        }
        prioritizedTasks.remove(task);
        if (task.getStartTime() != null) {
            removeTaskFromBaseIntervals(task);
        }
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Проверка на пересечение с существующими задачами
    public boolean validateTask(Task task) {
        if (task == null) {
            return false;
        }
        if (task.getStartTime() == null) {
            return true;
        }
        return validateTaskByIntervals(task);
    }

    // Проверка на пересечение сравнением со всеми задачами
    private boolean simpleValidateTask(Task task) {
        return prioritizedTasks.stream()
                .filter(presentTask -> presentTask.getStartTime() != null)
                .noneMatch(presentTask -> intercepts(task, presentTask));
    }

    // Проверка на пересечение с использованием интервалов
    private boolean validateTaskByIntervals(Task task) {
        LocalDateTime intervalTime = roundToBaseInterval(task.getStartTime());
        LocalDateTime endTime = task.getEndTime();
        while (intervalTime.isBefore(endTime)) {
            List<Task> tasks = tasksByInterval.get(intervalTime);
            if (tasks != null
                    && tasks.stream().anyMatch(presentTask -> intercepts(task, presentTask))) {
                return false;
            }
            intervalTime = intervalTime.plusMinutes(BASE_INTERVAL_MINUTES);
        }
        return true;
    }

    // Проверка пересечения двух задач
    private static boolean intercepts(Task task1, Task task2) {
        LocalDateTime startTime1 = task1.getStartTime();
        LocalDateTime startTime2 = task2.getStartTime();
        LocalDateTime endTime1 = task1.getEndTime();
        LocalDateTime endTime2 = task2.getEndTime();

        return (startTime1.isBefore(endTime2) && endTime1.isAfter(startTime2))
                || (startTime2.isBefore(endTime1) && endTime2.isAfter(startTime1));
    }

    // Вычисление, в какой интервал попадает время
    private static LocalDateTime roundToBaseInterval(LocalDateTime time) {
        long minutesFromBase = Duration.between(BASE_TIME, time).toMinutes();
        long roundMinutesFromBase = minutesFromBase - (minutesFromBase % BASE_INTERVAL_MINUTES);
        return BASE_TIME.plusMinutes(roundMinutesFromBase);
    }

    // Добавление интервалов задачи в таблицу
    private void addTaskToBaseIntervals(Task task) {
        LocalDateTime intervalTime = roundToBaseInterval(task.getStartTime());
        LocalDateTime endTime = task.getEndTime();
        while (intervalTime.isBefore(endTime)) {
            List<Task> tasks = tasksByInterval.computeIfAbsent(intervalTime, k -> new ArrayList<>());
            if (!tasks.contains(task)) {
                tasks.add(task);
            }
            intervalTime = intervalTime.plusMinutes(BASE_INTERVAL_MINUTES);
        }
    }

    // Удаление интервалов задачи из таблицы
    private void removeTaskFromBaseIntervals(Task task) {
        LocalDateTime intervalTime = roundToBaseInterval(task.getStartTime());
        LocalDateTime endTime = task.getEndTime();
        while (intervalTime.isBefore(endTime)) {
            List<Task> tasks = tasksByInterval.get(intervalTime);
            if (tasks != null) {
                tasks.remove(task);
                if (tasks.isEmpty()) {
                    tasksByInterval.remove(intervalTime);
                }
            }
            intervalTime = intervalTime.plusMinutes(BASE_INTERVAL_MINUTES);
        }
    }
}