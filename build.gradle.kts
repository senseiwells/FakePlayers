plugins {
    val jvmVersion = libs.versions.fabric.kotlin.get()
        .split("+kotlin.")[1]
        .split("+")[0]

    kotlin("jvm").version(jvmVersion)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.mod.publish)
    `maven-publish`
    java
}

repositories {
    mavenLocal()
    maven("https://maven.parchmentmc.org/")
    maven("https://maven.supersanta.me/snapshots")
    mavenCentral()
}


val modVersion = "0.1.0-beta.1"
val releaseVersion = "${modVersion}+mc${libs.versions.minecraft.get()}"
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

    include(modImplementation(libs.arcade.events.get())!!)
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

        displayName = "FakePlayers $modVersion for ${libs.versions.minecraft.get()}"
        version = releaseVersion

        modrinth {
            accessToken = providers.environmentVariable("MODRINTH_API_KEY")
            projectId = ""
            minecraftVersions.add(libs.versions.minecraft)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("MavenJava") {
            groupId = "me.senseiwells"
            artifactId = "fake-players"
            version = "${modVersion}+${libs.versions.minecraft.get()}"
            from(components["java"])

            updateReadme("./README.md")
        }
    }

    repositories {
        val mavenUrl = System.getenv("MAVEN_URL")
        if (mavenUrl != null) {
            maven {
                url = uri(mavenUrl)
                val mavenUsername = System.getenv("MAVEN_USERNAME")
                val mavenPassword = System.getenv("MAVEN_PASSWORD")
                if (mavenUsername != null && mavenPassword != null) {
                    credentials {
                        username = mavenUsername
                        password = mavenPassword
                    }
                }
            }
        }
    }
}

private fun DependencyHandler.includeModImplementation(provider: Provider<*>, action: Action<ExternalModuleDependency>) {
    include(provider, action)
    modImplementation(provider, action)
}

private fun MavenPublication.updateReadme(vararg readmes: String) {
    val location = "${groupId}:${artifactId}"
    val regex = Regex("""${Regex.escape(location)}:[\d\.\-a-zA-Z+]+""")
    val locationWithVersion = "${location}:${version}"
    for (path in readmes) {
        val readme = file(path)
        readme.writeText(readme.readText().replace(regex, locationWithVersion))
    }
}