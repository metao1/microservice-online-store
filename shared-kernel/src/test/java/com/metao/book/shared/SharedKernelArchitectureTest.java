package com.metao.book.shared;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class SharedKernelArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages("com.metao.book.shared");

    @Test
    void sharedDomainDoesNotDependOnFrameworkOrTransportTypes() {
        noClasses().that().resideInAnyPackage("com.metao.book.shared.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "jakarta.validation..",
                "org.apache.kafka..",
                "com.fasterxml.jackson..",
                "com.google.protobuf.."
            )
            .check(classes);
    }
}
