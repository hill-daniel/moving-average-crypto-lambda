package org.kiwi.rest;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

public class UnirestClient implements RestClient {

    @Override
    public String getResponseAsJsonFrom(String endpoint) {
        try {
            HttpResponse<JsonNode> response = Unirest.get(endpoint).asJson();
            return extractBodyFrom(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to receive response from endpoint [" + endpoint + "]", e);
        }
    }

    private String extractBodyFrom(HttpResponse<JsonNode> response) {
        checkSuccessOf(response);
        JsonNode body = response.getBody();
        if (body == null) {
            throw new RuntimeException("Request failed. Body is empty");
        }
        return body.toString();
    }

    private void checkSuccessOf(HttpResponse<JsonNode> response) {
        if (response.getStatus() >= 300) {
            throw new RuntimeException("Request Failed. Status code was [" + response.getStatus() + "] "
                    + "with text [" + response.getStatusText() + "]");
        }
    }
}
