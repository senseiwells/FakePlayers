package me.senseiwells.players.utils

import me.senseiwells.players.FakePlayers
import me.senseiwells.players.action.FakePlayerAction
import me.senseiwells.players.action.FakePlayerActionProvider
import net.casual.arcade.utils.registries.RegistryKeySupplier
import net.casual.arcade.utils.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

object FakePlayerRegistryKeys: RegistryKeySupplier(FakePlayers.MOD_ID) {
    @JvmField
    val ACTION_PROVIDERS: ResourceKey<Registry<FakePlayerActionProvider>> = create("action_providers")
}

object FakePlayerRegistries: RegistrySupplier() {
    @JvmField
    val ACTION_PROVIDERS: Registry<FakePlayerActionProvider> = create(FakePlayerRegistryKeys.ACTION_PROVIDERS, FakePlayerAction::bootstrap)
}