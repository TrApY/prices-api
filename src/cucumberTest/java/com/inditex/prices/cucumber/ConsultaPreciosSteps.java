package com.inditex.prices.cucumber;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsultaPreciosSteps {

    @LocalServerPort
    private int port;

    private final HttpClient http = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();

    private HttpResponse<String> response;

    @Cuando("consulto el precio del producto {long} de la cadena {long} a las {string}")
    public void consultoElPrecio(long productId, long brandId, String fecha) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/prices"
                        + "?applicationDate=" + fecha + "&productId=" + productId + "&brandId=" + brandId))
                .GET()
                .build();
        response = http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Entonces("se aplica la tarifa {int} con precio {string} EUR")
    public void seAplicaLaTarifa(int tarifa, String precio) {
        assertEquals(200, response.statusCode());
        JsonNode body = json.readTree(response.body());
        assertEquals(tarifa, body.get("priceList").asInt());
        assertEquals(0, new BigDecimal(precio).compareTo(body.get("price").decimalValue()));
        assertEquals("EUR", body.get("currency").asString());
    }

    @Entonces("la respuesta es un error {int} en formato problem json")
    public void laRespuestaEsUnError(int status) {
        assertEquals(status, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("")
                .contains("application/problem+json"));
        assertEquals(status, json.readTree(response.body()).get("status").asInt());
    }
}
