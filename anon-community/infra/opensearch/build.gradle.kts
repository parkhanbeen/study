plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    api(project(":domain:post"))

    implementation(Dependency.OPENSEARCH_JAVA)
    implementation(Dependency.OPENSEARCH_REST_HIGH_LEVEL)
    implementation(Dependency.JACKSON_MODULE_KOTLIN)
    implementation(Dependency.JACKSON_DATATYPE_JSR310)
    implementation(Dependency.SPRING_BOOT_STARTER_WEB)

    testImplementation(Dependency.SPRING_BOOT_STARTER_TEST)
    testImplementation(Dependency.KOTEST_RUNNER_JUNIT5)
    testImplementation(Dependency.KOTEST_ASSERTIONS_CORE)
    testImplementation(Dependency.MOCKK)
    testImplementation(Dependency.TESTCONTAINERS)
}
