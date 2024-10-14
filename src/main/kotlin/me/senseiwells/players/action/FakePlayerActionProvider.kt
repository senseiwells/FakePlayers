package me.senseiwells.players.action

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import me.senseiwells.players.utils.FakePlayerRegistries
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.Registry

/**
 * This interface provides methods for creating
 * [FakePlayerAction]s through commands and [CODEC]s.
 *
 * Implementations of this interface should be registered
 * to the [FakePlayerRegistries.ACTION_PROVIDERS].
 */
interface FakePlayerActionProvider: CodecProvider<FakePlayerAction> {
    /**
     * Whether the action can be run stand-alone.
     */
    val canRunAction: Boolean
        get() = true

    /**
     * Whether the action can be added to an action chain.
     */
    val canChainAction: Boolean
        get() = true

    fun addCommandArguments(builder: LiteralArgumentBuilder<CommandSourceStack>, command: Command<CommandSourceStack>)

    fun createCommandAction(context: CommandContext<CommandSourceStack>): FakePlayerAction

    companion object {
        fun FakePlayerActionProvider.register(registry: Registry<FakePlayerActionProvider>) {
            Registry.register(registry, this.ID, this)
        }
    }
}