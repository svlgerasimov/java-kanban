package ru.yandex.practicum.kanban.servers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.kanban.managers.Managers;
import ru.yandex.practicum.kanban.managers.TaskManager;
import ru.yandex.practicum.kanban.util.json.GsonBuilders;
import ru.yandex.practicum.kanban.tasks.Epic;
import ru.yandex.practicum.kanban.tasks.Subtask;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.util.kvstorage.KVServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.*;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

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
    private final TaskManager taskManager;

    static {
        gson = GsonBuilders.getBuilderSeparateTaskTypes().create();
    }

    // Для проверки api внешними средствами
    public static void main(String[] args) throws IOException {
        KVServer kvServer = new KVServer();
        kvServer.start();
        HttpTaskServer httpTaskServer = new HttpTaskServer(Managers.getDefault());
        httpTaskServer.start();
    }

    // Http серверу ведь не должно быть важно, с какой реализацией менеджера задач он работает.
    // Поэтому создание менеджера разумнее вынести за пределы класса.
    // А getDefault из ТЗ - будет в main )
    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;

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
        try {
            URI uri = exchange.getRequestURI();
            String[] pathParts = uri.getPath().split("/");

            Response response;
            if (!ENDPOINT_BASE.equals(pathParts[1])) { // Проверка, что отвечаем на "/tasks/*", а не "/tasks-что-то-еще/*"
                response = new Response(RESPONSE_CODE_NOT_FOUND, null);
            } else if (pathParts.length == 2) {    // /tasks/
                System.out.printf("Handling /%s/%n", ENDPOINT_BASE);
                response = handleSimpleGetRequest(exchange, taskManager::getPrioritizedTasks);
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
                        response = handleSimpleGetRequest(exchange, taskManager::getHistory);
                        break;
                    default:
                        response = new Response(RESPONSE_CODE_NOT_FOUND, null);
                }
            } else if (pathParts.length == 4 && ENDPOINT_SUBTASK_OPERATIONS.equals(pathParts[2])
                    && ENDPOINT_EPIC_SUBTASKS.equals(pathParts[3])) {
                // /tasks/subtask/epic/
                System.out.printf("Handling /%s/%s/%s%n",
                        ENDPOINT_BASE, ENDPOINT_SUBTASK_OPERATIONS, ENDPOINT_EPIC_SUBTASKS);
                response = handleEpicSubtasks(exchange);
            } else {
                response = new Response(RESPONSE_CODE_BAD_REQUEST, null);
            }
            response.send(exchange);
        } finally {
            exchange.close();
        }
    }

    private static Response handleSimpleGetRequest(HttpExchange exchange, Supplier<Object> supplier) {
        String requestMethod = exchange.getRequestMethod();
        if (!"GET".equals(requestMethod)) {
            return new Response(RESPONSE_CODE_METHOD_NOT_ALLOWED, null);
        }
        String response = gson.toJson(supplier.get());
        return new Response(RESPONSE_CODE_OK, response);
    }

    private Response handleEpicSubtasks(HttpExchange exchange) {
        String requestMethod = exchange.getRequestMethod();
        if (!"GET".equals(requestMethod)) {
            return new Response(RESPONSE_CODE_METHOD_NOT_ALLOWED, null);
        }
        Integer id = getIdFromRequestUri(exchange.getRequestURI());
        if (id == null) {
            return new Response(RESPONSE_CODE_BAD_REQUEST, null);
        }
        List<Subtask> subtasks = taskManager.getEpicsSubtasks(id);
        if (subtasks == null) {
            return new Response(RESPONSE_CODE_NOT_FOUND, null);
        }
        String response = gson.toJson(subtasks);
        return new Response(RESPONSE_CODE_OK, response);
    }

    private static  <T extends Task> Response handleTasks(HttpExchange exchange, Class<T> taskClass,
                                              IntFunction<T> taskGetter, Supplier<List<T>> allTasksGetter,
                                              UnaryOperator<T> taskAdder, Predicate<T> taskUpdater,
                                              IntPredicate taskRemover, Action taskClearer) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String requestQuery = exchange.getRequestURI().getQuery();
        switch (requestMethod) {
            case "GET":
                if (requestQuery == null) { // Нет строки запроса
                    return new Response(RESPONSE_CODE_OK, gson.toJson(allTasksGetter.get()));
                } else {
                    Integer id = getIdFromRequestQuery(requestQuery);
                    if (id == null) {
                        return new Response(RESPONSE_CODE_BAD_REQUEST, null);
                    }
                    T task = taskGetter.apply(id);
                    if (task == null) {
                        return new Response(RESPONSE_CODE_NOT_FOUND, null);
                    }
                    return new Response(RESPONSE_CODE_OK, gson.toJson(task));
                }
            case "POST":
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);

                // Чтобы различать запросы на добавление и обновление задач, будем считать,
                // что при добавлении клиент не указывает id задачи
                JsonElement jsonElement = JsonParser.parseString(requestBody);
                if (!jsonElement.isJsonObject()) {
                    return new Response(RESPONSE_CODE_BAD_REQUEST, null);
                }
                T task = gson.fromJson(requestBody, taskClass);
                if (task == null) { // корректная десериализация не удалась
                    return new Response(RESPONSE_CODE_BAD_REQUEST, null);
                }

                if (jsonElement.getAsJsonObject().get("id") == null) {
                    // добавление новой задачи
                    task = taskAdder.apply(task);
                    if (task == null) {
                        return new Response(RESPONSE_CODE_NOT_ACCEPTABLE, null);
                    }
                    return new Response(RESPONSE_CODE_OK, gson.toJson(task));
                } else {
                    // обновление задачи
                    return new Response(
                            taskUpdater.test(task) ? RESPONSE_CODE_OK : RESPONSE_CODE_NOT_ACCEPTABLE,
                            null);
                }
            case "DELETE":
                if (requestQuery == null) { // Нет строки запроса
                    taskClearer.run();
                    return new Response(RESPONSE_CODE_OK, null);
                } else {
                    Integer id = getIdFromRequestQuery(requestQuery);
                    if (id == null) {
                        return new Response(RESPONSE_CODE_BAD_REQUEST, null);
                    }
                    return new Response(
                            taskRemover.test(id) ? RESPONSE_CODE_OK : RESPONSE_CODE_NOT_ACCEPTABLE,
                            null);
                }
            default:
                return new Response(RESPONSE_CODE_METHOD_NOT_ALLOWED, null);
        }
    }

    private static Integer getIdFromRequestQuery(String requestQuery) {
        // На случай если будет несколько аргументов
        Optional<String> id = Arrays.stream(requestQuery.split("&"))
                .map(s -> s.split("="))
                .filter(words -> words.length == 2 && "id".equals(words[0]))
                .map(words -> words[1])
                .findFirst();
        if (id.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(id.get());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer getIdFromRequestUri(URI uri) {
        String requestQuery = uri.getQuery();
        return requestQuery == null ? null : getIdFromRequestQuery(requestQuery);
    }

    @FunctionalInterface
    private interface Action {
        void run();
    }

    private static class Response {
        private final int code;
        private final String body;

        public Response(int code, String body) {
            this.code = code;
            this.body = body;
        }

        public void send(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(code, 0);
            if (body != null) {
                exchange.getResponseBody().write(body.getBytes(DEFAULT_CHARSET));
            }
        }
    }
}
