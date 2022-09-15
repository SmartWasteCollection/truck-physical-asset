import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "swc"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.azure:azure-digitaltwins-core:1.3.1")
    implementation("com.azure:azure-messaging-servicebus:7.10.1")
    implementation("com.azure:azure-identity:1.5.4")
    implementation("com.microsoft.azure.sdk.iot:iot-device-client:1.30.1")
    implementation("com.google.code.gson:gson:2.9.1")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("swc.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
