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

import de.nicklasmatzulla.graphicalquests.config.QuestsConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.item.crafting.*;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftShapedRecipe;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class RecipeBookGui {

    public static void updateRecipeBook(final @NotNull QuestsConfig questsConfig, final @NotNull Player player) {
        player.undiscoverRecipes(player.getDiscoveredRecipes());
        final Profile profile = PlayerConverter.getID(player);
        final List<Objective> objectives = BetonQuest.getInstance().getPlayerObjectives(profile);
        final List<ResourceLocation> resourceLocations = new ArrayList<>();
        final List<RecipeHolder<?>> recipeHolders = new ArrayList<>();
        for (final Objective objective : objectives) {
            final String label = objective.getLabel();
            final String questName = label.split("\\.")[0].toLowerCase();
            if (!questsConfig.isQuestEnabled(questName) || resourceLocations.stream().anyMatch(resourceLocation -> resourceLocation.getPath().equals(questName))) {
                continue;
            }
            final NamespacedKey namespacedKey = new NamespacedKey("graphicalquests", questName);
            final ItemStack resultItemStack = questsConfig.getRecipeBookItemStack(player, questName);
            if (resultItemStack == null) {
                continue;
            }
            final CraftShapedRecipe recipe = new CraftShapedRecipe(namespacedKey, resultItemStack);
            recipe.setCategory(CraftingBookCategory.REDSTONE);
            recipe.shape("v ", "  ");
            recipe.setIngredient('v', Material.STONE);
            final ResourceLocation resourceLocation = CraftNamespacedKey.toMinecraft(recipe.getKey());
            final RecipeHolder<ShapedRecipe> recipeHolder = convertRecipeHolder(recipe);
            resourceLocations.add(resourceLocation);
            recipeHolders.add(recipeHolder);
        }
        final CraftPlayer craftPlayer = (CraftPlayer) player;
        final ServerPlayer serverPlayer = craftPlayer.getHandle();
        final RecipeBookSettings recipeBookSettings = serverPlayer.getRecipeBook().getBookSettings();
        final ClientboundRecipePacket initPacket = new ClientboundRecipePacket(ClientboundRecipePacket.State.INIT, resourceLocations, List.of(), recipeBookSettings);
        final ClientboundUpdateRecipesPacket updatePacket = new ClientboundUpdateRecipesPacket(recipeHolders);
        final ClientboundRecipePacket addPacket = new ClientboundRecipePacket(ClientboundRecipePacket.State.ADD, resourceLocations, List.of(), recipeBookSettings);
        serverPlayer.connection.send(updatePacket);
        serverPlayer.connection.send(initPacket);
        serverPlayer.connection.send(addPacket);
    }

    private static @NotNull RecipeHolder<ShapedRecipe> convertRecipeHolder(final @NotNull CraftShapedRecipe shapedRecipe) {
        final String[] shape = shapedRecipe.getShape();
        final Map<Character, RecipeChoice> ingredient = shapedRecipe.getChoiceMap();
        final int width = shape[0].length();
        final NonNullList<Ingredient> data = NonNullList.withSize(shape.length * width, Ingredient.EMPTY);
        for (int i = 0; i < shape.length; ++i) {
            final String row = shape[i];
            for (int j = 0; j < row.length(); ++j) {
                data.set(i * width + j, shapedRecipe.toNMS(ingredient.get(row.charAt(j)), false));
            }
        }
        return new RecipeHolder<>(CraftNamespacedKey.toMinecraft(shapedRecipe.getKey()), new ShapedRecipe(shapedRecipe.getGroup(), CraftRecipe.getCategory(shapedRecipe.getCategory()), new ShapedRecipePattern(width, shape.length, data, Optional.empty()), CraftItemStack.asNMSCopy(shapedRecipe.getResult()), false));
    }

}
