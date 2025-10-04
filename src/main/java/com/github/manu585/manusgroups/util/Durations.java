package com.github.manu585.manusgroups.util;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class that utilizes Regex to parse time correctly
 */
public class Durations {
    private static final Pattern TOKEN = Pattern.compile("(\\d+)([DHMSdhms])");

    private Durations() {}

    public static Duration parse(String input) {
        if (input == null) throw new IllegalArgumentException("null");
        String s = input.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) throw new IllegalArgumentException("empty");

        Matcher matcher = TOKEN.matcher(s);
        int pos = 0;
        Duration total = Duration.ZERO;

        while (matcher.find()) {
            long amount = Long.parseLong(matcher.group(1));
            char unit = matcher.group(2).charAt(0);
            if (amount < 0) throw new IllegalArgumentException("Negatives are not allowed");

            switch (unit) {
                case 'd' -> total = total.plusDays(amount);
                case 'h' -> total = total.plusHours(amount);
                case 'm' -> total = total.plusMinutes(amount);
                case 's' -> total = total.plusSeconds(amount);
                default -> throw new IllegalArgumentException("Unknown time unit: " + unit);
            }
            pos = matcher.end();
        }

        if (pos != s.length()) {
            throw new IllegalArgumentException("Invalid duration at: " + s.substring(pos));
        }

        if (total.isZero()) {
            throw new IllegalArgumentException("Zero duration");
        }

        return total;
    }

    public static String formatCompact(Duration duration) {
        if (duration.isZero() || duration.isNegative()) return "0s";

        long seconds = duration.getSeconds();
        long days = seconds / 86_400; seconds %= 86_400;
        long hours = seconds / 3_600; seconds %= 3_600;
        long minutes = seconds / 60;  seconds %= 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append('d');
        if (hours > 0) sb.append(hours).append('h');
        if (minutes> 0) sb.append(minutes).append('m');
        if (seconds> 0) sb.append(seconds).append('s');

        return sb.toString();
    }
}
