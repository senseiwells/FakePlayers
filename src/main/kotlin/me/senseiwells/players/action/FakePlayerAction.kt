package me.senseiwells.players.action

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import me.senseiwells.players.FakePlayer
import me.senseiwells.players.utils.FakePlayerRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import java.util.function.Function

interface FakePlayerAction {
    fun run(player: FakePlayer): Boolean

    fun codec(): MapCodec<out FakePlayerAction>

    companion object {
        val CODEC: Codec<FakePlayerAction> by lazy {
            FakePlayerRegistries.ACTIONS.byNameCodec()
                .dispatch(FakePlayerAction::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out FakePlayerAction>>) {
            AttackAction.register(registry)
            UseAction.register(registry)
            DelayAction.register(registry)
            DropAction.register(registry)
            OffhandAction.register(registry)
            SwapSlotAction.register(registry)
            JumpAction.register(registry)
        }
    }
}

