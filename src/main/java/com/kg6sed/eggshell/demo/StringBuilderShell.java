package com.kg6sed.eggshell.demo;

import com.kg6sed.eggshell.AbstractShell;
import com.kg6sed.eggshell.Command;
import com.kg6sed.eggshell.ExitShellException;
import com.kg6sed.eggshell.Shell;

import java.io.IOException;


/**
 * EggShell - Annotation driven command-line shell library
 * Copyright (C) 2010 Daniel Mattias Larsson
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * This class demonstrates how you can build a shell around a StringBuilder object.
 */
public class StringBuilderShell extends AbstractShell {
    private StringBuilder builder;

    public StringBuilderShell() throws IOException {
        super();
        this.builder = new StringBuilder();
    }

    /**
     * Generates context sensitive prompt that displays the current value of
     * the StringBuilder.
     *
     * @return prompt
     */
    @Override
    protected String generatePrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("[").append(this.builder).append("]> ");
        return prompt.toString();
    }

    /**
     * Create a new StringBuilder instance.
     */
    @Command(name = "new")
    private void doNew() {
        this.builder = new StringBuilder();
    }

    @Command
    private void append(String value) {
        this.builder.append(value);
    }


    /**
     * Quit the shell.
     */
    @Command
    private void quit() {
        throw new ExitShellException("Goodbye.");
    }

    public static void main(String[] args) {
        try {
            Shell shell = new StringBuilderShell();
            shell.run();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
