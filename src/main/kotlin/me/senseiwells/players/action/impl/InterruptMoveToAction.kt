package me.senseiwells.players.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.MapCodec
import me.senseiwells.players.ActionableFakePlayer
import me.senseiwells.players.action.FakePlayerAction
import me.senseiwells.players.action.FakePlayerActionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation

object InterruptMoveToAction: FakePlayerAction, FakePlayerActionProvider {
    override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("interrupt_move_to")

    override val CODEC: MapCodec<out FakePlayerAction> = MapCodec.unit(this)

    override val immediate: Boolean get() = true

    override fun run(player: ActionableFakePlayer): Boolean {
        player.actions.remove { it is MoveToAction }
        player.navigation.stop()
        return true
    }

    override fun provider(): FakePlayerActionProvider {
        return this
    }

    override fun addCommandArguments(
        builder: LiteralArgumentBuilder<CommandSourceStack>,
        command: Command<CommandSourceStack>
    ) {
        builder.executes(command)
    }

    override fun createCommandAction(context: CommandContext<CommandSourceStack>): FakePlayerAction {
        return this
    }
}