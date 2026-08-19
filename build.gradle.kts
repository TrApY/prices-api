plugins {
    java
    `jvm-test-suite`
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.24.0"
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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // Visor del contrato estático y anotaciones OpenAPI de las interfaces generadas.
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")
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
            }
        }

        // Suites BDD con source sets propios: Cucumber y Karate usan ambos ficheros
        // .feature y el engine de Cucumber no debe descubrir los de Karate.
        register<JvmTestSuite>("cucumberTest") {
            dependencies {
                implementation(project())
                implementation("org.springframework.boot:spring-boot-starter-test")
            }
        }

        register<JvmTestSuite>("karateTest") {
            dependencies {
                implementation(project())
                implementation("org.springframework.boot:spring-boot-starter-test")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Las suites BDD se rellenan en su propia tarea; hasta entonces no deben romper check.
listOf("cucumberTest", "karateTest").forEach { suite ->
    tasks.named<Test>(suite) {
        failOnNoDiscoveredTests = false
    }
}

tasks.named("check") {
    dependsOn(tasks.named("cucumberTest"), tasks.named("karateTest"))
}
