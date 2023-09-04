package ru.voidpointer.paper.voidemoji.config;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ConfigSerializable
public class EmojiConfig {
    @SuppressWarnings("FieldMayBeFinal") /* implicitly written via reflection */
    private Map<String, Emoji> emojis = ImmutableMap.of(
            ":heart:", new Emoji("❤"),
            ":smile:", new Emoji("\uD83D\uDE04"),
            ":clown:", new Emoji("\uD83E\uDD21"),
            ":kekw:", new Emoji("\uE000"),
            ":sadcat:", new Emoji("\uE001"),
            ":pepeclown:", new Emoji("\uE002"),
            ":kekwait:", new Emoji("\uE003"),
            ":listening:", new Emoji("\uE004"),
            ":smirk:", new Emoji("\uE005"),
            ":catsmirk:", new Emoji("\uE006")
    );

    private transient Map<String, Emoji> emojisView;
    private final transient ReadWriteLock cachedEmojisLock = new ReentrantReadWriteLock();

    @SuppressWarnings("DuplicatedCode")
    public Map<String, Emoji> getEmojis() {
        try {
            cachedEmojisLock.readLock().lock();
            if (emojisView == null) {
                cachedEmojisLock.readLock().unlock();
                cachedEmojisLock.writeLock().lock();
                try {
                    if (emojisView == null)
                        emojisView = Collections.unmodifiableMap(emojis);
                    cachedEmojisLock.readLock().lock();
                } finally {
                    cachedEmojisLock.writeLock().unlock();
                }
            }
            return emojisView;
        } finally {
            cachedEmojisLock.readLock().unlock();
        }
    }
}
