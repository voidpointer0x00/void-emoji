package ru.voidpointer.paper.voidemoji.config;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ConfigSerializable
public class EmojiConfig {
    private Map<String, Emoji> emojis;

    public EmojiConfig() {
        emojis = ImmutableMap.of(
                ":heart:", new Emoji("<3")
        );
    }

    private transient Map<String, Emoji> unmodifiableEmojis;
    private final transient ReadWriteLock cachedEmojisLock = new ReentrantReadWriteLock();

    public Map<String, Emoji> getEmojis() {
        try {
            cachedEmojisLock.readLock().lock();
            if (unmodifiableEmojis == null) {
                cachedEmojisLock.readLock().unlock();
                cachedEmojisLock.writeLock().lock();
                try {
                    if (unmodifiableEmojis == null)
                        unmodifiableEmojis = Collections.unmodifiableMap(emojis);
                    cachedEmojisLock.readLock().lock();
                } finally {
                    cachedEmojisLock.writeLock().unlock();
                }
            }
            return unmodifiableEmojis;
        } finally {
            cachedEmojisLock.readLock().unlock();
        }
    }
}
