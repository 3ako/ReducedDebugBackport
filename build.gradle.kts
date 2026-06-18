plugins {
    id("java")
}

group = "hw.zako"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.viaversion.com/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.12.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    // Только интерфейс ViaAPI для компиляции; рантайм даёт сам плагин Via.
    compileOnly("com.viaversion:viaversion-api:5.9.1") { isTransitive = false }
}

tasks.withType<JavaCompile> {
    // Java 8 байткод — грузится на любом Java, под которым крутится 1.16 сервер.
    options.release.set(8)
    options.encoding = "UTF-8"
}

tasks.processResources {
    filteringCharset = "UTF-8"
    val props = mapOf("version" to version.toString())
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}
