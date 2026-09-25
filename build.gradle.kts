// Modified for Defender on 2026-09-25; see release/SOURCE_CHANGES.md.
/**
 *          GrimAC Build Configuration
 *
 * Build Flags:
 * -PshadePE=true   - Enables 'lite' mode
 * -Prelocate=false - Adds 'no_relocate' modifier
 * -Prelease=true   - Removes commit/modifiers for release build
 *
 * Logic in: buildSrc/versioning/BuildConfig.kt & VersionUtil.kt
 */

import versioning.BuildConfig
import versioning.VersionUtil

BuildConfig.init(project)

val baseVersion = "0.1.0-alpha.2"
group = "ac.grim.grimac"
version = VersionUtil.computeVersion(project, baseVersion)
description = "Defender: unofficial GrimAC fork with experimental client intelligence and evidence review."

ext["timestamp"] = System.currentTimeMillis().toString()
ext["git_branch"] = VersionUtil.getGitBranch(project, true)
ext["git_commit"] = VersionUtil.getGitCommitHash(project, true)
ext["git_org"] = System.getenv("GRIM_GIT_ORG") ?: "TheSocialNetwork35"
ext["git_repo"] = System.getenv("GRIM_GIT_REPO") ?: "Defender"

println("Build configuration:")
println("    shadePE            = ${BuildConfig.shadePE}")
println("    relocate           = ${BuildConfig.relocate}")
println("    mavenLocalOverride = ${BuildConfig.mavenLocalOverride}")
println("    release            = ${BuildConfig.release}")
println("    version            = $version")

tasks.register("printVersion") {
    group = "versioning"
    description = "Prints the computed project version"
    doLast {
        println("VERSION=$version")
    }
}

// ---------- Java Compile Optimization ----------
subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.isFork = true
        options.isIncremental = true
    }
}
