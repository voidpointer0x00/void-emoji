package ru.voidpointer.paper.voidemoji;

import org.bukkit.plugin.java.JavaPlugin;
import ru.voidpointer.paper.voidemoji.config.EmojiConfig;
import ru.voidpointer.paper.voidemoji.config.loader.ConfigLoader;
import ru.voidpointer.paper.voidemoji.config.reload.AutoReloadConfigService;
import ru.voidpointer.paper.voidemoji.listener.ChatListener;

@SuppressWarnings("unused")
public final class VoidEmoji extends JavaPlugin {
    private EmojiConfig emojiConfig;
    private AutoReloadConfigService autoReloadConfigService;

    private ChatListener chatListener;

    @Override public void onLoad() {
        var emojiConfigLoader = new ConfigLoader<>(this, EmojiConfig.class);
        emojiConfig = emojiConfigLoader.loadAndSaveDefaultIfNotExists();

        autoReloadConfigService = new AutoReloadConfigService();
        if (autoReloadConfigService.startWatchingForModifications(getDataFolder().toPath().getFileSystem())) {
            autoReloadConfigService.subscribeToReload(emojiConfigLoader, updated -> {
                if (updated != null && chatListener != null) {
                    chatListener.setEmojiConfig(updated);
                    getSLF4JLogger().info("Automatically reloaded emoji config");
                } else {
                    getSLF4JLogger().warn(
                            "Could not update automatically reloaded emoji config:" +
                                    " instance == null -> {}; chatListener == null -> {}",
                            updated == null, chatListener == null
                    );
                }
            });
        }
    }

    @Override public void onEnable() {
        chatListener = new ChatListener(emojiConfig).register(this);
    }

    @Override public void onDisable() {
        if (autoReloadConfigService != null)
            autoReloadConfigService.shutdown();
    }
}
