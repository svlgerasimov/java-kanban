package ru.yandex.practicum.kanban.util.kvstorage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private final String serverUrl;
    private String apiToken;
    private final HttpClient httpClient;

    public KVTaskClient(String serverUrl) {
        this.serverUrl = serverUrl;
        httpClient = HttpClient.newHttpClient();
        authenticate();
    }

    private void authenticate() {
        URI registerUri = URI.create(serverUrl + "/register");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(registerUri)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            if (responseCode!= 200) {
                throw new ClientBadResponseException("Failed to authenticate. Response code " + responseCode);
            }
            apiToken = response.body();
        } catch (IOException | InterruptedException e) {
            throw new ClientSendException("Failed to send authentication request");
        }
    }

    public void put(String key, String json) {
        URI saveURI = URI.create(String.format("%s/save/%s?API_TOKEN=%s", serverUrl, key, apiToken));
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(saveURI)
                .POST(bodyPublisher)
                .build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            int responseCode = response.statusCode();
            if (responseCode != 200) {
                throw new ClientBadResponseException("Failed to put record. Response code " + responseCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new ClientSendException("Failed to send put request");
        }
    }

    public String load(String key) {
        URI loadURI = URI.create(String.format("%s/load/%s?API_TOKEN=%s", serverUrl, key, apiToken));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(loadURI)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            if (responseCode != 200) {
                throw new ClientBadResponseException("Failed to load value. Response code: " + responseCode);
            }
            return response.statusCode() == 200 ? response.body() : null;
        } catch (IOException | InterruptedException e) {
            throw new ClientSendException("Failed to send load request");
        }
    }
}
