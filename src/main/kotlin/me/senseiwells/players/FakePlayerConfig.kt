package me.senseiwells.players

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import net.fabricmc.loader.api.FabricLoader
import org.apache.commons.lang3.SerializationException
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Serializable
@OptIn(ExperimentalSerializationApi::class)
class FakePlayerConfig(
    @SerialName("reload_fake_players")
    val reloadFakePlayers: Boolean = true,
    @SerialName("use_mine_tools_api")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val useMineToolsApi: Boolean = false
) {
    companion object {
        private val path: Path = FabricLoader.getInstance().configDir.resolve("fake-player-config.json")
        private val json = Json {
            encodeDefaults = true
            prettyPrint = true
            prettyPrintIndent = "  "
        }

        fun read(): FakePlayerConfig {
            if (!this.path.exists()) {
                return FakePlayerConfig().also { this.write(it) }
            }
            return try {
                this.path.inputStream().use {
                    json.decodeFromStream(it)
                }
            } catch (e: Exception) {
                FakePlayers.logger.error("Failed to read replay config, generating default", e)
                FakePlayerConfig().also { this.write(it) }
            }
        }

        fun write(config: FakePlayerConfig) {
            try {
                this.path.parent.createDirectories()
                this.path.outputStream().use {
                    json.encodeToStream(config, it)
                }
            } catch (e: IOException) {
                FakePlayers.logger.error("Failed to write replay config", e)
            } catch (e: SerializationException) {
                FakePlayers.logger.error("Failed to serialize replay config", e)
            }
        }
    }
}