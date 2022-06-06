import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds;

    //в конструкторе нет статуса, т.к. он рассчитывается на основе подзадач
    public Epic(int id, String name, String description) {
        super(id, name, description, Task.STATUS_NEW);
        subtaskIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(int subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    public void removeSubtask(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));  //явная упаковка чтобы subtaskId не воспринимался как индекс
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    public void updateStatus(ArrayList<Integer> subtaskStatuses) {
        if (subtaskStatuses == null || subtaskStatuses.isEmpty()) {
            setStatus(STATUS_NEW);
            return;
        }
        int subtasksStatus = subtaskStatuses.get(0);
        for (int i = 1; i < subtaskStatuses.size(); i++) {
            if (subtaskStatuses.get(i) != subtasksStatus) {
                setStatus(STATUS_IN_PROGRESS);
                return;
            }
        }
        setStatus(subtasksStatus);
    }

    @Override
    public String toString() {
        return "Epic{" +
                super.toString() +
                ", subtaskIds=" + subtaskIds + "}";
    }
}
