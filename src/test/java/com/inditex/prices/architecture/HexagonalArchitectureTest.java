package com.inditex.prices.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

/**
 * La arquitectura como test: si una dependencia viola la dirección de las capas,
 * la suite falla — el diseño deja de ser una promesa del README.
 */
@AnalyzeClasses(packages = "com.inditex.prices", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule hexagonalLayers = onionArchitecture()
            .domainModels("..domain..")
            .applicationServices("..application..")
            .adapter("rest", "..infrastructure.adapter.in.rest..")
            .adapter("persistence", "..infrastructure.adapter.out.persistence..")
            // El composition root (wiring) es capa externa: puede conocerlo todo;
            // la regla garantiza que nadie depende de él.
            .adapter("configuration", "..infrastructure.config..")
            .withOptionalLayers(true);

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..",
                    "tools.jackson..", "com.fasterxml..", "org.mapstruct..");

    @ArchTest
    static final ArchRule applicationIsFrameworkFree = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..",
                    "tools.jackson..", "com.fasterxml..", "org.mapstruct..");
}
