package ru.yandex.practicum.kanban.util;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class KVServerTest {

    private final static int REQUEST_TIMEOUT_SECONDS = 1;

    private static final String URI_REGISTER = "http://localhost:" + KVServer.PORT + "/register";
    private static final String URI_SAVE = "http://localhost:" + KVServer.PORT + "/save/%s?API_TOKEN=%s";
    private static final String URI_LOAD = "http://localhost:" + KVServer.PORT + "/load/%s?API_TOKEN=%s";

    private KVServer kvServer;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newHttpClient();
    }

    @BeforeEach
    void setUp() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
    }

    @AfterEach
    void tearDown() {
        kvServer.stop(0);
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

    @Test
    void sendTextTest() throws IOException, InterruptedException {
        HttpResponse<String> response = getRequest(URI_REGISTER);
        String token = response.body();

        String key1 = "key1";
        String value1 = "value1";
        String key2 = "key2";
        String value2 = "value2";
        String key3 = "key3";
        postRequest(String.format(URI_SAVE, key1, token), value1);
        postRequest(String.format(URI_SAVE, key2, token), value2);

        assertEquals(value1, getRequest(String.format(URI_LOAD, key1, token)).body());
        assertEquals(value2, getRequest(String.format(URI_LOAD, key2, token)).body());
        assertEquals(404, getRequest(String.format(URI_LOAD, key3, token)).statusCode());
    }
}