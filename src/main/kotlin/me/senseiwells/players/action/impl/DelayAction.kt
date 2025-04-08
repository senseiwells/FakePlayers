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
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.TimeArgument
import net.minecraft.resources.ResourceLocation

class DelayAction(
    private val delay: MinecraftTimeDuration,
    private var ticks: Int = 0
): FakePlayerAction {
    override fun run(player: ActionableFakePlayer): Boolean {
        if (this.ticks++ >= this.delay.ticks) {
            this.ticks = 0
            return true
        }
        return false
    }

    override fun provider(): FakePlayerActionProvider {
        return DelayAction
    }

    companion object: FakePlayerActionProvider {
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

        override fun createCommandAction(context: CommandContext<CommandSourceStack>): FakePlayerAction {
            return DelayAction(IntegerArgumentType.getInteger(context, "delay").Ticks)
        }
    }
}