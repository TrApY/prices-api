package com.inditex.prices.karate;

import io.karatelabs.core.Runner;
import io.karatelabs.core.SuiteResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KarateApiTest {

    @LocalServerPort
    private int port;

    @Test
    void apiEndToEnd() {
        SuiteResult result = Runner.path("classpath:karate")
                .systemProperty("baseUrl", "http://localhost:" + port)
                .parallel(1);
        assertTrue(result.isPassed(), () -> "Features con fallos: " + result.getFailedFeatures());
    }
}
