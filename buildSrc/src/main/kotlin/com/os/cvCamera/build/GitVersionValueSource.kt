package com.os.cvCamera.build

import javax.inject.Inject
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream

abstract class GitVersionValueSource : ValueSource<String, ValueSourceParameters.None> {
    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): String {
        val output = ByteArrayOutputStream()
        return try {
            execOperations.exec {
                commandLine("git", "rev-parse", "--verify", "--short", "HEAD")
                standardOutput = output
            }
            String(output.toByteArray()).trim()
        } catch (e: Exception) {
            "unknown"
        }
    }
}
