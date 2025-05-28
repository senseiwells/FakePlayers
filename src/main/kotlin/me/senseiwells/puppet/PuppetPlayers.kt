package me.senseiwells.puppet

import me.senseiwells.puppet.command.PuppetPlayerCommand
import me.senseiwells.puppet.mixins.GameProfileCacheAccessor
import me.senseiwells.puppet.mixins.MinecraftServerAccessor
import me.senseiwells.puppet.mixins.ServicesAccessor
import me.senseiwells.puppet.network.MineToolsGameProfileRepository
import me.senseiwells.puppet.utils.PuppetPlayerRegistries
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

object PuppetPlayers: ModInitializer {
    const val MOD_ID = "puppet-players"

    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    val config = PuppetPlayerConfig.read()

    override fun onInitialize() {
        PuppetPlayerRegistries.load()

        GlobalEventHandler.Server.register<ServerRegisterCommandEvent> { event ->
            event.register(PuppetPlayerCommand)
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
                if (player is PuppetPlayer) {
                    player.connection.disconnect(Component.empty())
                }
            }
        }
    }

    private fun loadFakePlayers(server: MinecraftServer) {
        val path = this.getFakePlayerDat(server)
        if (!this.config.reloadPuppetPlayers || !path.exists()) {
            return
        }

        try {
            val wrapper = NbtIo.read(path) ?: return
            val players = wrapper.read("players", UUIDUtil.STRING_CODEC.listOf()).getOrNull()
            if (players != null) {
                for (player in players) {
                    FakePlayer.join(server, player, ::PuppetPlayer)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to load puppet players", e)
        }
    }

    private fun saveFakePlayers(server: MinecraftServer) {
        val players = ArrayList<UUID>()
        for (player in server.playerList.players) {
            if (player !is PuppetPlayer) {
                continue
            }
            players.add(player.uuid)
        }

        val wrapper = CompoundTag()
        wrapper.store("players", UUIDUtil.STRING_CODEC.listOf(), players)
        try {
            NbtIo.write(wrapper, this.getFakePlayerDat(server))
        } catch (e: Exception) {
            logger.error("Failed to save puppet players", e)
        }
    }

    private fun getFakePlayerDat(server: MinecraftServer): Path {
        return server.getWorldPath(LevelResource.ROOT).resolve("puppet-players.dat")
    }
}