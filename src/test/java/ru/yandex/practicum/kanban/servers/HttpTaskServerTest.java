package ru.yandex.practicum.kanban.servers;

import com.google.gson.*;
import org.junit.jupiter.api.*;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;
import ru.yandex.practicum.kanban.util.json.GsonBuilders;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {

    private static final String URI_PRIORITIZED_TASKS = "http://localhost:8080/tasks";
    private static final String URI_TASK_OPERATIONS = "http://localhost:8080/tasks/task";
    private static final String URI_SUBTASK_OPERATIONS = "http://localhost:8080/tasks/subtask";
    private static final String URI_EPIC_OPERATIONS = "http://localhost:8080/tasks/epic";
    private static final String URI_EPIC_SUBTASKS = "http://localhost:8080/tasks/subtask/epic";
    private static final String URI_HISTORY = "http://localhost:8080/tasks/history";
    private static final String ID_QUERY_FORMAT = "id=%d";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final Path FILE_PATH = Path.of("src","test", "resources", "taskManager.csv");
    private final static LocalDateTime DEFAULT_TIME =
            LocalDateTime.of(2022, Month.JANUARY, 1, 1, 1, 0);
    private final static int REQUEST_TIMEOUT_SECONDS = 1;

    private static Gson gson;
    private HttpTaskServer httpTaskServer;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newHttpClient();
        gson = GsonBuilders.getBuilderNoTaskTypes().create();
    }

    @AfterAll
    static void afterAll() {
    }

    @BeforeEach
    void setUp() throws IOException {
        httpTaskServer = new HttpTaskServer(FILE_PATH, false);
        httpTaskServer.start();
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

    private static HttpResponse<String> getRequest(String endPoint) throws IOException, InterruptedException {
        URI uri = URI.create(endPoint);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> deleteRequest(String endPoint) throws IOException, InterruptedException {
        URI uri = URI.create(endPoint);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
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

        Task task = gson.fromJson(response.body(), Task.class);
        assertNotNull(task);
        assertEquals(name, task.getName());
        assertEquals(description, task.getDescription());
        assertEquals(status, task.getStatus());
        assertEquals(startTime, task.getStartTime());
        assertEquals(duration, task.getDuration());
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

        Task task = gson.fromJson(response.body(), Task.class);
        assertNotNull(task);
        assertEquals("", task.getName());
        assertEquals("", task.getDescription());
        assertEquals(TaskStatus.NEW, task.getStatus());
        assertNull(task.getStartTime());
        assertEquals(0, task.getDuration());
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

        Epic epic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(epic);
        assertEquals(name, epic.getName());
        assertEquals(description, epic.getDescription());
        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertNull(epic.getStartTime());
        assertEquals(0, epic.getDuration());
        assertNull(epic.getEndTime());
        assertEquals(List.of(), epic.getSubtaskIds());
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

        Epic epic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(epic);
        assertEquals("", epic.getName());
        assertEquals("", epic.getDescription());
        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertNull(epic.getStartTime());
        assertEquals(0, epic.getDuration());
        assertNull(epic.getEndTime());
        assertEquals(List.of(), epic.getSubtaskIds());
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
        Epic epic = gson.fromJson(response.body(), Epic.class);

        String name = "name";
        String description = "description";
        TaskStatus status = TaskStatus.IN_PROGRESS;
        LocalDateTime startTime = DEFAULT_TIME;
        int duration = 10;

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, name, description, String.valueOf(status),
                        startTime.format(DATE_TIME_FORMATTER), String.valueOf(duration),
                        String.valueOf(epic.getId())));

        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        Subtask subtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(subtask);
        assertEquals(name, subtask.getName());
        assertEquals(description, subtask.getDescription());
        assertEquals(status, subtask.getStatus());
        assertEquals(startTime, subtask.getStartTime());
        assertEquals(duration, subtask.getDuration());
        assertEquals(epic.getId(), subtask.getEpicId());
    }

    @Test
    public void addNewSubtaskIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        Epic epic = gson.fromJson(response.body(), Epic.class);

        response = postRequest(URI_SUBTASK_OPERATIONS + "1",
                createJsonForTask(null, "", "", TaskStatus.NEW.toString(),
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10),
                        String.valueOf(epic.getId())));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void addNewSubtaskCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        Epic epic = gson.fromJson(response.body(), Epic.class);

        response = postRequest(URI_SUBTASK_OPERATIONS + "/",
                createJsonForTask(null, "", "", TaskStatus.NEW.toString(),
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10),
                        String.valueOf(epic.getId())));
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

    @Test
    public void addNewSubtaskAbsentFieldsTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        Epic epic = gson.fromJson(response.body(), Epic.class);

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, String.valueOf(epic.getId())));

        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        Subtask subtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals("", subtask.getName());
        assertEquals("", subtask.getDescription());
        assertEquals(TaskStatus.NEW, subtask.getStatus());
        assertNull(subtask.getStartTime());
        assertEquals(0, subtask.getDuration());
        assertEquals(epic.getId(), subtask.getEpicId());
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
        Epic epic = gson.fromJson(response.body(), Epic.class);

        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_SUBTASK_OPERATIONS, createJsonForTask(null, null, null, "",
                        null, null, String.valueOf(epic.getId()))).statusCode());
    }

    @Test
    public void addNewSubtaskIncorrectStartTimeTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        Epic epic = gson.fromJson(response.body(), Epic.class);

        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_SUBTASK_OPERATIONS, createJsonForTask(null, null, null, null,
                        "", null, String.valueOf(epic.getId()))).statusCode());
    }

    @Test
    public void addNewSubtaskIncorrectDurationTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        Epic epic = gson.fromJson(response.body(), Epic.class);

        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST,
                postRequest(URI_SUBTASK_OPERATIONS, createJsonForTask(null, null, null, null,
                        null, "", String.valueOf(epic.getId()))).statusCode());
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
        Epic epic = gson.fromJson(response.body(), Epic.class);

        postRequest(URI_SUBTASK_OPERATIONS, createJsonForTask(null, null, null, null,
                DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10), String.valueOf(epic.getId())));
        response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        DEFAULT_TIME.plusMinutes(5).format(DATE_TIME_FORMATTER), String.valueOf(10),
                        String.valueOf(epic.getId())));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_ACCEPTABLE, response.statusCode());
    }

    // get task

    @Test
    public void getTasksCorrectTest() throws IOException, InterruptedException {
        Task task1 = new Task(0, "name1", "description1", TaskStatus.IN_PROGRESS,
                DEFAULT_TIME, 10);
        Task task2 = new Task(0, "name2", "description2", TaskStatus.DONE,
                DEFAULT_TIME.plusMinutes(20), 10);

        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, task1.getName(), task1.getDescription(), String.valueOf(task1.getStatus()),
                        task1.getStartTime().format(DATE_TIME_FORMATTER), String.valueOf(task1.getDuration()),
                        null));
        Task receivedTask1 = gson.fromJson(response.body(), Task.class);
        task1.setId(receivedTask1.getId());

        response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, task2.getName(), task2.getDescription(), String.valueOf(task2.getStatus()),
                        task2.getStartTime().format(DATE_TIME_FORMATTER), String.valueOf(task2.getDuration()),
                        null));
        Task receivedTask2 = gson.fromJson(response.body(), Task.class);
        task2.setId(receivedTask2.getId());

        response = getRequest(URI_TASK_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, task1.getId()));
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        assertEquals(task1, gson.fromJson(response.body(), Task.class));

        response = getRequest(URI_TASK_OPERATIONS);
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        assertArrayEquals(new Task[] {task1, task2}, gson.fromJson(response.body(), Task[].class));
    }

    @Test
    public void getTaskIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = getRequest(URI_TASK_OPERATIONS + "1");
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void getTaskCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = getRequest(URI_TASK_OPERATIONS + "/");
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

    @Test
    public void getTaskIncorrectIdTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, "", "", TaskStatus.NEW.toString(),
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(0), null));
        int id = gson.fromJson(response.body(), Task.class).getId();

        response = getRequest(URI_TASK_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, id + 1));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    // get epic

    @Test
    public void getEpicsCorrectTest() throws IOException, InterruptedException {
        Epic epic1 = new Epic(0, "name1", "description1");
        Epic epic2 = new Epic(0, "name2", "description2");

        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, epic1.getName(), epic1.getDescription(), null,
                        null, null,null));
        Epic receivedEpic1 = gson.fromJson(response.body(), Epic.class);
        epic1.setId(receivedEpic1.getId());

        response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, epic2.getName(), epic2.getDescription(),
                        null, null, null, null));
        Epic receivedEpic2 = gson.fromJson(response.body(), Epic.class);
        epic2.setId(receivedEpic2.getId());

        response = getRequest(URI_EPIC_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, epic1.getId()));
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        assertEquals(epic1, gson.fromJson(response.body(), Epic.class));

        response = getRequest(URI_EPIC_OPERATIONS);
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        assertArrayEquals(new Epic[] {epic1, epic2}, gson.fromJson(response.body(), Epic[].class));
    }

    @Test
    public void getEpicWithSubtasksTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "",
                        null, null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();
        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null, null, null,
                        String.valueOf(epicId)));
        int subtaskId1 = gson.fromJson(response.body(), Subtask.class).getId();
        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null, null, null,
                        String.valueOf(epicId)));
        int subtaskId2 = gson.fromJson(response.body(), Subtask.class).getId();

        response = getRequest(URI_EPIC_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, epicId));
        assertEquals(List.of(subtaskId1, subtaskId2), gson.fromJson(response.body(), Epic.class).getSubtaskIds());
    }

    @Test
    public void getEpicIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = getRequest(URI_EPIC_OPERATIONS + "1");
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void getEpicCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = getRequest(URI_EPIC_OPERATIONS + "/");
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

    @Test
    public void getEpicIncorrectIdTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "",
                        null, null, null, null));
        int id = gson.fromJson(response.body(), Epic.class).getId();

        response = getRequest(URI_EPIC_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, id + 1));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    // get epic subtasks

    @Test
    public void getEpicSubtasksCorrectTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "",
                        null, null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null, null, null,
                        String.valueOf(epicId)));
        Subtask subtask1 = gson.fromJson(response.body(), Subtask.class);
        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null, null, null,
                        String.valueOf(epicId)));
        Subtask subtask2 = gson.fromJson(response.body(), Subtask.class);

        response = getRequest(URI_EPIC_SUBTASKS + "?" + String.format(ID_QUERY_FORMAT, epicId));
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        assertArrayEquals(new Subtask[] {subtask1, subtask2}, gson.fromJson(response.body(), Subtask[].class));
    }

    @Test
    public void getEpicSubtasksIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "",
                        null, null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        response = getRequest(URI_EPIC_SUBTASKS + "1" + "?" + String.format(ID_QUERY_FORMAT, epicId));
        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST, response.statusCode());
    }

    @Test
    public void getEpicSubtasksCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "",
                        null, null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        response = getRequest(URI_EPIC_SUBTASKS + "/" + "?" + String.format(ID_QUERY_FORMAT, epicId));
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

    @Test
    public void getEpicSubtasksIncorrectIdTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "",
                        null, null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        response = getRequest(URI_EPIC_SUBTASKS + "?" + String.format(ID_QUERY_FORMAT, epicId + 1));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void getEpicSubtasksAbsentIdInQueryTest() throws IOException, InterruptedException {
        postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "",
                        null, null, null, null));

        HttpResponse<String> response = getRequest(URI_EPIC_SUBTASKS);
        assertEquals(HttpTaskServer.RESPONSE_CODE_BAD_REQUEST, response.statusCode());
    }

    // get subtask

    @Test
    public void getSubtasksCorrectTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "",
                        null, null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        Subtask subtask1 = new Subtask(0, "name1", "description1", TaskStatus.IN_PROGRESS,
                epicId, DEFAULT_TIME, 10);
        Subtask subtask2 = new Subtask(0, "name2", "description2", TaskStatus.DONE,
                epicId, DEFAULT_TIME.plusMinutes(20), 10);

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, subtask1.getName(), subtask1.getDescription(),
                        String.valueOf(subtask1.getStatus()), subtask1.getStartTime().format(DATE_TIME_FORMATTER),
                        String.valueOf(subtask1.getDuration()), String.valueOf(subtask1.getEpicId())));
        Subtask receivedSubtask1 = gson.fromJson(response.body(), Subtask.class);
        subtask1.setId(receivedSubtask1.getId());

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, subtask2.getName(), subtask2.getDescription(),
                        String.valueOf(subtask2.getStatus()), subtask2.getStartTime().format(DATE_TIME_FORMATTER),
                        String.valueOf(subtask2.getDuration()), String.valueOf(subtask2.getEpicId())));
        Subtask receivedSubtask2 = gson.fromJson(response.body(), Subtask.class);
        subtask2.setId(receivedSubtask2.getId());

        response = getRequest(URI_SUBTASK_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, subtask1.getId()));
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        assertEquals(subtask1, gson.fromJson(response.body(), Subtask.class));

        response = getRequest(URI_SUBTASK_OPERATIONS);
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        assertArrayEquals(new Subtask[] {subtask1, subtask2}, gson.fromJson(response.body(), Subtask[].class));
    }

    @Test
    public void getSubtaskIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = getRequest(URI_SUBTASK_OPERATIONS + "1");
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void getSubtaskCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = getRequest(URI_SUBTASK_OPERATIONS + "/");
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

    @Test
    public void getSubtaskIncorrectIdTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "",
                        null, null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, "", "", TaskStatus.NEW.toString(),
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(0), String.valueOf(epicId)));
        int id = gson.fromJson(response.body(), Subtask.class).getId();

        response = getRequest(URI_SUBTASK_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, id + 1));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    // update task

    @Test
    public void updateTaskCorrectTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, null));
        int id = gson.fromJson(response.body(), Task.class).getId();
        Task updatedTask = new Task(id, "name", "description", TaskStatus.IN_PROGRESS,
                DEFAULT_TIME, 10);
        response = postRequest(URI_TASK_OPERATIONS, gson.toJson(updatedTask));

        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        response = getRequest(URI_TASK_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, id));
        assertEquals(updatedTask, gson.fromJson(response.body(), Task.class));
    }

    @Test
    public void updateTaskIncorrectIdTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, null));
        int id = gson.fromJson(response.body(), Task.class).getId();
        Task updatedTask = new Task(id + 1, "name", "description", TaskStatus.IN_PROGRESS,
                DEFAULT_TIME, 10);
        response = postRequest(URI_TASK_OPERATIONS, gson.toJson(updatedTask));

        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_ACCEPTABLE, response.statusCode());
    }

    @Test
    public void updateTaskWithTimeIntersectionTest() throws IOException, InterruptedException {
        postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10), null));
        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        DEFAULT_TIME.plusMinutes(20).format(DATE_TIME_FORMATTER), String.valueOf(10), null));
        int id = gson.fromJson(response.body(), Task.class).getId();
        Task updatedTask = new Task(id, "name", "description", TaskStatus.IN_PROGRESS,
                DEFAULT_TIME, 10);
        response = postRequest(URI_TASK_OPERATIONS, gson.toJson(updatedTask));

        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_ACCEPTABLE, response.statusCode());
    }

    // update epic

    @Test
    public void updateEpicCorrectTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, null));
        int id = gson.fromJson(response.body(), Epic.class).getId();
        Epic updatedEpic = new Epic(id, "name", "description");
        response = postRequest(URI_EPIC_OPERATIONS, gson.toJson(updatedEpic));

        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        response = getRequest(URI_EPIC_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, id));
        assertEquals(updatedEpic, gson.fromJson(response.body(), Epic.class));
    }

    @Test
    public void updateEpicIncorrectIdTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, null));
        int id = gson.fromJson(response.body(), Epic.class).getId();
        Epic updatedEpic = new Epic(id + 1, "name", "description");
        response = postRequest(URI_EPIC_OPERATIONS, gson.toJson(updatedEpic));

        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_ACCEPTABLE, response.statusCode());
    }

    // update subtask

    @Test
    public void updateSubtaskCorrectTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, String.valueOf(epicId)));
        int id = gson.fromJson(response.body(), Subtask.class).getId();
        Subtask updatedSubtask = new Subtask(id, "name", "description", TaskStatus.IN_PROGRESS,
                epicId, DEFAULT_TIME, 10);
        response = postRequest(URI_SUBTASK_OPERATIONS, gson.toJson(updatedSubtask));

        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        response = getRequest(URI_SUBTASK_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, id));
        assertEquals(updatedSubtask, gson.fromJson(response.body(), Subtask.class));
    }

    @Test
    public void updateSubtaskIncorrectIdTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, String.valueOf(epicId)));
        int id = gson.fromJson(response.body(), Subtask.class).getId();
        Subtask updatedSubtask = new Subtask(id + 1, "name", "description", TaskStatus.IN_PROGRESS,
                epicId, DEFAULT_TIME, 10);
        response = postRequest(URI_SUBTASK_OPERATIONS, gson.toJson(updatedSubtask));

        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_ACCEPTABLE, response.statusCode());
    }

    @Test
    public void updateSubtaskIncorrectEpicIdTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, String.valueOf(epicId)));
        int id = gson.fromJson(response.body(), Subtask.class).getId();
        Subtask updatedSubtask = new Subtask(id, "name", "description", TaskStatus.IN_PROGRESS,
                epicId + 10, DEFAULT_TIME, 10);
        response = postRequest(URI_SUBTASK_OPERATIONS, gson.toJson(updatedSubtask));

        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_ACCEPTABLE, response.statusCode());
    }

    @Test
    public void updateSubtaskWithTimeIntersectionTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10), String.valueOf(epicId)));
        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        DEFAULT_TIME.plusMinutes(20).format(DATE_TIME_FORMATTER), String.valueOf(10),
                        String.valueOf(epicId)));
        int id = gson.fromJson(response.body(), Subtask.class).getId();
        Subtask updatedSubtask = new Subtask(id, "name", "description", TaskStatus.IN_PROGRESS,
                epicId, DEFAULT_TIME, 10);
        response = postRequest(URI_SUBTASK_OPERATIONS, gson.toJson(updatedSubtask));

        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_ACCEPTABLE, response.statusCode());
    }

    // delete task

    @Test
    public void deleteAllTasksCorrectTest() throws IOException, InterruptedException {
        postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, "name1", "description1", TaskStatus.IN_PROGRESS.toString(),
                        null, null, null));
        postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, "name2", "description2", TaskStatus.DONE.toString(),
                        null, null, null));

        HttpResponse<String> response = deleteRequest(URI_TASK_OPERATIONS);
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        response = getRequest(URI_TASK_OPERATIONS);
        assertArrayEquals(new Task[0], gson.fromJson(response.body(), Task[].class));
    }

    @Test
    public void deleteTaskCorrectTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, "name1", "description1", TaskStatus.IN_PROGRESS.toString(),
                        null, null, null));
        Task task1 = gson.fromJson(response.body(), Task.class);
        response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, "name2", "description2", TaskStatus.DONE.toString(),
                        null, null, null));
        Task task2 = gson.fromJson(response.body(), Task.class);

        response = deleteRequest(URI_TASK_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, task1.getId()));
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        response = getRequest(URI_TASK_OPERATIONS);
        assertArrayEquals(new Task[] {task2}, gson.fromJson(response.body(), Task[].class));
    }

    @Test
    public void deleteAllTasksIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = deleteRequest(URI_TASK_OPERATIONS + "1");
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void deleteAllTasksCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = deleteRequest(URI_TASK_OPERATIONS + "/");
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

    @Test
    public void deleteTaskIncorrectIdTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, "", "", TaskStatus.NEW.toString(),
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(0), null));
        int id = gson.fromJson(response.body(), Task.class).getId();

        response = deleteRequest(URI_TASK_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, id + 1));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_ACCEPTABLE, response.statusCode());
    }

    // delete epic

    @Test
    public void deleteAllEpicsCorrectTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "name1", "description1", null,
                        null, null, null));
        Epic epic1 = gson.fromJson(response.body(), Epic.class);
        postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "name2", "description2", null,
                        null, null, null));

        postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, String.valueOf(epic1.getId())));

        response = deleteRequest(URI_EPIC_OPERATIONS);
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        response = getRequest(URI_EPIC_OPERATIONS);
        assertArrayEquals(new Epic[0], gson.fromJson(response.body(), Epic[].class));
        response = getRequest(URI_SUBTASK_OPERATIONS);
        assertArrayEquals(new Subtask[0], gson.fromJson(response.body(), Subtask[].class));
    }

    @Test
    public void deleteEpicCorrectTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "name1", "description1", null,
                        null, null, null));
        Epic epic1 = gson.fromJson(response.body(), Epic.class);
        response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "name2", "description2", null,
                        null, null, null));
        Epic epic2 = gson.fromJson(response.body(), Epic.class);

        postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, String.valueOf(epic1.getId())));

        response = deleteRequest(URI_EPIC_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, epic1.getId()));
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        response = getRequest(URI_EPIC_OPERATIONS);
        assertArrayEquals(new Epic[] {epic2}, gson.fromJson(response.body(), Epic[].class));
        response = getRequest(URI_SUBTASK_OPERATIONS);
        assertArrayEquals(new Subtask[0], gson.fromJson(response.body(), Subtask[].class));
    }

    @Test
    public void deleteAllEpicsIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = deleteRequest(URI_EPIC_OPERATIONS + "1");
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void deleteAllEpicsCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = deleteRequest(URI_EPIC_OPERATIONS + "/");
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

    @Test
    public void deleteEpicIncorrectIdTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "name1", "description1", null,
                        null, null, null));
        int id = gson.fromJson(response.body(), Epic.class).getId();

        response = deleteRequest(URI_EPIC_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, id + 1));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_ACCEPTABLE, response.statusCode());
    }

    // delete subtask

    @Test
    public void deleteAllSubtasksCorrectTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, "name1", "description1", TaskStatus.IN_PROGRESS.toString(),
                        null, null, String.valueOf(epicId)));
        postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, "name2", "description2", TaskStatus.DONE.toString(),
                        null, null, String.valueOf(epicId)));

        response = deleteRequest(URI_SUBTASK_OPERATIONS);
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        response = getRequest(URI_SUBTASK_OPERATIONS);
        assertArrayEquals(new Subtask[0], gson.fromJson(response.body(), Subtask[].class));
        response = getRequest(URI_EPIC_SUBTASKS + "?" + String.format(ID_QUERY_FORMAT, epicId));
        assertArrayEquals(new Subtask[0], gson.fromJson(response.body(), Subtask[].class));
    }

    @Test
    public void deleteSubtaskCorrectTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, "name1", "description1", TaskStatus.IN_PROGRESS.toString(),
                        null, null, String.valueOf(epicId)));
        Subtask subtask1 = gson.fromJson(response.body(), Subtask.class);
        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, "name2", "description2", TaskStatus.DONE.toString(),
                        null, null, String.valueOf(epicId)));
        Subtask subtask2 = gson.fromJson(response.body(), Subtask.class);

        response = deleteRequest(URI_SUBTASK_OPERATIONS + "?"
                + String.format(ID_QUERY_FORMAT, subtask1.getId()));
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
        response = getRequest(URI_SUBTASK_OPERATIONS);
        assertArrayEquals(new Subtask[] {subtask2}, gson.fromJson(response.body(), Subtask[].class));
        response = getRequest(URI_EPIC_SUBTASKS + "?" + String.format(ID_QUERY_FORMAT, epicId));
        assertArrayEquals(new Subtask[] {subtask2}, gson.fromJson(response.body(), Subtask[].class));
    }

    @Test
    public void deleteAllSubtasksIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = deleteRequest(URI_SUBTASK_OPERATIONS + "1");
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void deleteAllSubtasksCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = deleteRequest(URI_SUBTASK_OPERATIONS + "/");
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

    @Test
    public void deleteSubtaskIncorrectIdTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, "", "", null,
                        null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();

        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, "", "", TaskStatus.NEW.toString(),
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(0), String.valueOf(epicId)));
        int id = gson.fromJson(response.body(), Subtask.class).getId();

        response = deleteRequest(URI_SUBTASK_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, id + 1));
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_ACCEPTABLE, response.statusCode());
    }

    // get prioritized tasks

    @Test
    public void getPrioritizedTasksCorrectTest() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, "task name1", "task description 1", null,
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10), null));
        Task task1 = gson.fromJson(response.body(), Task.class);
        response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, "task name2", "task description 2", null,
                        DEFAULT_TIME.minusMinutes(60).format(DATE_TIME_FORMATTER), String.valueOf(10), null));
        Task task2 = gson.fromJson(response.body(), Task.class);
        response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, null));
        int epicId = gson.fromJson(response.body(), Epic.class).getId();
        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, "subtask name1", "subtask description 1", null,
                        DEFAULT_TIME.plusMinutes(60).format(DATE_TIME_FORMATTER), String.valueOf(10),
                        String.valueOf(epicId)));
        Subtask subtask1 = gson.fromJson(response.body(), Subtask.class);
        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, "subtask name2", "subtask description 2", null,
                        DEFAULT_TIME.minusMinutes(120).format(DATE_TIME_FORMATTER), String.valueOf(10),
                        String.valueOf(epicId)));
        Subtask subtask2 = gson.fromJson(response.body(), Subtask.class);

        response = getRequest(URI_PRIORITIZED_TASKS);
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(4, jsonArray.size());
        assertEquals(subtask2, gson.fromJson(jsonArray.get(0), Subtask.class));
        assertEquals(task2, gson.fromJson(jsonArray.get(1), Task.class));
        assertEquals(task1, gson.fromJson(jsonArray.get(2), Task.class));
        assertEquals(subtask1, gson.fromJson(jsonArray.get(3), Subtask.class));
    }

    @Test
    public void getPrioritizedTasksIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = getRequest(URI_PRIORITIZED_TASKS + "1");
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void getPrioritizedTasksCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = getRequest(URI_PRIORITIZED_TASKS + "/");
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

    // history

    @Test
    public void getHistoryCorrectTest() throws IOException, InterruptedException {
        postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, "task name1", "task description 1", null,
                        DEFAULT_TIME.format(DATE_TIME_FORMATTER), String.valueOf(10), null));
        HttpResponse<String> response = postRequest(URI_TASK_OPERATIONS,
                createJsonForTask(null, "task name2", "task description 2", null,
                        DEFAULT_TIME.minusMinutes(60).format(DATE_TIME_FORMATTER), String.valueOf(10), null));
        Task task = gson.fromJson(response.body(), Task.class);
        response = postRequest(URI_EPIC_OPERATIONS,
                createJsonForTask(null, null, null, null,
                        null, null, null));
        Epic epic = gson.fromJson(response.body(), Epic.class);
        int epicId = epic.getId();
        postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, "subtask name1", "subtask description 1", null,
                        DEFAULT_TIME.plusMinutes(60).format(DATE_TIME_FORMATTER), String.valueOf(10),
                        String.valueOf(epicId)));
        response = postRequest(URI_SUBTASK_OPERATIONS,
                createJsonForTask(null, "subtask name2", "subtask description 2", null,
                        DEFAULT_TIME.minusMinutes(120).format(DATE_TIME_FORMATTER), String.valueOf(10),
                        String.valueOf(epicId)));
        Subtask subtask = gson.fromJson(response.body(), Subtask.class);

        response = getRequest(URI_EPIC_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, epic.getId()));
        epic = gson.fromJson(response.body(), Epic.class);
        response = getRequest(URI_TASK_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, task.getId()));
        task = gson.fromJson(response.body(), Task.class);
        response = getRequest(URI_SUBTASK_OPERATIONS + "?" + String.format(ID_QUERY_FORMAT, subtask.getId()));
        subtask = gson.fromJson(response.body(), Subtask.class);

        response = getRequest(URI_HISTORY);
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(3, jsonArray.size());
        assertEquals(epic, gson.fromJson(jsonArray.get(0), Epic.class));
        assertEquals(task, gson.fromJson(jsonArray.get(1), Task.class));
        assertEquals(subtask, gson.fromJson(jsonArray.get(2), Subtask.class));
    }

    @Test
    public void getHistoryIncorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = getRequest(URI_HISTORY + "1");
        assertEquals(HttpTaskServer.RESPONSE_CODE_NOT_FOUND, response.statusCode());
    }

    @Test
    public void getHistoryCorrectPathTest() throws IOException, InterruptedException {
        HttpResponse<String> response = getRequest(URI_HISTORY + "/");
        assertEquals(HttpTaskServer.RESPONSE_CODE_OK, response.statusCode());
    }

}
