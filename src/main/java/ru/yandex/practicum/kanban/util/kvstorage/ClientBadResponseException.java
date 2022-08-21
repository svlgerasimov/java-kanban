package ru.yandex.practicum.kanban.util.kvstorage;

public class ClientBadResponseException extends RuntimeException {

    public ClientBadResponseException(final String message) {
        super(message);
    }
}
