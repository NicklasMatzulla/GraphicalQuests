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

package de.nicklasmatzulla.graphicalquests;

import de.nicklasmatzulla.graphicalquests.command.QuestsCommand;
import de.nicklasmatzulla.graphicalquests.config.GuiConfig;
import de.nicklasmatzulla.graphicalquests.config.MessagesConfig;
import de.nicklasmatzulla.graphicalquests.config.QuestsConfig;
import de.nicklasmatzulla.graphicalquests.gui.RecipeBookGui;
import de.nicklasmatzulla.graphicalquests.listener.PlayerConnectionListener;
import de.nicklasmatzulla.graphicalquests.listener.PlayerQuestListener;
import de.nicklasmatzulla.graphicalquests.listener.RecipeBookListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

@SuppressWarnings("unused")
public class GraphicalQuests extends JavaPlugin {

    @Override
    public void onEnable() {
        final Logger logger = getSLF4JLogger();
        final MessagesConfig messagesConfig = new MessagesConfig(logger);
        final QuestsConfig questsConfig = new QuestsConfig(logger);
        final GuiConfig guiConfig = new GuiConfig(logger, questsConfig);
        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerConnectionListener(questsConfig), this);
        pluginManager.registerEvents(new PlayerQuestListener(questsConfig), this);
        pluginManager.registerEvents(new RecipeBookListener(messagesConfig, questsConfig, guiConfig), this);
        final CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.register("", new QuestsCommand(messagesConfig, questsConfig, guiConfig));
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> Bukkit.getOnlinePlayers().forEach(player -> RecipeBookGui.updateRecipeBook(questsConfig, player)), 0, 100);
    }

}
