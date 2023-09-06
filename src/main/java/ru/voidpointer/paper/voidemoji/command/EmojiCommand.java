package ru.voidpointer.paper.voidemoji.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import ru.voidpointer.paper.voidemoji.locale.VoidLocales;

@CommandAlias("emoji|void-emoji|e|ve")
public final class EmojiCommand extends BaseCommand {
    @Dependency private VoidLocales locale;

    @HelpCommand
    @Description("{@@desc-help}")
    @CommandPermission("void-emoji.help")
    void help(final CommandHelp commandHelp) {
        commandHelp.showHelp();
    }
}
