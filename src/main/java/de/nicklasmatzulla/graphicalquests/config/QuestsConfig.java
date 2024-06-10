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
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.config.Config;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

public class QuestsConfig extends BaseConfig {

    public QuestsConfig(final @NotNull Logger logger) {
        super(logger, new File("plugins/GraphicalQuests/quests.yml"), "quests.yml", false);
        init();
    }

    public void init() {
        for (final Map.Entry<String, QuestPackage> entry : Config.getPackages().entrySet()) {
            final String packageName = entry.getKey();
            final QuestPackage questPackage = entry.getValue();
            final ConfigurationSection configurationSection = questPackage.getConfig().getConfigurationSection("objectives");
            if (configurationSection == null) {
                continue;
            }
            registerPackage(packageName.toLowerCase());
            configurationSection.getKeys(false).forEach(objective -> {
                final String objectiveKey = packageName.toLowerCase() + ".objectives." + objective.toLowerCase();
                registerObjective(objectiveKey);
            });
        }
        save();
    }

    private void registerPackage(final @NotNull String packageName) {
        if (this.config.contains(packageName)) {
            return;
        }
        this.config.set(packageName + ".enabled", true);
        this.config.set(packageName + ".item.recipeBook.material", "REDSTONE");
        this.config.set(packageName + ".item.recipeBook.customModelData", 0);
        this.config.set(packageName + ".item.recipeBook.category", "REDSTONE");
        this.config.set(packageName + ".item.recipeBook.displayName", "<dark_gray>»</dark_gray> <gradient:#249ae1:#f67200>" + packageName + "</gradient>");
        this.config.set(packageName + ".item.recipeBook.lore", List.of(
                "<dark_gray><strikethrough>■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■</strikethrough></dark_gray>",
                "",
                "<gray><dark_gray>»</dark_gray> The description of this objective</gray>",
                "<gray><dark_gray>»</dark_gray> can be edited in the configuration.</gray>",
                "",
                "<aqua><dark_gray>»</dark_gray> Click to configure</aqua>"
        ));
        this.config.set(packageName + ".item.gui.material", "REDSTONE");
        this.config.set(packageName + ".item.gui.customModelData", 0);
        this.config.set(packageName + ".item.gui.displayName", "<dark_gray>»</dark_gray> <gradient:#249ae1:#f67200>" + packageName + "</gradient>");
        this.config.set(packageName + ".item.gui.lore", List.of(
                "<dark_gray><strikethrough>■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■</strikethrough></dark_gray>",
                "",
                "<gray><dark_gray>»</dark_gray> The description of this objective</gray>",
                "<gray><dark_gray>»</dark_gray> can be edited in the configuration.</gray>",
                "",
                "<aqua><dark_gray>»</dark_gray> Click to configure</aqua>"
        ));
    }

    private void registerObjective(final @NotNull String packagedObjectiveName) {
        if (this.config.contains(packagedObjectiveName)) {
            return;
        }
        this.config.set(packagedObjectiveName + ".enabled", true);
        this.config.set(packagedObjectiveName + ".closeGuiOnCommand", false);
        this.config.set(packagedObjectiveName + ".commands", List.of("backpack"));
        this.config.set(packagedObjectiveName + ".location.world", "");
        this.config.set(packagedObjectiveName + ".location.x", 0F);
        this.config.set(packagedObjectiveName + ".location.y", 0F);
        this.config.set(packagedObjectiveName + ".location.z", 0F);
        this.config.set(packagedObjectiveName + ".item.material", "REDSTONE");
        this.config.set(packagedObjectiveName + ".item.customModelData", 0);
        this.config.set(packagedObjectiveName + ".item.displayName", "<dark_gray>»</dark_gray> <gradient:#249ae1:#f67200>" + packagedObjectiveName + "</gradient>");
        this.config.set(packagedObjectiveName + ".item.lore", List.of(
                "<dark_gray><strikethrough>■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■</strikethrough></dark_gray>",
                "",
                "<gray><dark_gray>»</dark_gray> The description of this objective</gray>",
                "<gray><dark_gray>»</dark_gray> can be edited in the configuration.</gray>",
                "<aqua><dark_gray>»</dark_gray> You're able to use placeholders.</aqua>",
                "",
                "<gray><dark_gray>»</dark_gray> Left click <dark_gray>|</dark_gray> <aqua>Open quest backpack</aqua></gray>",
                "<gray><dark_gray>»</dark_gray> Right click <dark_gray>|</dark_gray> <aqua>Set as compass target</aqua></gray>"
        ));
    }

    public boolean isQuestEnabled(final @NotNull String questKey) {
        return this.config.getBoolean(questKey + ".enabled");
    }

    public boolean isObjectiveEnabled(final @NotNull String objectiveKey) {
        return this.config.getBoolean(objectiveKey + ".enabled");
    }

    public @NotNull CraftingBookCategory getRecipeBookCategory(final @NotNull String questKey) {
        System.out.println(questKey + ".item.recipeBook.category");
        final String categoryName = this.config.getString(questKey + ".item.recipeBook.category", "REDSTONE");
        try {
            return CraftingBookCategory.valueOf(categoryName);
        } catch (final @NotNull IllegalArgumentException ignored) {
            return CraftingBookCategory.REDSTONE;
        }
    }

    public @Nullable ItemStack getRecipeBookItemStack(final @NotNull Player player, final @NotNull String questKey) {
        final BaseItemBuilder<?> itemBuilder = getItemBuilder(player, questKey + ".item.recipeBook");
        if (itemBuilder == null) {
            return null;
        }
        return itemBuilder.build();
    }

    public @Nullable BaseItemBuilder<?> getMainGuiItemBuilder(final @NotNull Player player, final @NotNull String questKey) {
        return getItemBuilder(player, questKey + ".item.gui");
    }

    public boolean isCloseGuiOnCommand(final @NotNull String objectiveName) {
        return this.config.getBoolean(objectiveName + ".closeGuiOnCommand");
    }

    public @NotNull List<String> getObjectiveCommands(final @NotNull String objectiveKey) {
        return this.config.getStringList(objectiveKey + ".commands");
    }

    public @Nullable Location getObjectiveLocation(final @NotNull String objectiveKey) {
        return getLocation(objectiveKey + ".location");
    }

    public @Nullable BaseItemBuilder<?> getObjectiveGuiItemBuilder(final @NotNull Player player, final @NotNull String objectiveKey) {
        return getItemBuilder(player, objectiveKey + ".item");
    }

}
