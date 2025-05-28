package me.senseiwells.puppet.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.puppet.PuppetPlayer
import me.senseiwells.puppet.action.ActionModifier
import me.senseiwells.puppet.action.PuppetPlayerAction
import me.senseiwells.puppet.action.PuppetPlayerActionProvider
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.arguments.EnumArgument
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation

class JumpAction(private val type: ActionModifier): PuppetPlayerAction {
    override fun run(player: PuppetPlayer): PuppetPlayerAction.Result {
        when (this.type) {
            ActionModifier.Once -> player.moveControl.jump()
            ActionModifier.Hold -> player.actions.jumping = true
            ActionModifier.Release -> player.actions.jumping = false
        }
        return PuppetPlayerAction.Result.Complete
    }

    override fun provider(): PuppetPlayerActionProvider {
        return JumpAction
    }

    companion object: PuppetPlayerActionProvider {
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

        override fun createCommandAction(context: CommandContext<CommandSourceStack>): PuppetPlayerAction {
            return JumpAction(EnumArgument.getEnumeration<ActionModifier>(context, "modifier"))
        }
    }
}