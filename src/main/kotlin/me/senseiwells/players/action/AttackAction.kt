package me.senseiwells.players.action

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.players.FakePlayer
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceLocation

class AttackAction(private val type: ActionModifier): FakePlayerAction {
    override fun run(player: FakePlayer): Boolean {
        when (this.type) {
            ActionModifier.Hold -> {
                player.actions.attacking = true
                player.actions.attackingHeld = true
            }
            ActionModifier.Click -> {
                player.actions.attacking = true
            }
            ActionModifier.Release -> {
                player.actions.attackingHeld = false
            }
        }
        return true
    }

    override fun codec(): MapCodec<out FakePlayerAction> {
        return CODEC
    }

    companion object: CodecProvider<AttackAction> {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("attack")

        override val CODEC: MapCodec<out AttackAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ActionModifier.CODEC.fieldOf("modifier").forGetter(AttackAction::type)
            ).apply(instance, ::AttackAction)
        }
    }
}