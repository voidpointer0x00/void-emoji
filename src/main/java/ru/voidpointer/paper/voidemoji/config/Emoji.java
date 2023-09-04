package ru.voidpointer.paper.voidemoji.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Data
@ConfigSerializable
@RequiredArgsConstructor
public class Emoji {
    @NotNull private String glyph;
}
