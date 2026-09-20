plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
}

tasks.register("projectTests") {
    group = "verification"
    description = "Runs every test suite: server, server end-to-end, and web view tests"
    dependsOn(":server:test", ":server:e2eTest", ":web:jvmTest")
}

allprojects {
    tasks.withType<Test>().configureEach {
        val taskPath = path

        // a requested test run always runs, so every suite reports its totals
        outputs.upToDateWhen { false }

        afterTest(KotlinClosure2<TestDescriptor, TestResult, Unit>({ test, result ->
            if (result.resultType == TestResult.ResultType.FAILURE) {
                logger.quiet("FAILED ${test.className?.substringAfterLast('.')} > ${test.name}")
                result.exceptions.forEach { error ->
                    val location = error.stackTrace.firstOrNull { it.fileName?.endsWith("Test.kt") == true }
                    logger.quiet("    ${error::class.simpleName}: ${error.message}")
                    if (location != null) logger.quiet("    at ${location.fileName}:${location.lineNumber}")
                }
            }
        }))

        afterSuite(KotlinClosure2<TestDescriptor, TestResult, Unit>({ suite, result ->
            if (suite.parent == null) {
                logger.quiet(
                    "$taskPath: ${result.testCount} tests, ${result.successfulTestCount} passed, " +
                        "${result.failedTestCount} failed, ${result.skippedTestCount} skipped"
                )
            }
        }))
    }
}
