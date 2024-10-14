package me.senseiwells.players.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.players.FakePlayer
import me.senseiwells.players.action.FakePlayerAction
import me.senseiwells.players.action.FakePlayerActionProvider
import net.casual.arcade.commands.argument
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation

class DropAction(private val dropEntireStack: Boolean = false): FakePlayerAction {
    override fun run(player: FakePlayer): Boolean {
        if (!player.isSpectator) {
            player.drop(this.dropEntireStack)
        }
        return true
    }

    override fun provider(): FakePlayerActionProvider {
        return DropAction
    }

    companion object: FakePlayerActionProvider {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("drop")

        override val CODEC: MapCodec<out DropAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.BOOL.fieldOf("drop_entire_stack").forGetter(DropAction::dropEntireStack)
            ).apply(instance, ::DropAction)
        }

        override fun addCommandArguments(
            builder: LiteralArgumentBuilder<CommandSourceStack>,
            command: Command<CommandSourceStack>
        ) {
            builder.argument("stack", BoolArgumentType.bool()) {
                executes(command)
            }
        }

        override fun createCommandAction(context: CommandContext<CommandSourceStack>): FakePlayerAction {
            return DropAction(BoolArgumentType.getBool(context, "stack"))
        }
    }
}