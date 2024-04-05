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
import dev.triumphteam.gui.builder.item.BaseItemBuilder;
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

import java.util.List;

public class QuestsGui {

    private static final short[] HIDDEN_SLOTS = new short[]{10,11,12,13,14,15,16,19,25,28,29,30,31,32,33,34};
    private static final int PREVIOUS_PAGE_ITEM_SLOT = 37;
    private static final int NEXT_PAGE_ITEM_SLOT = 43;

    @SuppressWarnings("DuplicatedCode")
    public static void openQuestsGui(final @NotNull MessagesConfig messagesConfig, final @NotNull QuestsConfig questsConfig, final @NotNull GuiConfig guiConfig, final @NotNull Player player) {
        final Component title = guiConfig.getQuestsGuiTitle();
        final PaginatedGui gui = Gui.paginated()
                .title(title)
                .rows(5)
                .pageSize(5)
                .disableAllInteractions()
                .create();
        final GuiItem placeholderGuiItem = guiConfig.getPlaceholderItemBuilder().asGuiItem();
        gui.getFiller().fillBorder(placeholderGuiItem);
        final GuiItem invisibleGuiItem = ItemBuilder.from(Material.AIR).asGuiItem();
        for (final int slot : HIDDEN_SLOTS) {
            gui.setItem(slot, invisibleGuiItem);
        }
        final Profile profile = PlayerConverter.getID(player);
        final List<String> questKeys = BetonQuest.getInstance().getPlayerObjectives(profile).stream()
                .map(Objective::getLabel)
                .map(label -> label.split("\\.")[0].toLowerCase())
                .distinct()
                .filter(questsConfig::isQuestEnabled)
                .toList();
        if (questKeys.isEmpty()) {
            final GuiItem noQuestsGuiItem = guiConfig.getNoQuestsItemsBuilder().asGuiItem();
            gui.setItem(22, noQuestsGuiItem);
        } else {
            for (final String questKey : questKeys) {
                final BaseItemBuilder<?> itemBuilder = questsConfig.getMainGuiItemBuilder(questKey);
                if (itemBuilder != null) {
                    final GuiItem guiItem = itemBuilder.asGuiItem(event -> openObjectiveGui(messagesConfig, questsConfig, guiConfig, questKey, player));
                    gui.addItem(guiItem);
                }
            }
            final int leftEntries = 5 - questKeys.size() % 5;
            final GuiItem noOtherQuestsGuiItem = guiConfig.getNoOtherQuestsItemBuilder().asGuiItem();
            for (int i = 0; i < leftEntries; i++) {
                gui.addItem(noOtherQuestsGuiItem);
            }
            if (gui.getPagesNum() > 1) {
                gui.setItem(NEXT_PAGE_ITEM_SLOT, createNextPageGuiItem(gui, guiConfig));
            }
        }
        gui.open(player);
    }

    @SuppressWarnings("DuplicatedCode")
    public static void openObjectiveGui(final @NotNull MessagesConfig messagesConfig, final @NotNull QuestsConfig questsConfig, final @NotNull GuiConfig guiConfig, final @NotNull String questKey, final @NotNull Player player) {
        final Component title = guiConfig.getObjectiveGuiTitle();
        final PaginatedGui gui = Gui.paginated()
                .title(title)
                .rows(5)
                .pageSize(5)
                .disableAllInteractions()
                .create();
        final GuiItem placeholderGuiItem = guiConfig.getPlaceholderItemBuilder().asGuiItem();
        gui.getFiller().fillBorder(placeholderGuiItem);
        final GuiItem invisibleGuiItem = ItemBuilder.from(Material.AIR).asGuiItem();
        for (final int slot : HIDDEN_SLOTS) {
            gui.setItem(slot, invisibleGuiItem);
        }
        final Profile profile = PlayerConverter.getID(player);
        final List<String> objectiveKeys = BetonQuest.getInstance().getPlayerObjectives(profile).stream()
                .map(objective -> {
                    final String objectiveKey = objective.getLabel().split("\\.")[1].toLowerCase();
                    return questKey + ".objectives." + objectiveKey;
                })
                .filter(objectiveKey -> objectiveKey.startsWith(questKey))
                .filter(questsConfig::isObjectiveEnabled)
                .toList();
        for (final String objectiveKey : objectiveKeys) {
            final BaseItemBuilder<?> itemBuilder = questsConfig.getObjectiveGuiItemBuilder(objectiveKey);
            if (itemBuilder != null) {
                final GuiItem guiItem = itemBuilder.asGuiItem(event -> {
                    switch (event.getClick()) {
                        case LEFT -> {
                            final List<String> commands = questsConfig.getObjectiveCommands(objectiveKey);
                            if (commands.isEmpty()) {
                                final Component objectiveNoCommandComponent = messagesConfig.getObjectiveNoCommandComponent();
                                player.sendMessage(objectiveNoCommandComponent);
                                return;
                            }
                            commands.forEach(player::performCommand);
                            gui.close(player);
                        }
                        case RIGHT -> {
                            final String[] splitObjectiveKey = objectiveKey.split("\\.");
                            BetonQuest.getInstance().getPlayerObjectives(profile).stream()
                                    .filter(objective -> objective.getLabel().equalsIgnoreCase(splitObjectiveKey[0] + "." + splitObjectiveKey[2]))
                                    .forEach(objective -> objective.cancelObjectiveForPlayer(profile));
                            gui.close(player);
                            final Component canceledObjectiveComponent = messagesConfig.getCanceledObjectiveComponent();
                            player.sendMessage(canceledObjectiveComponent);
                        }
                        case DROP -> {
                            final Location location = questsConfig.getObjectiveLocation(objectiveKey);
                            if (location == null) {
                                final Component objectiveNoLocationComponent = messagesConfig.getObjectiveNoLocationComponent();
                                player.sendMessage(objectiveNoLocationComponent);
                                return;
                            }
                            player.setCompassTarget(location);
                            gui.close(player);
                            final Component updatedCompassComponent = messagesConfig.getUpdatedCompassComponent();
                            player.sendMessage(updatedCompassComponent);
                        }
                    }
                });
                gui.addItem(guiItem);
            }
        }
        final int leftEntries = 5 - objectiveKeys.size() % 5;
        final GuiItem noOtherQuestsGuiItem = guiConfig.getNoOtherQuestsItemBuilder().asGuiItem();
        for (int i = 0; i < leftEntries; i++) {
            gui.addItem(noOtherQuestsGuiItem);
        }
        if (gui.getPagesNum() > 1) {
            gui.setItem(NEXT_PAGE_ITEM_SLOT, createNextPageGuiItem(gui, guiConfig));
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
