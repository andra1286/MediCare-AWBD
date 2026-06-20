package com.medicare.webui.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClientResponseException;

/** Extracts a user-friendly message from a backend error response (the shared ErrorResponse JSON). */
final class ApiErrors {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ApiErrors() {
    }

    static String message(RestClientResponseException ex) {
        try {
            JsonNode node = MAPPER.readTree(ex.getResponseBodyAsString());
            if (node.hasNonNull("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignore) {
            // fall through to status text
        }
        return ex.getStatusText();
    }
}
