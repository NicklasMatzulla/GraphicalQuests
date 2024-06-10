@file:Suppress("VulnerableLibrariesLocal")

import io.papermc.paperweight.userdev.ReobfArtifactConfiguration


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

plugins {
    alias(libs.plugins.runPaper)
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight)
    id("java")
}

group = "de.nicklasmatzulla"
version = "1.4.0"
description = "An addon for BetonQuest to display quests in the recipe book and in a GUI."

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://nexus.betonquest.org/repository/betonquest/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    annotationProcessor(libs.lombok)
    compileOnly(libs.lombok)
    implementation(libs.annotations)
    paperweight.paperDevBundle(libs.versions.paperDevBundle)
    compileOnly(libs.paperApi)
    compileOnly(libs.betonQuest)
    compileOnly(libs.placeholderApi)
    compileOnly(files("libs/CalebCompass.jar"))
    implementation(libs.triumphGui)
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
                "name" to project.name,
                "version" to project.version,
                "description" to project.description,
                "apiVersion" to "1.20.6"
        )
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}