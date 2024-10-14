package me.senseiwells.players.utils

import com.mojang.authlib.properties.PropertyMap
import net.minecraft.world.item.component.ResolvableProfile
import java.util.*

fun ResolvableProfile(name: String): ResolvableProfile {
    return ResolvableProfile(Optional.of(name), Optional.empty(), PropertyMap())
}

fun ResolvableProfile(uuid: UUID): ResolvableProfile {
    return ResolvableProfile(Optional.empty(), Optional.of(uuid), PropertyMap())
}
