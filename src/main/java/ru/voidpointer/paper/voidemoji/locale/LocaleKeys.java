package ru.voidpointer.paper.voidemoji.locale;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static co.aikar.locales.MessageKey.of;

@Getter
@AllArgsConstructor
public enum LocaleKeys implements MessageKeyProvider {
    SENDING_RESOURCE_PACK("<green> <b>»</b> </green><white>Sending resource pack update.</white>"),
    MISSING_TRANSLATION("<red> <b>»</b> </red><white>Missing translation, please, report this incident.</white>"),
    DESC_HELP("<white>Show this message</white>"),
    PERMISSION_DENIED(
            of("acf-core.permission_denied"),
            "<red> <b>»</b> </red><white>You do not have permission for this command.</white>"
    ),
    HELP_HEADER(
            of("acf-core.help_header"),
            "<aqua> <b>» <white>Showing help for <aqua>{commandprefix}{command}"
    ),
    HELP_FORMAT(
            of("acf-core.help_format"),
            "<aqua>/{command} <dark_aqua>{parameters} <white>{separator} {description}"
    ),
    HELP_DETAILED_HEADER(
            of("acf-core.help_detailed_header"),
            "<aqua> <b>» <white>Showing detailed help for <aqua>{commandprefix}{command}"
    ),
    HELP_DETAILED_COMMAND_FORMAT(
            of("acf-core.help_detailed_command_format"),
            "<aqua>/{command} <dark_aqua>{parameters} <white>{separator} {description}"
    ),
    HELP_DETAILED_PARAMETER_FORMAT(
            of("acf-core.help_detailed_parameter_format"),
            "<aqua>{syntaxorname}<white>: {description}"
    ),
    HELP_SEARCH_HEADER(
            of("acf-core.help_search_header"),
            "<aqua> <b>» <white>Search results for <aqua>{commandprefix}{command} {search}"
    ),;

    private final MessageKey messageKey;
    private final String defaultValue;

    LocaleKeys(final String defaultValue) {
        this.messageKey = of(toString().toLowerCase().replace('_', '-'));
        this.defaultValue = defaultValue;
    }
}
