package ru.voidpointer.paper.voidemoji.resourcepack;

import co.aikar.commands.lib.expiringmap.internal.NamedThreadFactory;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ru.voidpointer.paper.voidemoji.config.loader.ConfigLoader;
import ru.voidpointer.paper.voidemoji.config.reload.AutoReloadConfigService;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public final class PackFileServer {
    private final Set<InetAddress> allowedConnections = ConcurrentHashMap.newKeySet();
    private final ResourcePack resourcePack;
    private @NotNull ResourcePackConfig config;
    private final Logger log;
    private final ReadWriteLock configRwl = new ReentrantReadWriteLock();

    private HttpServer httpServer;

    public PackFileServer(
            final @NotNull Logger log,
            final @NotNull File dataFolder,
            final @NotNull AutoReloadConfigService reloadService,
            final @NotNull Function<Path, Boolean> saveResource
    ) {
        this.log = log;
        var configLoader = new ConfigLoader<>(dataFolder, ResourcePackConfig.class);
        this.config = configLoader.loadAndSaveDefaultIfNotExists();
        this.resourcePack = new ResourcePack(log, dataFolder, config.distribution(), saveResource);
        reloadService.ifSubscribable(() -> reloadService.subscribeToReload(configLoader, this::updateConfig));
    }

    public Optional<Exception> start() {
        // TODO start event
        try {
            long startMillis = System.currentTimeMillis();
            var httpConfig = config().http();
            if (httpServer != null)
                httpServer.stop(httpConfig.stopDelayInSeconds());
            httpServer = HttpServer.create(httpConfig.inetAddress(), httpConfig.backlog());
            httpServer.createContext("/", this::handleResourcePackRequest);
            httpServer.setExecutor(Executors.newFixedThreadPool(
                    httpConfig.workerThreads(), new NamedThreadFactory("HttpPackThread#%d")
            ));
            httpServer.start();
            log.info("HTTP resource pack hosting server started at {} in {}s",
                    httpServer.getAddress(), (System.currentTimeMillis() - startMillis) / 1000.);
        } catch (final IOException ioException) {
            return Optional.of(ioException);
        }
        return Optional.empty();
    }

    public void shutdown() {
        // TODO stop event
        if (httpServer != null)
            httpServer.stop(config().http().stopDelayInSeconds());
    }

    private void handleResourcePackRequest(final HttpExchange request) {
        // TODO cancellable request event
        var config = config();
        var remoteAddress = request.getRemoteAddress().getAddress();
        if (config.firewall().enabled() && !allowedConnections.contains(remoteAddress)) {
            request.close();
            return;
        }
        try (request) {
            // TODO optionally send chunked instead of full in one go
            //  (viable thanks to the ChunkedOutputStream if content-length = 0)
            var packStream = resourcePack.getBytes();
            if (packStream == null) {
                request.sendResponseHeaders(404, -1);
                return;
            }
            request.getResponseHeaders().set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + config.distribution().packFilename());
            request.sendResponseHeaders(200, packStream.length);
            request.getResponseBody().write(packStream);
        } catch (final IOException ioException) {
            log.error("Could not process resource pack request from {}: {}", remoteAddress, ioException.getMessage());
        }
    }

    public void allowConnections(final InetAddress inetAddress) {
        allowedConnections.add(inetAddress);
    }

    public void disallowConnections(final InetAddress inetAddress) {
        allowedConnections.remove(inetAddress);
    }

    public void updateConfig(final ResourcePackConfig config) {
        if (config != null) {
            configRwl.writeLock().lock();
            try {
                this.config = config;
                resourcePack.updateDistributionSettings(config.distribution());
            } finally {
                configRwl.writeLock().unlock();
            }
        }
    }

    private ResourcePackConfig config() {
        configRwl.readLock().lock();
        try {
            return config;
        } finally {
            configRwl.readLock().unlock();
        }
    }
}
