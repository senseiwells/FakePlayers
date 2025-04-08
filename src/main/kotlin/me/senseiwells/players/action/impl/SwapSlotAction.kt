package me.senseiwells.players.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.players.ActionableFakePlayer
import me.senseiwells.players.action.FakePlayerAction
import me.senseiwells.players.action.FakePlayerActionProvider
import net.casual.arcade.commands.argument
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.resources.ResourceLocation

class SwapSlotAction(private val slot: Int): FakePlayerAction {
    override fun run(player: ActionableFakePlayer): Boolean {
        player.inventory.selected = this.slot
        return true
    }

    override fun provider(): FakePlayerActionProvider {
        return SwapSlotAction
    }

    companion object: FakePlayerActionProvider {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("swap_slot")

        override val CODEC: MapCodec<out SwapSlotAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.intRange(0, 8).fieldOf("slot").forGetter(SwapSlotAction::slot)
            ).apply(instance, ::SwapSlotAction)
        }

        override fun addCommandArguments(
            builder: LiteralArgumentBuilder<CommandSourceStack>,
            command: Command<CommandSourceStack>
        ) {
            builder.argument("slot", IntegerArgumentType.integer(0, 8)) {
                suggests { _, b -> SharedSuggestionProvider.suggest((0..8).map(Int::toString), b) }
                executes(command)
            }
        }

        override fun createCommandAction(context: CommandContext<CommandSourceStack>): FakePlayerAction {
            return SwapSlotAction(IntegerArgumentType.getInteger(context, "slot"))
        }
    }
}