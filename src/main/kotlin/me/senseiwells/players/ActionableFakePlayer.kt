package me.senseiwells.players

import com.mojang.authlib.GameProfile
import me.senseiwells.players.action.FakePlayerActions
import me.senseiwells.players.network.ActionableFakeGamePacketListenerImpl
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.network.FakeGamePacketListenerImpl
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Connection
import net.minecraft.server.MinecraftServer
import net.minecraft.server.TickTask
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.network.CommonListenerCookie
import org.jetbrains.annotations.ApiStatus.Internal
import kotlin.jvm.optionals.getOrNull

class ActionableFakePlayer @Internal constructor(
    server: MinecraftServer,
    level: ServerLevel,
    profile: GameProfile
): FakePlayer(server, level, profile) {
    val actions = FakePlayerActions(this)

    override fun createConnection(
        server: MinecraftServer,
        connection: Connection,
        cookie: CommonListenerCookie
    ): FakeGamePacketListenerImpl {
        return ActionableFakeGamePacketListenerImpl(server, connection, this, cookie)
    }

    override fun createRespawned(server: MinecraftServer, level: ServerLevel, profile: GameProfile): FakePlayer {
        return ActionableFakePlayer(server, level, profile)
    }

    override fun connection(): ActionableFakeGamePacketListenerImpl {
        return this.connection as ActionableFakeGamePacketListenerImpl
    }

    override fun tick() {
        super.tick()

        this.server.schedule(TickTask(this.server.tickCount) {
            // All player actions should be handled in the packet phase
            this.actions.tick()
        })
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        val packed = compound.read("fake_actions", FakePlayerActions.Packed.CODEC).getOrNull()
        if (packed != null) {
            this.actions.unpack(packed)
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.store("fake_actions", FakePlayerActions.Packed.CODEC, this.actions.pack())
    }
}