plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

architectury {
    common("forge", "fabric")
    platformSetupLoomIde()
}

dependencies {
    minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modCompileOnly("com.cobblemon:mod:${property("cobblemon_version")}")
    // alL fabric dependencies:
    modCompileOnly("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    modImplementation("dev.architectury:architectury:${property("architectury_version")}")
    modImplementation("ca.landonjw.gooeylibs:api:${property("gooeylibs_version")}")
    modImplementation("com.cobblemon:mod:${property("cobblemon_version")}")

    // PlaceholderAPI
    modImplementation("eu.pb4:placeholder-api:${property("placeholder_api_version_fabricandforge")}")
    modImplementation("me.clip:placeholderapi:${property("placeholder_api_version_spigot")}")

    // Cobbleuitls
    modImplementation(files("libs/CobbleUtils-common-1.0.7.jar"))

    // Lombok
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    implementation("org.projectlombok:lombok:1.18.20")

    // Database
    api("org.mongodb:mongodb-driver-reactivestreams:5.1.2")
    modImplementation("org.slf4j:slf4j-api:2.0.0")
    modImplementation("org.slf4j:slf4j-simple:2.0.0")
    // Kyori
    modImplementation("net.kyori:examination-api:1.3.0")
    modImplementation("net.kyori:examination-string:1.3.0")
    modImplementation("net.kyori:adventure-api:4.14.0")
    modImplementation("net.kyori:adventure-key:4.14.0")
    modImplementation("net.kyori:adventure-nbt:4.14.0")
    modImplementation("net.kyori:adventure-text-serializer-plain:4.14.0")
    modImplementation("net.kyori:adventure-text-serializer-legacy:4.14.0")
    modImplementation("net.kyori:adventure-text-serializer-gson:4.14.0")
    modImplementation("net.kyori:adventure-text-serializer-json:4.14.0")
    modImplementation("net.kyori:adventure-text-minimessage:4.14.0")
    modImplementation("net.kyori:adventure-text-logger-slf4j:4.14.0")
}
