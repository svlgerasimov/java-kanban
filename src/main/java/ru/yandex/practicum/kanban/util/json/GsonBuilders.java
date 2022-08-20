package ru.yandex.practicum.kanban.util.json;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.util.json.adapters.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class GsonBuilders {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private GsonBuilders() {
    }

    public static GsonBuilder getBuilderNoTaskTypes() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter jsonWriter, LocalDateTime time) throws IOException {
                        jsonWriter.value(time == null ? null : time.format(DATE_TIME_FORMATTER));
                    }

                    @Override
                    public LocalDateTime read(JsonReader jsonReader) throws IOException {
                        try {
                            return LocalDateTime.parse(jsonReader.nextString(), DATE_TIME_FORMATTER);
                        } catch (DateTimeParseException e) {
                            throw new JsonParseException("Incorrect DateTime format");
                        }
                    }
                });
    }

    public static GsonBuilder getBuilderSeparateTaskTypes() {
        return getBuilderNoTaskTypes()
                .registerTypeAdapter(Epic.class, new EpicDeserializer<>() {
                    @Override
                    protected Epic createInstance() {
                        return new Epic(id, name, description);
                    }
                })
                .registerTypeAdapter(Subtask.class, new SubtaskDeserializer<>() {
                    @Override
                    protected Subtask createInstance() {
                        return new Subtask(id, name, description, status, epicId, startTime, duration);
                    }
                })
                .registerTypeAdapter(Task.class, new TaskDeserializer<>() {
                    @Override
                    protected Task createInstance() {
                        return new Task(id, name, description, status, startTime, duration);
                    }
                });
    }
}
