package ru.yandex.practicum.kanban.servers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.kanban.managers.Managers;
import ru.yandex.practicum.kanban.managers.TaskManager;
import ru.yandex.practicum.kanban.tasks.Task;
import ru.yandex.practicum.kanban.tasks.TaskStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final int RESPONSE_CODE_OK = 200;
    private static final int RESPONSE_CODE_BAD_REQUEST = 400;
    private static final int RESPONSE_CODE_NOT_FOUND = 404;
    private static final int RESPONSE_CODE_METHOD_NOT_ALLOWED = 405;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final HttpServer httpServer;
    private final TaskManager taskManager;
    private final Gson gson;

    public static void main(String[] args) {
        HttpTaskServer httpTaskServer;
        try {
            httpTaskServer = new HttpTaskServer();
            httpTaskServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpTaskServer() throws IOException {
        Path filePath = Path.of("resources", "taskManager.csv");
        taskManager = Managers.getFileBacked(filePath);

        // Чтобы не передавать время в виде лишних json объектов и чтобы не было illegal reflective access operation
        TypeAdapter<LocalDateTime> localDateTimeTypeAdapter = new TypeAdapter<LocalDateTime>() {
            @Override
            public void write(JsonWriter jsonWriter, LocalDateTime time) throws IOException {
                jsonWriter.value(time.format(dateTimeFormatter));
            }

            @Override
            public LocalDateTime read(JsonReader jsonReader) throws IOException {
                return LocalDateTime.parse(jsonReader.nextString(), dateTimeFormatter);
            }
        };
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, localDateTimeTypeAdapter)
                .create();

        //------НЕ ЗАБЫТЬ УДАЛИТЬ!!!---------
        taskManager.addTask(new Task(0, "first task", "description", TaskStatus.NEW,
                LocalDateTime.now(), 10));
        taskManager.addTask(new Task(0, "second task", "description", TaskStatus.DONE,
                LocalDateTime.now().minusMinutes(60), 10));
        //--------------------------

        InetSocketAddress inetSocketAddress = new InetSocketAddress(PORT);
        httpServer = HttpServer.create();
        httpServer.bind(inetSocketAddress, 0);

        httpServer.createContext("/tasks/task", this::handleTasks);
        httpServer.createContext("/tasks", this::handlePrioritizedTasks);
    }

    public void start() {
        httpServer.start();
        System.out.println("HttpTaskServer started");
    }

    private void handlePrioritizedTasks(HttpExchange exchange) throws IOException {
        System.out.println("handling \"/tasks\"");
        String requestMethod = exchange.getRequestMethod();
        if (!"GET".equals(requestMethod)) {
            exchange.sendResponseHeaders(RESPONSE_CODE_METHOD_NOT_ALLOWED, -1);
            return;
        }
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length != 2 || !"tasks".equals(pathParts[1])) {
            exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
            return;
        }

        exchange.sendResponseHeaders(RESPONSE_CODE_OK, 0);
        String response = gson.toJson(taskManager.getPrioritizedTasks());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(DEFAULT_CHARSET));
        }
    }

    private void handleTasks(HttpExchange exchange) throws IOException {
        System.out.println("handling \"/tasks/task\"");

        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length != 3 || !"tasks".equals(pathParts[1]) || !"task".equals(pathParts[2])) {
            exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
            return;
        }

        String requestMethod = exchange.getRequestMethod();
        String response;
        switch (requestMethod) {
            case "GET":
                String requestQuery = exchange.getRequestURI().getQuery();
                if (requestQuery == null) { // Нет строки запроса
                    response = gson.toJson(taskManager.getTasks());
                    exchange.sendResponseHeaders(RESPONSE_CODE_OK, 0);
                } else {
                    String[] queryParts = requestQuery.split("[&=]");  // Здесь "&" строго говоря незачем,
                                                                             // т.к. параметр пока только один
                    if (!"id".equals(queryParts[0]) || queryParts.length < 2) {
                        exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
                        return;
                    }
                    try {
                        int id = Integer.parseInt(queryParts[1]);
                        Task task = taskManager.getTask(id);
                        if (task == null) {
                            exchange.sendResponseHeaders(RESPONSE_CODE_NOT_FOUND, -1);
                        }
                        response = gson.toJson(task);
                        exchange.sendResponseHeaders(RESPONSE_CODE_OK, 0);
                    } catch (NumberFormatException e) {
                        exchange.sendResponseHeaders(RESPONSE_CODE_BAD_REQUEST, -1);
                        return;
                    }
                }
                break;
            case "POST":
                return;
            //break;
            case "DELETE":
                return;
            //break;
            default:
                exchange.sendResponseHeaders(RESPONSE_CODE_METHOD_NOT_ALLOWED, -1);
                return;
        }
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(DEFAULT_CHARSET));
        }
    }


}
