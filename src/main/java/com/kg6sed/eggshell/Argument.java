package com.kg6sed.eggshell;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
 * Arguments
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Argument {
    public static String NO_COMPLETIONS = "__NO_COMPLETIONS__";
    public static String SIMPLE = "simple";
    public static String SIMPLE_METHOD = "simple_method";
    public static String COMPLETOR_GETTER = "completor_getter";
    
    String name();

    String type() default SIMPLE;

    String[] completions() default NO_COMPLETIONS;
}
