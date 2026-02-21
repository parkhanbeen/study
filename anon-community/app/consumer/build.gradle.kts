plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.google.cloud.tools.jib")
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
    }
    to {
        image = "anon-community-consumer"
        tags = setOf("latest")
    }
    container {
        jvmFlags = listOf("-Xms256m", "-Xmx512m", "-XX:+UseG1GC")
        ports = listOf("8081")
        creationTime.set("USE_CURRENT_TIMESTAMP")
    }
}

dependencies {
    implementation(project(":app:usecase"))
    implementation(project(":infra:rdb"))
    implementation(project(":infra:opensearch"))
    implementation(project(":infra:kafka"))

    implementation(Dependency.SPRING_BOOT_STARTER)
    implementation(Dependency.SPRING_BOOT_STARTER_DATA_JPA)
    implementation(Dependency.SPRING_BOOT_STARTER_ACTUATOR)
    implementation(Dependency.MICROMETER_PROMETHEUS)
    implementation(Dependency.SPRING_KAFKA)
    implementation(Dependency.FLYWAY_CORE)
    implementation(Dependency.FLYWAY_MYSQL)
    implementation(Dependency.JACKSON_MODULE_KOTLIN)
    implementation(Dependency.JACKSON_DATATYPE_JSR310)
    implementation(Dependency.KOTLIN_REFLECT)
    implementation(Dependency.SPRING_SECURITY_CRYPTO)

    runtimeOnly(Dependency.MYSQL_CONNECTOR)

    testImplementation(Dependency.SPRING_BOOT_STARTER_TEST)
    testImplementation(Dependency.KOTEST_RUNNER_JUNIT5)
    testImplementation(Dependency.KOTEST_ASSERTIONS_CORE)
    testImplementation(Dependency.MOCKK)
}
