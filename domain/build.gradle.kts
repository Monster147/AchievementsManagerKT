plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-webmvc:6.2.11")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.2")

    // To use Servlet API
    implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")

    // To get password encode
    api("org.springframework.security:spring-security-core:6.5.5")

    testImplementation("org.springframework:spring-test:6.2.11")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.jdbi:jdbi3-kotlin:3.37.1")
    implementation("org.jdbi:jdbi3-postgres:3.37.1")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.5.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
