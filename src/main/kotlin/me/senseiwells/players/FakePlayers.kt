package me.senseiwells.players

import me.senseiwells.players.command.FakePlayerCommand
import me.senseiwells.players.utils.FakePlayerRegistries
import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object FakePlayers: ModInitializer {
    const val MOD_ID = "fake-players"

    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        FakePlayerRegistries.load()

        GlobalEventHandler.register<ServerRegisterCommandEvent> { event ->
            event.register(FakePlayerCommand)
        }
    }
}