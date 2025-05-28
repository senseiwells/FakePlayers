package me.senseiwells.puppet.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.puppet.PuppetPlayer
import me.senseiwells.puppet.action.PuppetPlayerAction
import me.senseiwells.puppet.action.PuppetPlayerActionProvider
import net.casual.arcade.commands.argument
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.resources.ResourceLocation

class SwapSlotAction(private val slot: Int): PuppetPlayerAction {
    override fun run(player: PuppetPlayer): PuppetPlayerAction.Result {
        player.inventory.selectedSlot = this.slot
        return PuppetPlayerAction.Result.Complete
    }

    override fun provider(): PuppetPlayerActionProvider {
        return SwapSlotAction
    }

    companion object: PuppetPlayerActionProvider {
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

        override fun createCommandAction(context: CommandContext<CommandSourceStack>): PuppetPlayerAction {
            return SwapSlotAction(IntegerArgumentType.getInteger(context, "slot"))
        }
    }
}