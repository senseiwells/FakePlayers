package me.senseiwells.players.network

import com.mojang.authlib.GameProfile
import com.mojang.authlib.GameProfileRepository
import com.mojang.authlib.ProfileLookupCallback
import com.mojang.authlib.exceptions.MinecraftClientException
import com.mojang.authlib.minecraft.client.MinecraftClient
import java.net.Proxy
import java.net.URI

class MineToolsGameProfileRepository(proxy: Proxy): GameProfileRepository {
    private val client = MinecraftClient.unauthenticated(proxy)

    override fun findProfilesByNames(names: Array<out String?>, callback: ProfileLookupCallback) {
        val criteria = names.filterNotNull().filter { it.isNotEmpty() }.toSet()

        for (name in criteria) {
            val url = URI("https://api.minetools.eu/uuid/${name}").toURL()
            try {
                val profile = this.client.get(url, GameProfile::class.java)
                callback.onProfileLookupSucceeded(profile)
            } catch (e: MinecraftClientException) {
                callback.onProfileLookupFailed(name, e)
            }
        }
    }
}