package ru.yandex.practicum.kanban.managers.backed.filebacked;

import ru.yandex.practicum.kanban.managers.HistoryManager;
import ru.yandex.practicum.kanban.tasks.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class CSVUtil {

    private final static String DELIMITER = ",";
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public final static String FILE_HEADER = "id" + DELIMITER +
            "type" + DELIMITER + "name" + DELIMITER +
            "status" + DELIMITER + "description" + DELIMITER +
            "start time" + DELIMITER + "duration" + DELIMITER +
            "epic";

    private CSVUtil() {
    }

    public static String taskToString(Task task) {
        if (task == null) {
            return "";
        }
        LocalDateTime startTime = task.getStartTime();
        String formattedStartTime = startTime == null ? "" : startTime.format(FORMATTER);
        return task.getId() + DELIMITER +
                task.getType() + DELIMITER +
                task.getName() + DELIMITER +
                task.getStatus() + DELIMITER +
                task.getDescription() + DELIMITER +
                formattedStartTime + DELIMITER +
                task.getDuration() + DELIMITER;
    }

    // перегрузка метода для Subtask, чтобы не использовать в общем методе ветвление с instanceOf
    // и не делать сужающее приведение типов на основании getType()
    public static String taskToString(Subtask subtask) {
        if (subtask == null) {
            return "";
        }
        return taskToString((Task) subtask) + subtask.getEpicId();
    }

    public static Task taskFromString(String csvLine) {
        try {
            String[] words = csvLine.split(DELIMITER);
            final int id = Integer.parseInt(words[0]);
            final TaskType type = TaskType.valueOf(words[1]);
            final String name = words[2];
            final TaskStatus status = TaskStatus.valueOf(words[3]);
            final String description = words[4];
            final String formattedDateTime = words[5];
            final LocalDateTime startTime =
                    formattedDateTime.isEmpty() ? null : LocalDateTime.parse(formattedDateTime, FORMATTER);
            final int duration = Integer.parseInt(words[6]);
            switch (type) {
                case TASK:
                    return new Task(id, name, description, status, startTime, duration);
                case SUBTASK:
                    final int epic = Integer.parseInt(words[7]);
                    return new Subtask(id, name, description, status, epic, startTime, duration);
                case EPIC:
                    return new Epic(id, name, description);
            }
            throw new WrongCSVFormatException("Unsupported task type " + type + " in line {" + csvLine + "}");
        } catch (Exception e) {
            throw new WrongCSVFormatException("CSV format error in line {" + csvLine + "}");
        }
    }

    public static String historyToString(HistoryManager historyManager) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Task> history = historyManager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            Task task = history.get(i);
            stringBuilder.append(task.getId());
            if (i < history.size() - 1) {
                stringBuilder.append(DELIMITER);
            }
        }
        return stringBuilder.toString();
    }

    public static List<Integer> historyIdsFromString(String line) {
        List<Integer> result = new ArrayList<>();
        try {
            for (String word : line.split(DELIMITER)) {
                result.add(Integer.valueOf(word));
            }
        } catch (NumberFormatException e) {
            throw new WrongCSVFormatException("CSV format error in line {" + line + "} (history)");
        }
        return result;
    }
}