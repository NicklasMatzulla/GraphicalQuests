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
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.builder.item.SkullBuilder;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseConfig {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    protected final File configFile;
    private final String classpathFile;
    protected FileConfiguration config;

    public BaseConfig(final @NotNull File configFile, final @NotNull String classpathFile) {
        this.configFile = configFile;
        this.classpathFile = classpathFile;
        load();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void create() {
        if (this.configFile.exists()) {
            return;
        }
        this.configFile.getParentFile().mkdirs();
        try (final InputStream configInputStream = getClass().getClassLoader().getResourceAsStream(this.classpathFile)) {
            if (configInputStream == null) {
                throw new FileNotFoundException("The classpath configuration " + this.classpathFile + " was not found");
            }
            Files.copy(configInputStream, this.configFile.toPath());
        } catch (final @NotNull IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        try {
            create();
            this.config = new YamlConfiguration();
            this.config.load(this.configFile);
        } catch (final @NotNull IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            this.config.save(this.configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected @NotNull Component getComponent(final @NotNull String key, final @NotNull TagResolver... tagResolvers) {
        final String value = this.config.getString(key);
        return MINI_MESSAGE.deserialize(value != null ? value : "<red>" + key + "</red>", tagResolvers)
                .colorIfAbsent(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false);
    }

    protected @NotNull List<Component> getComponentList(final @NotNull String key, final @NotNull TagResolver... tagResolvers) {
        final ArrayList<Component> components = new ArrayList<>();
        for (final String value : this.config.getStringList(key)) {
            final Component component = MINI_MESSAGE.deserialize(value, tagResolvers)
                    .colorIfAbsent(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false);
            components.add(component);
        }
        if (components.isEmpty()) {
            final Component errorComponent = MINI_MESSAGE.deserialize("<red>" + key + "</red>", tagResolvers)
                    .colorIfAbsent(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false);
            components.add(errorComponent);
        }
        return Collections.unmodifiableList(components);
    }

    @SuppressWarnings("DataFlowIssue")
    public @Nullable Location getLocation(final @NotNull String label) {
        final String worldName = this.config.getString(label + ".world");
        final World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        final double x = this.config.getDouble(label + ".x");
        final double y = this.config.getDouble(label + ".y");
        final double z = this.config.getDouble(label + ".z");
        return new Location(world, x, y, z);
    }

    @SuppressWarnings("DataFlowIssue")
    public BaseItemBuilder<?> getItemBuilder(final @Nullable Player player, final @NotNull String label) {
        if (!this.config.contains(label + ".material")) {
            return null;
        }
        final String materialName = this.config.getString(label + ".material");
        final Material material = Material.valueOf(materialName);
        final BaseItemBuilder<?> itemBuilder;
        if (material == Material.PLAYER_HEAD) {
            itemBuilder = ItemBuilder.skull();
            final String texture = this.config.getString(label + ".texture");
            ((SkullBuilder) itemBuilder).texture(texture);
        } else {
            itemBuilder = ItemBuilder.from(material);
        }
        if (this.config.contains(label + ".customModelData")) {
            final int customModelData = this.config.getInt(label + ".customModelData");
            itemBuilder.model(customModelData);
        }
        if (this.config.contains(label + ".displayName")) {
            final Component displayName;
            if (player != null) {
                String rawDisplayName = this.config.getString(label + ".displayName");
                rawDisplayName = PlaceholderAPI.setPlaceholders(player, rawDisplayName);
                displayName = MINI_MESSAGE.deserialize(rawDisplayName)
                        .colorIfAbsent(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false);
            } else {
                displayName = getComponent(label + ".displayName");
            }
            itemBuilder.name(displayName);
        }
        if (this.config.contains(label + ".lore")) {
            final List<Component> lore;
            if (player != null) {
                List<String> rawLore = this.config.getStringList(label + ".lore");
                rawLore = PlaceholderAPI.setPlaceholders(player, rawLore);
                lore = rawLore.stream()
                        .map(element -> MINI_MESSAGE.deserialize(element)
                                .colorIfAbsent(NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false))
                        .toList();
            } else{
                lore = getComponentList(label + ".lore");
            }
            itemBuilder.lore(lore);
        }
        return itemBuilder;
    }

    public BaseItemBuilder<?> getItemBuilder(final @NotNull String label) {
        return getItemBuilder(null, label);
    }

}
