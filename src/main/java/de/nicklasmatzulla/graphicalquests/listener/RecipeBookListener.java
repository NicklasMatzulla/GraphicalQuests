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

package de.nicklasmatzulla.graphicalquests.listener;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import de.nicklasmatzulla.graphicalquests.config.GuiConfig;
import de.nicklasmatzulla.graphicalquests.config.MessagesConfig;
import de.nicklasmatzulla.graphicalquests.config.QuestsConfig;
import de.nicklasmatzulla.graphicalquests.gui.QuestsGui;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@AllArgsConstructor
public class RecipeBookListener implements Listener {

    private static final String NAMESPACE_PREFIX = "graphicalquests";

    private final MessagesConfig messagesConfig;
    private final QuestsConfig questsConfig;
    private final GuiConfig guiConfig;

    @EventHandler
    public void onPlayerRecipeDiscoverEvent(final @NotNull PlayerRecipeDiscoverEvent event) {
        if (event.getRecipe().getNamespace().startsWith(NAMESPACE_PREFIX)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerRecipeBookClickEvent(final @NotNull PlayerRecipeBookClickEvent event) {
        if (!event.getRecipe().getNamespace().startsWith(NAMESPACE_PREFIX)) {
            return;
        }
        event.setCancelled(true);
        final Player player = event.getPlayer();
        final String label = event.getRecipe().getKey();
        QuestsGui.openQuestsGui(this.messagesConfig, this.questsConfig, this.guiConfig, player, label);
    }

}
