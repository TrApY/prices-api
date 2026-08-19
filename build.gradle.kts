plugins {
    java
    `jvm-test-suite`
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
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
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
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
