package me.senseiwells.players.action

import com.mojang.serialization.MapCodec
import me.senseiwells.players.FakePlayer
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceLocation

object JumpAction: FakePlayerAction, CodecProvider<JumpAction> {
    override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("jump")

    override val CODEC: MapCodec<out JumpAction> = MapCodec.unit(this)

    override fun run(player: FakePlayer): Boolean {
        if (player.onGround()) {
            player.jumpFromGround()
        }
        return true
    }

    override fun codec(): MapCodec<out FakePlayerAction> {
        return CODEC
    }
}