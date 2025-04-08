package me.senseiwells.players.network

import me.senseiwells.players.FakePlayers
import net.casual.arcade.npc.network.FakeGamePacketListenerImpl
import net.minecraft.network.Connection
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.world.InteractionResult
import org.jetbrains.annotations.ApiStatus.Internal

class ActionableFakeGamePacketListenerImpl(
    server: MinecraftServer,
    connection: Connection,
    player: ServerPlayer,
    cookie: CommonListenerCookie
): FakeGamePacketListenerImpl(server, connection, player, cookie) {
    private var result: InteractionResult? = null

    @Internal
    fun pushResult(result: InteractionResult) {
        if (this.result != null) {
            FakePlayers.logger.warn("Pushed interaction result before last was popped!")
        }
        this.result = result
    }

    @Internal
    fun popResult(default: InteractionResult): InteractionResult {
        val result = this.result ?: return default
        this.result = null
        return result
    }
}