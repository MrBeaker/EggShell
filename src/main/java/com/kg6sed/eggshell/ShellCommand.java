package com.kg6sed.eggshell;

/*
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

import java.lang.reflect.Method;

/**
 *  Class representing a shell command.
 */
public class ShellCommand {
    private String name;
    private String help;
    private Method method;
    private int requiredArgumentCount;

    public ShellCommand(String name, Method method, int requiredArgumentCount) {
        this(name, method, requiredArgumentCount, null);
    }

    public ShellCommand(String name, Method method, int requiredArgumentCount, String help) {
        this.name = name;
        this.method = method;
        this.help = help;
        this.requiredArgumentCount = requiredArgumentCount;
    }

    public String getName() {
        return this.name;
    }

    public String getHelp() {
        return this.help;
    }

    public Method getMethod() {
        return this.method;
    }

    public int getRequiredArgumentCount() {
        return requiredArgumentCount;
    }

}
