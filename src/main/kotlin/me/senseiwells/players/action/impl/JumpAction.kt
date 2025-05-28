package me.senseiwells.players.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.players.ActionableFakePlayer
import me.senseiwells.players.action.ActionModifier
import me.senseiwells.players.action.FakePlayerAction
import me.senseiwells.players.action.FakePlayerActionProvider
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.arguments.EnumArgument
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation

class JumpAction(private val type: ActionModifier): FakePlayerAction {
    override fun run(player: ActionableFakePlayer): FakePlayerAction.Result {
        when (this.type) {
            ActionModifier.Once -> player.moveControl.jump()
            ActionModifier.Hold -> player.actions.jumping = true
            ActionModifier.Release -> player.actions.jumping = false
        }
        return FakePlayerAction.Result.Complete
    }

    override fun provider(): FakePlayerActionProvider {
        return JumpAction
    }

    companion object: FakePlayerActionProvider {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("jump")

        override val CODEC: MapCodec<out JumpAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ActionModifier.CODEC.fieldOf("modifier").forGetter(JumpAction::type)
            ).apply(instance, ::JumpAction)
        }

        override fun addCommandArguments(
            builder: LiteralArgumentBuilder<CommandSourceStack>,
            command: Command<CommandSourceStack>
        ) {
            builder.argument("modifier", EnumArgument.enumeration<ActionModifier>()) {
                executes(command)
            }
        }

        override fun createCommandAction(context: CommandContext<CommandSourceStack>): FakePlayerAction {
            return JumpAction(EnumArgument.getEnumeration<ActionModifier>(context, "modifier"))
        }
    }
}