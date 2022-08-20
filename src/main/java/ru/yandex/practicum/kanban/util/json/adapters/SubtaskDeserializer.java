package ru.yandex.practicum.kanban.util.json.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ru.yandex.practicum.kanban.tasks.Task;

public abstract class SubtaskDeserializer<T extends Task> extends TaskDeserializer<T> {
    protected int epicId;

    @Override
    protected JsonObject parseElements(JsonElement jsonElement,
                                       JsonDeserializationContext jsonDeserializationContext) {
        JsonObject jsonObject = super.parseElements(jsonElement, jsonDeserializationContext);
        if (jsonObject == null) {
            return null;
        }
        if (!jsonObject.has("epicId")) {
            return null;
        }
        try {
            epicId = jsonObject.get("epicId").getAsInt();
        } catch (JsonParseException e) {
            return null;
        }
        return jsonObject;
    }
}
