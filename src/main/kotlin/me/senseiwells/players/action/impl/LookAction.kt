package me.senseiwells.players.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.players.FakePlayer
import me.senseiwells.players.action.FakePlayerAction
import me.senseiwells.players.action.FakePlayerActionProvider
import net.casual.arcade.commands.argument
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.coordinates.RotationArgument
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec2

class LookAction(private val rotation: Vec2): FakePlayerAction {
    override fun run(player: FakePlayer): Boolean {
        player.connection.send(
            ServerboundMovePlayerPacket.Rot(this.rotation.y, this.rotation.x, player.onGround(), player.horizontalCollision)
        )
        return true
    }

    override fun provider(): FakePlayerActionProvider {
        return LookAction
    }

    companion object: FakePlayerActionProvider {
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
        override fun createCommandAction(context: CommandContext<CommandSourceStack>): FakePlayerAction {
            return LookAction(RotationArgument.getRotation(context, "rotation").getRotation(context.source))
        }

    }
}