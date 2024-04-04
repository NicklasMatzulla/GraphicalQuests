/*
 * MIT License
 *
 * Copyright (c) 2024 Nicklas Matzulla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.nicklasmatzulla.graphicalquests.command;

import de.nicklasmatzulla.graphicalquests.config.GuiConfig;
import de.nicklasmatzulla.graphicalquests.config.MessagesConfig;
import de.nicklasmatzulla.graphicalquests.config.QuestsConfig;
import de.nicklasmatzulla.graphicalquests.gui.QuestsGui;
import de.nicklasmatzulla.graphicalquests.gui.RecipeBookGui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuestsCommand extends Command {

    private static final String RELOAD_PERMISSION = "graphicalquests.reload";

    private final MessagesConfig messagesConfig;
    private final QuestsConfig questsConfig;
    private final GuiConfig guiConfig;

    public QuestsCommand(final @NotNull MessagesConfig messagesConfig, final @NotNull QuestsConfig questsConfig, final @NotNull GuiConfig guiConfig) {
        super("quests");
        this.messagesConfig = messagesConfig;
        this.questsConfig = questsConfig;
        this.guiConfig = guiConfig;
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        if (strings.length > 0 && strings[0].equalsIgnoreCase("reload") && commandSender.hasPermission(RELOAD_PERMISSION)) {
            Bukkit.clearRecipes();
            this.messagesConfig.load();
            this.messagesConfig.init();
            this.guiConfig.load();
            this.guiConfig.init();
            this.questsConfig.load();
            this.questsConfig.init();
            Bukkit.getOnlinePlayers().forEach(player -> RecipeBookGui.updateRecipeBook(this.questsConfig, player));
            final Component reloadedComponent = this.messagesConfig.getReloadedComponent();
            commandSender.sendMessage(reloadedComponent);
            return true;
        }
        if (!(commandSender instanceof final Player player)) {
            final Component onlyPlayersComponent = this.messagesConfig.getOnlyPlayersComponent();
            commandSender.sendMessage(onlyPlayersComponent);
            return true;
        }
        QuestsGui.openQuestsGui(this.messagesConfig, this.questsConfig, this.guiConfig, player);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (sender.hasPermission(RELOAD_PERMISSION) && args.length == 1) {
            return List.of("reload");
        }
        return List.of();
    }
}
