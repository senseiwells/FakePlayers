package me.senseiwells.puppet.utils

import me.senseiwells.puppet.PuppetPlayers
import me.senseiwells.puppet.action.PuppetPlayerAction
import me.senseiwells.puppet.action.PuppetPlayerActionProvider
import net.casual.arcade.utils.registries.RegistryKeySupplier
import net.casual.arcade.utils.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

object PuppetPlayerRegistryKeys: RegistryKeySupplier(PuppetPlayers.MOD_ID) {
    @JvmField
    val ACTION_PROVIDERS: ResourceKey<Registry<PuppetPlayerActionProvider>> = create("action_providers")
}

object PuppetPlayerRegistries: RegistrySupplier() {
    @JvmField
    val ACTION_PROVIDERS: Registry<PuppetPlayerActionProvider> = create(PuppetPlayerRegistryKeys.ACTION_PROVIDERS, PuppetPlayerAction::bootstrap)
}