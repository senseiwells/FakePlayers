package me.senseiwells.players.action

import com.mojang.serialization.Codec
import me.senseiwells.players.ActionableFakePlayer
import me.senseiwells.players.action.FakePlayerActionProvider.Companion.register
import me.senseiwells.players.action.impl.*
import me.senseiwells.players.utils.FakePlayerRegistries
import net.minecraft.core.Registry

/**
 * This interface represents an action that can be
 * run by a fake player.
 */
interface FakePlayerAction {
    /**
     * This runs the action, this method will be called
     * every tick until the action has finished running.
     *
     * This method should only return `true` after it
     * has finished running.
     * If this method returns `false` then the run method
     * must be called again the next tick.
     *
     * @param player The player doing the action.
     * @return Whether the action is finished.
     */
    fun run(player: ActionableFakePlayer): Boolean

    /**
     * The provider for the given action.
     *
     * @return The action provider.
     */
    fun provider(): FakePlayerActionProvider

    companion object {
        val CODEC: Codec<FakePlayerAction> = Codec.lazyInitialized {
            FakePlayerRegistries.ACTION_PROVIDERS.byNameCodec()
                .dispatch(FakePlayerAction::provider, FakePlayerActionProvider::CODEC)
        }

        internal fun bootstrap(registry: Registry<FakePlayerActionProvider>) {
            AttackAction.register(registry)
            DelayAction.register(registry)
            DropAction.register(registry)
            JumpAction.register(registry)
            LookAction.register(registry)
            MoveToAction.register(registry)
            OffhandAction.register(registry)
            SprintAction.register(registry)
            SwapSlotAction.register(registry)
            UseAction.register(registry)
        }
    }
}

