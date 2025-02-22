package me.senseiwells.players.network

import com.mojang.authlib.GameProfile
import me.senseiwells.players.mixins.ServerLoginPacketListenerImplAccessor
import me.senseiwells.players.utils.asCompletableFuture
import net.fabricmc.fabric.api.networking.v1.LoginPacketSender
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions
import net.minecraft.Util
import net.minecraft.network.Connection
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerLoginPacketListenerImpl
import java.util.concurrent.CompletableFuture

class FakeLoginPacketListenerImpl(
    private val server: MinecraftServer,
    connection: Connection,
    private val profile: GameProfile
): ServerLoginPacketListenerImpl(server, connection, false) {
    init {
        @Suppress("CAST_NEVER_SUCCEEDS")
        (this as ServerLoginPacketListenerImplAccessor).setProfile(this.profile)
    }

    @Suppress("UnstableApiUsage", "CAST_NEVER_SUCCEEDS")
    fun handleQueries(): CompletableFuture<*> {
        val futures = ArrayList<CompletableFuture<*>>()
        val addon = (this as NetworkHandlerExtensions).addon as LoginPacketSender
        ServerLoginConnectionEvents.QUERY_START.invoker().onLoginStart(this, this.server, addon) {
            futures.add(it.asCompletableFuture())
        }
        return Util.sequenceFailFast(futures)
    }
}