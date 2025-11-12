import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer

val shadowCommon: Configuration by configurations.creating

architectury {
    platformSetupLoomIde()
    fabric()
}

configurations {
    compileClasspath.get().extendsFrom(configurations["shadowCommon"])
    runtimeClasspath.get().extendsFrom(configurations["shadowCommon"])
    getByName("developmentFabric").extendsFrom(configurations["shadowCommon"])
}
loom {
    enableTransitiveAccessWideners.set(true)
    silentMojangMappingsLicense()
}

dependencies {
    modImplementation("com.cobblemon:fabric:${property("cobblemon_version")}")
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    shadowCommon(project(":common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", "transformProductionFabric")) { isTransitive = false }

}
tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
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
}

tasks {
    base.archivesName.set(
        "${project.property("minecraft_version")}/${project.property("mod_version")}/${
            project.property
                ("archives_base_name")
        }-fabric" +
                "-${
                    project.property(
                        "mod_version"
                    )
                }"
    )


    shadowJar {
        exclude("generations/gg/generations/core/generationscore/fabric/datagen/**")
        exclude("data/forge/**")
        exclude("architectury.common.json")
        exclude("com/google/gson/**/*")
        exclude("org/intellij/**/*")
        exclude("org/jetbrains/**/*")
        // Vault
        exclude("org/bukkit/**/*")
        exclude("org/apache/**/*")
        exclude("org/yaml/**/*")
        exclude("org/junit/**/*")
        exclude("org/java_websocket/**/*")
        exclude("org/hamcrest/**/*")
        exclude("com/google/**/*")
        exclude("org/slf4j/**")

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


