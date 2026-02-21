dependencies {
    api(project(":contract"))

    testImplementation(Dependency.KOTEST_RUNNER_JUNIT5)
    testImplementation(Dependency.KOTEST_ASSERTIONS_CORE)
}
