package me.senseiwells.players

import me.senseiwells.players.command.FakePlayerCommand
import me.senseiwells.players.utils.FakePlayerRegistries
import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.ServerStoppingEvent
import net.fabricmc.api.ModInitializer
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists

object FakePlayers: ModInitializer {
    const val MOD_ID = "fake-players"

    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    val config = FakePlayerConfig.read()

    override fun onInitialize() {
        FakePlayerRegistries.load()

        GlobalEventHandler.register<ServerRegisterCommandEvent> { event ->
            event.register(FakePlayerCommand)
        }
        GlobalEventHandler.register<ServerLoadedEvent> { (server) ->
            this.loadFakePlayers(server)
        }
        GlobalEventHandler.register<ServerSaveEvent> { (server, stopping) ->
            if (!stopping) {
                this.saveFakePlayers(server)
            }
        }
        GlobalEventHandler.register<ServerStoppingEvent> { (server) ->
            this.saveFakePlayers(server)
        }
    }

    private fun loadFakePlayers(server: MinecraftServer) {
        val path = this.getFakePlayerDat(server)
        if (!this.config.reloadFakePlayers || !path.exists()) {
            return
        }

        try {
            val wrapper = NbtIo.read(path) ?: return
            val players = wrapper.getList("players", Tag.TAG_STRING.toInt())
            for (data in players) {
                FakePlayer.join(server, UUID.fromString(data.asString))
            }
        } catch (e: Exception) {
            logger.error("Failed to load fake players", e)
        }
    }

    private fun saveFakePlayers(server: MinecraftServer) {
        val players = ListTag()
        for (player in server.playerList.players) {
            if (player !is FakePlayer) {
                continue
            }
            players.add(StringTag.valueOf(player.stringUUID))
        }

        val wrapper = CompoundTag()
        wrapper.put("players", players)
        try {
            NbtIo.write(wrapper, this.getFakePlayerDat(server))
        } catch (e: Exception) {
            logger.error("Failed to save fake players", e)
        }
    }

    private fun getFakePlayerDat(server: MinecraftServer): Path {
        return server.getWorldPath(LevelResource.ROOT).resolve("fake-players.dat")
    }
}