package ru.voidpointer.paper.voidemoji.resourcepack;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ru.voidpointer.paper.voidemoji.resourcepack.ResourcePackConfig.DistributionSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class ResourcePack {
    private final @NotNull Logger log;
    private final @NotNull File dataFolder;
    private final @NotNull Function<Path, Boolean> saveResource;
    private DistributionSettings distributionSettings;
    private final ReadWriteLock distributionRwl = new ReentrantReadWriteLock();
    private volatile byte[] packBytes;

    ResourcePack(
            final @NotNull Logger log,
            final @NotNull File dataFolder,
            final @NotNull DistributionSettings distributionSettings,
            final @NotNull Function<Path, Boolean> saveResource
    ) {
        this.log = log;
        this.dataFolder = dataFolder;
        this.saveResource = saveResource;
        updateDistributionSettings(distributionSettings);
    }

    public byte[] getBytes() {
        return packBytes;
    }

    void updateDistributionSettings(final @NotNull DistributionSettings distributionSettings) {
        distributionRwl.writeLock().lock();
        try {
            // TODO make caching optional; cache only for a certain period
            final String packFilename = distributionSettings.packFilename();
            final boolean shouldUpdateCache = this.distributionSettings == null
                    || !this.distributionSettings.packFilename().equals(packFilename);
            if (shouldUpdateCache) {
                final Path pathToResourcePack = dataFolder.toPath().resolve(packFilename);
                if (pathToResourcePack.toFile().exists() || saveResource.apply(Path.of(packFilename)))
                    packBytes = Files.readAllBytes(pathToResourcePack);
                else
                    log.warn("Resource pack file {} does not exist!", pathToResourcePack);
            }
            this.distributionSettings = distributionSettings;
        } catch (final IOException ioException) {
            log.error("Could not update resource pack on distribution settings update: {}", ioException.getMessage());
        } finally {
            distributionRwl.writeLock().unlock();
        }
    }

    private @NotNull DistributionSettings distributionSettings() {
        distributionRwl.readLock().lock();
        try {
            return distributionSettings;
        } finally {
            distributionRwl.readLock().unlock();
        }
    }
}
