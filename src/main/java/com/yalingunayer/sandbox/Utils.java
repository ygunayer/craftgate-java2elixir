package com.yalingunayer.sandbox;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    public static final Pattern CASE_SPLITTERS = Pattern.compile("(?<!([A-Z\\-_.\\s]))");
    public static final Pattern IGNORED = Pattern.compile("^[-_.]+");

    private static final List<Character> DELIMITERS = List.of(' ', '\n', '\r', '\t', '_', '-', '.');

    private enum State {
        INIT,
        UPPERCASE,
        LOWERCASE,
        NUMERIC,
        DELIMITER,
        OTHER;

        public static State nextFor(char c) {
            if (Character.isLowerCase(c)) {
                return LOWERCASE;
            }

            if (Character.isUpperCase(c)) {
                return UPPERCASE;
            }

            if (Character.isDigit(c)) {
                return NUMERIC;
            }

            if (isDelimiter(c)) {
                return DELIMITER;
            }

            return OTHER;
        }

        public boolean canFollowFrom(State prevState) {
            if (this == LOWERCASE) {
                return prevState == UPPERCASE || prevState == NUMERIC;
            }

            if (this == UPPERCASE) {
                return prevState == NUMERIC;
            }

            return false;
        }
    }

    public static boolean isDelimiter(char c) {
        return DELIMITERS.contains(c);
    }

    public static List<String> segmentize(String s) {
        var segments = new ArrayList<String>();

        var sb = new StringBuilder();
        var state = State.INIT;

        for (char c : s.toCharArray()) {
            var nextState = State.nextFor(c);

            var shouldBreak = state != nextState;

            if (shouldBreak && nextState.canFollowFrom(state) && sb.length() < 2) {
                shouldBreak = false;
            }

            if (shouldBreak) {
                if (!sb.isEmpty() && state != State.DELIMITER) {
                    segments.add(sb.toString());
                }

                sb = new StringBuilder();
            }

            sb.append(c);
            state = nextState;
        }

        if (!sb.isEmpty() && state != State.DELIMITER) {
            segments.add(sb.toString());
        }

        return segments;
    }

    public static String toTitleCase(String s) {
        return segmentize(s)
                .stream()
                .map(str -> Character.toTitleCase(str.charAt(0)) + str.substring(1))
                .collect(Collectors.joining());
    }

    public static String toSnakeCase(String s) {
        return segmentize(s)
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.joining("_"));
    }

    public static String packageNameToNamespace(String name) {
        var skippedSegments = name.startsWith("io.craftgate") ? 1 : 0;
        return segmentize(name)
                .stream()
                .skip(skippedSegments)
                .map(Utils::toTitleCase)
                .collect(Collectors.joining("."));
    }
}
