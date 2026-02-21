plugins {
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.google.cloud.tools.jib")
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
    }
    to {
        image = "anon-community-api"
        tags = setOf("latest")
    }
    container {
        jvmFlags = listOf("-Xms256m", "-Xmx512m", "-XX:+UseG1GC")
        ports = listOf("8080")
        creationTime.set("USE_CURRENT_TIMESTAMP")
    }
}

dependencies {
    implementation(project(":app:usecase"))
    implementation(project(":infra:rdb"))
    implementation(project(":infra:redis"))
    implementation(project(":infra:opensearch"))
    implementation(project(":infra:kafka"))

    implementation(Dependency.SPRING_BOOT_STARTER_WEB)
    implementation(Dependency.SPRING_BOOT_STARTER_DATA_JPA)
    implementation(Dependency.SPRING_BOOT_STARTER_VALIDATION)
    implementation(Dependency.SPRING_BOOT_STARTER_ACTUATOR)
    implementation(Dependency.MICROMETER_PROMETHEUS)
    implementation(Dependency.SPRINGDOC_OPENAPI)
    implementation(Dependency.SPRING_KAFKA)
    implementation(Dependency.FLYWAY_CORE)
    implementation(Dependency.FLYWAY_MYSQL)
    implementation(Dependency.JACKSON_MODULE_KOTLIN)
    implementation(Dependency.JACKSON_DATATYPE_JSR310)
    implementation(Dependency.KOTLIN_REFLECT)

    implementation(Dependency.JJWT_API)
    implementation(Dependency.SPRING_SECURITY_CRYPTO)
    runtimeOnly(Dependency.JJWT_IMPL)
    runtimeOnly(Dependency.JJWT_JACKSON)

    runtimeOnly(Dependency.MYSQL_CONNECTOR)

    testImplementation(Dependency.SPRING_BOOT_STARTER_TEST)
    testImplementation(Dependency.KOTEST_RUNNER_JUNIT5)
    testImplementation(Dependency.KOTEST_ASSERTIONS_CORE)
    testImplementation(Dependency.KOTEST_EXTENSIONS_SPRING)
    testImplementation(Dependency.MOCKK)
}
