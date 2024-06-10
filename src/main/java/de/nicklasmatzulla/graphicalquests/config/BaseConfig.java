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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class BaseConfig {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    protected final Logger logger;
    protected final File configFile;
    private final String classpathFile;
    protected FileConfiguration config;

    public BaseConfig(final @NotNull Logger logger, final @NotNull File configFile, final @NotNull String classpathFile, final boolean update) {
        this.logger = logger;
        this.configFile = configFile;
        this.classpathFile = classpathFile;
        load();
        if (update) {
            update();
        }
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

    @SuppressWarnings({"DataFlowIssue", "LoggingSimilarMessage"})
    public void update() {
        this.logger.info("Updating configuration \"{}\"...", this.configFile.getPath());
        try (final InputStream templateConfigInputStream = getClass().getClassLoader().getResourceAsStream(this.classpathFile)) {
            final YamlConfiguration defaultConfig = new YamlConfiguration();
            defaultConfig.load(new InputStreamReader(templateConfigInputStream, StandardCharsets.UTF_8));
            final Map<String, Object> defaultConfigValues = defaultConfig.getValues(true);
            final Set<String> currentKeys = new HashSet<>(this.config.getKeys(true));
            for (final String key : currentKeys) {
                final String cleanedKey = key.startsWith(".") ? key.substring(1) : key;
                if (!defaultConfigValues.containsKey(cleanedKey)) {
                    this.logger.info("Removing key \"{}\"...", key);
                    this.config.set(key, null);
                }
            }
            for (final String key : defaultConfigValues.keySet()) {
                if (!this.config.contains(key)) {
                    this.logger.info("Adding key \"{}\"...", key);
                    this.config.set(key, defaultConfigValues.get(key));
                    if (!defaultConfig.getComments(key).isEmpty()) {
                        this.config.setComments(key, defaultConfig.getComments(key));
                    }
                } else {
                    if (this.config.isConfigurationSection(key) && defaultConfig.isConfigurationSection(key)) {
                        final ConfigurationSection currentSection = this.config.getConfigurationSection(key);
                        final ConfigurationSection defaultSection = defaultConfig.getConfigurationSection(key);
                        for (final String subKey : defaultSection.getKeys(true)) {
                            if (!currentSection.contains(subKey)) {
                                final String sectionKey = key + "." + subKey;
                                this.logger.info("Adding key \"{}\"...", sectionKey);
                                currentSection.set(subKey, defaultSection.get(subKey));
                                if (!defaultConfig.getComments(sectionKey).isEmpty()) {
                                    this.config.setComments(sectionKey, defaultConfig.getComments(sectionKey));
                                }
                            }
                        }
                    }
                }
            }
            this.config.save(this.configFile);
        } catch (@NotNull final IOException e) {
            throw new RuntimeException("Configuration \"" + this.configFile.getPath() + "\" could not be updated", e);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException("Classpath configuration \"" + this.classpathFile + "\" could not be loaded.", e);
        }
    }

    public void save() {
        try {
            this.config.save(this.configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected @NotNull Component getComponent(final @Nullable Player player, final @NotNull String key, final @NotNull TagResolver... tagResolvers) {
        String value = this.config.getString(key);
        if (value == null) {
            value = "<red>" + key + "</red>";
        } else if (player != null) {
            value = PlaceholderAPI.setPlaceholders(player, value);
        }
        return MINI_MESSAGE.deserialize(value, tagResolvers)
                .colorIfAbsent(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false);
    }

    protected @NotNull Component getComponent(final @NotNull String key, final @NotNull TagResolver... tagResolvers) {
        return getComponent(null, key, tagResolvers);
    }

    protected @NotNull List<Component> getComponentList(final @Nullable Player player, final @NotNull String key, final @NotNull TagResolver... tagResolvers) {
        final ArrayList<Component> components = new ArrayList<>();
        for (String value : this.config.getStringList(key)) {
            if (player != null) {
                value = PlaceholderAPI.setPlaceholders(player, value);
            }
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
        if (this.config.contains(label + ".flags")) {
            final List<String> flags = this.config.getStringList(label + ".flags");
            final ItemFlag[] itemFlags = flags.stream()
                    .map(flag -> {
                        try {
                            return ItemFlag.valueOf(flag);
                        } catch (IllegalArgumentException ignored) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toArray(ItemFlag[]::new);
            itemBuilder.flags(itemFlags);
        }
        if (this.config.contains(label + ".displayName")) {
            final Component displayName = getComponent(player, label + ".displayName");
            itemBuilder.name(displayName);
        }
        if (this.config.contains(label + ".lore")) {
            final List<Component> lore = getComponentList(player, label + ".lore");
            itemBuilder.lore(lore);
        }
        return itemBuilder;
    }

    public BaseItemBuilder<?> getItemBuilder(final @NotNull String label) {
        return getItemBuilder(null, label);
    }

}
