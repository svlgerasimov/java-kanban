import java.util.Objects;

public class Task {
    public static final int STATUS_NEW = 1;
    public static final int STATUS_IN_PROGRESS = 2;
    public static final int STATUS_DONE = 3;

    private String name;
    private String description;
    private int id;
    protected int status;

    public Task(int id, String name, String description, int status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    //При замене на обновленный экземпляр задачи она ищется по id, поэтому сравнение только по этому полю
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + statusText() +
                '}';
    }

    public String statusText() {
        String result;
        switch (status) {
            case STATUS_NEW:
                result = "NEW";
                break;
            case  STATUS_IN_PROGRESS:
                result = "IN_PROGRESS";
                break;
            case STATUS_DONE:
                result = "DONE";
                break;
            default:
                result = "UNDEFINED";
        }
        return result;
    }
}
