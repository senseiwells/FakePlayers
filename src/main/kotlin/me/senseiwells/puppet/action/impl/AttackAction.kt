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

class AttackAction(private val type: ActionModifier): PuppetPlayerAction {
    override fun run(player: PuppetPlayer): PuppetPlayerAction.Result {
        when (this.type) {
            ActionModifier.Hold -> {
                player.actions.attacking = true
                player.actions.attackingHeld = true
            }
            ActionModifier.Once -> {
                player.actions.attacking = true
            }
            ActionModifier.Release -> {
                player.actions.attackingHeld = false
            }
        }
        return PuppetPlayerAction.Result.Complete
    }

    override fun provider(): PuppetPlayerActionProvider {
        return AttackAction
    }

    companion object: PuppetPlayerActionProvider {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("attack")

        override val CODEC: MapCodec<out AttackAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ActionModifier.CODEC.fieldOf("modifier").forGetter(AttackAction::type)
            ).apply(instance, ::AttackAction)
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
            return AttackAction(EnumArgument.getEnumeration<ActionModifier>(context, "modifier"))
        }
    }
}