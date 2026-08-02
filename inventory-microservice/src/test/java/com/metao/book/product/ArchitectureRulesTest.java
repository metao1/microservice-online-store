package com.metao.book.product;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.metao.book.shared.architecture.ApplicationUseCase;
import com.metao.book.shared.architecture.InboundAdapter;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages("com.metao.book.product");

    @Test
    void coreDomainDoesNotDependOnFrameworksOrAdapters() {
        noClasses().that().resideInAnyPackage(
                "..domain.model.aggregate..",
                "..domain.model.entity..",
                "..domain.model.event..",
                "..domain.exception.."
            )
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "org.apache.kafka..",
                "com.fasterxml.jackson..",
                "..presentation..",
                "..infrastructure.."
            )
            .check(classes);
    }

    @Test
    void inboundAdaptersDoNotDependOnProductDomainService() {
        noClasses().that().haveFullyQualifiedName(
                "com.metao.book.product.presentation.ProductController"
            )
            .should().dependOnClassesThat().haveFullyQualifiedName(
                "com.metao.book.product.application.service.ProductDomainService"
            )
            .check(classes);
        noClasses().that().haveFullyQualifiedName(
                "com.metao.book.product.infrastructure.factory.handler.ProductGenerator"
            )
            .should().dependOnClassesThat().haveFullyQualifiedName(
                "com.metao.book.product.application.service.ProductDomainService"
            )
            .check(classes);
    }

    @Test
    void messagingInboundAdaptersInjectOnlyAnnotatedUseCases() {
        classes.stream()
            .filter(type -> type.isAnnotatedWith(InboundAdapter.class))
            .filter(type -> type.getAnnotationOfType(InboundAdapter.class).value() == InboundAdapter.Kind.MESSAGING)
            .flatMap(type -> type.getFields().stream())
            .filter(field -> field.getRawType().getFullName().contains(".application."))
            .forEach(this::assertUseCasePort);
    }

    private void assertUseCasePort(JavaField field) {
        assertTrue(field.getRawType().isAnnotatedWith(ApplicationUseCase.class),
            () -> "%s must inject an @ApplicationUseCase, but %s is not annotated"
                .formatted(field.getOwner().getFullName(), field.getRawType().getFullName()));
    }

    @Test
    void jpaTypesRemainInInfrastructurePersistence() {
        classes().that().areAnnotatedWith(Entity.class)
            .should().resideInAnyPackage("..infrastructure.persistence..")
            .check(classes);
    }
}
