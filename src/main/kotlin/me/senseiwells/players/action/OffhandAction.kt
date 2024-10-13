package me.senseiwells.players.action

import com.mojang.serialization.MapCodec
import me.senseiwells.players.FakePlayer
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action
import net.minecraft.resources.ResourceLocation

object OffhandAction: FakePlayerAction, CodecProvider<OffhandAction> {
    override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("offhand")

    override val CODEC: MapCodec<out OffhandAction> = MapCodec.unit(this)

    override fun run(player: FakePlayer): Boolean {
        player.connection.handlePlayerAction(
            ServerboundPlayerActionPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN)
        )
        return true
    }

    override fun codec(): MapCodec<out FakePlayerAction> {
        return CODEC
    }
}