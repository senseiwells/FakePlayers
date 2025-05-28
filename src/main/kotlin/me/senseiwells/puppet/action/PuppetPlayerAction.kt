package me.senseiwells.puppet.action

import com.mojang.serialization.Codec
import me.senseiwells.puppet.PuppetPlayer
import me.senseiwells.puppet.action.PuppetPlayerActionProvider.Companion.register
import me.senseiwells.puppet.action.impl.*
import me.senseiwells.puppet.utils.PuppetPlayerRegistries
import net.minecraft.core.Registry

/**
 * This interface represents an action that can be
 * run by a fake player.
 */
interface PuppetPlayerAction {
    /**
     * Whether the action will be run immediately or
     * whether to schedule it in the action tick phase.
     */
    val immediate: Boolean get() = false

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
    fun run(player: PuppetPlayer): Result

    /**
     * The provider for the given action.
     *
     * @return The action provider.
     */
    fun provider(): PuppetPlayerActionProvider

    enum class Result {
        Incomplete,
        Complete
    }

    companion object {
        val CODEC: Codec<PuppetPlayerAction> = Codec.lazyInitialized {
            PuppetPlayerRegistries.ACTION_PROVIDERS.byNameCodec()
                .dispatch(PuppetPlayerAction::provider, PuppetPlayerActionProvider::CODEC)
        }

        internal fun bootstrap(registry: Registry<PuppetPlayerActionProvider>) {
            AttackAction.register(registry)
            DelayAction.register(registry)
            DropAction.register(registry)
            InterruptMoveToAction.register(registry)
            JumpAction.register(registry)
            LookAction.register(registry)
            MoveToAction.register(registry)
            OffhandAction.register(registry)
            SneakAction.register(registry)
            SprintAction.register(registry)
            SwapSlotAction.register(registry)
            UseAction.register(registry)
        }
    }
}

