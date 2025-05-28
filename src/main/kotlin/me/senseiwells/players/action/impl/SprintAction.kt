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

class SprintAction(private val sprinting: Boolean): FakePlayerAction {
    override fun run(player: ActionableFakePlayer): FakePlayerAction.Result {
        player.moveControl.sprinting = this.sprinting
        return FakePlayerAction.Result.Complete
    }

    override fun provider(): FakePlayerActionProvider {
        return SprintAction
    }

    companion object: FakePlayerActionProvider {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("sprint")

        override val CODEC: MapCodec<out SprintAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.BOOL.fieldOf("sprinting").forGetter(SprintAction::sprinting)
            ).apply(instance, ::SprintAction)
        }

        override fun addCommandArguments(
            builder: LiteralArgumentBuilder<CommandSourceStack>,
            command: Command<CommandSourceStack>
        ) {
            builder.argument("sprinting", BoolArgumentType.bool()) {
                executes(command)
            }
        }

        override fun createCommandAction(context: CommandContext<CommandSourceStack>): FakePlayerAction {
            return SprintAction(BoolArgumentType.getBool(context, "sprinting"))
        }
    }
}