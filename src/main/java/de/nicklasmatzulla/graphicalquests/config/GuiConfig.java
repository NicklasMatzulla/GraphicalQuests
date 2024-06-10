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

package de.nicklasmatzulla.graphicalquests.config;

import dev.triumphteam.gui.builder.item.BaseItemBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;

@Getter
public class GuiConfig extends BaseConfig {

    @Getter(AccessLevel.NONE)
    private final QuestsConfig questsConfig;

    private int questsGuiRows;
    private List<Integer> questsGuiPlaceholderSlots;
    private int questsGuiNoQuestsItemSlot;
    private int questsGuiNextPageItemSlot;
    private int questsGuiPreviousPageItemSlot;

    private int objectivesGuiRows;
    private List<Integer> objectivesGuiPlaceholderSlots;
    private int objectivesGuiNoObjectivesItemSlot;
    private int objectivesGuiNextPageItemSlot;
    private int objectivesGuiPreviousPageItemSlot;

    public GuiConfig(final @NotNull Logger logger, final @NotNull QuestsConfig questsConfig) {
        super(logger, new File("plugins/GraphicalQuests/gui.yml"), "gui.yml", true);
        this.questsConfig = questsConfig;
        init();
    }

    public void init() {
        this.questsGuiRows = this.config.getInt("quests.rows");
        this.questsGuiPlaceholderSlots = this.config.getIntegerList("quests.placeholder_slots");
        this.questsGuiNoQuestsItemSlot = this.config.getInt("quests.items.noQuests.slot");
        this.questsGuiNextPageItemSlot = this.config.getInt("quests.items.nextPage.slot");
        this.questsGuiPreviousPageItemSlot = this.config.getInt("quests.items.previousPage.slot");

        this.objectivesGuiRows = this.config.getInt("objectives.rows");
        this.objectivesGuiPlaceholderSlots = this.config.getIntegerList("objectives.placeholder_slots");
        this.objectivesGuiNoObjectivesItemSlot = this.config.getInt("objectives.items.noQuests.slot");
        this.objectivesGuiNextPageItemSlot = this.config.getInt("objectives.items.nextPage.slot");
        this.objectivesGuiPreviousPageItemSlot = this.config.getInt("objectives.items.previousPage.slot");
    }

    public @NotNull BaseItemBuilder<?> getPlaceholderItemBuilder(final @NotNull Player player) {
        return getItemBuilder(player, "global.items.placeholder");
    }

    public @NotNull Component getQuestsGuiTitle(final @NotNull Player player) {
        return getComponent(player, "quests.title");
    }

    public @NotNull BaseItemBuilder<?> getQuestsGuiNoQuestsItemBuilder(final @NotNull Player player) {
        return getItemBuilder(player, "quests.items.noQuests");
    }

    public @NotNull BaseItemBuilder<?> getQuestsGuiNoOtherQuestsItemBuilder(final @NotNull Player player) {
        return getItemBuilder(player, "quests.items.noOtherQuests");
    }

    public @NotNull BaseItemBuilder<?> getQuestsGuiNextPageItemBuilder(final @NotNull Player player) {
        return getItemBuilder(player, "quests.items.nextPage");
    }

    public @NotNull BaseItemBuilder<?> getQuestsGuiPreviousPageItemBuilder(final @NotNull Player player) {
        return getItemBuilder(player, "quests.items.previousPage");
    }

    public @NotNull Component getObjectivesGuiTitle(final @NotNull Player player) {
        return getComponent(player, "objectives.title");
    }

    public @NotNull BaseItemBuilder<?> getObjectivesGuiNoOtherObjectivesItemBuilder(final @NotNull Player player) {
        return getItemBuilder(player, "objectives.items.noOtherObjectives");
    }

    public @NotNull BaseItemBuilder<?> getObjectivesGuiNextPageItemBuilder(final @NotNull Player player) {
        return getItemBuilder(player, "objectives.items.nextPage");
    }

    public @NotNull BaseItemBuilder<?> getObjectivesGuiPreviousPageItemBuilder(final @NotNull Player player) {
        return getItemBuilder(player, "objectives.items.previousPage");
    }

}
