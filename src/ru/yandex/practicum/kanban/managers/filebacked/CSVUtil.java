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

    public static String historyToString(HistoryManager historyManager) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Task task : historyManager.getHistory()) {
            stringBuilder.append(task.getId()).append(DELIMITER);
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

    //Класс для конвертации строк CSV файла в поля классов задач и обратно
    public static class TaskFieldsCSV {
        private final int id;
        private final TaskType type;
        private final String name;
        private final TaskStatus status;
        private final String description;
        private final Integer epic;

        public TaskFieldsCSV(String csvLine) {
            try {
                String[] words = csvLine.split(DELIMITER);
                id = Integer.parseInt(words[0]);
                type = TaskType.valueOf(words[1]);
                name = words[2];
                status = TaskStatus.valueOf(words[3]);
                if (words.length > 4) {
                    description = words[4];
                } else {
                    description = "";   //если описание задачи пусто, split обрежет этот столбец, т.к. он последний
                }
                if (TaskType.SUBTASK.equals(type)) {
                    epic = Integer.valueOf(words[5]);
                } else {
                    epic = null;
                }
            } catch (Exception e) {
                throw new WrongCSVFormatException("CSV format error in line {" + csvLine + "}");
            }
        }

        public TaskFieldsCSV(Task task) {
            this.id = task.getId();
            this.type = TaskType.TASK;
            this.name = task.getName();
            this.status = task.getStatus();
            this.description = task.getDescription();
            this.epic = null;
        }

        public TaskFieldsCSV(Epic epic) {
            this.id = epic.getId();
            this.type = TaskType.EPIC;
            this.name = epic.getName();
            this.status = epic.getStatus();
            this.description = epic.getDescription();
            this.epic = null;
        }

        public TaskFieldsCSV(Subtask subtask) {
            this.id = subtask.getId();
            this.type = TaskType.SUBTASK;
            this.name = subtask.getName();
            this.status = subtask.getStatus();
            this.description = subtask.getDescription();
            this.epic = subtask.getEpicId();
        }

        @Override
        public String toString() {
            String result = id + DELIMITER +
                    type + DELIMITER +
                    name + DELIMITER +
                    status + DELIMITER +
                    description + DELIMITER;
            if (TaskType.SUBTASK.equals(type)) {
                result += epic;
            }
            return result;
        }

        public int getId() {
            return id;
        }

        public TaskType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public TaskStatus getStatus() {
            return status;
        }

        public String getDescription() {
            return description;
        }

        public Integer getEpic() {
            return epic;
        }
    }
}
