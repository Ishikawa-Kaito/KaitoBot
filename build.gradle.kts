import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
    id("com.github.johnrengelman.shadow") version "7.1.1"
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


    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.github.xuwei-k:html2image:0.1.0")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("org.reflections:reflections:0.10.2")



    implementation("cn.hutool:hutool-http:$hutoolVersion")
    implementation("cn.hutool:hutool-crypto:$hutoolVersion")

    implementation("net.mamoe.yamlkt:yamlkt:0.10.2")


    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")


    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-io:1.3.2")

    implementation("com.squareup:gifencoder:0.10.1")

    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("com.j256.ormlite:ormlite-core:6.1")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")

    implementation("com.kennycason:kumo-core:1.28")
    implementation("com.kennycason:kumo-tokenizers:1.28")

    implementation("com.huaban:jieba-analysis:1.0.2")

    // 汉字转拼音
    implementation("com.belerweb:pinyin4j:2.5.1")

    implementation("com.microsoft.onnxruntime:onnxruntime:1.11.0")

    implementation("org.bytedeco:javacv:1.5.7")
    implementation("org.bytedeco:opencv:4.5.5-1.5.7:windows-x86_64")
    implementation("org.bytedeco:openblas:0.3.19-1.5.7:windows-x86_64")
    implementation("org.bytedeco:javacpp:1.5.7:windows-x86_64")
    runtimeOnly("org.bytedeco:opencv:4.5.5-1.5.7:linux-x86_64")
    runtimeOnly("org.bytedeco:openblas:0.3.19-1.5.7:linux-x86_64")
    runtimeOnly("org.bytedeco:javacpp:1.5.7:linux-x86_64")

    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-api:0.4.0-alpha-3")
    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-onnx:0.4.0-alpha-3")
    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-dataset:0.4.0-alpha-3")



    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")


}

tasks.shadowJar{
    exclude("*windows-x86_64*")
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
