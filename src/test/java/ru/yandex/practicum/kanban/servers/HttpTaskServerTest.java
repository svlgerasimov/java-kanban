package ru.yandex.practicum.kanban.servers;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.*;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {

    private static final String URI_PRIORITIZED_TASKS = "http://localhost:8080/tasks";
    private static final String URI_TASK_OPERATIONS = "http://localhost:8080/tasks/task";
    private static final String URI_SUBTASK_OPERATIONS = "http://localhost:8080/tasks/subtask";
    private static final String URI_EPIC_OPERATIONS = "http://localhost:8080/tasks/epic";
    private static final String URI_EPIC_SUBTASKS = "http://localhost:8080/tasks/subtask/epic";
    private static final String URI_HISTORY = "http://localhost:8080/tasks/history";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final Path FILE_PATH = Path.of("src","test", "resources", "taskManager.csv");;
    private final static LocalDateTime DEFAULT_TIME =
            LocalDateTime.of(2022, Month.JANUARY, 1, 1, 1, 0);
    private final static int REQUEST_TIMEOUT_SECONDS = 1;

//    private static Gson gson;
    private HttpTaskServer httpTaskServer;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newHttpClient();
//        gson = new GsonBuilder()
//                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
//                    @Override
//                    public void write(JsonWriter jsonWriter, LocalDateTime time) throws IOException {
//                        jsonWriter.value(time == null ? null : time.format(DATE_TIME_FORMATTER));
//                    }
//
//                    @Override
//                    public LocalDateTime read(JsonReader jsonReader) throws IOException {
//                        return LocalDateTime.parse(jsonReader.nextString(), DATE_TIME_FORMATTER);
//                    }
//                })
//                .create();
    }

    @AfterAll
    static void afterAll() {
    }

    @BeforeEach
    void setUp() throws IOException {
        httpTaskServer = new HttpTaskServer(FILE_PATH, false);
        httpTaskServer.start();
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @AfterEach
    void tearDown() {
        httpTaskServer.stop(0);
    }

    // Чтобы проверить запросы с отсутствующими или некорректными полями gson.toGson(task) не подойдет
    private static String createJsonForTask(String id, String name, String description, String status,
                                     String startTime, String duration, String epicId){
        List<String> fields = new ArrayList<>();
        if (id != null) {
            fields.add(String.format("\"id\"=\"%s\"", id));
        }
        if (name != null) {
            fields.add(String.format("\"name\"=\"%s\"", name));
        }
        if (description != null) {
            fields.add(String.format("\"description\"=\"%s\"", description));
        }
        if (status != null) {
            fields.add(String.format("\"status\"=\"%s\"", status));
        }
        if (startTime != null) {
            fields.add(String.format("\"startTime\"=\"%s\"", startTime));
        }
        if (duration != null) {
            fields.add(String.format("\"duration\"=\"%s\"", duration));
        }
        if (epicId != null) {
            fields.add(String.format("\"epicId\"=\"%s\"", epicId));
        }
        return "{" + String.join(",", fields) + "}";
    }

    private static HttpResponse<String> postRequest(String endPoint, String body)
            throws IOException, InterruptedException {
        URI uri = URI.create(endPoint);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(bodyPublisher)
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void incorrectUriTest() throws IOException, InterruptedException {
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND,
                postRequest(URI_PRIORITIZED_TASKS + "1", "").statusCode());
    }

    // Add task

    @Test
    public void addNewTaskCorrectTest() throws IOException, InterruptedException {
        String name = "name";
        String description = "description";
        TaskStatus status = TaskStatus.IN_PROGRESS;
        LocalDateTime startTime = DEFAULT_TIME;
        int duration = 10;

        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, name, description, String.valueOf(status),
                        startTime.format(DATE_TIME_FORMATTER), String.valueOf(duration), null));

        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        String responseBody = response.body();
        JsonElement jsonElement = JsonParser.parseString(responseBody);
        assertTrue(jsonElement.isJsonObject());

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals(name, jsonObject.get("name").getAsString());
        assertEquals(description, jsonObject.get("description").getAsString());
        assertEquals(status, TaskStatus.valueOf(jsonObject.get("status").getAsString()));
        assertEquals(startTime, LocalDateTime.parse(jsonObject.get("startTime").getAsString(), DATE_TIME_FORMATTER));
        assertEquals(duration, jsonObject.get("duration").getAsInt());
    }

    @Test
    public void addNewTaskIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS + "1",
                createJsonForTask(null, "", "", TaskStatus.NEW.toString(),
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10), null));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void addNewTaskCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS + "/",
                createJsonForTask(null, "", "", TaskStatus.NEW.toString(),
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10), null));
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

    @Test
    public void addNewTaskAbsentFieldsTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, null));

        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        String responseBody = response.body();
        JsonElement jsonElement = JsonParser.parseString(responseBody);
        assertTrue(jsonElement.isJsonObject());

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals("", jsonObject.get("name").getAsString());
        assertEquals("", jsonObject.get("description").getAsString());
        assertEquals(TaskStatus.NEW, TaskStatus.valueOf(jsonObject.get("status").getAsString()));
        assertFalse(jsonObject.has("startTime"));
        assertEquals(0, jsonObject.get("duration").getAsInt());
    }

    @Test
    public void addNewTaskIncorrectBodyTest() throws IOException, InterruptedException {
        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_TASK_OPERATIONS, "").statusCode());
    }

    @Test
    public void addNewTaskIncorrectStatusTest() throws IOException, InterruptedException {
        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_TASK_OPERATIONS, createJsonForTask(null, null, null, "",
                        null, null, null)).statusCode());
    }

    @Test
    public void addNewTaskIncorrectStartTimeTest() throws IOException, InterruptedException {
        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_TASK_OPERATIONS, createJsonForTask(null, null, null, null,
                        "", null, null)).statusCode());
    }

    @Test
    public void addNewTaskIncorrectDurationTest() throws IOException, InterruptedException {
        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_TASK_OPERATIONS, createJsonForTask(null, null, null, null,
                        null, "", null)).statusCode());
    }

    @Test
    public void addNewTaskWithTimeIntersectionTest() throws IOException, InterruptedException {
        postRequest(URI_TASK_OPERATIONS, createJsonForTask(null, null, null, null,
                DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10), null));
        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        DEFAULT_TIME.plusMinutes(5).format(DATE_TIME_FORMATTER), String.valueOf(10), null));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_ACCEPTABLE, response.statusCode());
    }

    // add epic

    @Test
    public void addNewEpicCorrectTest() throws IOException, InterruptedException {
        String name = "name";
        String description = "description";

        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, name, description, null,
                        null, null, null));

        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        String responseBody = response.body();
        JsonElement jsonElement = JsonParser.parseString(responseBody);
        assertTrue(jsonElement.isJsonObject());

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals(name, jsonObject.get("name").getAsString());
        assertEquals(description, jsonObject.get("description").getAsString());
        assertEquals(TaskStatus.NEW, TaskStatus.valueOf(jsonObject.get("status").getAsString()));
        assertFalse(jsonObject.has("startTime"));
        assertEquals(0, jsonObject.get("duration").getAsInt());
        assertFalse(jsonObject.has("endTime"));
        assertTrue(jsonObject.has("subtaskIds"));
        JsonElement subtasksElement = jsonObject.get("subtaskIds");
        assertTrue(subtasksElement.isJsonArray());
        assertEquals(0, subtasksElement.getAsJsonArray().size());
    }

    @Test
    public void addNewEpicIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS + "1",
                createJsonForTask(null, "", "", null,
                        null, null, null));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void addNewEpicCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS + "/",
                createJsonForTask(null, "", "", null,
                        null, null, null));
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

    @Test
    public void addNewEpicAbsentFieldsTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, null));

        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        String responseBody = response.body();
        JsonElement jsonElement = JsonParser.parseString(responseBody);
        assertTrue(jsonElement.isJsonObject());

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals("", jsonObject.get("name").getAsString());
        assertEquals("", jsonObject.get("description").getAsString());
        assertEquals(TaskStatus.NEW, TaskStatus.valueOf(jsonObject.get("status").getAsString()));
        assertFalse(jsonObject.has("startTime"));
        assertEquals(0, jsonObject.get("duration").getAsInt());
        assertFalse(jsonObject.has("endTime"));
        assertTrue(jsonObject.has("subtaskIds"));
        JsonElement subtasksElement = jsonObject.get("subtaskIds");
        assertTrue(subtasksElement.isJsonArray());
        assertEquals(0, subtasksElement.getAsJsonArray().size());
    }

    @Test
    public void addNewEpicIncorrectBodyTest() throws IOException, InterruptedException {
        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_EPIC_OPERATIONS, "").statusCode());
    }

    // Add Subtask

    @Test
    public void addNewSubtaskCorrectTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        int epicId = JsonParser.parseString(response.body()).getAsJsonObject().get("id").getAsInt();

        String name = "name";
        String description = "description";
        TaskStatus status = TaskStatus.IN_PROGRESS;
        LocalDateTime startTime = DEFAULT_TIME;
        int duration = 10;

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, name, description, String.valueOf(status),
                        startTime.format(DATE_TIME_FORMATTER), String.valueOf(duration), String.valueOf(epicId)));

        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        String responseBody = response.body();
        JsonElement jsonElement = JsonParser.parseString(responseBody);
        assertTrue(jsonElement.isJsonObject());

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals(name, jsonObject.get("name").getAsString());
        assertEquals(description, jsonObject.get("description").getAsString());
        assertEquals(status, TaskStatus.valueOf(jsonObject.get("status").getAsString()));
        assertEquals(startTime, LocalDateTime.parse(jsonObject.get("startTime").getAsString(), DATE_TIME_FORMATTER));
        assertEquals(duration, jsonObject.get("duration").getAsInt());
        assertEquals(epicId, jsonObject.get("epicId").getAsInt());
    }

    @Test
    public void addNewSubtaskIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        int epicId = JsonParser.parseString(response.body()).getAsJsonObject().get("id").getAsInt();

        response = postRequest(URI_SUBTASK_OPERATIONS + "1",
                createJsonForTask(null, "", "", TaskStatus.NEW.toString(),
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10), String.valueOf(epicId)));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void addNewSubtaskCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        int epicId = JsonParser.parseString(response.body()).getAsJsonObject().get("id").getAsInt();

        response = postRequest(URI_SUBTASK_OPERATIONS + "/",
                createJsonForTask(null, "", "", TaskStatus.NEW.toString(),
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10), String.valueOf(epicId)));
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

    @Test
    public void addNewSubtaskAbsentFieldsTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        int epicId = JsonParser.parseString(response.body()).getAsJsonObject().get("id").getAsInt();

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, String.valueOf(epicId)));

        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        String responseBody = response.body();
        JsonElement jsonElement = JsonParser.parseString(responseBody);
        assertTrue(jsonElement.isJsonObject());

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals("", jsonObject.get("name").getAsString());
        assertEquals("", jsonObject.get("description").getAsString());
        assertEquals(TaskStatus.NEW, TaskStatus.valueOf(jsonObject.get("status").getAsString()));
        assertFalse(jsonObject.has("startTime"));
        assertEquals(0, jsonObject.get("duration").getAsInt());
    }

    @Test
    public void addNewSubtaskIncorrectBodyTest() throws IOException, InterruptedException {
        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_SUBTASK_OPERATIONS, "").statusCode());
    }

    @Test
    public void addNewSubtaskIncorrectStatusTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        int epicId = JsonParser.parseString(response.body()).getAsJsonObject().get("id").getAsInt();

        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_SUBTASK_OPERATIONS, createJsonForTask(null, null, null, "",
                        null, null, String.valueOf(epicId))).statusCode());
    }

    @Test
    public void addNewSubtaskIncorrectStartTimeTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        int epicId = JsonParser.parseString(response.body()).getAsJsonObject().get("id").getAsInt();

        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_SUBTASK_OPERATIONS, createJsonForTask(null, null, null, null,
                        "", null, String.valueOf(epicId))).statusCode());
    }

    @Test
    public void addNewSubtaskIncorrectDurationTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        int epicId = JsonParser.parseString(response.body()).getAsJsonObject().get("id").getAsInt();

        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_SUBTASK_OPERATIONS, createJsonForTask(null, null, null, null,
                        null, "", String.valueOf(epicId))).statusCode());
    }

    @Test
    public void addNewSubtaskIncorrectEpicIdTest() throws IOException, InterruptedException {
        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_SUBTASK_OPERATIONS, createJsonForTask(null, null, null, null,
                        null, "", "")).statusCode());
    }

    @Test
    public void addNewSubtaskAbsentEpicIdTest() throws IOException, InterruptedException {
        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_SUBTASK_OPERATIONS, createJsonForTask(null, null, null, null,
                        null, "", "1")).statusCode());
    }

    @Test
    public void addNewSubtaskWithTimeIntersectionTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        int epicId = JsonParser.parseString(response.body()).getAsJsonObject().get("id").getAsInt();

        postRequest(URI_SUBTASK_OPERATIONS, createJsonForTask(null, null, null, null,
                DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10), String.valueOf(epicId)));
        response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        DEFAULT_TIME.plusMinutes(5).format(DATE_TIME_FORMATTER), String.valueOf(10),
                        String.valueOf(epicId)));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_ACCEPTABLE, response.statusCode());
    }


}
