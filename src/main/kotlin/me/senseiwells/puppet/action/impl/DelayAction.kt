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
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.TimeArgument
import net.minecraft.resources.ResourceLocation

class DelayAction(
    private val delay: MinecraftTimeDuration,
    private var ticks: Int = 0
): PuppetPlayerAction {
    override fun run(player: PuppetPlayer): PuppetPlayerAction.Result {
        if (this.ticks++ >= this.delay.ticks) {
            this.ticks = 0
            return PuppetPlayerAction.Result.Complete
        }
        return PuppetPlayerAction.Result.Incomplete
    }

    override fun provider(): PuppetPlayerActionProvider {
        return DelayAction
    }

    companion object: PuppetPlayerActionProvider {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("delay")

        override val CODEC: MapCodec<out DelayAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                MinecraftTimeDuration.CODEC.fieldOf("delay").forGetter(DelayAction::delay),
                Codec.INT.fieldOf("ticks").forGetter(DelayAction::ticks)
            ).apply(instance, ::DelayAction)
        }

        override val canRunAction: Boolean
            get() = false

        override fun addCommandArguments(
            builder: LiteralArgumentBuilder<CommandSourceStack>,
            command: Command<CommandSourceStack>
        ) {
            builder.argument("delay", TimeArgument.time(1)) {
                executes(command)
            }
        }

        override fun createCommandAction(context: CommandContext<CommandSourceStack>): PuppetPlayerAction {
            return DelayAction(IntegerArgumentType.getInteger(context, "delay").Ticks)
        }
    }
}