package me.senseiwells.players.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import me.senseiwells.players.FakePlayer
import me.senseiwells.players.action.ActionModifier
import me.senseiwells.players.action.AttackAction
import me.senseiwells.players.action.DelayAction
import me.senseiwells.players.action.UseAction
import net.casual.arcade.commands.*
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.utils.MathUtils.component1
import net.casual.arcade.utils.MathUtils.component2
import net.casual.arcade.utils.MathUtils.component3
import net.casual.arcade.utils.TimeUtils.Ticks
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.DimensionArgument
import net.minecraft.commands.arguments.GameModeArgument
import net.minecraft.commands.arguments.TimeArgument
import net.minecraft.commands.arguments.coordinates.Vec2Argument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.util.concurrent.CompletableFuture

object FakePlayerCommand: CommandTree {
    private val FAKE_PLAYERS_ONLY = SimpleCommandExceptionType(
        Component.literal("Only fake players may be affected by this command")
    )
    private val PLAYER_ALREADY_ONLINE = SimpleCommandExceptionType(
        Component.literal("Player is already online")
    )
    private val PLAYER_ALREADY_JOINING = SimpleCommandExceptionType(
        Component.literal("Player is already joining")
    )

    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("fake-player") {
            requiresPermission(2)

            argument("username", UsernameArgument.username()) {
                literal("join") {
                    executes(::fakePlayerJoin)
                }
                literal("spawn") {
                    executes { c -> spawnFakePlayer(c, c.source.position, c.source.rotation, c.source.level, null) }
                    literal("at") {
                        argument("pos", Vec3Argument.vec3()) {
                            executes { c -> spawnFakePlayer(c, rotation = null, dimension = c.source.level, gamemode = null) }
                            literal("facing") {
                                argument("rotation", Vec2Argument.vec2()) {
                                    executes { c -> spawnFakePlayer(c, dimension = c.source.level, gamemode = null) }
                                    literal("in") {
                                        argument("dimension", DimensionArgument.dimension()) {
                                            executes { c -> spawnFakePlayer(c, gamemode = null) }
                                            literal("in") {
                                                argument("gamemode", GameModeArgument.gameMode()) {
                                                    executes(::spawnFakePlayer)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                literal("leave") {
                    executes(::fakePlayerLeave)
                }

                literal("attacking") {
                    argument("attacking", BoolArgumentType.bool()) {
                        executes(::setAttacking)
                    }
                }
                literal("using") {
                    argument("using", BoolArgumentType.bool()) {
                        executes(::setUsing)
                    }
                }

                literal("actions") {
                    literal("add") {
                        literal("attack") {
                            argument("modifier", EnumArgument.enumeration<ActionModifier>()) {
                                executes(::addAttackAction)
                            }
                        }
                        literal("use") {
                            argument("modifier", EnumArgument.enumeration<ActionModifier>()) {
                                executes(::addUseAction)
                            }
                        }
                        literal("delay") {
                            argument("delay", TimeArgument.time(1)) {
                                executes(::addDelayAction)
                            }
                        }
                    }
                    literal("loop") {
                        argument("loop", BoolArgumentType.bool()) {
                            executes(::loopActions)
                        }
                    }
                    literal("pause") {
                        executes(::pauseActions)
                    }
                    literal("resume") {
                        executes(::resumeActions)
                    }
                    literal("restart") {
                        executes(::restartActions)
                    }
                    literal("stop") {
                        executes(::stopActions)
                    }
                }
            }
        }
    }

    private fun fakePlayerJoin(context: CommandContext<CommandSourceStack>): Int {
        val username = UsernameArgument.getUsername(context, "username")
        this.addFakePlayerOrThrow(context, username)
        return context.source.success("Fake player is joining...")
    }

    private fun spawnFakePlayer(
        context: CommandContext<CommandSourceStack>,
        position: Vec3 = Vec3Argument.getVec3(context, "position"),
        rotation: Vec2? = Vec2Argument.getVec2(context, "rotation"),
        dimension: ServerLevel? = DimensionArgument.getDimension(context, "dimension"),
        gamemode: GameType? = GameModeArgument.getGameMode(context, "gamemode")
    ): Int {
        val username = UsernameArgument.getUsername(context, "username")
        this.addFakePlayerOrThrow(context, username).thenApply { player ->
            val level = dimension ?: player.serverLevel()
            val (x, y, z) = position
            val (yRot, xRot) = rotation ?: player.rotationVector
            if (gamemode != null) {
                player.gameMode.changeGameModeForPlayer(gamemode)
            }
            player.teleportTo(level, x, y, z, yRot, xRot)
        }
        return context.source.success("Fake player is spawning...")
    }

    private fun fakePlayerLeave(context: CommandContext<CommandSourceStack>): Int {
        val player = this.getFakePlayerOrThrow(context)
        player.connection.disconnect(Component.literal("Removed via command"))
        return Command.SINGLE_SUCCESS
    }

    private fun addFakePlayerOrThrow(
        context: CommandContext<CommandSourceStack>,
        username: String
    ): CompletableFuture<FakePlayer> {
        val server = context.source.server
        val existing = server.playerList.getPlayerByName(username)
        if (existing != null) {
            throw PLAYER_ALREADY_ONLINE.create()
        }
        if (FakePlayer.isJoining(username)) {
            throw PLAYER_ALREADY_JOINING.create()
        }
        return FakePlayer.join(context.source.server, username).whenComplete { _, throwable ->
            if (throwable != null) {
                context.source.fail("Fake player $username failed to join, see logs for more info")
            }
        }
    }

    private fun setAttacking(context: CommandContext<CommandSourceStack>): Int {
        val player = this.getFakePlayerOrThrow(context)
        val attacking = BoolArgumentType.getBool(context, "attacking")
        player.actions.attacking = attacking
        player.actions.attackingHeld = attacking
        return context.source.success("Successfully set attacking to $attacking")
    }

    private fun setUsing(context: CommandContext<CommandSourceStack>): Int {
        val player = this.getFakePlayerOrThrow(context)
        val using = BoolArgumentType.getBool(context, "using")
        player.actions.using = using
        player.actions.usingHeld = using
        return context.source.success("Successfully set using to $using")
    }

    private fun addAttackAction(context: CommandContext<CommandSourceStack>): Int {
        val player = this.getFakePlayerOrThrow(context)
        val modifier = EnumArgument.getEnumeration<ActionModifier>(context, "modifier")
        player.actions.add(AttackAction(modifier))
        return context.source.success("Successfully added attack action")
    }

    private fun addUseAction(context: CommandContext<CommandSourceStack>): Int {
        val player = this.getFakePlayerOrThrow(context)
        val modifier = EnumArgument.getEnumeration<ActionModifier>(context, "modifier")
        player.actions.add(UseAction(modifier))
        return context.source.success("Successfully added use action")
    }

    private fun addDelayAction(context: CommandContext<CommandSourceStack>): Int {
        val player = this.getFakePlayerOrThrow(context)
        val delay = IntegerArgumentType.getInteger(context, "delay").Ticks
        player.actions.add(DelayAction(delay))
        return context.source.success("Successfully added delay action")
    }

    private fun loopActions(context: CommandContext<CommandSourceStack>): Int {
        val player = this.getFakePlayerOrThrow(context)
        val loop = BoolArgumentType.getBool(context, "loop")
        player.actions.loop = loop
        if (loop) {
            return context.source.success("Successfully set actions to loop")
        }
        return context.source.success("Successfully set actions to not loop")
    }

    private fun pauseActions(context: CommandContext<CommandSourceStack>): Int {
        val player = this.getFakePlayerOrThrow(context)
        if (player.actions.paused) {
            return context.source.fail("Actions are already paused")
        }
        player.actions.paused = true
        return context.source.success("Successfully paused actions")
    }

    private fun resumeActions(context: CommandContext<CommandSourceStack>): Int {
        val player = this.getFakePlayerOrThrow(context)
        if (!player.actions.paused) {
            return context.source.fail("Actions are not paused")
        }
        player.actions.paused = false
        return context.source.success("Successfully resumed actions")
    }

    private fun restartActions(context: CommandContext<CommandSourceStack>): Int {
        val player = this.getFakePlayerOrThrow(context)
        player.actions.restart()
        return context.source.success("Successfully restarted actions")
    }

    private fun stopActions(context: CommandContext<CommandSourceStack>): Int {
        val player = this.getFakePlayerOrThrow(context)
        player.actions.clear()
        return context.source.success("Successfully stopped all actions")
    }

    private fun getFakePlayerOrThrow(context: CommandContext<CommandSourceStack>): FakePlayer {
        val username = UsernameArgument.getUsername(context, "username")
        val player = context.source.server.playerList.getPlayerByName(username)
        if (player !is FakePlayer) {
            throw FAKE_PLAYERS_ONLY.create()
        }
        return player
    }
}