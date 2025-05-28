package me.senseiwells.puppet.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.puppet.PuppetPlayer
import me.senseiwells.puppet.action.PuppetPlayerAction
import me.senseiwells.puppet.action.PuppetPlayerActionProvider
import net.casual.arcade.commands.argument
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.coordinates.RotationArgument
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec2

class LookAction(private val rotation: Vec2): PuppetPlayerAction {
    override fun run(player: PuppetPlayer): PuppetPlayerAction.Result {
        player.connection.send(
            ServerboundMovePlayerPacket.Rot(this.rotation.y, this.rotation.x, player.onGround(), player.horizontalCollision)
        )
        return PuppetPlayerAction.Result.Complete
    }

    override fun provider(): PuppetPlayerActionProvider {
        return LookAction
    }

    companion object: PuppetPlayerActionProvider {
        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("look")

        override val CODEC: MapCodec<out LookAction> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ArcadeExtraCodecs.VEC2.fieldOf("rotation").forGetter(LookAction::rotation)
            ).apply(instance, ::LookAction)
        }

        override fun addCommandArguments(
            builder: LiteralArgumentBuilder<CommandSourceStack>,
            command: Command<CommandSourceStack>
        ) {
            builder.argument("rotation", RotationArgument.rotation()) {
                executes(command)
            }
        }
        override fun createCommandAction(context: CommandContext<CommandSourceStack>): PuppetPlayerAction {
            return LookAction(RotationArgument.getRotation(context, "rotation").getRotation(context.source))
        }

    }
}