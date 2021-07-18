plugins {
    kotlin("jvm") version "1.5.21"
}

group = "org.feuer.partners.zuxces"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("net.dv8tion:JDA:4.3.0_294")
    implementation("io.github.cdimascio:java-dotenv:5.2.1")
    implementation("com.github.Kosert.FlowBus:FlowBus:1.1")
    implementation("com.github.ajalt:mordant:1.2.1")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.mongodb:mongodb-driver-legacy:4.1.0-beta2")
    implementation("com.google.code.gson:gson:2.8.6")
}
