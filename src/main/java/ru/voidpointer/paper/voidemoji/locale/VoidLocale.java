package ru.voidpointer.paper.voidemoji.locale;

import lombok.NoArgsConstructor;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@NoArgsConstructor
@ConfigSerializable
public final class VoidLocale {
    @Setting private Map<String, String> messages = new ConcurrentHashMap<>() {{
        for (final LocaleKeys key : LocaleKeys.values())
            put(key.getMessageKey().getKey(), key.getDefaultValue());
    }};

    private transient Map<String, String> messagesView;
    private final transient ReadWriteLock cachedMessagesLock = new ReentrantReadWriteLock();

    @SuppressWarnings("DuplicatedCode")
    public Map<String, String> messages() {
        cachedMessagesLock.readLock().lock();
        try {
            if (messagesView == null) {
                cachedMessagesLock.readLock().unlock();
                cachedMessagesLock.writeLock().lock();
                try {
                    if (messagesView == null) /* recheck cause one could update between lock exchange */
                        messagesView = Collections.unmodifiableMap(messages);
                    cachedMessagesLock.readLock().lock();
                } finally {
                    cachedMessagesLock.writeLock().unlock();
                }
            }
            return messagesView;
        } finally {
            cachedMessagesLock.readLock().unlock();
        }
    }
}
