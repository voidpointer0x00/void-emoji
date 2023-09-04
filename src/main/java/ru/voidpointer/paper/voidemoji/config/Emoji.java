package ru.voidpointer.paper.voidemoji.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@ConfigSerializable
public class Emoji {
    @NotNull private String glyph;
}
