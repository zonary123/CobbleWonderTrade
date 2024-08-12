import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating

architectury {
    platformSetupLoomIde()
    fabric()
}

configurations {
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentFabric").extendsFrom(configurations["common"])
}

loom {
    enableTransitiveAccessWideners.set(true)
    silentMojangMappingsLicense()
}

dependencies {
    minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    modImplementation("com.cobblemon:fabric:${property("cobblemon_version")}")
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    implementation("org.mongodb:mongodb-driver-reactivestreams:5.1.2")
    implementation("org.reactivestreams:reactive-streams:1.0.4")
    implementation("io.projectreactor:reactor-core:3.6.8")

    shadowCommon("org.mongodb:mongodb-driver-reactivestreams:5.1.2")
    shadowCommon("org.reactivestreams:reactive-streams:1.0.4")
    shadowCommon("io.projectreactor:reactor-core:3.6.8")

    listOf(
        "org.mongodb:mongodb-driver-reactivestreams:5.1.2",
        "org.reactivestreams:reactive-streams:1.0.4",
        "io.projectreactor:reactor-core:3.6.8"
    ).forEach {
        include(it)
    }

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProductionFabric")) { isTransitive = false }
}

tasks.processResources {
    expand(
        mapOf(
            "mod_name" to project.property("mod_name"),
            "mod_id" to project.property("mod_id"),
            "mod_version" to project.property("mod_version"),
            "mod_description" to project.property("mod_description"),
            "author" to project.property("author"),
            "repository" to project.property("repository"),
            "license" to project.property("license"),
            "mod_icon" to project.property("mod_icon"),
            "environment" to project.property("environment"),
            "supported_minecraft_versions" to project.property("supported_minecraft_versions")
        )
    )
}

tasks {
    base.archivesName.set("${project.property("mod_version")}/${project.property("archives_base_name")}-fabric")
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        exclude("generations/gg/generations/core/generationscore/fabric/datagen/**")
        exclude("data/forge/**")
        exclude("com/google/gson/**/*")
        exclude("org/intellij/**/*")
        exclude("org/jetbrains/**/*")

        relocate("reactor.core", "com.kingpixel.wondertrade.reactor")
        relocate("org.reactivestreams", "com.kingpixel.wondertrade.reactivestreams")
        relocate("com.mongodb", "com.kingpixel.wondertrade.mongodb")
        relocate("org.bson", "com.kingpixel.wondertrade.bson")

        transformers.add(ServiceFileTransformer())

        configurations = listOf(project.configurations.getByName("shadowCommon"))
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        injectAccessWidener.set(true)
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }

    jar.get().archiveClassifier.set("dev")
}
