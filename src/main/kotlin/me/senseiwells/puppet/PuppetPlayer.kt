package me.senseiwells.puppet

import com.mojang.authlib.GameProfile
import me.senseiwells.puppet.action.PuppetPlayerActions
import me.senseiwells.puppet.network.PuppetGamePacketListenerImpl
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

class PuppetPlayer @Internal constructor(
    server: MinecraftServer,
    level: ServerLevel,
    profile: GameProfile
): FakePlayer(server, level, profile) {
    val actions = PuppetPlayerActions(this)

    override fun createConnection(
        server: MinecraftServer,
        connection: Connection,
        cookie: CommonListenerCookie
    ): FakeGamePacketListenerImpl {
        return PuppetGamePacketListenerImpl(server, connection, this, cookie)
    }

    override fun createRespawned(server: MinecraftServer, level: ServerLevel, profile: GameProfile): FakePlayer {
        return PuppetPlayer(server, level, profile)
    }

    override fun connection(): PuppetGamePacketListenerImpl {
        return this.connection as PuppetGamePacketListenerImpl
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
        val packed = compound.read("fake_actions", PuppetPlayerActions.Packed.CODEC).getOrNull()
        if (packed != null) {
            this.actions.unpack(packed)
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.store("fake_actions", PuppetPlayerActions.Packed.CODEC, this.actions.pack())
    }
}