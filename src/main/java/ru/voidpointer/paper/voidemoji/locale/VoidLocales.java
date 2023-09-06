package ru.voidpointer.paper.voidemoji.locale;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.BukkitLocales;
import co.aikar.commands.CommandIssuer;
import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import ru.voidpointer.paper.voidemoji.config.loader.ConfigLoader;
import ru.voidpointer.paper.voidemoji.config.reload.AutoReloadConfigService;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class VoidLocales extends BukkitLocales {
    private final BukkitCommandManager manager;
    private final ConfigLoader<VoidLocale> localeLoader;
    private VoidLocale voidLocale;
    private final ReadWriteLock localeReadWriteLock = new ReentrantReadWriteLock();

    public VoidLocales(BukkitCommandManager manager) {
        super(manager);
        this.manager = manager;
        this.localeLoader = new ConfigLoader<>(manager.getPlugin().getDataFolder(), VoidLocale.class);
    }

    public void subscribeToLocaleReload(final AutoReloadConfigService reloadConfigService) {
        reloadConfigService.subscribeToReload(localeLoader, reloaded -> {
            if (reloaded != null) {
                setVoidLocale(reloaded);
                manager.getPlugin().getSLF4JLogger().info("Automatically reloaded locale");
            }
        });
    }

    @Override public String getMessage(CommandIssuer issuer, MessageKeyProvider keyProvider) {
        return getRaw(keyProvider.getMessageKey());
    }

    @Override public String getOptionalMessage(CommandIssuer issuer, MessageKey messageKey) {
        return getVoidLocale().messages().get(messageKey.getKey());
    }

    private String getRaw(final MessageKey messageKey) {
        final String key = messageKey.getKey();
        final String message = getVoidLocale().messages().get(key);
        if (message == null) {
            manager.getPlugin().getSLF4JLogger().warn("Missing translation for {}", key);
            return LocaleKeys.MISSING_TRANSLATION.getDefaultValue();
        }
        return message;
    }

    public void send(Audience audience, LocaleKeys localeKey) {
        String key = localeKey.getMessageKey().getKey();
        String message = getVoidLocale().messages().get(key);
        if (message == null) {
            manager.getPlugin().getSLF4JLogger().warn("Missing translation for {}", key);
            message = localeKey.getDefaultValue();
        }
        audience.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    @Override public void loadLanguages() {
        setVoidLocale(localeLoader.loadAndSaveDefaultIfNotExists());
        manager.getPlugin().getSLF4JLogger().info("Locale loaded");
    }

    @Override public boolean loadLanguage(FileConfiguration config, Locale locale) {
        return true;
    }

    @Override public void loadMissingBundles() {}

    @Override public boolean loadYamlLanguageFile(File file, Locale locale) {
        return true;
    }

    @Override public boolean loadYamlLanguageFile(String file, Locale locale) {
        return true;
    }

    private void setVoidLocale(final VoidLocale locale) {
        localeReadWriteLock.writeLock().lock();
        try {
            this.voidLocale = locale;
        } finally {
            localeReadWriteLock.writeLock().unlock();
        }
    }

    private VoidLocale getVoidLocale() {
        localeReadWriteLock.readLock().lock();
        try {
            return voidLocale;
        } finally {
            localeReadWriteLock.readLock().unlock();
        }
    }
}
