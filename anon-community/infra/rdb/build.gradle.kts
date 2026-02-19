plugins {
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    implementation(project(":domain:post"))
    implementation(project(":domain:comment"))
    implementation(project(":domain:like"))
    implementation(project(":domain:member"))

    implementation(Dependency.SPRING_BOOT_STARTER_DATA_JPA)
    implementation(Dependency.JACKSON_MODULE_KOTLIN)
    runtimeOnly(Dependency.MYSQL_CONNECTOR)

    testImplementation(Dependency.KOTEST_RUNNER_JUNIT5)
    testImplementation(Dependency.KOTEST_ASSERTIONS_CORE)
}
