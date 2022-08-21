package ru.yandex.practicum.kanban.util.kvstorage;

public class ClientSendException extends RuntimeException {

    public ClientSendException(final String message) {
        super(message);
    }
}
