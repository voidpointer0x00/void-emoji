package ru.voidpointer.paper.voidemoji;

import org.bukkit.plugin.java.JavaPlugin;
import ru.voidpointer.paper.voidemoji.command.EmojiCommand;
import ru.voidpointer.paper.voidemoji.command.acf.VoidCommandManager;
import ru.voidpointer.paper.voidemoji.config.EmojiConfig;
import ru.voidpointer.paper.voidemoji.config.loader.ConfigLoader;
import ru.voidpointer.paper.voidemoji.config.reload.AutoReloadConfigService;
import ru.voidpointer.paper.voidemoji.listener.ChatListener;
import ru.voidpointer.paper.voidemoji.locale.VoidLocales;

@SuppressWarnings("unused")
public final class VoidEmoji extends JavaPlugin {
    private EmojiConfig emojiConfig;
    private AutoReloadConfigService reloadConfigService;

    private ChatListener chatListener;

    @Override public void onLoad() {
        var emojiConfigLoader = new ConfigLoader<>(getDataFolder(), EmojiConfig.class);
        emojiConfig = emojiConfigLoader.loadAndSaveDefaultIfNotExists();

        reloadConfigService = new AutoReloadConfigService();
        if (reloadConfigService.startWatchingForModifications(getDataFolder().toPath().getFileSystem())) {
            reloadConfigService.subscribeToReload(emojiConfigLoader, updated -> {
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

        var cmdManager = new VoidCommandManager(this, reloadConfigService);
        cmdManager.registerDependency(VoidLocales.class, cmdManager.getLocales());
        cmdManager.enableUnstableAPI("help");
        cmdManager.registerCommand(new EmojiCommand());
    }

    @Override public void onDisable() {
        if (reloadConfigService != null)
            reloadConfigService.shutdown();
    }
}
