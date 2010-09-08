package com.kg6sed.eggshell;

import com.kg6sed.eggshell.jline.WrappingCompletor;
import jline.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
 * Super class for all shells.
 */
public abstract class AbstractShell implements Shell {
    private Map<String, ShellCommand> commands;
    protected ConsoleReader console;

    protected AbstractShell() throws IOException {
        this.commands = new HashMap<String, ShellCommand>();
        this.console = new ConsoleReader();


        findAnnotations(this.getClass());


        for (ShellCommand command : this.commands.values()) {
            List<Completor> completors = new LinkedList<Completor>();
            completors.add(new SimpleCompletor(command.getName()));

            for (Annotation[] annArray : command.getMethod().getParameterAnnotations()) {
                boolean addedCompletor = false;
                for (Annotation annotation : annArray) {
                    if (annotation instanceof Argument) {
                        Argument arg = (Argument) annotation;
                        if (!arg.completions()[0].equals(Argument.NO_COMPLETIONS)) {
                            if (arg.type().equalsIgnoreCase(Argument.SIMPLE)) {
                                completors.add(new SimpleCompletor(arg.completions()));
                                addedCompletor = true;
                            } else if (arg.type().equalsIgnoreCase(Argument.SIMPLE_METHOD)) {
                                String[] result = new String[0];
                                try {
                                    Method m = this.getClass().getDeclaredMethod(arg.completions()[0]);
                                    m.setAccessible(true);
                                    result = (String[]) m.invoke(this);
                                } catch (Exception e) {
                                    // TODO: log statement
                                }
                                completors.add(new SimpleCompletor(result));
                                addedCompletor = true;
                            } else if (arg.type().equalsIgnoreCase(Argument.COMPLETOR_GETTER)) {
                                try {
                                    Method m = this.getClass().getDeclaredMethod(arg.completions()[0]);
                                    m.setAccessible(true);
                                    completors.add(new WrappingCompletor(this, m));
                                    addedCompletor = true;
                                } catch (NoSuchMethodException e) {
                                    // TOOD: log statement
                                } 
                            }
                        } else if (arg.type().equalsIgnoreCase("filename")) {
                            completors.add(new FileNameCompletor());
                            addedCompletor = true;
                        }
                    }
                }
                if (!addedCompletor) {
                    completors.add(new NullCompletor());
                }
            }


            completors.add(new NullCompletor());
            ArgumentCompletor ac = new ArgumentCompletor(completors);
            ac.setStrict(false);
            console.addCompletor(ac);
        }


    }

    protected String generatePrompt() {
        return "> ";
    }

    protected void printMOTD() throws IOException {
        this.console.printString("Welcome. Type 'help' for help.");
        this.console.printNewline();
    }

    public void run() {
        try {
            this.printMOTD();
            for (String line = console.readLine(this.generatePrompt()); line != null; line = console.readLine(this.generatePrompt())) {


                String[] args = this.splitArguments(line);
                if (args == null || args.length < 1 || args[0] == null) {
                    continue;
                }
                ShellCommand command = this.commands.get(args[0].trim().toLowerCase());
                if (command == null) {
                    this.console.printString(String.format("Unknown command '%s', try 'help'", args[0]));
                    this.console.printNewline();
                    continue;
                } else {
                    // attempt to execute command

                    Object[] arguments = new Object[command.getMethod().getParameterTypes().length];

                    if (args.length != arguments.length + 1) {
                        this.console.printString(String.format("'%s': Invalid number of arguments, got %d, expected %d", command.getName(), args.length - 1, arguments.length));
                        this.console.printNewline();
                        StringBuilder usage = new StringBuilder("Usage: ");
                        usage.append(command.getName());
                        Annotation[][] annArray = command.getMethod().getParameterAnnotations();
                        for (int i = 0; i < annArray.length; i++) {
                            boolean usageAdded = false;
                            for (int j = 0; j < annArray[i].length; j++) {
                                if (annArray[i][j] instanceof Argument) {
                                    Argument argument = (Argument) annArray[i][j];
                                    usage.append(' ');
                                    usage.append(argument.name());
                                    usageAdded = true;
                                    break;
                                }
                            }
                            if (!usageAdded) {
                                usage.append(' ');
                                usage.append(command.getMethod().getParameterTypes()[i].getName());
                            }
                        }
                        this.console.printString(usage.toString());
                        this.console.printNewline();
                        continue;
                    }

                    // have the right number of arguments, fill out the argument array

                    Class[] parameters = command.getMethod().getParameterTypes();

                    boolean error = false;
                    for (int i = 0; i < parameters.length; i++) {
                        if (parameters[i] == String.class) {
                            arguments[i] = args[i + 1];
                        } else if (parameters[i] == long.class) {
                            try {
                                arguments[i] = Long.parseLong(args[i + 1]);
                            } catch (NumberFormatException e) {
                                this.console.printString(String.format("Cannot parse '%s' to long for parameter %d", args[i + 1], i));
                                this.console.printNewline();
                                error = true;
                                break;
                            }
                        } else if (parameters[i] == int.class) {
                            try {
                                arguments[i] = Integer.parseInt(args[i + 1]);
                            } catch (NumberFormatException e) {
                                this.console.printString(String.format("Cannot parse '%s' to int for parameter %d", args[i + 1], i));
                                this.console.printNewline();
                                error = true;
                                break;
                            }
                        } else if (parameters[i] == short.class) {
                            try {
                                arguments[i] = Short.parseShort(args[i + 1]);
                            } catch (NumberFormatException e) {
                                this.console.printString(String.format("Cannot parse '%s' to short for parameter %d", args[i + 1], i));
                                this.console.printNewline();
                                error = true;
                                break;
                            }
                        } else if (parameters[i] == char.class) {
                            if (args[i + 1].length() != 1) {
                                this.console.printString(String.format("Cannot parse '%s' to char for parameter %d", args[i + 1], i));
                                this.console.printNewline();
                                error = true;
                                break;
                            } else {
                                arguments[i] = args[i + 1].charAt(0);
                            }
                        } else if (parameters[i] == byte.class) {
                            try {
                                arguments[i] = Byte.parseByte(args[i + 1]);
                            } catch (NumberFormatException e) {
                                this.console.printString(String.format("Cannot parse '%s' to byte for parameter %d", args[i + 1], i));
                                this.console.printNewline();
                                error = true;
                                break;
                            }
                        } else if (parameters[i] == boolean.class) {
                            arguments[i] = !(args[i + 1].length() < 1 || (!args[i + 1].startsWith("t") && !args[i + 1].startsWith("T")));
                        } else {
                            // check if the class has a constructor that takes a string
                            Constructor constructor = parameters[i].getConstructor(String.class);

                            if (constructor != null) {
                                try {
                                    arguments[i] = constructor.newInstance(args[i + 1]);
                                } catch (Throwable t) {
                                    this.console.printString(String.format("Programming error, unable to construct %s from string, parameter %d", parameters[i], i));
                                    this.console.printNewline();
                                    error = true;
                                    break;
                                }
                            } else {

                                this.console.printString(String.format("Programming error, unable to assign string to %s, parameter %d", parameters[i], i));
                                this.console.printNewline();
                                error = true;
                                break;
                            }
                        }
                    }
                    if (error) {
                        continue;
                    }

                    try {
                        command.getMethod().invoke(this, arguments);
                    } catch (InvocationTargetException e) {
                        if (e.getCause() instanceof ExitShellException) {
                            try {
                                this.console.printString(e.getCause().getMessage());
                                this.console.printNewline();
                            } catch (IOException e1) {
                                // boo hoo, so they didnt get to see the exit message, big deal
                            }
                            return;
                        } else {
                            ByteArrayOutputStream sw = new ByteArrayOutputStream();
                            PrintStream ps = new PrintStream(sw);
                            e.getCause().printStackTrace(ps);
                            ps.close();
                            this.console.printString(sw.toString());
                            this.console.printNewline();
                        }
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String[] splitArguments(String line) {
        LinkedList<String> result = new LinkedList<String>();
        StringBuilder currentArgument = new StringBuilder();
        int state = 0;
        char prevChar = 0;
        for (char c : line.toCharArray()) {
            switch (state) {
                case 0:
                    // outside quoted string
                    if (c == '"') {
                        state = 1;
                    } else if (c != ' ') {
                        currentArgument.append(c);
                    } else {
                        if (currentArgument.length() > 0) {
                            result.add(currentArgument.toString());
                            currentArgument = new StringBuilder();
                        }
                    }
                    break;
                case 1:
                    // inside quoted string
                    if (c == '"' && prevChar != '\\') {
                        if (currentArgument.length() > 0) {
                            result.add(currentArgument.toString());
                            currentArgument = new StringBuilder();
                        }
                        state = 0;
                    } else {
                        currentArgument.append(c);
                    }
                    break;
            }
            prevChar = c;
        }
        if (currentArgument.length() > 0) {
            result.add(currentArgument.toString());
        }
        return result.toArray(new String[result.size()]);
    }

    private void findAnnotations(Class clazz) {

        if (clazz.getSuperclass() != null) {
            findAnnotations(clazz.getSuperclass());
        }

        for (Method m : clazz.getDeclaredMethods()) {
            Command c = null;
            m.setAccessible(true);

            if ((c = m.getAnnotation(Command.class)) != null) {

                String commandName = c.name();
                if (commandName.equals(Command.METHOD_NAME)) {
                    commandName = m.getName();
                }

                if (c.help().equals(Command.NO_HELP)) {
                    this.commands.put(commandName.trim().toLowerCase(), new ShellCommand(commandName, m));
                } else {
                    this.commands.put(commandName.trim().toLowerCase(), new ShellCommand(commandName, m, c.help()));
                }
            }

        }
    }

    @Command
    public void help() throws IOException {
        this.console.printString("Available commands:");
        this.console.printNewline();

        for (ShellCommand command : this.commands.values()) {
            if (command.getHelp() != null) {
                this.console.printString(String.format("%s - %s", command.getName(), command.getHelp()));
            } else {
                this.console.printString(String.format("%s", command.getName()));

            }
            this.console.printNewline();
        }


    }

}
