package me.senseiwells.puppet.action

import com.mojang.serialization.Codec
import net.minecraft.util.StringRepresentable

enum class ActionModifier: StringRepresentable {
    Once, Hold, Release;

    override fun getSerializedName(): String {
        return this.name.lowercase()
    }

    companion object {
        val CODEC: Codec<ActionModifier> = StringRepresentable.fromEnum(ActionModifier::values)
    }
}