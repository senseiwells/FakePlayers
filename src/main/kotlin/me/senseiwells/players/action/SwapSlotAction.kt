package me.senseiwells.players.action

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.players.FakePlayer
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceLocation

class SwapSlotAction(private val slot: Int): FakePlayerAction {
    override fun run(player: FakePlayer): Boolean {
        player.inventory.selected = this.slot
        return true
    }

    override fun codec(): MapCodec<out FakePlayerAction> {
        return CODEC
    }

    companion object: CodecProvider<SwapSlotAction> {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("swap_slot")

        override val CODEC: MapCodec<out SwapSlotAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.intRange(0, 8).fieldOf("slot").forGetter(SwapSlotAction::slot)
            ).apply(instance, ::SwapSlotAction)
        }
    }
}