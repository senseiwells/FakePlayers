package me.senseiwells.players

import me.senseiwells.players.command.FakePlayerCommand
import me.senseiwells.players.utils.FakePlayerRegistries
import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.fabricmc.api.ModInitializer
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.Tag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
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
        GlobalEventHandler.register<ServerSaveEvent> { (server) ->
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
            val players = wrapper.getList("players", Tag.TAG_COMPOUND.toInt())
            for (data in players) {
                data as CompoundTag
                val uuid = data.getUUID("uuid")
                val actions = data.getCompound("actions")
                FakePlayer.join(server, uuid).thenApply { player ->
                    player.actions.deserialize(actions)
                }
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

            val data = CompoundTag()
            data.putUUID("uuid", player.uuid)
            data.put("actions", player.actions.serialize())
            players.add(data)
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