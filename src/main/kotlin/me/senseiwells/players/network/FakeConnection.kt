package me.senseiwells.players.network

import io.netty.channel.embedded.EmbeddedChannel
import me.senseiwells.players.mixins.ConnectionAccessor
import net.minecraft.network.Connection
import net.minecraft.network.PacketListener
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.protocol.PacketFlow

@Suppress("CAST_NEVER_SUCCEEDS")
class FakeConnection: Connection(PacketFlow.SERVERBOUND) {
    init {
        (this as ConnectionAccessor).setChannel(EmbeddedChannel())
    }

    override fun <T: PacketListener?> setupInboundProtocol(protocolInfo: ProtocolInfo<T>, listener: T) {
        (this as ConnectionAccessor).setPacketListener(listener)
    }
}