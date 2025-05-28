package me.senseiwells.puppet.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
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
import net.minecraft.resources.ResourceLocation

class SprintAction(private val sprinting: Boolean): PuppetPlayerAction {
    override fun run(player: PuppetPlayer): PuppetPlayerAction.Result {
        player.moveControl.sprinting = this.sprinting
        return PuppetPlayerAction.Result.Complete
    }

    override fun provider(): PuppetPlayerActionProvider {
        return SprintAction
    }

    companion object: PuppetPlayerActionProvider {
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

        override fun createCommandAction(context: CommandContext<CommandSourceStack>): PuppetPlayerAction {
            return SprintAction(BoolArgumentType.getBool(context, "sprinting"))
        }
    }
}