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

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;

@Getter
public class MessagesConfig extends BaseConfig {

    private Component prefixComponent;
    private Component onlyPlayersComponent;
    private Component objectiveNoLocationComponent;
    private Component objectiveNoCommandComponent;
    private Component updatedCompassComponent;
    private Component canceledObjectiveComponent;
    private Component reloadedComponent;

    public MessagesConfig(final @NotNull Logger logger) {
        super(logger, new File("plugins/GraphicalQuests/messages.yml"), "messages.yml", true);
        init();
    }

    public void init() {
        this.prefixComponent = getComponent("prefix");
        this.onlyPlayersComponent = getPrefixedComponent("onlyPlayers");
        this.objectiveNoLocationComponent = getPrefixedComponent("objectiveNoLocation");
        this.objectiveNoCommandComponent = getPrefixedComponent("objectiveNoCommand");
        this.updatedCompassComponent = getPrefixedComponent("updatedCompass");
        this.canceledObjectiveComponent = getPrefixedComponent("canceledObjective");
        this.reloadedComponent = getPrefixedComponent("reloaded");
    }

    private @NotNull Component getPrefixedComponent(final @NotNull String key, final @NotNull TagResolver... tagResolvers) {
        return this.prefixComponent.append(getComponent(key, tagResolvers));
    }

}
