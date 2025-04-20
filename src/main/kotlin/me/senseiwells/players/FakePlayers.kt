package me.senseiwells.players

import me.senseiwells.players.command.FakePlayerCommand
import me.senseiwells.players.mixins.GameProfileCacheAccessor
import me.senseiwells.players.mixins.MinecraftServerAccessor
import me.senseiwells.players.mixins.ServicesAccessor
import me.senseiwells.players.network.MineToolsGameProfileRepository
import me.senseiwells.players.utils.FakePlayerRegistries
import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.ServerStoppingEvent
import net.casual.arcade.npc.FakePlayer
import net.fabricmc.api.ModInitializer
import net.minecraft.core.UUIDUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.Proxy
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists
import kotlin.jvm.optionals.getOrNull

object FakePlayers: ModInitializer {
    const val MOD_ID = "fake-players"

    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    val config = FakePlayerConfig.read()

    override fun onInitialize() {
        FakePlayerRegistries.load()

        GlobalEventHandler.Server.register<ServerRegisterCommandEvent> { event ->
            event.register(FakePlayerCommand)
        }
        GlobalEventHandler.Server.register<ServerLoadedEvent> { (server) ->
            this.loadFakePlayers(server)
            if (this.config.useMineToolsApi) {
                val repository = MineToolsGameProfileRepository(Proxy.NO_PROXY)
                val services = (server as MinecraftServerAccessor).services
                @Suppress("CAST_NEVER_SUCCEEDS")
                (services as ServicesAccessor).setProfileRepository(repository)
                (services.profileCache as GameProfileCacheAccessor).setProfileRepository(repository)
            }
        }
        GlobalEventHandler.Server.register<ServerSaveEvent> { (server, stopping) ->
            if (!stopping) {
                this.saveFakePlayers(server)
            }
        }
        GlobalEventHandler.Server.register<ServerStoppingEvent> { (server) ->
            this.saveFakePlayers(server)
            // We dc fake players here because luckperms is silly
            for (player in server.playerList.players.toList()) {
                if (player is ActionableFakePlayer) {
                    player.connection.disconnect(Component.empty())
                }
            }
        }
    }

    private fun loadFakePlayers(server: MinecraftServer) {
        val path = this.getFakePlayerDat(server)
        if (!this.config.reloadFakePlayers || !path.exists()) {
            return
        }

        try {
            val wrapper = NbtIo.read(path) ?: return
            val players = wrapper.read("players", UUIDUtil.STRING_CODEC.listOf()).getOrNull()
            if (players != null) {
                for (player in players) {
                    FakePlayer.join(server, player, ::ActionableFakePlayer)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to load fake players", e)
        }
    }

    private fun saveFakePlayers(server: MinecraftServer) {
        val players = ArrayList<UUID>()
        for (player in server.playerList.players) {
            if (player !is ActionableFakePlayer) {
                continue
            }
            players.add(player.uuid)
        }

        val wrapper = CompoundTag()
        wrapper.store("players", UUIDUtil.STRING_CODEC.listOf(), players)
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