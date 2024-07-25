rootProject.name = "CobbleUtils"

pluginManagement {
    repositories {
        maven(url = "https://maven.fabricmc.net/")
        maven(url = "https://maven.architectury.dev/")
        maven(url = "https://maven.impactdev.net/repository/development")
        maven(url = "https://repo.maven.apache.org/maven2/")
        maven(url = "https://maven.impactdev.net/repository/development/")
        maven(url = "https://repo.spongepowered.org/maven/")
        maven(url = "https://files.minecraftforge.net/maven/")
        maven(url = "https://papermc.io/repo/repository/maven-public/")
        maven {
            name = "sonatype-oss-snapshots1"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://maven.nucleoid.xyz/")
            name = "Nucleoid"
        }
    }

    listOf(
        "net.kyori:examination-api:1.3.0",
        "net.kyori:examination-string:1.3.0",
        "net.kyori:adventure-api:4.14.0",
        "net.kyori:adventure-key:4.14.0",
        "net.kyori:adventure-nbt:4.14.0",
        "net.kyori:adventure-text-serializer-plain:4.14.0",
        "net.kyori:adventure-text-serializer-legacy:4.14.0",
        "net.kyori:adventure-text-serializer-gson:4.14.0",
        "net.kyori:adventure-text-serializer-json:4.14.0",
        "net.kyori:adventure-text-minimessage:4.14.0",
        "net.kyori:adventure-text-logger-slf4j:4.14.0",
        "net.kyori:event-api:5.0.0-SNAPSHOT",
    ).forEach { include(it) }


}

include("common", "fabric", "forge")