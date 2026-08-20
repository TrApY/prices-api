package com.inditex.prices.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Los 5 tests pedidos por el enunciado, contra el endpoint REST real con la H2
 * inicializada por Flyway, más los casos negativos del contrato.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FindApplicablePriceApiTest {

    private static final String PRICES_PATH = "/api/v1/prices";
    private static final long PRODUCT_35455 = 35455;
    private static final long BRAND_ZARA = 1;

    @LocalServerPort
    private int port;

    private final HttpClient http = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    @DisplayName("Test 1: petición a las 10:00 del día 14 → tarifa base 35.50")
    void test1_at10OnDay14_appliesBaseTariff() throws Exception {
        assertApplicablePrice("2020-06-14T10:00:00",
                1, "35.50", "2020-06-14T00:00:00", "2020-12-31T23:59:59");
    }

    @Test
    @DisplayName("Test 2: petición a las 16:00 del día 14 → gana la promo de prioridad 1 (25.45)")
    void test2_at16OnDay14_appliesThePriorityPromo() throws Exception {
        assertApplicablePrice("2020-06-14T16:00:00",
                2, "25.45", "2020-06-14T15:00:00", "2020-06-14T18:30:00");
    }

    @Test
    @DisplayName("Test 3: petición a las 21:00 del día 14 → la promo expiró a las 18:30, vuelve la base")
    void test3_at21OnDay14_fallsBackToBaseAfterPromoExpires() throws Exception {
        assertApplicablePrice("2020-06-14T21:00:00",
                1, "35.50", "2020-06-14T00:00:00", "2020-12-31T23:59:59");
    }

    @Test
    @DisplayName("Test 4: petición a las 10:00 del día 15 → tarifa matinal 30.50")
    void test4_at10OnDay15_appliesTheMorningTariff() throws Exception {
        assertApplicablePrice("2020-06-15T10:00:00",
                3, "30.50", "2020-06-15T00:00:00", "2020-06-15T11:00:00");
    }

    @Test
    @DisplayName("Test 5: petición a las 21:00 del día 16 → tarifa de tarde 38.95")
    void test5_at21OnDay16_appliesTheEveningTariff() throws Exception {
        assertApplicablePrice("2020-06-16T21:00:00",
                4, "38.95", "2020-06-15T16:00:00", "2020-12-31T23:59:59");
    }

    @Test
    void answersNotFoundAsProblemJsonWhenNoTariffApplies() throws Exception {
        HttpResponse<String> response =
                get("applicationDate=2019-01-01T00:00:00&productId=35455&brandId=1");

        JsonNode problem = assertProblemJson(response, 404);
        assertTrue(problem.get("detail").asString().contains("35455"));
    }

    @Test
    void answersBadRequestAsProblemJsonOnMalformedDate() throws Exception {
        assertProblemJson(get("applicationDate=no-es-una-fecha&productId=35455&brandId=1"), 400);
    }

    @Test
    void answersBadRequestWhenARequiredParameterIsMissing() throws Exception {
        assertProblemJson(get("productId=35455&brandId=1"), 400);
    }

    @Test
    void answersBadRequestOnNonPositiveIdentifiers() throws Exception {
        assertProblemJson(get("applicationDate=2020-06-14T10:00:00&productId=0&brandId=1"), 400);
    }

    private void assertApplicablePrice(
            String applicationDate, int expectedPriceList, String expectedPrice,
            String expectedStart, String expectedEnd) throws Exception {
        HttpResponse<String> response = get(
                "applicationDate=" + applicationDate + "&productId=" + PRODUCT_35455 + "&brandId=" + BRAND_ZARA);

        assertEquals(200, response.statusCode());
        JsonNode body = json.readTree(response.body());
        assertEquals(PRODUCT_35455, body.get("productId").asLong());
        assertEquals(BRAND_ZARA, body.get("brandId").asLong());
        assertEquals(expectedPriceList, body.get("priceList").asInt());
        assertEquals(0, new BigDecimal(expectedPrice).compareTo(body.get("price").decimalValue()));
        assertEquals(expectedStart, body.get("startDate").asString());
        assertEquals(expectedEnd, body.get("endDate").asString());
        assertEquals("EUR", body.get("currency").asString());
    }

    private JsonNode assertProblemJson(HttpResponse<String> response, int expectedStatus) {
        assertEquals(expectedStatus, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("")
                .contains("application/problem+json"));
        JsonNode problem = json.readTree(response.body());
        assertEquals(expectedStatus, problem.get("status").asInt());
        assertFalse(problem.get("title").asString().isBlank());
        return problem;
    }

    private HttpResponse<String> get(String queryString) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + PRICES_PATH + "?" + queryString))
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
