package wiki.minecraft.heywiki.command.suggestion;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CommandNameSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    static Set<String> getCommands(CommandDispatcher<?> dispatcher) {
        return dispatcher.getRoot().getChildren().stream().map(CommandNode::getName).collect(Collectors.toSet());
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(getCommands(Objects.requireNonNull(context.getSource().getClient().getNetworkHandler()).getCommandDispatcher()), builder);
    }
}