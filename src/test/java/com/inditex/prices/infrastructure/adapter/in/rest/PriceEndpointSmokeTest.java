package com.inditex.prices.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PriceEndpointSmokeTest {

    private static final String PRICES_PATH = "/api/v1/prices";

    @LocalServerPort
    private int port;

    private final HttpClient http = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void returnsTheApplicablePriceWithItsSixFields() throws Exception {
        HttpResponse<String> response =
                get("applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1");

        assertEquals(200, response.statusCode());
        JsonNode body = json.readTree(response.body());
        assertEquals(35455, body.get("productId").asLong());
        assertEquals(1, body.get("brandId").asLong());
        assertEquals(2, body.get("priceList").asInt());
        assertEquals("2020-06-14T15:00:00", body.get("startDate").asString());
        assertEquals("2020-06-14T18:30:00", body.get("endDate").asString());
        assertEquals(25.45, body.get("price").asDouble());
        assertEquals("EUR", body.get("currency").asString());
    }

    @Test
    void answersNotFoundAsProblemJsonWhenNoTariffApplies() throws Exception {
        HttpResponse<String> response =
                get("applicationDate=2019-01-01T00:00:00&productId=35455&brandId=1");

        assertEquals(404, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/problem+json"));
        JsonNode body = json.readTree(response.body());
        assertEquals(404, body.get("status").asInt());
        assertTrue(body.get("detail").asString().contains("35455"));
    }

    @Test
    void answersBadRequestAsProblemJsonOnMalformedDate() throws Exception {
        HttpResponse<String> response =
                get("applicationDate=no-es-una-fecha&productId=35455&brandId=1");

        assertEquals(400, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/problem+json"));
    }

    @Test
    void answersBadRequestWhenARequiredParameterIsMissing() throws Exception {
        HttpResponse<String> response = get("productId=35455&brandId=1");

        assertEquals(400, response.statusCode());
    }

    private HttpResponse<String> get(String queryString) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + PRICES_PATH + "?" + queryString))
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
