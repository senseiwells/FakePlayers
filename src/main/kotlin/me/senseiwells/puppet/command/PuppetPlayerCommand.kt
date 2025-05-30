package me.senseiwells.puppet.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.senseiwells.puppet.PuppetPlayer
import me.senseiwells.puppet.PuppetPlayers
import me.senseiwells.puppet.action.PuppetPlayerActionProvider
import me.senseiwells.puppet.utils.PuppetPlayerRegistries
import net.casual.arcade.commands.*
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.utils.MathUtils.component1
import net.casual.arcade.utils.MathUtils.component2
import net.casual.arcade.utils.MathUtils.component3
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.DimensionArgument
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.GameModeArgument
import net.minecraft.commands.arguments.coordinates.Vec2Argument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.commands.arguments.selector.EntitySelectorParser
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.util.concurrent.CompletableFuture

object PuppetPlayerCommand: CommandTree {
    private val FAKE_PLAYERS_ONLY = SimpleCommandExceptionType(
        Component.literal("Only puppet players may be affected by this command")
    )
    private val PLAYER_ALREADY_ONLINE = SimpleCommandExceptionType(
        Component.literal("Player is already online")
    )
    private val PLAYER_ALREADY_JOINING = SimpleCommandExceptionType(
        Component.literal("Player is already joining")
    )

    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("puppet") {
            requires { it.hasPermission(2) || !PuppetPlayers.config.operatorRequiredForPuppets }

            argument("username", UsernameArgument.username()) {
                literal("join") {
                    executes(::fakePlayerJoin)
                }
                literal("spawn") {
                    executes { c -> spawnFakePlayer(c, c.source.position, c.source.rotation, c.source.level, null) }
                    literal("at") {
                        argument("position", Vec3Argument.vec3()) {
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

                addCommonCommandTree(this)
            }
            argument("player", EntityArgument.player()) {
                suggests(::suggestFakePlayerNamesAndSelectors)
                addCommonCommandTree(this)
            }
        }
    }

    private fun addCommonCommandTree(builder: ArgumentBuilder<CommandSourceStack, *>) {
        builder.literal("leave") {
            executes(::fakePlayerLeave)
        }

        builder.literal("actions") {
            literal("run") {
                for (provider in PuppetPlayerRegistries.ACTION_PROVIDERS) {
                    if (provider.canRunAction) {
                        literal(provider.ID.toString()) {
                            provider.addCommandArguments(this) { context ->
                                runAction(context, provider)
                            }
                        }
                    }
                }
            }
            literal("chain") {
                literal("add") {
                    for (provider in PuppetPlayerRegistries.ACTION_PROVIDERS) {
                        if (provider.canChainAction) {
                            literal(provider.ID.toString()) {
                                provider.addCommandArguments(this) { context ->
                                    addAction(context, provider)
                                }
                            }
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

    private fun suggestFakePlayerNamesAndSelectors(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val reader = StringReader(builder.input)
        reader.cursor = builder.start
        val parser = EntitySelectorParser(reader, context.source.hasPermission(2))
        runCatching(parser::parse)
        return parser.fillSuggestions(builder) {
            val names = context.source.server.playerList.players
                .filterIsInstance<PuppetPlayer>()
                .map { player -> player.scoreboardName }
            SharedSuggestionProvider.suggest(names, it)
        }
    }

    private fun fakePlayerJoin(context: CommandContext<CommandSourceStack>): Int {
        val username = UsernameArgument.getUsername(context, "username")
        this.addFakePlayerOrThrow(context, username)
        return context.source.success("Puppet player is joining...")
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
            player.teleportTo(level, x, y, z, setOf(), yRot, xRot, true)
        }
        return context.source.success("Puppet player is spawning...")
    }

    private fun fakePlayerLeave(context: CommandContext<CommandSourceStack>): Int {
        val player = this.getFakePlayerOrThrow(context)
        player.connection.disconnect(Component.literal("Removed via command"))
        return Command.SINGLE_SUCCESS
    }

    private fun addFakePlayerOrThrow(
        context: CommandContext<CommandSourceStack>,
        username: String
    ): CompletableFuture<PuppetPlayer> {
        val server = context.source.server
        val existing = server.playerList.getPlayerByName(username)
        if (existing != null) {
            throw PLAYER_ALREADY_ONLINE.create()
        }
        if (FakePlayer.isJoining(username)) {
            throw PLAYER_ALREADY_JOINING.create()
        }
        return FakePlayer.join(context.source.server, username, ::PuppetPlayer).whenComplete { _, throwable ->
            if (throwable != null) {
                context.source.fail("Puppet player $username failed to join, see logs for more info")
                PuppetPlayers.logger.error("Puppet player $username failed to join", throwable)
            }
        }
    }

    private fun runAction(context: CommandContext<CommandSourceStack>, provider: PuppetPlayerActionProvider): Int {
        val player = this.getFakePlayerOrThrow(context)
        val action = provider.createCommandAction(context)
        player.actions.run(action)
        return context.source.success("Successfully ran action '${provider.ID}'")
    }

    private fun addAction(context: CommandContext<CommandSourceStack>, provider: PuppetPlayerActionProvider): Int {
        val player = this.getFakePlayerOrThrow(context)
        val action = provider.createCommandAction(context)
        player.actions.chain(action)
        return context.source.success("Successfully added '${provider.ID}' action")
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

    private fun getFakePlayerOrThrow(context: CommandContext<CommandSourceStack>): PuppetPlayer {
        val player = if (context.hasArgument("username")) {
            val username = UsernameArgument.getUsername(context, "username")
            context.source.server.playerList.getPlayerByName(username)
        } else {
            EntityArgument.getPlayer(context, "player")
        }
        if (player !is PuppetPlayer) {
            throw FAKE_PLAYERS_ONLY.create()
        }
        return player
    }
}