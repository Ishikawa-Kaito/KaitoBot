import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "zeshawn"
version = "0.1-DEV"

repositories {
    mavenCentral()
}

dependencies {
    val miraiVersion = "2.9.0-M1"
    val hutoolVersion = "5.7.17"
    val jacksonVersion = "2.13.1"
    api("net.mamoe", "mirai-core-api", miraiVersion)     // 编译代码使用
    runtimeOnly("net.mamoe", "mirai-core", miraiVersion) // 运行时使用

    implementation ("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("org.reflections:reflections:0.10.2")



    implementation ("cn.hutool:hutool-http:$hutoolVersion")
    implementation ("cn.hutool:hutool-crypto:$hutoolVersion")

    implementation("net.mamoe.yamlkt:yamlkt:0.10.2")


    implementation ("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation ("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation ("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation ("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-jackson:2.9.0")


    implementation ("org.apache.commons:commons-lang3:3.12.0")
    implementation ("org.apache.commons:commons-io:1.3.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "12"
}

application {
    mainClass.set("cn.zeshawn.kaitobot.KaitoApp")
}