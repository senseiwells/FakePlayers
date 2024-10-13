package me.senseiwells.players.action

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.players.FakePlayer
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceLocation

class UseAction(private val type: ActionModifier): FakePlayerAction {
    override fun run(player: FakePlayer): Boolean {
        when (this.type) {
            ActionModifier.Hold -> {
                player.actions.using = true
                player.actions.usingHeld = true
            }
            ActionModifier.Click -> {
                player.actions.using = true
            }
            ActionModifier.Release -> {
                player.actions.usingHeld = false
            }
        }
        return true
    }

    override fun codec(): MapCodec<out FakePlayerAction> {
        return CODEC
    }

    companion object: CodecProvider<UseAction> {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("use")

        override val CODEC: MapCodec<out UseAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ActionModifier.CODEC.fieldOf("modifier").forGetter(UseAction::type)
            ).apply(instance, ::UseAction)
        }
    }
}