package me.senseiwells.players.action

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.players.FakePlayer
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.resources.ResourceLocation

class DelayAction(
    private val delay: MinecraftTimeDuration,
    private var ticks: Int = 0
): FakePlayerAction {
    override fun run(player: FakePlayer): Boolean {
        if (this.ticks++ >= this.delay.ticks) {
            this.ticks = 0
            return true
        }
        return false
    }

    override fun codec(): MapCodec<out FakePlayerAction> {
        return CODEC
    }

    companion object: CodecProvider<DelayAction> {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("delay")

        override val CODEC: MapCodec<out DelayAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                MinecraftTimeDuration.CODEC.fieldOf("delay").forGetter(DelayAction::delay),
                Codec.INT.fieldOf("ticks").forGetter(DelayAction::ticks)
            ).apply(instance, ::DelayAction)
        }
    }
}