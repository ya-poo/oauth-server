import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.springframework.boot") version "2.7.3"
    id("io.spring.dependency-management") version "1.0.14.RELEASE"
    id("org.flywaydb.flyway") version "8.4.3"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"

    kotlin("jvm") version "1.7.20"
    kotlin("plugin.spring") version "1.7.20"
}

group = "me.yapoo"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.arrow-kt:arrow-core:1.1.2")
    implementation("com.password4j:password4j:1.6.0")
    implementation("com.auth0:java-jwt:4.1.0")
// 	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
// 	runtimeOnly("org.postgresql:postgresql")
// 	runtimeOnly("org.postgresql:r2dbc-postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<BootRun> {
    jvmArgs = listOf("-Dreactor.netty.http.server.accessLogEnabled=true")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

ktlint {
    disabledRules.set(setOf("no-wildcard-imports"))
}

flyway {
    url = "jdbc:postgresql://localhost:5432/oauth"
    user = "root"
    password = "password"
}
