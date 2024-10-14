package me.senseiwells.players.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.MapCodec
import me.senseiwells.players.FakePlayer
import me.senseiwells.players.action.FakePlayerAction
import me.senseiwells.players.action.FakePlayerActionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action
import net.minecraft.resources.ResourceLocation

object OffhandAction: FakePlayerAction, FakePlayerActionProvider {
    override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("offhand")

    override val CODEC: MapCodec<out OffhandAction> = MapCodec.unit(this)

    override fun run(player: FakePlayer): Boolean {
        player.connection.handlePlayerAction(
            ServerboundPlayerActionPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN)
        )
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