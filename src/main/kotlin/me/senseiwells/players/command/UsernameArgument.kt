package me.senseiwells.players.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.senseiwells.players.FakePlayer
import net.casual.arcade.commands.type.CustomArgumentType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.util.StringUtil
import java.util.concurrent.CompletableFuture

class UsernameArgument: CustomArgumentType<String>() {
    override fun parse(reader: StringReader): String {
        val name = reader.readUnquotedString()
        if (!StringUtil.isValidPlayerName(name)) {
            throw INVALID_USERNAME.create(name)
        }
        return name
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return suggestFakePlayers(context, builder)
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

        fun suggestFakePlayers(context: CommandContext<*>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            val source = context.source
            if (source is CommandSourceStack) {
                val names = source.server.playerList.players.filterIsInstance<FakePlayer>().map { it.scoreboardName }
                return SharedSuggestionProvider.suggest(names, builder)
            }
            return Suggestions.empty()
        }
    }
}