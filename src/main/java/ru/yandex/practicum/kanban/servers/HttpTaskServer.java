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
import java.util.List;
import java.util.function.*;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final int RESPONSE_CODE_OK = 200;
    private static final int RESPONSE_CODE_BAD_REQUEST = 400;
    private static final int RESPONSE_CODE_NOT_FOUND = 404;
    private static final int RESPONSE_CODE_METHOD_NOT_ALLOWED = 405;
    private static final int RESPONSE_CODE_NOT_ACCEPTABLE = 406;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final Gson gson;

    private final HttpServer httpServer;
    private TaskManager taskManager;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter jsonWriter, LocalDateTime time) throws IOException {
                        jsonWriter.value(time == null ? null : time.format(dateTimeFormatter));
                    }

                    @Override
                    public LocalDateTime read(JsonReader jsonReader) throws IOException {
                        return LocalDateTime.parse(jsonReader.nextString(), dateTimeFormatter);
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
            httpTaskServer = new HttpTaskServer(Path.of("src","main", "resources", "taskManager.csv"));
            httpTaskServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpTaskServer(Path filePath) throws IOException {
//        Path filePath = Path.of("resources", "taskManager.csv");
        try {
            taskManager = FileBackedTaskManager.loadFromFile(filePath);
        } catch (ManagerLoadException e) {
            taskManager = Managers.getFileBacked(filePath);
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(PORT);
        httpServer = HttpServer.create();
        httpServer.bind(inetSocketAddress, 0);

        httpServer.createContext("/tasks", this::handleRequest);
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
        if (!"tasks".equals(pathParts[1])) {    // Проверка, что отвечаем на "/tasks/*", а не "/tasks-что-то-еще/*"
//            exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
//            return;
            response = new Response(RESPONSE_CODE_BAD_REQUEST, null);
        } else if (pathParts.length == 2) {    // /tasks/
            System.out.println("Handling /tasks/");
            //handlePrioritizedTasks(exchange);
            response = handleSimpleGetRequest(exchange, taskManager::getPrioritizedTasks);
//            return;
        } else if (pathParts.length == 3) {    // /tasks/*/
            switch (pathParts[2]) {
                case "task":    // /tasks/task/
                    System.out.println("Handling /tasks/task");
                    response = handleTasks(exchange, Task.class,
                            taskManager::getTask, taskManager::getTasks,
                            taskManager::addTask, taskManager::updateTask,
                            taskManager::removeTask, taskManager::clearTasks);
                    break;
                case "epic":    // /tasks/epic/
                    System.out.println("Handling /tasks/epic");
                    response = handleTasks(exchange, Epic.class,
                            taskManager::getEpic, taskManager::getEpics,
                            taskManager::addEpic, taskManager::updateEpic,
                            taskManager::removeEpic, taskManager::clearEpics);
                    break;
                case "subtask":    // /tasks/subtask/
                    System.out.println("Handling /tasks/subtask");
                    response = handleTasks(exchange, Subtask.class,
                            taskManager::getSubtask, taskManager::getSubtasks,
                            taskManager::addSubtask, taskManager::updateSubtask,
                            taskManager::removeSubtask, taskManager::clearSubtasks);
                    break;
                case "history":    // /tasks/history/
                    System.out.println("Handling /tasks/history");
                    //handleHistory(exchange);
                    response = handleSimpleGetRequest(exchange, taskManager::getHistory);
                    break;
                default:
//                    exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
                    response = new Response(RESPONSE_CODE_BAD_REQUEST, null);
            }
//            return;
        } else if (pathParts.length == 4 && "subtask".equals(pathParts[2]) && "epic".equals(pathParts[3])) {
            // /tasks/subtask/epic/
            System.out.println("Handling /tasks/subtask/epic");
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
            exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
            return new Response(RESPONSE_CODE_BAD_REQUEST, null);
        }
        String response = gson.toJson(taskManager.getEpicsSubtasks(id));
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
            exchange.sendResponseHeaders(code, body == null ? -1 : 0);
            if (body != null) {
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body.getBytes(DEFAULT_CHARSET));
                }
            }
        }
    }
}
