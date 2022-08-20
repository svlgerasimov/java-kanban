package ru.yandex.practicum.kanban.managers.backed;

public class ManagerSaveException extends RuntimeException{
    //RuntimeException - чтобы не менять сигнатуру методов TaskManager

    public ManagerSaveException(final String message) {
        super(message);
    }
}
