package ru.yandex.practicum.kanban.util.json.adapters;

import com.google.gson.*;
import ru.yandex.practicum.kanban.tasks.Task;

import java.lang.reflect.Type;

// Классы для десериализации нужны, чтобы создавать задачи через конструктор,
// контролировать наличие нужных полей и игнорировать ненужные поля в полученном json.
// Иерархия EpicDeserializer -> TaskDeserializer -> SubtaskDeserializer,
// в порядке возрастания количества параметров в конструкторе десериализируемого класса

public abstract class EpicDeserializer<T extends Task> implements JsonDeserializer<T> {
    protected int id;
    protected String name;
    protected String description;

    // Возвращаем JsonObject, чтобы наследники могли продолжать разбирать нужные им поля,
    // null - если что-то пошло не так
    protected JsonObject parseElements(JsonElement jsonElement,
                                       JsonDeserializationContext jsonDeserializationContext) {
        if (!jsonElement.isJsonObject()) {
            return null;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "";
        description = jsonObject.has("description") ?
                jsonObject.get("description").getAsString() : "";
        id = jsonObject.has("id") ? jsonObject.get("id").getAsInt() : 0;
        return jsonObject;
    }

    // В реализации предполагается конструктор, который берет аргументы из полей класса
    protected abstract T createInstance();

    @Override
    public T deserialize(JsonElement jsonElement, Type type,
                         JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return parseElements(jsonElement, jsonDeserializationContext) == null ? null : createInstance();
    }
}
