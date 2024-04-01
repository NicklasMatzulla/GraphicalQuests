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

import dev.triumphteam.gui.builder.item.ItemBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class GuiConfig extends BaseConfig {

    @Getter(AccessLevel.NONE)
    private final QuestsConfig questsConfig;

    private Component guiTitle;
    private ItemBuilder placeholderItemBuilder;
    private ItemBuilder nextPageItemBuilder;
    private ItemBuilder previousPageItemBuilder;
    private ItemBuilder noObjectivesItemBuilder;

    public GuiConfig(final @NotNull QuestsConfig questsConfig) {
        super(new File("plugins/GraphicalQuests/gui.yml"), "gui.yml");
        this.questsConfig = questsConfig;
        init();
    }

    public void init() {
        this.guiTitle = getComponent("title");
        this.placeholderItemBuilder = getItemBuilder("placeholderItem");
        this.nextPageItemBuilder = getItemBuilder("nextPageItem");
        this.previousPageItemBuilder = getItemBuilder("previousPageItem");
        this.noObjectivesItemBuilder = getItemBuilder("noObjectives");
        registerRecipes();
    }

    private @Nullable ItemBuilder getRecipeBookObjectiveItemBuilder(final @NotNull String label) {
        final ItemBuilder itemBuilder = this.questsConfig.getItemBuilder(label + ".item");
        if (itemBuilder == null) {
            return null;
        }
        final ArrayList<Component> lore = new ArrayList<>(this.questsConfig.getComponentList(label + ".item.lore"));
        final List<Component> additionalLore = getComponentList("infoLore.recipeBook");
        lore.addAll(additionalLore);
        itemBuilder.lore(lore);
        return itemBuilder;
    }

    private void registerRecipes() {
        for (final String label : this.questsConfig.labels) {
            final ItemBuilder itemBuilder = getRecipeBookObjectiveItemBuilder(label);
            if (itemBuilder == null) {
                continue;
            }
            final ItemStack itemStack = itemBuilder.build();
            final NamespacedKey namespacedKey = new NamespacedKey("graphicalquests", label);
            final ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, itemStack);
            recipe.setCategory(CraftingBookCategory.REDSTONE);
            final ItemStack crafingItemStack = new ItemStack(Material.VOID_AIR);
            recipe.addIngredient(new RecipeChoice.ExactChoice(crafingItemStack));
            Bukkit.addRecipe(recipe);
        }
    }

    public @Nullable ItemBuilder getGuiObjectiveItemBuilder(final @NotNull String label) {
        final ItemBuilder itemBuilder = this.questsConfig.getItemBuilder(label + ".item");
        if (itemBuilder == null) {
            return null;
        }
        final ArrayList<Component> lore = new ArrayList<>(this.questsConfig.getComponentList(label + ".item.lore"));
        final List<Component> additionalLore = getComponentList("infoLore.gui");
        lore.addAll(additionalLore);
        itemBuilder.lore(lore);
        return itemBuilder;
    }

}
