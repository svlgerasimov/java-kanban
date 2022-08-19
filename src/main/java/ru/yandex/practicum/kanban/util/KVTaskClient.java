package ru.yandex.practicum.kanban.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private final String serverUrl;
    private final String apiToken;
    private final HttpClient httpClient;

    public KVTaskClient(String serverUrl) throws IOException, InterruptedException {
        this.serverUrl = serverUrl;
        httpClient = HttpClient.newHttpClient();

        URI registerUri = URI.create(serverUrl + "/register");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(registerUri)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        apiToken = response.body();
    }

    public void put(String key, String json) throws IOException, InterruptedException {
        URI saveURI = URI.create(String.format("%s/save/%s?API_TOKEN=%s", serverUrl, key, apiToken));
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(saveURI)
                .POST(bodyPublisher)
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String load(String key) throws IOException, InterruptedException {
        URI loadURI = URI.create(String.format("%s/load/%s?API_TOKEN=%s", serverUrl, key, apiToken));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(loadURI)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200 ? response.body() : null;
    }
}
