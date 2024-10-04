package me.senseiwells.players.network

import me.senseiwells.players.FakePlayers
import net.minecraft.network.Connection
import net.minecraft.network.PacketSendListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.InteractionResult
import org.jetbrains.annotations.ApiStatus.Internal

class FakeGamePacketListenerImpl(
    server: MinecraftServer,
    connection: Connection,
    player: ServerPlayer,
    cookie: CommonListenerCookie
): ServerGamePacketListenerImpl(server, connection, player, cookie) {
    private var result: InteractionResult? = null

    override fun tick() {
        // We do this here to keep the player tick
        // phase consistent with vanilla players
        this.player.doTick()
    }

    override fun send(packet: Packet<*>, listener: PacketSendListener?) {
        if (packet is ClientboundPlayerPositionPacket) {
            this.handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket(packet.id))
        }
    }

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