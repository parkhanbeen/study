package com.helloboot.parkhanbeen.config;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class MyAutoConfigImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] {
            "com.helloboot.parkhanbeen.config.autoconfig.DispatcherServletConfig",
            "com.helloboot.parkhanbeen.config.autoconfig.TomcatWebserverConfig"
        };
    }
}
