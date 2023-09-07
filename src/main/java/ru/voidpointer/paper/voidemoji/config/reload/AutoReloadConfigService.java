package ru.voidpointer.paper.voidemoji.config.reload;

import lombok.extern.slf4j.Slf4j;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.reference.WatchServiceListener;
import ru.voidpointer.paper.voidemoji.config.loader.ConfigLoader;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

@Slf4j
public final class AutoReloadConfigService {
    private WatchServiceListener watchServiceListener;

    public boolean startWatchingForModifications(final FileSystem fileSystem) {
        try {
            watchServiceListener = WatchServiceListener.builder().fileSystem(fileSystem).build();
            log.info("Auto reload configuration service created.");
            return true;
        } catch (final IOException ioException) {
            log.warn("Could not start watching for config files modifications for auto reload: {}",
                    ioException.getMessage());
            return false;
        }
    }

    public void ifSubscribable(final Runnable runnable) {
        if (watchServiceListener != null)
            runnable.run();
    }

    /**
     * @throws IllegalStateException if was not yet initialized via startWatchingForModifications()
     *      or the initialization failed.
     */
    public <T> void subscribeToReload(final ConfigLoader<T> configLoader, final Consumer<T> onReload)
            throws IllegalStateException {
        if (watchServiceListener == null)
            throw new IllegalStateException("The watch service listener was not initialized");
        try {
            watchServiceListener.listenToFile(configLoader.getDestination(), (watchEvent) -> {
                if (watchEvent.kind().equals(ENTRY_DELETE) || watchEvent.kind().equals(ENTRY_MODIFY)) {
                    log.trace("{} was deleted or modified, reloading", configLoader.getDestination());
                    T loadedConfig = configLoader.loadAndSaveDefaultIfNotExists();
                    onReload.accept(loadedConfig);
                }
            });
            log.info("{} was registered for an auto reload action.", configLoader.getDestination());
        } catch (final ConfigurateException ex) {
            log.error("Could not subscribe to updates of " + configLoader.getDestination().toAbsolutePath(), ex);
        }
    }

    public void shutdown() {
        try {
            if (watchServiceListener != null) {
                watchServiceListener.close();
                watchServiceListener = null;
            }
        } catch (final IOException ioException) {
            log.error("Could not close auto reload config service: {}", ioException.getMessage());
        }
    }
}
