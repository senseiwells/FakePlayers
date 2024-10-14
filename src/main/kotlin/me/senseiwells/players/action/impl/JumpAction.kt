package me.senseiwells.players.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.MapCodec
import me.senseiwells.players.FakePlayer
import me.senseiwells.players.action.FakePlayerAction
import me.senseiwells.players.action.FakePlayerActionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation

object JumpAction: FakePlayerAction, FakePlayerActionProvider {
    override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("jump")

    override val CODEC: MapCodec<out JumpAction> = MapCodec.unit(this)

    override fun run(player: FakePlayer): Boolean {
        if (player.onGround()) {
            player.jumpFromGround()
        }
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