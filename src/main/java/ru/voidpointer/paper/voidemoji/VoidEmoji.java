package ru.voidpointer.paper.voidemoji;

import org.bukkit.plugin.java.JavaPlugin;
import ru.voidpointer.paper.voidemoji.command.EmojiCommand;
import ru.voidpointer.paper.voidemoji.command.acf.VoidCommandManager;
import ru.voidpointer.paper.voidemoji.config.EmojiConfig;
import ru.voidpointer.paper.voidemoji.config.loader.ConfigLoader;
import ru.voidpointer.paper.voidemoji.config.reload.AutoReloadConfigService;
import ru.voidpointer.paper.voidemoji.listener.ChatListener;
import ru.voidpointer.paper.voidemoji.locale.VoidLocales;
import ru.voidpointer.paper.voidemoji.resourcepack.PackFileServer;

import java.nio.file.Path;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("unused")
public final class VoidEmoji extends JavaPlugin {
    private EmojiConfig emojiConfig;
    private AutoReloadConfigService reloadConfigService;

    private volatile PackFileServer packFileServer;
    private final ReadWriteLock packFileServerRwl = new ReentrantReadWriteLock();

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
        /* start resource pack hosting server in another thread to prevent halt on MC server startup */
        new Thread(() -> {
            packFileServerRwl.writeLock().lock();
            try {
                packFileServer = new PackFileServer(
                        getSLF4JLogger(), getDataFolder(), reloadConfigService, this::saveResource);
                packFileServer.start().ifPresent(startupException -> {
                    getSLF4JLogger().error(
                            "Resource pack hosting http server startup failed, please, inspect the following" +
                                    " reason and reload. Otherwise players won't be able to download your" +
                                    " resource pack",
                            startupException
                    );
                });
            } finally {
                packFileServerRwl.writeLock().unlock();
            }
        }, "PackHttpServerStarter").start();
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
        packFileServerRwl.readLock().lock();
        try {
            if (packFileServer != null)
                packFileServer.shutdown();
        } finally {
            packFileServerRwl.readLock().unlock();
        }
    }

    private boolean saveResource(final Path path) {
        try {
            saveResource(path.toString(), false);
            return path.toFile().exists();
        } catch (final Exception resourceNotFound) {
            getSLF4JLogger().warn("Resource {} not found: {}", path, resourceNotFound.getMessage());
            return false;
        }
    }
}
