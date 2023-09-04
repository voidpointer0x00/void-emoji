package ru.voidpointer.paper.voidemoji.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.voidpointer.paper.voidemoji.config.EmojiConfig;

@RequiredArgsConstructor
public final class ChatListener implements Listener {
    @Setter private @NotNull EmojiConfig emojiConfig;

    @Contract("_ -> this")
    public @NotNull ChatListener register(@NotNull final Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        return this;
    }

    @SuppressWarnings("CodeBlock2Expr") /* single line lambda warning */
    @EventHandler(priority = EventPriority.HIGH)
    void translateEmojis(final AsyncChatEvent event) {
        event.message(event.message().replaceText(builder -> {
            emojiConfig.getEmojis().forEach((literal, emoji) -> {
                builder.matchLiteral(literal).replacement(emoji.getGlyph());
            });
        }));
    }
}
