plugins {
    val jvmVersion = libs.versions.fabric.kotlin.get()
        .split("+kotlin.")[1]
        .split("+")[0]

    kotlin("jvm").version(jvmVersion)
    kotlin("plugin.serialization").version(jvmVersion)
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.mod.publish)
    `maven-publish`
    java
}

repositories {
    mavenLocal()
    maven("https://maven.parchmentmc.org/")
    maven("https://maven.supersanta.me/snapshots")
    maven("https://api.modrinth.com/maven")
    mavenCentral()
}


val modVersion = "1.0.0"
val releaseVersion = "${modVersion}+${libs.versions.minecraft.get()}"
version = releaseVersion
group = "me.senseiwells"

dependencies {
    minecraft(libs.minecraft)
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${libs.versions.parchment.get()}@zip")
    })

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.kotlin)

    include(modImplementation(libs.arcade.commands.get())!!)

    include(modApi(libs.arcade.npcs.get())!!)
    include(modImplementation(libs.arcade.event.registry.get())!!)
    include(modImplementation(libs.arcade.events.server.get())!!)
    include(modImplementation(libs.arcade.scheduler.get())!!)
    include(modImplementation(libs.arcade.utils.get())!!)
}

loom {
    runs {
        getByName("server") {
            runDir = "run/server"
        }

        getByName("client") {
            runDir = "run/client"
        }
    }
}

java {
    withSourcesJar()
}

tasks {
    processResources {
        inputs.property("version", modVersion)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to modVersion))
        }
    }


    publishMods {
        file = remapJar.get().archiveFile
        changelog.set(
            """
                
            """.trimIndent()
        )
        type = STABLE
        modLoaders.add("fabric")

        displayName = "PuppetPlayers $modVersion for ${libs.versions.minecraft.get()}"
        version = releaseVersion

        modrinth {
            accessToken = providers.environmentVariable("MODRINTH_API_KEY")
            projectId = ""
            minecraftVersions.add(libs.versions.minecraft)
        }
    }
}

private fun DependencyHandler.includeModImplementation(provider: Provider<*>, action: Action<ExternalModuleDependency>) {
    include(provider, action)
    modImplementation(provider, action)
}