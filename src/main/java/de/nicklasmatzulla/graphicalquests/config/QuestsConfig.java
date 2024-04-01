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

import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.config.Config;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestsConfig extends BaseConfig {

    protected final List<String> labels = new ArrayList<>();

    public QuestsConfig() {
        super(new File("plugins/GraphicalQuests/quests.yml"), "quests.yml");
        init();
    }

    public void init() {
        this.labels.clear();
        for (final Map.Entry<String, QuestPackage> entry : Config.getPackages().entrySet()) {
            final String packageName = entry.getKey();
            final QuestPackage questPackage = entry.getValue();
            final ConfigurationSection configurationSection = questPackage.getConfig().getConfigurationSection("objectives");
            if (configurationSection == null) {
                continue;
            }
            configurationSection.getKeys(false).forEach(objective -> {
                final String objectiveName = packageName.toLowerCase() + "." + objective.toLowerCase();
                registerObjective(objectiveName);
                labels.add(objectiveName);
            });
        }
        save();
    }

    private void registerObjective(final @NotNull String label) {
        if (this.config.contains(label)) {
            return;
        }
        this.config.set(label + ".location.world", "world");
        this.config.set(label + ".location.x", 0F);
        this.config.set(label + ".location.y", 0F);
        this.config.set(label + ".location.z", 0F);
        this.config.set(label + ".item.material", "REDSTONE");
        this.config.set(label + ".item.displayName", "<dark_gray>»</dark_gray> <gradient:#249ae1:#f67200>" + label + "</gradient>");
        this.config.set(label + ".item.lore", List.of(
                "<dark_gray><strikethrough>■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■</strikethrough></dark_gray>",
                "",
                "<gray><dark_gray>»</dark_gray> The description of this objective</gray>",
                "<gray><dark_gray>»</dark_gray> can be edited in the configuration.</gray>"
        ));
    }
}
