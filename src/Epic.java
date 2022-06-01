import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks;

    //в конструкторе нет статуса, т.к. он рассчитывается на основе подзадач
    public Epic(int id, String name, String description, ArrayList<Subtask> subtasks) {
        super(id, name, description, Task.STATUS_NEW);
        if(subtasks == null) {
            this.subtasks = new ArrayList<>();
        } else {
            this.subtasks = subtasks;
        }
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
        subtask.setEpic(this);
        status = calculateStatus();
    }

    //TODO подумать, нужно ли обнулять ссылку а эпик
    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
        subtask.setEpic(null);
        status = calculateStatus();
    }

    public void updateSubtask(Subtask subtask) {
        int index = subtasks.indexOf(subtask);
        if(index >= 0) {
            subtasks.set(index, subtask);
        }
        status = calculateStatus();
    }

    private int calculateStatus() {
        if(subtasks.isEmpty()) {
            return STATUS_NEW;
        }

        int subtasksStatus = subtasks.get(0).getStatus();
        for (int i = 1; i < subtasks.size(); i++) {
            if(subtasks.get(i).getStatus() != subtasksStatus) {
                return STATUS_IN_PROGRESS;
            }
        }
        return subtasksStatus;
    }


    @Override
    public String toString() {
        String result = "Epic{" +
                super.toString() +
                ", subtasks=[";
        for (int i = 0; i < subtasks.size(); i++) {
            result += '\'' + subtasks.get(i).getName() + '\'';
            if(i < subtasks.size() - 1) {
                result += ", ";
            }
        }
        result += "]}";
        return result;
    }

}
