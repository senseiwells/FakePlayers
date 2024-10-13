package me.senseiwells.players.action

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.players.FakePlayer
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceLocation

class DropAction(private val dropEntireStack: Boolean = false): FakePlayerAction {
    override fun run(player: FakePlayer): Boolean {
        if (!player.isSpectator) {
            player.drop(this.dropEntireStack)
        }
        return true
    }

    override fun codec(): MapCodec<out FakePlayerAction> {
        return CODEC
    }

    companion object: CodecProvider<DropAction> {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("drop")

        override val CODEC: MapCodec<out DropAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.BOOL.fieldOf("drop_entire_stack").forGetter(DropAction::dropEntireStack)
            ).apply(instance, ::DropAction)
        }
    }
}