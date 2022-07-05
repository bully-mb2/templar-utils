package com.templars_server.util.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Command<T> {

    private static final Logger LOG = LoggerFactory.getLogger(Command.class);
    private static final String DEFAULT_SEPARATOR = " ";

    private final Pattern pattern;
    private final boolean requireExclamation;
    private final String usage;
    private final List<String> args;
    private final String separator;

    public Command(String regex, boolean requireExclamation, String usage) {
        this(regex, requireExclamation, usage, DEFAULT_SEPARATOR);
    }

    public Command(String regex, boolean requireExclamation, String usage, String separator) {
        this.pattern = Pattern.compile("^!?" + regex + " (.*)?$");
        this.requireExclamation = requireExclamation;
        this.usage = String.format("Usage: %s", usage);
        this.args = new ArrayList<>();
        this.separator = separator;
    }

    public boolean execute(int slot, String message, T context) throws InvalidArgumentException {
        if (requireExclamation && !message.startsWith("!")) {
            return false;
        }

        Matcher matcher = pattern.matcher(message + " ");
        if (matcher.find()) {
            args.clear();
            String argString = matcher.group(1);
            if (argString != null && !argString.isEmpty()) {
                args.addAll(List.of(matcher.group(1).split(separator)));
            }

            LOG.debug(message);
            LOG.debug(String.format("Executing %s with args %s player %d", getClass().getSimpleName(), args, slot));
            onExecute(slot, context);
            return true;
        }
        return false;
    }

    protected abstract void onExecute(int slot, T context) throws InvalidArgumentException;

    protected List<String> getArgs() {
        return args;
    }

    protected String getArg(int index) throws InvalidArgumentException {
        return getArg(index, null, false);
    }

    protected String getArg(int index, String defaultValue) throws InvalidArgumentException {
        return getArg(index, defaultValue, true);
    }

    protected String getArg(int index, String defaultValue, boolean optional) throws InvalidArgumentException {
        if (args.size() <= index) {
            if (optional) {
                return defaultValue;
            }
            throw new InvalidArgumentException();
        }

        return args.get(index);
    }

    protected Integer getArgInt(int index) throws InvalidArgumentException {
        return getArgInt(index, 0, false);
    }

    protected int getArgInt(int index, int defaultValue) throws InvalidArgumentException {
        return getArgInt(index, defaultValue, true);
    }

    protected int getArgInt(int index, int defaultValue, boolean optional) throws InvalidArgumentException {
        String value = getArg(index, String.valueOf(defaultValue), optional);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException();
        }
    }

    public boolean isExclamationRequired() {
        return requireExclamation;
    }

    public String getUsage() {
        return usage;
    }

}
