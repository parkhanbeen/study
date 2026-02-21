plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    api(project(":contract"))
    api(project(":domain:post"))
    api(project(":domain:comment"))
    api(project(":domain:like"))
    api(project(":domain:member"))

    implementation(Dependency.SPRING_BOOT_STARTER)
    implementation(Dependency.SPRING_TX)
    implementation(Dependency.SPRING_SECURITY_CRYPTO)

    testImplementation(Dependency.KOTEST_RUNNER_JUNIT5)
    testImplementation(Dependency.KOTEST_ASSERTIONS_CORE)
    testImplementation(Dependency.MOCKK)
}
