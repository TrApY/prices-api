# prices-api

[![CI](https://github.com/TrApY/prices-api/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/TrApY/prices-api/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=TrApY_prices-api&metric=alert_status)](https://sonarcloud.io/project/overview?id=TrApY_prices-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=TrApY_prices-api&metric=coverage)](https://sonarcloud.io/component_measures?id=TrApY_prices-api&metric=coverage)

Servicio REST que resuelve el precio aplicable a un producto de una cadena del grupo
en una fecha dada. Cuando varias tarifas están vigentes en esa fecha, aplica la de
mayor prioridad.

```bash
curl 'http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1'
```

```json
{
  "productId": 35455, "brandId": 1, "priceList": 2,
  "startDate": "2020-06-14T15:00:00", "endDate": "2020-06-14T18:30:00",
  "price": 25.45, "currency": "EUR"
}
```

## Cómo ejecutarlo

**Con Gradle** (el wrapper descarga el JDK 25 vía toolchain si no lo tienes):

```bash
./gradlew bootRun
```

**Con Docker**, sin necesidad de JDK:

```bash
docker build -t prices-api . && docker run -p 8080:8080 prices-api
```

**Sin compilar nada**, desde la imagen publicada por la CI:

```bash
docker run -p 8080:8080 ghcr.io/trapy/prices-api:latest
```

La documentación navegable de la API queda en `http://localhost:8080/swagger-ui.html`.
El contrato vive en [`docs/openapi.yaml`](docs/openapi.yaml) y puede verse renderizado
en [Swagger Editor](https://editor.swagger.io/?url=https://raw.githubusercontent.com/TrApY/prices-api/main/docs/openapi.yaml).

## Los 5 tests del enunciado

```bash
./gradlew test --tests FindApplicablePriceApiTest
```

| Petición (producto 35455, cadena 1) | Tarifa | Precio |
|---|---|---|
| 14-jun 10:00 | 1 | 35.50 € |
| 14-jun 16:00 | 2 | 25.45 € |
| 14-jun 21:00 | 1 | 35.50 € |
| 15-jun 10:00 | 3 | 30.50 € |
| 16-jun 21:00 | 4 | 38.95 € |

Se ejecutan contra el endpoint real con la base H2 inicializada por Flyway, y se
acompañan de casos negativos (404 y 400 en formato `application/problem+json`).

## Decisiones de diseño

**Arquitectura hexagonal en un solo módulo, verificada por test.** El dominio
([`domain`](src/main/java/com/inditex/prices/domain)) no conoce Spring, JPA, Jackson
ni MapStruct; los casos de uso y puertos viven en
[`application`](src/main/java/com/inditex/prices/application) y los adaptadores en
[`infrastructure`](src/main/java/com/inditex/prices/infrastructure). Para este alcance,
separar en módulos de Gradle habría sido ceremonia: las reglas de dependencia las
garantiza [ArchUnit](src/test/java/com/inditex/prices/architecture/HexagonalArchitectureTest.java)
en cada build, incluida la pureza del dominio y que nadie depende del composition root.

**La regla de negocio vive en el dominio y se ve.** El adaptador de persistencia
aporta solo las tarifas vigentes del producto y cadena (consulta acotada con índice
compuesto — nunca un `findAll`), y la elección entre candidatas es una política del
dominio: [`HighestPriorityWins`](src/main/java/com/inditex/prices/domain/model/HighestPriorityWins.java),
con desempate determinista por identificador de tarifa y test unitario sin arrancar
nada. A escala, ese desempate se empujaría a la consulta (`ORDER BY priority DESC
LIMIT 1`); con el volumen del enunciado, la versión en dominio es más expresiva y
igual de correcta.

**API-First de verdad.** [`docs/openapi.yaml`](docs/openapi.yaml) es la fuente de
verdad: de él se generan la interfaz del controller y los modelos en tiempo de build
(no se commitean), el controller la implementa y, si el contrato cambia, el código no
compila hasta cumplirlo. Swagger UI actúa como visor de ese mismo yaml. No hay
anotaciones de documentación escritas a mano: las únicas viven en el código generado
desde el contrato, que no puede divergir de él.

**Java moderno con criterio.** El dominio usa records con invariantes en el
constructor; `Optional` solo como retorno; dinero como `BigDecimal` con su divisa
([`Money`](src/main/java/com/inditex/prices/domain/model/Money.java)). Lombok queda
confinado a infraestructura (entidad JPA y constructores de adaptadores), y los
mappers son interfaces de MapStruct con los value objects resueltos en métodos
`default`. Las fechas son `LocalDateTime`: el enunciado no define zona horaria y el
API la refleja tal cual; con múltiples husos la decisión habría sido `Instant` en UTC.

**Errores como Problem Details (RFC 9457).** Sin tarifa aplicable → 404 con detalle
útil; parámetros inválidos, ausentes o malformados → 400. Todo en
`application/problem+json` desde un
[`@RestControllerAdvice`](src/main/java/com/inditex/prices/infrastructure/adapter/in/rest/ApiExceptionHandler.java)
que extiende el manejo estándar de Spring.

**H2 en runtime, PostgreSQL en los tests del adaptador.** El enunciado pide base en
memoria y se respeta (H2 + Flyway con los datos del ejemplo). La arquitectura permite
además verificar el mismo adaptador y las mismas migraciones contra un PostgreSQL
real con Testcontainers: mismo puerto de salida, dos motores, mismos tests.

**Secretos nunca en claro.** Las propiedades sensibles van cifradas con Jasypt
(`ENC(...)`) y la clave maestra llega por entorno; en CI, un gate de `detect-secrets`
corta el pipeline si aparece un secreto plano. La clave de esta kata lleva un default
deliberado para que el evaluador ejecute sin fricción — en un entorno real no
existiría y viviría en el vault.

## Testing

La pirámide completa, cada capa con su papel:

- **Unitarios de dominio** sin Spring: la regla de prioridad, el desempate y las
  invariantes.
- **Integración sobre el endpoint real**: los 5 casos del enunciado con todos sus
  campos, más los negativos del contrato.
- **ArchUnit**: la arquitectura como test.
- **Testcontainers + PostgreSQL**: el adaptador de persistencia es agnóstico del motor.
- **Cucumber** ([escenarios en español](src/cucumberTest/resources/features/consulta_precios.feature)):
  la regla de negocio como documentación ejecutable; cada build genera un informe HTML
  navegable pensado para perfiles no técnicos (artifact `reports-cucumber` de la CI).
- **Karate**: el contrato técnico HTTP/JSON de punta a punta, incluido el formato de
  los errores.

Cobertura medida con JaCoCo y doble gate: umbral del 80 % por paquete en dominio y
aplicación verificado en el build (`./gradlew check`), y quality gate de plataforma
en [SonarCloud](https://sonarcloud.io/project/overview?id=TrApY_prices-api).

## CI/CD

Pipeline por stages en GitHub Actions:

```
secrets-check → build → test (unit | cucumber | karate) → coverage → sonar
                                  └→ contract → dockerize → security-scan
```

- **secrets-check**: `detect-secrets` contra el baseline; un secreto plano corta todo.
- **test**: las tres suites en paralelo, con sus informes como artifacts.
- **coverage**: gate de JaCoCo y resumen en el job.
- **sonar**: análisis de SonarCloud con la cobertura importada del XML de JaCoCo.
- **contract**: validación del OpenAPI.
- **dockerize**: imagen multi-stage (builder JDK 25, runtime JRE alpine, usuario no
  root, capas cacheables con `jarmode=tools`), publicada en
  [GHCR](https://github.com/TrApY/prices-api/pkgs/container/prices-api) en cada push a
  `develop`/`main`.
- **security-scan**: Trivy sobre la imagen — bloquea por CRITICAL con fix disponible
  (hallazgos visibles en el log) y sube el informe completo como SARIF al Security tab.

## Metodología

El trabajo está organizado como un sprint visible en el propio repo: backlog en
[issues con criterios de aceptación](https://github.com/TrApY/prices-api/milestone/1),
una rama y una pull request por tarea con su pipeline en verde antes del merge, y
Conventional Commits. La historia de commits y PRs cuenta la evolución real del
proyecto, decisiones y correcciones incluidas.

## Asunciones y qué quedó fuera (a propósito)

- **Autenticación/autorización**: fuera de alcance del enunciado; el diseño hexagonal
  la acomodaría como filtro en el adaptador REST sin tocar dominio.
- **Caché de precios**: las tarifas tienen vigencia temporal; una caché exigiría TTL
  coherente con esas ventanas o invalidación por cambio de tarifas. Sin evidencia de
  necesidad, YAGNI.
- **Paginación y listados**: el contrato pide una consulta puntual; no hay colecciones
  que paginar.
- **Versionado del API**: la ruta ya declara `v1`; Spring Framework 7 añade versionado
  nativo de endpoints si hiciera falta evolucionar.
- **Tiempo real / SSE, métricas, tracing**: valor dudoso para el alcance; el pipeline
  y la arquitectura dejan sitio para añadirlos sin reestructurar.
