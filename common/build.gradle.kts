plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")

}
architectury {
    common("forge", "fabric")
    platformSetupLoomIde()
}

dependencies {

    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")

    modCompileOnly("com.cobblemon:mod:${property("cobblemon_version")}")
    // alL fabric dependencies:
    modCompileOnly("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    modImplementation("dev.architectury:architectury:${property("architectury_version")}")
    modImplementation("ca.landonjw.gooeylibs:api:${property("gooeylibs_version")}")

    // Fabric API

    // Forge API
    api("net.minecraftforge:forge:${property("forge_version")}")

    // PlaceholderAPI
    modImplementation("eu.pb4:placeholder-api:${property("placeholder_api_version_fabricandforge")}")
    modImplementation("me.clip:placeholderapi:${property("placeholder_api_version_spigot")}")

    // Database
    api("org.mongodb:mongodb-driver-sync:${property("mongodb_version")}")

    // Lombok
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    implementation("org.projectlombok:lombok:1.18.20")

    // Permissions
    api("net.luckperms:api:${property("luckperms_version")}")

    // Economy
    // Economy Impactor
    implementation("net.impactdev.impactor.api:economy:${property("impactor_version")}")

    // Economy Blanket
    modImplementation(files("libs/CobbleUtils-common-1.1.1.jar"))

    // Economy Vault
    api("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    api("com.github.MilkBowl:VaultAPI:1.7")

    // Kyori Adventure
    api("net.kyori:adventure-text-serializer-gson:${property("kyori_version")}")
    api("net.kyori:adventure-text-minimessage:${property("kyori_version")}")

    //Discord
    api("club.minnced:discord-webhooks:${property("discord_webhooks_version")}")
}

