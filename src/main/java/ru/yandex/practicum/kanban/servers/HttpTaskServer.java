package ru.yandex.practicum.kanban.servers;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.kanban.managers.Managers;
import ru.yandex.practicum.kanban.managers.TaskManager;
import ru.yandex.practicum.kanban.managers.filebacked.FileBackedTaskManager;
import ru.yandex.practicum.kanban.managers.filebacked.ManagerLoadException;
import ru.yandex.practicum.kanban.servers.json.adapters.EpicDeserializer;
import ru.yandex.practicum.kanban.servers.json.adapters.SubtaskDeserializer;
import ru.yandex.practicum.kanban.servers.json.adapters.TaskDeserializer;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.*;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static final int RESPONSE_CODE_OK = 200;
    public static final int RESPONSE_CODE_BAD_REQUEST = 400;
    public static final int RESPONSE_CODE_NOT_FOUND = 404;
    public static final int RESPONSE_CODE_METHOD_NOT_ALLOWED = 405;
    public static final int RESPONSE_CODE_NOT_ACCEPTABLE = 406;

    private static final String ENDPOINT_BASE = "tasks";
    private static final String ENDPOINT_TASK_OPERATIONS = "task";
    private static final String ENDPOINT_SUBTASK_OPERATIONS = "subtask";
    private static final String ENDPOINT_EPIC_OPERATIONS = "epic";
    private static final String ENDPOINT_HISTORY = "history";
    private static final String ENDPOINT_EPIC_SUBTASKS = "epic";

    private static final Gson gson;

    private final HttpServer httpServer;
    private TaskManager taskManager;

    static {
        gson = new GsonBuilder()
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
                })
                .registerTypeAdapter(Task.class, new TaskDeserializer<>() {
                    @Override
                    protected Task createInstance() {
                        return new Task(id, name, description, status, startTime, duration);
                    }
                })
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
                .create();
    }

    public static void main(String[] args) {
        HttpTaskServer httpTaskServer;
        try {
            httpTaskServer = new HttpTaskServer(Path.of("src","main", "resources", "taskManager.csv"),
                    true);
            httpTaskServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpTaskServer(Path filePath, boolean restoreTaskManager) throws IOException {
        if (restoreTaskManager) {
            try {
                taskManager = Managers.restoreFileBacked(filePath);
            } catch (ManagerLoadException e) {
                taskManager = Managers.getFileBacked(filePath);
            }
        } else {
            taskManager = Managers.getFileBacked(filePath);
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(PORT);
        httpServer = HttpServer.create();
        httpServer.bind(inetSocketAddress, 0);

        httpServer.createContext("/" + ENDPOINT_BASE, this::handleRequest);
    }

    public void start() {
        httpServer.start();
        System.out.println("HttpTaskServer started");
    }

    public void stop(int delay) {
        httpServer.stop(delay);
        System.out.println("HttpTaskServer stops with " + delay + " seconds delay");
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        Response response;
        if (!ENDPOINT_BASE.equals(pathParts[1])) { // Проверка, что отвечаем на "/tasks/*", а не "/tasks-что-то-еще/*"
//            exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
//            return;
            response = new Response(RESPONSE_CODE_NOT_FOUND, null);
        } else if (pathParts.length == 2) {    // /tasks/
            System.out.printf("Handling /%s/%n", ENDPOINT_BASE);
            //handlePrioritizedTasks(exchange);
            response = handleSimpleGetRequest(exchange, taskManager::getPrioritizedTasks);
//            return;
        } else if (pathParts.length == 3) {    // /tasks/*/
            switch (pathParts[2]) {
                case ENDPOINT_TASK_OPERATIONS:    // /tasks/task/
                    System.out.printf("Handling /%s/%s%n", ENDPOINT_BASE, ENDPOINT_TASK_OPERATIONS);
                    response = handleTasks(exchange, Task.class,
                            taskManager::getTask, taskManager::getTasks,
                            taskManager::addTask, taskManager::updateTask,
                            taskManager::removeTask, taskManager::clearTasks);
                    break;
                case ENDPOINT_EPIC_OPERATIONS:    // /tasks/epic/
                    System.out.printf("Handling /%s/%s%n", ENDPOINT_BASE, ENDPOINT_EPIC_OPERATIONS);
                    response = handleTasks(exchange, Epic.class,
                            taskManager::getEpic, taskManager::getEpics,
                            taskManager::addEpic, taskManager::updateEpic,
                            taskManager::removeEpic, taskManager::clearEpics);
                    break;
                case ENDPOINT_SUBTASK_OPERATIONS:    // /tasks/subtask/
                    System.out.printf("Handling /%s/%s%n", ENDPOINT_BASE, ENDPOINT_SUBTASK_OPERATIONS);
                    response = handleTasks(exchange, Subtask.class,
                            taskManager::getSubtask, taskManager::getSubtasks,
                            taskManager::addSubtask, taskManager::updateSubtask,
                            taskManager::removeSubtask, taskManager::clearSubtasks);
                    break;
                case ENDPOINT_HISTORY:    // /tasks/history/
                    System.out.printf("Handling /%s/%s%n", ENDPOINT_BASE, ENDPOINT_HISTORY);
                    //handleHistory(exchange);
                    response = handleSimpleGetRequest(exchange, taskManager::getHistory);
                    break;
                default:
//                    exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
                    response = new Response(RESPONSE_CODE_NOT_FOUND, null);
            }
//            return;
        } else if (pathParts.length == 4 && ENDPOINT_SUBTASK_OPERATIONS.equals(pathParts[2])
                && ENDPOINT_EPIC_SUBTASKS.equals(pathParts[3])) {
            // /tasks/subtask/epic/
            System.out.printf("Handling /%s/%s/%s%n",
                    ENDPOINT_BASE, ENDPOINT_SUBTASK_OPERATIONS, ENDPOINT_EPIC_SUBTASKS);
            response = handleEpicSubtasks(exchange);
//            return;
        } else {
            response = new Response(RESPONSE_CODE_BAD_REQUEST, null);
        }
//        exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
        response.send(exchange);
    }

    private static Response handleSimpleGetRequest(HttpExchange exchange, Supplier<Object> supplier) {
        String requestMethod = exchange.getRequestMethod();
        if (!"GET".equals(requestMethod)) {
//            exchange.sendResponseHeaders(RESPONSE_CODE_METHOD_NOT_ALLOWED, -1);
            return new Response(RESPONSE_CODE_METHOD_NOT_ALLOWED, null);
        }
        String response = gson.toJson(supplier.get());
//        exchange.sendResponseHeaders(RESPONSE_CODE_OK, 0);
//        try (OutputStream os = exchange.getResponseBody()) {
//            os.write(response.getBytes(DEFAULT_CHARSET));
//        }
        return new Response(RESPONSE_CODE_OK, response);
    }

//    private void handlePrioritizedTasks(HttpExchange exchange) throws IOException {
//        String requestMethod = exchange.getRequestMethod();
//        if (!"GET".equals(requestMethod)) {
//            exchange.sendResponseHeaders(RESPONSE_CODE_METHOD_NOT_ALLOWED, -1);
//            return;
//        }
//        String response = gson.toJson(taskManager.getPrioritizedTasks());
//        exchange.sendResponseHeaders(RESPONSE_CODE_OK, 0);
//        try (OutputStream os = exchange.getResponseBody()) {
//            os.write(response.getBytes(DEFAULT_CHARSET));
//        }
//    }
//
//    private void handleHistory(HttpExchange exchange) throws IOException {
//        String requestMethod = exchange.getRequestMethod();
//        if (!"GET".equals(requestMethod)) {
//            exchange.sendResponseHeaders(RESPONSE_CODE_METHOD_NOT_ALLOWED, -1);
//            return;
//        }
//        String response = gson.toJson(taskManager.getHistory());
//        exchange.sendResponseHeaders(RESPONSE_CODE_OK, 0);
//        try (OutputStream os = exchange.getResponseBody()) {
//            os.write(response.getBytes(DEFAULT_CHARSET));
//        }
//    }

    private Response handleEpicSubtasks(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (!"GET".equals(requestMethod)) {
//            exchange.sendResponseHeaders(RESPONSE_CODE_METHOD_NOT_ALLOWED, -1);
            return new Response(RESPONSE_CODE_METHOD_NOT_ALLOWED, null);
        }
        Integer id = getIdFromRequestUri(exchange.getRequestURI());
        if (id == null) {
//            exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
            return new Response(RESPONSE_CODE_BAD_REQUEST, null);
        }
        List<Subtask> subtasks = taskManager.getEpicsSubtasks(id);
        if (subtasks == null) {
            return new Response(RESPONSE_CODE_NOT_FOUND, null);
        }
        String response = gson.toJson(subtasks);
//        exchange.sendResponseHeaders(RESPONSE_CODE_OK, 0);
//        try (OutputStream os = exchange.getResponseBody()) {
//            os.write(response.getBytes(DEFAULT_CHARSET));
//        }
        return new Response(RESPONSE_CODE_OK, response);
    }

    private static  <T extends Task> Response handleTasks(HttpExchange exchange, Class<T> taskClass,
                                              IntFunction<T> taskGetter, Supplier<List<T>> allTasksGetter,
                                              UnaryOperator<T> taskAdder, Predicate<T> taskUpdater,
                                              IntPredicate taskRemover, Runnable taskClearer) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String requestQuery = exchange.getRequestURI().getQuery();
//        String response;
        switch (requestMethod) {
            case "GET":
                if (requestQuery == null) { // Нет строки запроса
//                    response = gson.toJson(allTasksGetter.get());
//                    exchange.sendResponseHeaders(RESPONSE_CODE_OK, 0);
                    return new Response(RESPONSE_CODE_OK, gson.toJson(allTasksGetter.get()));
                } else {
                    Integer id = getIdFromRequestQuery(requestQuery);
                    if (id == null) {
//                        exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
                        return new Response(RESPONSE_CODE_BAD_REQUEST, null);
                    }
                    T task = taskGetter.apply(id);
                    if (task == null) {
//                        exchange.sendResponseHeaders(RESPONSE_CODE_NOT_FOUND, -1);
                        return new Response(RESPONSE_CODE_NOT_FOUND, null);
                    }
//                    response = gson.toJson(task);
//                    exchange.sendResponseHeaders(RESPONSE_CODE_OK, 0);
                    return new Response(RESPONSE_CODE_OK, gson.toJson(task));
                }
//                break;
            case "POST":
                try (InputStream inputStream = exchange.getRequestBody()) {
//                    byte[] bytes = inputStream.readAllBytes();
                    String requestBody = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

                    // Чтобы различать запросы на добавление и обновление задач, будем считать,
                    // что при добавлении клиент не указывает id задачи
                    JsonElement jsonElement = JsonParser.parseString(requestBody);
                    if (!jsonElement.isJsonObject()) {
//                        exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
                        return new Response(RESPONSE_CODE_BAD_REQUEST, null);
                    }
                    T task = gson.fromJson(requestBody, taskClass);
                    if (task == null) { // корректная десериализация не удалась
//                        exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
                        return new Response(RESPONSE_CODE_BAD_REQUEST, null);
                    }

                    if (jsonElement.getAsJsonObject().get("id") == null) {
                        // добавление новой задачи
                        task = taskAdder.apply(task);
                        if (task == null) {
//                            exchange.sendResponseHeaders(RESPONSE_CODE_NOT_ACCEPTABLE, -1);
                            return new Response(RESPONSE_CODE_NOT_ACCEPTABLE, null);
                        }
//                        response = gson.toJson(task);
//                        exchange.sendResponseHeaders(RESPONSE_CODE_OK, 0);
                        return new Response(RESPONSE_CODE_OK, gson.toJson(task));
                    } else {
                        // обновление задачи
//                        exchange.sendResponseHeaders(
//                                taskUpdater.test(task) ? RESPONSE_CODE_OK : RESPONSE_CODE_NOT_ACCEPTABLE,
//                                -1);
                        return new Response(
                                taskUpdater.test(task) ? RESPONSE_CODE_OK : RESPONSE_CODE_NOT_ACCEPTABLE,
                                null);
                    }
                }
//                break;
            case "DELETE":
                if (requestQuery == null) { // Нет строки запроса
                    taskClearer.run();
//                    exchange.sendResponseHeaders(RESPONSE_CODE_OK, -1);
                    return new Response(RESPONSE_CODE_OK, null);
                } else {
                    Integer id = getIdFromRequestQuery(requestQuery);
                    if (id == null) {
//                        exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
                        return new Response(RESPONSE_CODE_BAD_REQUEST, null);
                    }
//                    exchange.sendResponseHeaders(
//                            taskRemover.test(id) ? RESPONSE_CODE_OK : RESPONSE_CODE_NOT_ACCEPTABLE, -1);
                    return new Response(
                            taskRemover.test(id) ? RESPONSE_CODE_OK : RESPONSE_CODE_NOT_ACCEPTABLE,
                            null);
                }
//                return;
            default:
//                exchange.sendResponseHeaders(RESPONSE_CODE_METHOD_NOT_ALLOWED, -1);
                return new Response(RESPONSE_CODE_METHOD_NOT_ALLOWED, null);
        }
//        try (OutputStream os = exchange.getResponseBody()) {
//            os.write(response.getBytes(DEFAULT_CHARSET));
//        }
    }

    private static Integer getIdFromRequestQuery(String requestQuery) {
        String[] queryParts = requestQuery.split("[&=]");
        if (queryParts.length < 2 || !"id".equals(queryParts[0])) {
            return null;
        }
        try {
            return Integer.valueOf(queryParts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer getIdFromRequestUri(URI uri) {
        String requestQuery = uri.getQuery();
        return requestQuery == null ? null : getIdFromRequestQuery(requestQuery);
    }



    private static class Response {
        private final int code;
        private final String body;

        public Response(int code, String body) {
            this.code = code;
            this.body = body;
        }

        public void send(HttpExchange exchange) throws IOException {
//            exchange.getRequestBody().close();
            exchange.sendResponseHeaders(code, /*body == null ? -1 :*/ 0);
            if (body != null) {
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body.getBytes(DEFAULT_CHARSET));
                }
            }
            exchange.close();
        }
    }
}
