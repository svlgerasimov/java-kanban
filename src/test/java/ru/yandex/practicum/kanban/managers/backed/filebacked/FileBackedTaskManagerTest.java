package ru.yandex.practicum.kanban.managers.backed.filebacked;

import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.kanban.managers.backed.BackedTaskManagerTest;

import java.nio.file.Path;

public class FileBackedTaskManagerTest extends BackedTaskManagerTest {

    private final Path filePath = Path.of("src","test", "resources", "taskManager.csv");

    @Override
    protected FileBackedTaskManager loadStateInNewManager() {
        return new FileBackedTaskManager(filePath.toString(), true);
    }

    @BeforeEach
    public void beforeEach() {
        taskManager = new FileBackedTaskManager(filePath.toString());
        taskManager.save(); // чтобы сохранить новый пустой менеджер в файл, если в нём что-то было
    }

}
