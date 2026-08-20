plugins {
    java
    jacoco
    `jvm-test-suite`
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.24.0"
    id("org.sonarqube") version "7.4.0.8496"
}

group = "com.inditex"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// Versiones deterministas: cualquier cambio de dependencia pasa por el lockfile.
dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Boot 4 modulariza las auto-configs: flyway-core solo no activa las migraciones.
    implementation("org.springframework.boot:spring-boot-flyway")
    implementation("org.flywaydb:flyway-core")
    // Visor del contrato estático y anotaciones OpenAPI de las interfaces generadas.
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")
    implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:4.0.4")

    compileOnly("org.projectlombok:lombok")
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.projectlombok:lombok")
    // El binding ordena los processors: Lombok genera antes de que MapStruct lea.
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    runtimeOnly("com.h2database:h2")
}

// Interfaces y modelos generados desde el contrato; viven en build/ y no se commitean.
openApiGenerate {
    generatorName = "spring"
    inputSpec = layout.projectDirectory.file("docs/openapi.yaml")
    outputDir = layout.buildDirectory.dir("generated/openapi")
    apiPackage = "com.inditex.prices.infrastructure.adapter.in.rest.api"
    modelPackage = "com.inditex.prices.infrastructure.adapter.in.rest.api.model"
    configOptions = mapOf(
        "interfaceOnly" to "true",
        "useSpringBoot4" to "true",
        "useJspecify" to "true",
        "openApiNullable" to "false",
        "useTags" to "true",
        "useBeanValidation" to "true",
        "dateLibrary" to "java8",
        "skipDefaultInterface" to "true"
    )
    // El enunciado no define zona horaria: las fechas del API son LocalDateTime.
    typeMappings = mapOf("DateTime" to "LocalDateTime")
    importMappings = mapOf("LocalDateTime" to "java.time.LocalDateTime")
}

openApiValidate {
    inputSpec = layout.projectDirectory.file("docs/openapi.yaml")
}

sourceSets["main"].java.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/java"))

tasks.compileJava {
    dependsOn(tasks.openApiGenerate)
}

tasks.named("check") {
    dependsOn(tasks.openApiValidate)
}

// Copia el contrato a static/ para servirlo en runtime sin duplicar fuente.
tasks.processResources {
    from(layout.projectDirectory.file("docs/openapi.yaml")) {
        into("static")
    }
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            dependencies {
                implementation("org.springframework.boot:spring-boot-starter-test")
                // Boot 4 modulariza los test slices: @DataJpaTest y @AutoConfigureTestDatabase
                // viven en módulos -test propios, ya no en el starter.
                implementation("org.springframework.boot:spring-boot-data-jpa-test")
                implementation("org.springframework.boot:spring-boot-jdbc-test")
                implementation("org.springframework.boot:spring-boot-testcontainers")
                implementation("com.tngtech.archunit:archunit-junit5:1.4.2")
                implementation("org.testcontainers:testcontainers-junit-jupiter")
                implementation("org.testcontainers:testcontainers-postgresql")
                runtimeOnly("org.postgresql:postgresql")
                runtimeOnly("org.flywaydb:flyway-database-postgresql")
            }
        }

        // Suites BDD con source sets propios: Cucumber y Karate usan ambos ficheros
        // .feature y el engine de Cucumber no debe descubrir los de Karate.
        register<JvmTestSuite>("cucumberTest") {
            dependencies {
                implementation(project())
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("io.cucumber:cucumber-java:7.34.7")
                implementation("io.cucumber:cucumber-spring:7.34.7")
                implementation("io.cucumber:cucumber-junit-platform-engine:7.34.7")
                implementation("org.junit.platform:junit-platform-suite")
                // Las suites no heredan las deps implementation del proyecto.
                implementation("tools.jackson.core:jackson-databind")
            }
        }

        register<JvmTestSuite>("karateTest") {
            dependencies {
                implementation(project())
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("io.karatelabs:karate-core:2.1.2")
                implementation("io.karatelabs:karate-junit6:2.1.2")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
}

// Gate de cobertura a nivel de build: fino por paquete, funciona en local y sin
// depender de servicios externos. El gate de plataforma lo pone SonarCloud.
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            element = "PACKAGE"
            includes = listOf("com.inditex.prices.domain*", "com.inditex.prices.application*")
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

sonar {
    properties {
        property("sonar.projectKey", "TrApY_prices-api")
        property("sonar.organization", "trapy")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        // El análisis espera el resultado del quality gate y falla si no pasa.
        property("sonar.qualitygate.wait", "true")
    }
}

tasks.named("check") {
    dependsOn(tasks.named("cucumberTest"), tasks.named("karateTest"))
}
