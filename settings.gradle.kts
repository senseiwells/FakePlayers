rootProject.name = "FakePlayers`    "

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven2.bai.lol")
        mavenCentral()
        gradlePluginPortal()
    }
}