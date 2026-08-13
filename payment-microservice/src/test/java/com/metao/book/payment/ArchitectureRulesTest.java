package com.metao.book.payment;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.metao.book.shared.architecture.ApplicationUseCase;
import com.metao.book.shared.architecture.ApplicationService;
import com.metao.book.shared.architecture.InboundAdapter;
import com.metao.book.shared.architecture.OutboundAdapter;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages("com.metao.book.payment");

    @Test
    void coreDomainDoesNotDependOnFrameworksOrAdapters() {
        noClasses().that().resideInAnyPackage(
                "..domain.model.aggregate..",
                "..domain.model.event..",
                "..domain.exception..",
                "..domain.service.."
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
    void inboundAdaptersDoNotDependOnPaymentApplicationService() {
        noClasses().that().haveFullyQualifiedName(
                "com.metao.book.payment.presentation.PaymentController"
            )
            .should().dependOnClassesThat().haveFullyQualifiedName(
                "com.metao.book.payment.application.service.PaymentApplicationService"
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

    @Test
    void applicationServicesDeclareTheirArchitectureRole() {
        classes().that().areAnnotatedWith(Service.class)
            .and().resideInAnyPackage("..application..")
            .should().beAnnotatedWith(ApplicationService.class)
            .check(classes);
    }

    @Test
    void repositoryAdaptersDeclareTheirArchitectureRole() {
        classes().that().areAnnotatedWith(Repository.class)
            .and().resideInAnyPackage("..infrastructure..")
            .should().beAnnotatedWith(OutboundAdapter.class)
            .check(classes);
    }
}
