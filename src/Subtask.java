public class Subtask extends Task {
    private final int epicId;

    public Subtask(int id, String name, String description, int status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        String result = "Subtask{" +
                super.toString() +
                ", epicId=" + epicId;
        result += '}';
        return result;
    }
}
