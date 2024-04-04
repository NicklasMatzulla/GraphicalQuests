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
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Getter
public class GuiConfig extends BaseConfig {

    @Getter(AccessLevel.NONE)
    private final QuestsConfig questsConfig;

    private Component questsGuiTitle;
    private Component objectiveGuiTitle;
    private BaseItemBuilder<?> placeholderItemBuilder;
    private BaseItemBuilder<?> nextPageItemBuilder;
    private BaseItemBuilder<?> previousPageItemBuilder;
    private BaseItemBuilder<?> noObjectivesItemBuilder;
    private BaseItemBuilder<?> noOtherQuestsItemBuilder;
    private BaseItemBuilder<?> noOtherObjectivesItemBuilder;

    public GuiConfig(final @NotNull QuestsConfig questsConfig) {
        super(new File("plugins/GraphicalQuests/gui.yml"), "gui.yml");
        this.questsConfig = questsConfig;
        init();
    }

    public void init() {
        this.questsGuiTitle = getComponent("titles.questsGui");
        this.objectiveGuiTitle = getComponent("titles.objectiveGui");
        this.placeholderItemBuilder = getItemBuilder("placeholderItem");
        this.nextPageItemBuilder = getItemBuilder("nextPageItem");
        this.previousPageItemBuilder = getItemBuilder("previousPageItem");
        this.noObjectivesItemBuilder = getItemBuilder("noObjectives");
        this.noOtherQuestsItemBuilder = getItemBuilder("noOtherQuests");
        this.noOtherObjectivesItemBuilder = getItemBuilder("noOtherObjectives");
    }

}
