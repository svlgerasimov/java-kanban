package ru.yandex.practicum.kanban.managers.filebacked;

public class WrongCSVFormatException extends RuntimeException {

    public WrongCSVFormatException(final String message) {
        super(message);
    }
}
