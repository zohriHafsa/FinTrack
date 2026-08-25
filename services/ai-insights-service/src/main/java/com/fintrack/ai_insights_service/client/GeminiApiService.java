package com.fintrack.ai_insights_service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiApiService {

    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;
    private final ObjectMapper objectMapper;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    public GeminiApiService(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.url}") String apiUrl,
            WebClient webClient,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public String generateInsights(String prompt) {
        try {
            log.info("Calling Gemini API");

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            String response = webClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("content-type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(errorBody -> {
                                        log.error("Gemini API error - Status: {}, Body: {}",
                                                clientResponse.statusCode(), errorBody);
                                        return new RuntimeException("Gemini API error: " + errorBody);
                                    })
                    )
                    .bodyToMono(String.class)
                    .timeout(REQUEST_TIMEOUT)
                    .block();

            log.debug("Received response from Gemini API");
            return extractTextFromResponse(response);

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate insights from Gemini API", e);
        }
    }

    private String extractTextFromResponse(String jsonResponse) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, Map.class);

            // Gemini response format:
            // candidates[0].content.parts[0].text
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) responseMap.get("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> firstCandidate = candidates.get(0);
                Map<String, Object> content =
                        (Map<String, Object>) firstCandidate.get("content");

                if (content != null) {
                    List<Map<String, Object>> parts =
                            (List<Map<String, Object>>) content.get("parts");

                    if (parts != null && !parts.isEmpty()) {
                        String text = (String) parts.get(0).get("text");
                        log.info("Successfully extracted text from Gemini response");
                        return text;
                    }
                }
            }

            log.error("No text found in Gemini response: {}", jsonResponse);
            throw new RuntimeException("No text content in Gemini response");

        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Gemini API response", e);
        }
    }
}