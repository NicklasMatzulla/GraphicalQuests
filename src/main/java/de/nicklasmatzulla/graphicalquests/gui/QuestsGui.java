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

package de.nicklasmatzulla.graphicalquests.gui;

import de.nicklasmatzulla.graphicalquests.config.GuiConfig;
import de.nicklasmatzulla.graphicalquests.config.MessagesConfig;
import de.nicklasmatzulla.graphicalquests.config.QuestsConfig;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QuestsGui {

    private static final short[] HIDDEN_SLOTS = new short[]{10,11,12,13,14,15,16,19,25,28,29,30,31,32,33,34};
    private static final int PREVIOUS_PAGE_ITEM_SLOT = 37;
    private static final int NEXT_PAGE_ITEM_SLOT = 43;

    public static void openQuestsGui(final @NotNull MessagesConfig messagesConfig, final @NotNull QuestsConfig questsConfig, final @NotNull GuiConfig guiConfig, final @NotNull Player player, final @Nullable String singleObjectiveLabel) {
        final PaginatedGui gui = Gui.paginated()
                .title(guiConfig.getGuiTitle())
                .rows(5)
                .pageSize(5)
                .disableAllInteractions()
                .create();
        final GuiItem placeholderGuiItem = guiConfig.getPlaceholderItemBuilder().asGuiItem();
        gui.getFiller().fillBorder(placeholderGuiItem);
        final Profile profile = PlayerConverter.getID(player);
        List<Objective> objectives = BetonQuest.getInstance().getPlayerObjectives(profile);
        if (singleObjectiveLabel != null) {
            objectives = objectives.stream()
                    .filter(objective -> objective.getLabel().equalsIgnoreCase(singleObjectiveLabel))
                    .toList();
        }
        if (objectives.isEmpty()) {
            final GuiItem noObjectivesGuiItem = guiConfig.getNoObjectivesItemBuilder().asGuiItem();
            gui.setItem(22, noObjectivesGuiItem);
        } else {
            final GuiItem invisibleGuiItem = ItemBuilder.from(Material.AIR).asGuiItem();
            for (final int slot : HIDDEN_SLOTS) {
                gui.setItem(slot, invisibleGuiItem);
            }
            for (final Objective objective : objectives) {
                final String label = objective.getLabel().toLowerCase();
                final ItemBuilder objectiveItemBuilder = guiConfig.getGuiObjectiveItemBuilder(label);
                if (objectiveItemBuilder == null) {
                    continue;
                }
                final GuiItem objectiveGuiItem = objectiveItemBuilder.asGuiItem(event -> {
                    switch (event.getClick()) {
                        case LEFT -> {
                            final Location location = questsConfig.getLocation(label + ".location");
                            if (location == null) {
                                final Component objectiveNoLocationComponent = messagesConfig.getObjectiveNoLocationComponent();
                                player.sendMessage(objectiveNoLocationComponent);
                                return;
                            }
                            player.setCompassTarget(location);
                            final Component updatedCompassComponent = messagesConfig.getUpdatedCompassComponent();
                            player.sendMessage(updatedCompassComponent);
                        }
                        case RIGHT -> {
                            objective.cancelObjectiveForPlayer(profile);
                            RecipeBookGui.updateRecipeBook(player);
                            gui.close(player);
                            final Component canceledObjectiveComponent = messagesConfig.getCanceledObjectiveComponent();
                            player.sendMessage(canceledObjectiveComponent);
                        }
                    }
                });
                gui.addItem(objectiveGuiItem);
            }
            if (gui.getPagesNum() > 1) {
                gui.setItem(NEXT_PAGE_ITEM_SLOT, createNextPageGuiItem(gui, guiConfig));
            }
        }
        gui.open(player);
    }

    private static @NotNull GuiItem createPreviousPageGuiItem(final @NotNull PaginatedGui gui, final @NotNull GuiConfig guiConfig) {
        return guiConfig.getPreviousPageItemBuilder().asGuiItem(event -> {
            gui.previous();
            if (gui.getCurrentPageNum() == 1) {
                final GuiItem placeholderGuiItem = guiConfig.getPlaceholderItemBuilder().asGuiItem();
                gui.updateItem(PREVIOUS_PAGE_ITEM_SLOT, placeholderGuiItem);
            }
            gui.updateItem(NEXT_PAGE_ITEM_SLOT, createNextPageGuiItem(gui, guiConfig));
        });
    }

    private static @NotNull GuiItem createNextPageGuiItem(final @NotNull PaginatedGui gui, final @NotNull GuiConfig guiConfig) {
        return guiConfig.getNextPageItemBuilder().asGuiItem(event -> {
            gui.next();
            if (gui.getCurrentPageNum() == gui.getPagesNum()) {
                final GuiItem placeholderGuiItem = guiConfig.getPlaceholderItemBuilder().asGuiItem();
                gui.updateItem(NEXT_PAGE_ITEM_SLOT, placeholderGuiItem);
            }
            gui.updateItem(PREVIOUS_PAGE_ITEM_SLOT, createPreviousPageGuiItem(gui, guiConfig));
        });
    }

}
