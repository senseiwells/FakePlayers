package me.senseiwells.puppet.action

import com.mojang.serialization.Codec
import net.minecraft.util.StringRepresentable

/**
 * Enum representing a generic action modifier.
 */
enum class ActionModifier: StringRepresentable {
    /**
     * Run the action exactly once.
     */
    Once,

    /**
     * Hold the action (continuously run it).
     */
    Hold,

    /**
     * Release the action.
     */
    Release;

    override fun getSerializedName(): String {
        return this.name.lowercase()
    }

    companion object {
        val CODEC: Codec<ActionModifier> = StringRepresentable.fromEnum(ActionModifier::values)
    }
}