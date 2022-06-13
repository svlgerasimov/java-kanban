package ru.yandex.practicum.kanban.managers;

public class Managers {

    public TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}
