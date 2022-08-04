package ru.yandex.practicum.kanban.managers.filebacked;

public class WrongCSVFormatException extends RuntimeException {
    //RuntimeException - чтобы не менять сигнатуру методов TaskManager

    public WrongCSVFormatException(final String message) {
        super(message);
    }
}
