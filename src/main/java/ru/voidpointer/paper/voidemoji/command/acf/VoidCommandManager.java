package ru.voidpointer.paper.voidemoji.command.acf;

import co.aikar.commands.PaperCommandManager;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.voidpointer.paper.voidemoji.config.reload.AutoReloadConfigService;
import ru.voidpointer.paper.voidemoji.locale.VoidLocales;

@Slf4j
public class VoidCommandManager extends PaperCommandManager {
    public VoidCommandManager(final Plugin plugin, final AutoReloadConfigService reloadConfigService) {
        super(plugin);
        if (!(super.locales instanceof VoidLocales locale))
            getLocales().subscribeToLocaleReload(reloadConfigService);
        else
            locale.subscribeToLocaleReload(reloadConfigService);
        super.formatters.replaceAll((t, v) -> MiniMessageFormatter.formatter());
    }

    @Override public @NotNull VoidLocales getLocales() {
        if (super.locales instanceof VoidLocales locale)
            return locale;
        var locale = new VoidLocales(this);
        locale.loadLanguages();
        super.locales = locale;
        return locale;
    }
}
