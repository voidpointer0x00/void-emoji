package ru.voidpointer.paper.voidemoji.command.acf;

import co.aikar.commands.BukkitMessageFormatter;
import net.jcip.annotations.ThreadSafe;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

@ThreadSafe
public final class MiniMessageFormatter extends BukkitMessageFormatter {
    private static final class InstanceHolder { /* thread-safe singleton */
        private static final MiniMessageFormatter INSTANCE = new MiniMessageFormatter();
    }

    public static MiniMessageFormatter formatter() {
        return InstanceHolder.INSTANCE;
    }

    @Override public String format(int index, String message) {
        return format(message);
    }

    @Override public String format(String message) {
        return legacySection().serialize(miniMessage().deserialize(message));
    }
}
