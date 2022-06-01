public class Subtask extends Task {

    private Epic epic;

    public Subtask(int id, String name, String description, int status, Epic epic) {
        super(id, name, description, status);
        this.epic = epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public String toString() {
        String result = "Subtask{" +
                super.toString() +
                ", epic=";
        if(epic == null) {
            result += null;
        } else {
            result += '\'' + epic.getName() + '\'';
        }
        result += '}';
        return result;
    }
}
