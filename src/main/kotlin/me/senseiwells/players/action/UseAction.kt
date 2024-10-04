package me.senseiwells.players.action

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceLocation

class UseAction(private val type: ActionModifier): FakePlayerAction {
    override fun run(actions: FakePlayerActions): Boolean {
        when (this.type) {
            ActionModifier.Hold -> {
                actions.using = true
                actions.usingHeld = true
            }
            ActionModifier.Click -> {
                actions.using = true
            }
            ActionModifier.Release -> {
                actions.usingHeld = false
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