// Modified for Defender on 2026-09-25; see release/SOURCE_CHANGES.md.
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission
import versioning.BuildConfig
import java.util.zip.ZipFile
import java.security.MessageDigest

plugins {
    `maven-publish`
    grim.`base-conventions`
    grim.`shadow-conventions`
    id("de.eldoria.plugin-yml.bukkit") version "0.8.0"
    id("xyz.jpenilla.run-paper") version "3.0.0-beta.1"
}

repositories {
    val localOverride = if (BuildConfig.mavenLocalOverride) mavenLocal() else null

    // Exclusive Repositories (One HTTP request per dep)
    exclusive("https://repo.papermc.io/repository/maven-public/", { name = "papermc" }) {
        includeGroup("io.papermc.paper")
        includeGroup("net.md-5")
    }

    exclusive("https://libraries.minecraft.net", { mavenContent { releasesOnly() } }) {
        includeModule("com.mojang", "brigadier")
    }

    exclusive("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        includeGroup("me.clip")
    }

    val grimPublicReleases = maven("https://maven.grim.ac/public/releases") {
        mavenContent { releasesOnly() }
    }
    val grimPublicSnapshots = maven("https://maven.grim.ac/public/snapshots") {
        mavenContent { snapshotsOnly() }
    }
    val grimLegacySnapshots = maven("https://repo.grim.ac/snapshots")
    exclusiveContent {
        forRepositories(*listOfNotNull(localOverride, grimPublicReleases, grimPublicSnapshots, grimLegacySnapshots).toTypedArray())
        filter {
            includeGroup("ac.grim.grimac")
        }
    }

    exclusive("https://repo.codemc.io/repository/maven-snapshots/") {
        includeGroup("com.github.retrooper")
    }

    exclusive("https://nexus.scarsz.me/content/repositories/releases", { mavenContent { releasesOnly() } }) {
        includeGroup("github.scarsz")
    }

    mavenCentral()
}

val configuredLiteSharedProviderJar = providers.gradleProperty("grim.liteSharedProviderJar")
    .orElse(providers.gradleProperty("liteSharedProviderJar"))

// PE's published Spigot artifact is thin. Its runtime jar bundles these modules,
// with text serializers relocated into PE's own namespace.
val liteSharedLibraries by configurations.creating {
    isCanBeConsumed = false
    isTransitive = false
}
val liteCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
if (!BuildConfig.shadePE) {
    val adventureVersion = liteCatalog.findVersion("adventure").get().requiredVersion
    val examinationVersion = liteCatalog.findVersion("examination").get().requiredVersion
    for (module in listOf("adventure-api", "adventure-key", "adventure-nbt")) {
        dependencies.add(liteSharedLibraries.name, "net.kyori:$module:$adventureVersion")
    }
    for (module in listOf("examination-api", "examination-string")) {
        dependencies.add(liteSharedLibraries.name, "net.kyori:$module:$examinationVersion")
    }
}

val liteSharedPrefixes = listOf(
    "net/kyori/adventure/",
    "net/kyori/examination/",
    "net/kyori/option/",
)

var cachedLiteSharedProviderClassEntries: Set<String>? = null
var cachedLiteSharedProviderFiles: List<File>? = null

fun liteSharedProviderFiles(): List<File> {
    cachedLiteSharedProviderFiles?.let { return it }

    val configured = configuredLiteSharedProviderJar.orNull
        ?.split(File.pathSeparator)
        ?.filter { it.isNotBlank() }
        ?.map { file(it) }
        ?.takeIf { it.isNotEmpty() }

    val files = configured ?: liteSharedLibraries.resolve().toList()

    cachedLiteSharedProviderFiles = files
    return files
}

fun liteSharedProviderClassEntries(): Set<String> {
    cachedLiteSharedProviderClassEntries?.let { return it }

    val entries = liteSharedProviderFiles().flatMap { jar ->
        ZipFile(jar).use { zip ->
            zip.entries().asSequence()
                .map { it.name }
                .filter { name ->
                    name.endsWith(".class") && liteSharedPrefixes.any(name::startsWith) &&
                        !name.startsWith("net/kyori/adventure/text/serializer/")
                }
                .toList()
        }
    }.toSet()

    cachedLiteSharedProviderClassEntries = entries
    return entries
}


dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.luckperms)

    if (BuildConfig.shadePE) {
        implementation(libs.packetevents.spigot)
    } else {
        compileOnly(libs.packetevents.spigot)
    }
    implementation(libs.cloud.paper)
    implementation(libs.adventure.platform.bukkit)
    implementation(libs.grim.bukkit.internal)

    implementation(project(":common"))
    shadow(project(":common"))
}

bukkit {
    name = "Defender"
    authors = listOf("GrimAC contributors", "Yannis Ress Lasser")
    main = "ac.grim.grimac.platform.bukkit.GrimACBukkitLoaderPlugin"
    website = "https://github.com/TheSocialNetwork35/Defender"
    // Defender alpha.2 targets modern servers; older versions are not validated.
    apiVersion = "1.21.11"
    // Region-threaded lifecycle/gameplay has not been validated.
    foliaSupported = false

    if (!BuildConfig.shadePE) {
        depend = listOf("packetevents")
    }

    softDepend = listOf(
        "ProtocolLib",
        "ProtocolSupport",
        "Essentials",
        "ViaVersion",
        "ViaBackwards",
        "ViaRewind",
        "Geyser-Spigot",
        "floodgate",
        "FastLogin",
        "PlaceholderAPI",
        "LuckPerms",
        // Driver holder mods — softdepend so each backend's driver class
        // resolves through the linked classloader.
        "sqlite-jdbc",
        "mysql-jdbc",
        "postgresql-jdbc",
        "mongodb-driver",
        "jedis",
    )

    permissions {
        register("defender.admin") {
            description = "Inspect Defender technical evidence and reload signatures"
            default = Permission.Default.OP
        }
        register("grim.alerts") {
            description = "Receive alerts for violations"
            default = Permission.Default.OP
        }

        register("grim.alerts.enable-on-join") {
            description = "Enable alerts on join"
            default = Permission.Default.OP
        }

        register("grim.performance") {
            description = "Check performance metrics"
            default = Permission.Default.OP
        }

        register("grim.profile") {
            description = "Check user profile"
            default = Permission.Default.OP
        }

        register("grim.brand") {
            description = "Show client brands on join"
            default = Permission.Default.OP
        }

        register("grim.brand.enable-on-join") {
            description = "Enable showing client brands on join"
            default = Permission.Default.OP
        }

        register("grim.sendalert") {
            description = "Send cheater alert"
            default = Permission.Default.OP
        }

        register("grim.nosetback") {
            description = "Disable setback"
            default = Permission.Default.FALSE
        }

        register("grim.nomodifypacket") {
            description = "Disable modifying packets"
            default = Permission.Default.FALSE
        }

        register("grim.disabled") {
            description = "Disable Grim checks while keeping player state tracked"
            default = Permission.Default.FALSE
        }

        register("grim.exempt") {
            description = "Exempt from all checks"
            default = Permission.Default.FALSE
        }

        register("grim.verbose") {
            description = "Receive verbose alerts for violations"
            default = Permission.Default.OP
        }

        register("grim.verbose.enable-on-join") {
            description =
                "Enable verbose alerts on join"
            default = Permission.Default.FALSE
        }

        register("grim.list") {
            description =
                "Shows lists of specific data"
            default = Permission.Default.FALSE
        }

    }
}

publishing.publications.create<MavenPublication>("maven") {
    artifact(tasks["shadowJar"])
}

tasks {
    // 1.8.8 - 1.16.5   = Java 8
    // 1.17             = Java 16
    // 1.18 - 1.20.4    = Java 17
    // 1.20.5 - 1.21.11 = Java 21
    // 26.1+            = Java 25
    val version = "26.3"
    val javaVersion = JavaLanguageVersion.of(25)

    val jvmArgsExternal = listOf(
        "-Dcom.mojang.eula.agree=true",
        "-Dpaper.explicit-flush=true",
        "-DPaper.IgnoreJavaVersion=true"
    )

    runServer {
        minecraftVersion(version)
        runDirectory = projectDir.resolve("run/$version")

        val javaToolchains = project.extensions.getByType<JavaToolchainService>()
        javaLauncher = javaToolchains.launcherFor {
            vendor = JvmVendorSpec.JETBRAINS
            languageVersion = javaVersion
        }

        jvmArgs = jvmArgsExternal
    }

    shadowJar {
        from(rootProject.file("LICENSE")) { into("META-INF/defender") }
        from(rootProject.file("release/ATTRIBUTION.md")) { into("META-INF/defender") }
        from(rootProject.file("release/third-party-notices")) { into("META-INF/defender/third-party-notices") }
        exclude("META-INF/services/javax.annotation.processing.Processor")

        if (!BuildConfig.shadePE) {
            inputs.files(provider { liteSharedProviderFiles() }).withPropertyName("liteSharedProviders")
            exclude {
                val path = it.path
                path.endsWith(".class") && path in liteSharedProviderClassEntries()
            }

            doFirst {
                logger.lifecycle(
                    "Excluding ${liteSharedProviderClassEntries().size} shared class entries supplied by PacketEvents: " +
                        liteSharedProviderFiles().joinToString { it.name }
                )
            }
        }

        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }
}

// Defender release audit: archive resolved dependency coordinates and exact binary digests.
tasks.register("defenderDependencyInventory") {
    dependsOn(":common:jar", ":defender-core:jar")
    doLast {
        val output = rootProject.file("release/DEPENDENCIES.tsv")
        output.parentFile.mkdirs()
        val digest = MessageDigest.getInstance("SHA-256")
        output.writeText("coordinate\tfilename\tsha256\n")
        configurations.runtimeClasspath.get().resolvedConfiguration.resolvedArtifacts
            .sortedBy { it.moduleVersion.id.toString() }.forEach { artifact ->
                val hash = digest.digest(artifact.file.readBytes()).joinToString("") { "%02x".format(it) }
                output.appendText("${artifact.moduleVersion.id}\t${artifact.file.name}\t$hash\n")
            }
    }
}

// Collect corresponding dependency sources for an auditable local release bundle.
tasks.register("defenderDependencySources") {
    doLast {
        val output = rootProject.file("release/dependency-sources")
        output.mkdirs()
        val components = configurations.runtimeClasspath.get().incoming.resolutionResult.allComponents
            .map { it.id }.filterIsInstance<org.gradle.api.artifacts.component.ModuleComponentIdentifier>()
        val result = dependencies.createArtifactResolutionQuery().forComponents(components)
            .withArtifacts(org.gradle.jvm.JvmLibrary::class.java, org.gradle.language.base.artifact.SourcesArtifact::class.java)
            .execute()
        val report = rootProject.file("release/DEPENDENCY_SOURCES.txt")
        report.writeText("Resolved source artifacts (missing sources require manual audit):\n")
        result.resolvedComponents.forEach { component ->
            component.getArtifacts(org.gradle.language.base.artifact.SourcesArtifact::class.java).forEach { artifact ->
                if (artifact is org.gradle.api.artifacts.result.ResolvedArtifactResult) {
                    val id = component.id.displayName.replace(':', '-')
                    artifact.file.copyTo(output.resolve("$id-sources.jar"), overwrite = true)
                    report.appendText("OK ${component.id}\n")
                } else report.appendText("MISSING ${component.id}: $artifact\n")
            }
        }
        result.components.filter { it !is org.gradle.api.artifacts.result.ComponentArtifactsResult }.forEach { report.appendText("UNRESOLVED ${it.id}\n") }
    }
}
