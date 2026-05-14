package com.azerl0.gutitubow.command.argument;

import com.azerl0.gutitubow.gameevent.GameEventType;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class GameEventTypeArgument implements CustomArgumentType<@NotNull GameEventType, @NotNull String> {

    private static final DynamicCommandExceptionType ERROR_INVALID_GAMEEVENT_TYPE =
            new DynamicCommandExceptionType(name ->
                    MessageComponentSerializer.message().serialize(Component.text(name + "というイベントは存在しません")));


    @Override
    public GameEventType parse(StringReader reader) throws CommandSyntaxException {
        try {
            return GameEventType.valueOf(reader.readUnquotedString().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ERROR_INVALID_GAMEEVENT_TYPE.create(reader.readUnquotedString());
        }
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext context, @NotNull SuggestionsBuilder builder) {
        for (GameEventType eventType : GameEventType.values()) {
            String name = eventType.name().toLowerCase();
            builder.suggest(name);
        }
        return builder.buildFuture();
    }
}
