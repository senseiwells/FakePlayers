package me.senseiwells.players.utils

import com.mojang.serialization.MapCodec
import me.senseiwells.players.FakePlayers
import me.senseiwells.players.action.FakePlayerAction
import net.casual.arcade.utils.registries.RegistryKeySupplier
import net.casual.arcade.utils.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

object FakePlayerRegistryKeys: RegistryKeySupplier(FakePlayers.MOD_ID) {
    @JvmField
    val ACTIONS: ResourceKey<Registry<MapCodec<out FakePlayerAction>>> = create("action")
}

object FakePlayerRegistries: RegistrySupplier() {
    @JvmField
    val ACTIONS: Registry<MapCodec<out FakePlayerAction>> = create(FakePlayerRegistryKeys.ACTIONS, FakePlayerAction::bootstrap)
}