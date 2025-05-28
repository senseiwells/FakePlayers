package me.senseiwells.players.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
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
import net.minecraft.resources.ResourceLocation

class SneakAction(private val sneaking: Boolean): FakePlayerAction {
    override fun run(player: ActionableFakePlayer): FakePlayerAction.Result {
        player.isShiftKeyDown = this.sneaking
        return FakePlayerAction.Result.Complete
    }

    override fun provider(): FakePlayerActionProvider {
        return SneakAction
    }

    companion object: FakePlayerActionProvider {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("sneak")

        override val CODEC: MapCodec<out SneakAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.BOOL.fieldOf("sneaking").forGetter(SneakAction::sneaking)
            ).apply(instance, ::SneakAction)
        }

        override fun addCommandArguments(
            builder: LiteralArgumentBuilder<CommandSourceStack>,
            command: Command<CommandSourceStack>
        ) {
            builder.argument("sneaking", BoolArgumentType.bool()) {
                executes(command)
            }
        }

        override fun createCommandAction(context: CommandContext<CommandSourceStack>): FakePlayerAction {
            return SneakAction(BoolArgumentType.getBool(context, "sneaking"))
        }
    }
}