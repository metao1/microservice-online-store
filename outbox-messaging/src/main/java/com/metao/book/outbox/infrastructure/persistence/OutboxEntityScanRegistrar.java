package com.metao.book.outbox.infrastructure.persistence;

import java.util.ArrayList;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.persistence.autoconfigure.EntityScanPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

final class OutboxEntityScanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(
        AnnotationMetadata importingClassMetadata,
        BeanDefinitionRegistry registry
    ) {
        var packages = new ArrayList<String>();
        if (registry instanceof BeanFactory beanFactory && AutoConfigurationPackages.has(beanFactory)) {
            packages.addAll(AutoConfigurationPackages.get(beanFactory));
        }
        packages.add(JpaOutboxEntity.class.getPackageName());
        EntityScanPackages.register(registry, packages);
    }
}
