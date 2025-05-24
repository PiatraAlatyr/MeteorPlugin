plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "ru.elementcraft"
version = "1.0"

bukkit {
    name = "ElementMeteor"
    version = "1.0"
    main = "ru.elementcraft.elementmeteor.ElementMeteor"
    apiVersion = "1.16"
    author = "PiatraAlatyr"
    description = "Тестовое задание для ElementCraft"
    website = "https://github.com/PiatraAlatyr"
    depend = listOf("ProjectKorra")
    softDepend = listOf("LiteCommands")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")                                 //ProjectKorra
    maven("https://repo.papermc.io/repository/maven-public/")   //Paper
    maven("https://repo.panda-lang.org/releases")               //LiteCommands
    maven("https://repo.xenondevs.xyz/releases")                //InvUI
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.github.ProjectKorra:ProjectKorra:v1.12.0")

    implementation("dev.rollczi:litecommands-core:3.9.7")
    implementation("dev.rollczi:litecommands-bukkit:3.9.7")
    implementation("xyz.xenondevs.invui:invui:1.45")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
}

tasks.register<Copy>("copyToDevEnv") {
    from("build/libs/MeteorPlugin-1.0.jar")
    into("testServer/plugins/")
}

tasks.named("build") {
    finalizedBy("copyToDevEnv")
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
}

tasks.shadowJar {
    archiveClassifier.set("") // чтоб jar назывался просто ElementMeteor.jar
    // Исключаем лишний мусор
    exclude("org/jetbrains/annotations/**")
    exclude("org/intellij/lang/annotations/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
    exclude("META-INF/annotations.kotlin_module")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}