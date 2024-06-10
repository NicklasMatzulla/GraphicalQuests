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

import calebcompass.calebcompass.util.CompassInstance;
import calebcompass.calebcompass.util.CompassLocation;
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
import org.betonquest.betonquest.api.QuestCompassTargetChangeEvent;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuestsGui {

    @SuppressWarnings("DuplicatedCode")
    public static void openQuestsGui(final @NotNull MessagesConfig messagesConfig, final @NotNull QuestsConfig questsConfig, final @NotNull GuiConfig guiConfig, final @NotNull Player player) {
        final int rows = guiConfig.getQuestsGuiRows();
        final Component title = guiConfig.getQuestsGuiTitle(player);
        final PaginatedGui gui = Gui.paginated()
                .rows(rows)
                .title(title)
                .disableAllInteractions()
                .pageSize(5)
                .create();
        final GuiItem placeholderGuiItem = guiConfig.getPlaceholderItemBuilder(player).asGuiItem();
        final List<Integer> placeholderSlots = guiConfig.getQuestsGuiPlaceholderSlots();
        for (final int slot : placeholderSlots) {
            gui.setItem(slot, placeholderGuiItem);
        }
        final GuiItem emptyGuiItem = ItemBuilder.from(Material.AIR).asGuiItem();
        for (int slot = 0; slot < 11; slot++) {
            if (gui.getGuiItem(slot) == null) {
                gui.setItem(slot, emptyGuiItem);
            }
        }
        final Profile profile = PlayerConverter.getID(player);
        final List<String> questKeys = BetonQuest.getInstance().getPlayerObjectives(profile).stream()
                .map(Objective::getLabel)
                .map(label -> label.split("\\.")[0].toLowerCase())
                .distinct()
                .filter(questsConfig::isQuestEnabled)
                .toList();
        if (questKeys.isEmpty()) {
            final GuiItem noQuestsGuiItem = guiConfig.getQuestsGuiNoQuestsItemBuilder(player).asGuiItem();
            final int slot = guiConfig.getQuestsGuiNoQuestsItemSlot();
            gui.setItem(slot, noQuestsGuiItem);
        } else {
            for (final String questKey : questKeys) {
                final BaseItemBuilder<?> itemBuilder = questsConfig.getMainGuiItemBuilder(player, questKey);
                if (itemBuilder != null) {
                    // TODO 20.04.2024: Add cancel quest feature
                    final GuiItem guiItem = itemBuilder.asGuiItem(event -> openObjectiveGui(messagesConfig, questsConfig, guiConfig, questKey, player));
                    gui.addItem(guiItem);
                }
            }
            final int leftEntries = 5 - questKeys.size() % 5;
            final GuiItem noOtherQuestsGuiItem = guiConfig.getQuestsGuiNoOtherQuestsItemBuilder(player).asGuiItem();
            for (int i = 0; i < leftEntries; i++) {
                gui.addItem(noOtherQuestsGuiItem);
            }
            if (gui.getPagesNum() > 1) {
                final int nextPageSlot = guiConfig.getQuestsGuiNextPageItemSlot();
                final int previousPageSlot = guiConfig.getQuestsGuiPreviousPageItemSlot();
                final BaseItemBuilder<?> placeholderItemBuilder = guiConfig.getPlaceholderItemBuilder(player);
                final BaseItemBuilder<?> nextPageItemBuilder = guiConfig.getQuestsGuiNextPageItemBuilder(player);
                final BaseItemBuilder<?> previousPageItemBuilder = guiConfig.getQuestsGuiPreviousPageItemBuilder(player);
                final boolean hasNextPageSlotPlaceholder = placeholderSlots.contains(nextPageSlot);
                final boolean hasPreviousPageSlotPlaceholder = placeholderSlots.contains(previousPageSlot);
                gui.setItem(nextPageSlot, createNextPageGuiItem(nextPageSlot, previousPageSlot, hasNextPageSlotPlaceholder, nextPageItemBuilder, hasPreviousPageSlotPlaceholder, previousPageItemBuilder, placeholderItemBuilder, gui));
            }
        }
        gui.open(player);
    }

    @SuppressWarnings("DuplicatedCode")
    public static void openObjectiveGui(final @NotNull MessagesConfig messagesConfig, final @NotNull QuestsConfig questsConfig, final @NotNull GuiConfig guiConfig, final @NotNull String questKey, final @NotNull Player player) {
        final int rows = guiConfig.getObjectivesGuiRows();
        final Component title = guiConfig.getObjectivesGuiTitle(player);
        final PaginatedGui gui = Gui.paginated()
                .title(title)
                .rows(rows)
                .pageSize(5)
                .disableAllInteractions()
                .create();
        final GuiItem placeholderGuiItem = guiConfig.getPlaceholderItemBuilder(player).asGuiItem();
        final List<Integer> placeholderSlots = guiConfig.getQuestsGuiPlaceholderSlots();
        for (final int slot : placeholderSlots) {
            gui.setItem(slot, placeholderGuiItem);
        }
        final GuiItem emptyGuiItem = ItemBuilder.from(Material.AIR).asGuiItem();
        for (int slot = 0; slot < 11; slot++) {
            if (gui.getGuiItem(slot) == null) {
                gui.setItem(slot, emptyGuiItem);
            }
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
            final BaseItemBuilder<?> itemBuilder = questsConfig.getObjectiveGuiItemBuilder(player, objectiveKey);
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
                            if (questsConfig.isCloseGuiOnCommand(objectiveKey)) {
                                gui.close(player);
                            }
                            commands.forEach(player::performCommand);
                        }
                        /* Feature removed: Cancel objective
                        case RIGHT -> {
                            final String[] splitObjectiveKey = objectiveKey.split("\\.");
                            BetonQuest.getInstance().getPlayerObjectives(profile).stream()
                                    .filter(objective -> objective.getLabel().equalsIgnoreCase(splitObjectiveKey[0] + "." + splitObjectiveKey[2]))
                                    .forEach(objective -> objective.cancelObjectiveForPlayer(profile));
                            gui.close(player);
                            final Component canceledObjectiveComponent = messagesConfig.getCanceledObjectiveComponent();
                            player.sendMessage(canceledObjectiveComponent);
                        }
                         */
                        case RIGHT -> {
                            final Location compassLocation = questsConfig.getObjectiveLocation(objectiveKey);
                            if (compassLocation == null) {
                                final Component objectiveNoLocationComponent = messagesConfig.getObjectiveNoLocationComponent();
                                player.sendMessage(objectiveNoLocationComponent);
                                return;
                            }
                            player.setCompassTarget(compassLocation);

                            // BetonQuestGUI integration
                            final QuestCompassTargetChangeEvent questCompassEvent = new QuestCompassTargetChangeEvent(profile, compassLocation);
                            final PluginManager pluginManager = Bukkit.getPluginManager();
                            pluginManager.callEvent(questCompassEvent);

                            // CalebCompass integration
                            CompassLocation hudLocation = CompassInstance.getInstance().getCompassLocation(player);
                            if (hudLocation == null) {
                                CompassInstance.getInstance().addCompassLocation(player, player.getLocation(), compassLocation);
                            }
                            hudLocation = CompassInstance.getInstance().getCompassLocation(player);
                            hudLocation.setOrigin(player.getLocation());
                            hudLocation.setTarget(compassLocation);
                            hudLocation.setTracking(true);
                            CompassInstance.getInstance().saveData();

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
        final GuiItem noOtherObjectivesGuiItem = guiConfig.getObjectivesGuiNoOtherObjectivesItemBuilder(player).asGuiItem();
        for (int i = 0; i < leftEntries; i++) {
            gui.addItem(noOtherObjectivesGuiItem);
        }
        if (gui.getPagesNum() > 1) {
            final int nextPageSlot = guiConfig.getObjectivesGuiNextPageItemSlot();
            final int previousPageSlot = guiConfig.getObjectivesGuiPreviousPageItemSlot();
            final BaseItemBuilder<?> placeholderItemBuilder = guiConfig.getPlaceholderItemBuilder(player);
            final BaseItemBuilder<?> nextPageItemBuilder = guiConfig.getObjectivesGuiNextPageItemBuilder(player);
            final BaseItemBuilder<?> previousPageItemBuilder = guiConfig.getObjectivesGuiPreviousPageItemBuilder(player);
            final boolean hasNextPageSlotPlaceholder = placeholderSlots.contains(nextPageSlot);
            final boolean hasPreviousPageSlotPlaceholder = placeholderSlots.contains(previousPageSlot);
            gui.setItem(nextPageSlot, createNextPageGuiItem(nextPageSlot, previousPageSlot, hasNextPageSlotPlaceholder, nextPageItemBuilder, hasPreviousPageSlotPlaceholder, previousPageItemBuilder, placeholderItemBuilder, gui));
        }
        gui.open(player);
    }

    private static @NotNull GuiItem createPreviousPageGuiItem(final int nextPageSlot, final int previousPageSlot, final boolean hasNextPageSlotPlaceholder,
                                                              final @NotNull BaseItemBuilder<?> nextPageItemBuilder, final boolean hasPreviousPageSlotPlaceholder,
                                                              final @NotNull BaseItemBuilder<?> previousPageItemBuilder, final @NotNull BaseItemBuilder<?> placeholderItemBuilder,
                                                              final @NotNull PaginatedGui gui) {
        return previousPageItemBuilder.asGuiItem(event -> {
            gui.previous();
            if (gui.getCurrentPageNum() == 1) {
                if (hasPreviousPageSlotPlaceholder) {
                    gui.updateItem(previousPageSlot, placeholderItemBuilder.asGuiItem());
                } else {
                    gui.updateItem(previousPageSlot, new ItemStack(Material.AIR));
                }
            }
            gui.updateItem(nextPageSlot, createNextPageGuiItem(nextPageSlot, previousPageSlot, hasNextPageSlotPlaceholder, nextPageItemBuilder, hasPreviousPageSlotPlaceholder, previousPageItemBuilder, placeholderItemBuilder, gui));
        });
    }

    private static @NotNull GuiItem createNextPageGuiItem(final int nextPageSlot, final int previousPageSlot, final boolean hasNextPageSlotPlaceholder,
                                                          final @NotNull BaseItemBuilder<?> nextPageItemBuilder, final boolean hasPreviousPageSlotPlaceholder,
                                                          final @NotNull BaseItemBuilder<?> previousPageItemBuilder, final @NotNull BaseItemBuilder<?> placeholderItemBuilder,
                                                          final @NotNull PaginatedGui gui) {
        return nextPageItemBuilder.asGuiItem(event -> {
            gui.next();
            if (gui.getCurrentPageNum() == gui.getPagesNum()) {
                if (hasNextPageSlotPlaceholder) {
                    gui.updateItem(nextPageSlot, placeholderItemBuilder.asGuiItem());
                } else {
                    gui.updateItem(nextPageSlot, new ItemStack(Material.AIR));
                }
            }
            gui.updateItem(previousPageSlot, createPreviousPageGuiItem(nextPageSlot, previousPageSlot, hasNextPageSlotPlaceholder, nextPageItemBuilder, hasPreviousPageSlotPlaceholder, previousPageItemBuilder, placeholderItemBuilder, gui));
        });
    }

}
