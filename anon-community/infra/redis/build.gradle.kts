plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    api(project(":contract"))
    implementation(project(":domain:member"))

    implementation(Dependency.SPRING_BOOT_STARTER_DATA_REDIS)
    implementation(Dependency.JACKSON_MODULE_KOTLIN)
    implementation(Dependency.JACKSON_DATATYPE_JSR310)
}
