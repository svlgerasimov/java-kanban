package ru.yandex.practicum.kanban.servers.json.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;

import java.time.LocalDateTime;

public abstract class TaskDeserializer<T extends Task> extends EpicDeserializer<T> {
    protected TaskStatus status;
    protected LocalDateTime startTime;
    protected int duration;

    @Override
    protected JsonObject parseElements(JsonElement jsonElement,
                                       JsonDeserializationContext jsonDeserializationContext) {
        JsonObject jsonObject = super.parseElements(jsonElement, jsonDeserializationContext);
        if (jsonObject == null) {
            return null;
        }
        try {
            status = jsonObject.has("status") ?
                    TaskStatus.valueOf(jsonObject.get("status").getAsString()) : TaskStatus.NEW;
        } catch (IllegalArgumentException e) {
            return null;
        }
        try {
            startTime = jsonObject.has("startTime") ? jsonDeserializationContext.
                    deserialize(jsonObject.get("startTime"), LocalDateTime.class) : null;
        } catch (JsonParseException e) {
            return null;
        }
        try {
            duration = jsonObject.has("duration") ? jsonObject.get("duration").getAsInt() : 0;
        } catch (NumberFormatException e) {
            return null;
        }
        return jsonObject;
    }
}
