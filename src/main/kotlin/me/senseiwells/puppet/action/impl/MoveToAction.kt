package me.senseiwells.puppet.action.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.senseiwells.puppet.PuppetPlayer
import me.senseiwells.puppet.action.PuppetPlayerAction
import me.senseiwells.puppet.action.PuppetPlayerActionProvider
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.hasArgument
import net.casual.arcade.commands.literal
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.core.UUIDUtil
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import java.util.*

sealed class MoveToAction(
    private val sprint: Boolean,
    private val jump: Boolean
): PuppetPlayerAction {
    abstract fun getTarget(player: PuppetPlayer): Vec3?

    override fun run(player: PuppetPlayer): PuppetPlayerAction.Result {
        val target = this.getTarget(player) ?: return PuppetPlayerAction.Result.Complete
        val canNavigate = player.navigation.moveTo(target.x, target.y, target.z, 1.0)
        if (canNavigate) {
            if (this.sprint) {
                player.moveControl.sprinting = true
            }
            if (this.jump) {
                player.moveControl.jump()
            }
            return PuppetPlayerAction.Result.Incomplete
        }
        return PuppetPlayerAction.Result.Complete
    }

    override fun provider(): PuppetPlayerActionProvider {
        return MoveToAction
    }

    private class MoveToPositionAction(
        val target: Vec3,
        sprint: Boolean,
        jump: Boolean
    ): MoveToAction(sprint, jump) {
        override fun getTarget(player: PuppetPlayer): Vec3 {
            return this.target
        }
    }

    private class MoveToEntityAction(
        val uuid: UUID,
        sprint: Boolean,
        jump: Boolean
    ): MoveToAction(sprint, jump) {
        override fun getTarget(player: PuppetPlayer): Vec3? {
            val entity = player.serverLevel().getEntity(this.uuid)
            return entity?.position()
        }
    }

    companion object: PuppetPlayerActionProvider {
        private val POSITION_CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Vec3.CODEC.fieldOf("target").forGetter(MoveToPositionAction::target),
                Codec.BOOL.fieldOf("sprint").forGetter(MoveToAction::sprint),
                Codec.BOOL.fieldOf("jump").forGetter(MoveToAction::jump)
            ).apply(instance, ::MoveToPositionAction)
        }

        private val ENTITY_CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                UUIDUtil.CODEC.fieldOf("uuid").forGetter(MoveToEntityAction::uuid),
                Codec.BOOL.fieldOf("sprint").forGetter(MoveToAction::sprint),
                Codec.BOOL.fieldOf("jump").forGetter(MoveToAction::jump)
            ).apply(instance, ::MoveToEntityAction)
        }

        override val ID: ResourceLocation = ResourceLocation.withDefaultNamespace("move_to")

        override val CODEC: MapCodec<out MoveToAction> = Codec.mapEither(POSITION_CODEC, ENTITY_CODEC).xmap(
            { either -> either.map({ it }, { it }) },
            { action -> if (action is MoveToPositionAction) Either.left(action) else Either.right(action as MoveToEntityAction) }
        )

        override fun addCommandArguments(
            builder: LiteralArgumentBuilder<CommandSourceStack>,
            command: Command<CommandSourceStack>
        ) {
            builder.literal("position") {
                argument("position", Vec3Argument.vec3()) {
                    executes(command)
                    argument("sprint", BoolArgumentType.bool()) {
                        executes(command)
                        argument("jump", BoolArgumentType.bool()) {
                            executes(command)
                        }
                    }
                }
            }
            builder.literal("entity") {
                argument("entity", EntityArgument.entity()) {
                    executes(command)
                    argument("sprint", BoolArgumentType.bool()) {
                        executes(command)
                        argument("jump", BoolArgumentType.bool()) {
                            executes(command)
                        }
                    }
                }
            }
        }

        override fun createCommandAction(context: CommandContext<CommandSourceStack>): PuppetPlayerAction {
            val sprint = context.hasArgument("sprint") && BoolArgumentType.getBool(context, "sprint")
            val jump = context.hasArgument("jump") && BoolArgumentType.getBool(context, "jump")
            if (context.hasArgument("position")) {
                val position = Vec3Argument.getVec3(context, "position")
                return MoveToPositionAction(position, sprint, jump)
            }
            val entity = EntityArgument.getEntity(context, "entity")
            return MoveToEntityAction(entity.uuid, sprint, jump)
        }
    }
}