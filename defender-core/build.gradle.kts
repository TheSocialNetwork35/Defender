plugins { `java-library` }
group = rootProject.group
version = rootProject.version
repositories { mavenCentral() }
java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)); withSourcesJar() }
tasks.withType<JavaCompile>().configureEach { options.release.set(17) }
dependencies {
    implementation("org.yaml:snakeyaml:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.test { useJUnitPlatform() }
