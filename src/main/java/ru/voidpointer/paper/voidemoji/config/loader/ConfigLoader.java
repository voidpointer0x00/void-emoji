package ru.voidpointer.paper.voidemoji.config.loader;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.function.Supplier;

@Data
@Slf4j
public class ConfigLoader<ConfigT> {
    private final @NotNull Path destination;
    private final @NotNull Class<ConfigT> configClass;
    private final @NotNull Supplier<ConfigT> defaultSupplier;
    private final @Nullable Supplier<Boolean> saveAsResource;

    /**
     * @throws IllegalArgumentException if the default constructor of the configuration class
     *      does not exist or is not accessible from this class.
     */
    public ConfigLoader(Path pathToDataFolder, Class<ConfigT> configClass) throws IllegalArgumentException {
        this(pathToDataFolder, configClass, null);
    }

    /**
     * @throws IllegalArgumentException if the default constructor of the configuration class
     *      does not exist or is not accessible from this class.
     */
    public ConfigLoader(Plugin plugin, Class<ConfigT> configClass) throws IllegalArgumentException {
        this(plugin.getDataFolder().toPath(), configClass, () -> {
            String filename = configClass.getSimpleName().replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase() + ".yml";
            try {
                plugin.saveResource(filename, false);
                return true;
            } catch (final IllegalArgumentException illegalArgumentException) {
                log.error("Could not save default {} from plugin resources: {}",
                        filename, illegalArgumentException.getMessage());
                return false;
            }
        });
    }

    /**
     * @throws IllegalArgumentException if the default constructor of the configuration class
     *      does not exist or is not accessible from this class.
     */
    private ConfigLoader(
            @NotNull Path pathToDataFolder,
            @NotNull Class<ConfigT> configClass,
            @Nullable Supplier<Boolean> saveAsResource
    ) throws IllegalArgumentException {
        this.destination = pathToDataFolder.resolve(
                configClass.getSimpleName().replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase() + ".yml");
        this.configClass = configClass;
        this.saveAsResource = saveAsResource;

        final Constructor<ConfigT> constructor;
        try {
            constructor = configClass.getConstructor();
        } catch (final NoSuchMethodException defaultConstructorUndefined) {
            throw new IllegalArgumentException(configClass.getName() + " does not declare a default constructor");
        }
        if (!constructor.canAccess(null))
            throw new IllegalArgumentException(configClass.getName() + "'s default constructor must be accessible");

        this.defaultSupplier = () -> {
            try {
                return constructor.newInstance();
            } catch (final InstantiationException ex) {
                log.error("Could not instantiate an abstract config " + configClass.getName(), ex);
            } catch (final IllegalAccessException ex) {
                log.error("Could not access default constructor of " + configClass.getName(), ex);
            } catch (final InvocationTargetException ex) {
                log.error("Config class constructor threw an exception " + configClass.getName(), ex);
            }
            return null;
        };
    }

    public ConfigT loadAndSaveDefaultIfNotExists() {
        return destination.toFile().exists() || (saveAsResource != null && saveAsResource.get())
                ? YamlConfigLoader.load(destination, configClass, defaultSupplier)
                : YamlConfigLoader.loadAndSave(destination, configClass, defaultSupplier);
    }

    public void save(final ConfigT configT) {
        YamlConfigLoader.save(destination, configT);
    }
}
