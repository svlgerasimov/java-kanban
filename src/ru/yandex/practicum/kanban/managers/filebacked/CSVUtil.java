package ru.yandex.practicum.kanban.managers.filebacked;

import ru.yandex.practicum.kanban.managers.HistoryManager;
import ru.yandex.practicum.kanban.tasks.*;

import java.util.ArrayList;
import java.util.List;

public final class CSVUtil {

    private final static String DELIMITER = ",";
    public final static String FILE_HEADER = "id" + DELIMITER +
            "type" + DELIMITER + "name" + DELIMITER +
            "status" + DELIMITER + "description" + DELIMITER +
            "epic";

    private CSVUtil() {
    }

    public static String taskToString(Task task) {
        if (task == null) {
            return "";
        }
        return task.getId() + DELIMITER +
                task.getType() + DELIMITER +
                task.getName() + DELIMITER +
                task.getStatus() + DELIMITER +
                task.getDescription() + DELIMITER;
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
            final String description;
            if (words.length > 4) {
                description = words[4];
            } else {
                description = "";   //если описание задачи пусто, split обрежет этот столбец, т.к. он последний
            }
            switch (type) {
                case TASK:
                    return new Task(id,name, description, status);
                case SUBTASK:
                    final int epic = Integer.parseInt(words[5]);
                    return new Subtask(id, name, description, status, epic);
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

//    //Класс для конвертации строк CSV файла в поля классов задач и обратно
//    public static class TaskFieldsCSV {
//        private final int id;
//        private final TaskType type;
//        private final String name;
//        private final TaskStatus status;
//        private final String description;
//        private final Integer epic;
//
//        public TaskFieldsCSV(String csvLine) {
//            try {
//                String[] words = csvLine.split(DELIMITER);
//                id = Integer.parseInt(words[0]);
//                type = TaskType.valueOf(words[1]);
//                name = words[2];
//                status = TaskStatus.valueOf(words[3]);
//                if (words.length > 4) {
//                    description = words[4];
//                } else {
//                    description = "";   //если описание задачи пусто, split обрежет этот столбец, т.к. он последний
//                }
//                if (TaskType.SUBTASK.equals(type)) {
//                    epic = Integer.valueOf(words[5]);
//                } else {
//                    epic = null;
//                }
//            } catch (Exception e) {
//                throw new WrongCSVFormatException("CSV format error in line {" + csvLine + "}");
//            }
//        }
//
//        public int getId() {
//            return id;
//        }
//
//        public TaskType getType() {
//            return type;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public TaskStatus getStatus() {
//            return status;
//        }
//
//        public String getDescription() {
//            return description;
//        }
//
//        public Integer getEpic() {
//            return epic;
//        }
//    }
}
