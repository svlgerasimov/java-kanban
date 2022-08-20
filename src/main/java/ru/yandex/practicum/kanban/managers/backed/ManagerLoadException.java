package ru.yandex.practicum.kanban.managers.backed;

public class ManagerLoadException extends RuntimeException {
    //RuntimeException - чтобы не менять сигнатуру методов TaskManager

    public ManagerLoadException(final String message) {
        super(message);
    }
}
