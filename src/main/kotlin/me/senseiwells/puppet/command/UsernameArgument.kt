package me.senseiwells.puppet.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.casual.arcade.commands.type.CustomArgumentType
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.util.StringUtil

class UsernameArgument: CustomArgumentType<String>() {
    override fun parse(reader: StringReader): String {
        val name = reader.readUnquotedString()
        if (!StringUtil.isValidPlayerName(name)) {
            throw INVALID_USERNAME.create(name)
        }
        return name
    }

    override fun getSuggestionProvider(): SuggestionProvider<SharedSuggestionProvider>? {
        return null
    }

    companion object {
        private val INVALID_USERNAME = DynamicCommandExceptionType { Component.literal("$it is not a valid username") }

        @JvmStatic
        fun username(): UsernameArgument {
            return UsernameArgument()
        }

        @JvmStatic
        fun getUsername(context: CommandContext<*>, string: String): String {
            return context.getArgument(string, String::class.java)
        }
    }
}