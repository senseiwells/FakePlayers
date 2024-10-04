package me.senseiwells.players

import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.senseiwells.players.action.FakePlayerActions
import me.senseiwells.players.network.FakeConnection
import me.senseiwells.players.network.FakeGamePacketListenerImpl
import me.senseiwells.players.utils.ResolvableProfile
import net.casual.arcade.utils.contains
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.TickTask
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.level.block.state.BlockState
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.concurrent.CompletableFuture

class FakePlayer @Internal constructor(
    server: MinecraftServer,
    level: ServerLevel,
    profile: GameProfile
): ServerPlayer(server, level, profile, ClientInformation.createDefault()) {
    val actions = FakePlayerActions(this)

    fun connection(): FakeGamePacketListenerImpl {
        return this.connection as FakeGamePacketListenerImpl
    }

    override fun tick() {
        // The player will never send move packets,
        // so we need to manually move the player.
        // This keeps the ticket manager updated
        if (this.server.tickCount % 10 == 0) {
            this.serverLevel().chunkSource.move(this)
        }
        super.tick()

        this.server.tell(TickTask(this.server.tickCount) {
            // All player actions should be handled in the packet phase
            this.actions.tick()
        })
    }

    override fun tickDeath() {
        this.connection.handleClientCommand(
            ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN)
        )
    }

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {
        this.doCheckFallDamage(0.0, y, 0.0, onGround)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        if (compound.contains("fake_actions", Tag.TAG_COMPOUND)) {
            this.actions.deserialize(compound.getCompound("fake_actions"))
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.put("fake_actions", this.actions.serialize())
    }

    override fun die(source: DamageSource) {
        super.die(source)
    }

    override fun showEndCredits() {
        this.seenCredits = true
    }

    companion object {
        private val joining = Object2ObjectOpenHashMap<String, CompletableFuture<FakePlayer>>()

        fun join(server: MinecraftServer, profile: GameProfile): FakePlayer {
            val player = FakePlayer(server, server.overworld(), profile)
            player.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, 0x7F)
            val connection = FakeConnection()
            server.playerList.placeNewPlayer(
                connection, player, CommonListenerCookie(profile, 0, player.clientInformation(), false)
            )
            server.connection.connections.add(connection)
            return player
        }

        fun join(server: MinecraftServer, username: String): CompletableFuture<FakePlayer> {
            return this.joining.getOrPut(username) {
                ResolvableProfile(username).resolve().whenCompleteAsync({ _, throwable ->
                    this.joining.remove(username)
                    if (throwable != null) {
                        FakePlayers.logger.error("Fake player $username failed to join", throwable)
                    }
                }, server).thenApply { resolved ->
                    this.join(server, resolved.gameProfile)
                }
            }
        }

        fun isJoining(username: String): Boolean {
            return this.joining.containsKey(username)
        }
    }
}