plugins {
    id("java")
    id("org.springframework.boot") version "2.5.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
//    kotlin("jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.google.protobuf") version "0.8.18"
    id("org.jetbrains.kotlin.jvm") version "1.6.21"

    application

}

application {
    mainClassName = "ru.org.mando.DiskSpaceMonitorBot"
}



group = "org.example"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    jcenter()

//    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.telegram:telegrambots-spring-boot-starter:5.1.1")
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.jcraft:jsch:0.1.55")
    implementation("com.google.protobuf:protobuf-java-util:3.19.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xskip-metadata-version-check")
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

springBoot {
    mainClass.set("ru.org.mando")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClassName
    }
    from(sourceSets.main.get().output.classesDirs.toList())
}