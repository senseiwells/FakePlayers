package me.senseiwells.puppet.network

import com.mojang.authlib.GameProfile
import com.mojang.authlib.GameProfileRepository
import com.mojang.authlib.ProfileLookupCallback
import com.mojang.authlib.exceptions.MinecraftClientException
import com.mojang.authlib.minecraft.client.MinecraftClient
import java.net.Proxy
import java.net.URI
import java.util.*

class MineToolsGameProfileRepository(proxy: Proxy): GameProfileRepository {
    private val client = MinecraftClient.unauthenticated(proxy)

    private var nextQueryTime = 0L

    override fun findProfilesByNames(names: Array<out String?>, callback: ProfileLookupCallback) {
        val criteria = names.filterNotNull().filter { it.isNotEmpty() }.toSet()

        for (name in criteria) {
            val cooldown = this.nextQueryTime - System.currentTimeMillis()
            if (cooldown > 0) {
                Thread.sleep(cooldown)
            }

            this.nextQueryTime = System.currentTimeMillis() + QUERY_COOLDOWN
            val url = URI("https://api.minetools.eu/uuid/${name}").toURL()
            try {
                val profile = this.client.get(url, GameProfile::class.java)
                callback.onProfileLookupSucceeded(profile)
            } catch (e: MinecraftClientException) {
                callback.onProfileLookupFailed(name, e)
            }
        }
    }

    override fun findProfileByName(name: String): Optional<GameProfile> {
        val url = URI("https://api.minetools.eu/uuid/${name}").toURL()
        try {
            return Optional.ofNullable(this.client.get(url, GameProfile::class.java))
        } catch (_: MinecraftClientException) {

        }
        return Optional.empty()
    }

    companion object {
        private const val QUERY_COOLDOWN = 200
    }
}