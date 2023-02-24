package net.yeyito.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFilter {
    @Deprecated public static String extractRobuxFromHTML(String input) {
        // Compile the regular expression
        String robuxLine = parseStringUsingRegex(input, "<span class=\"text-robux-lg wait-for-i18n-format-render \">(.*?)<");
        return robuxLine.replaceAll("[^0-9]", "");
    }

    @Deprecated public static String extractItemNameFromHTML(String input) {
        String nameLine = parseStringUsingRegex(input, "<title>(.*?)-");
        if (nameLine == null) {
            return null;
        }
        return nameLine.replaceAll("\\s+$", "");
    }

    @Deprecated public static String extractStatusFromHTML(String input) {
        String limitedLine = parseStringUsingRegex(input, "-md\">(.*?)<");

        if (limitedLine == null) {
            return "Regular";
        } else {
            return "Limited";
        }
    }

    public static Long extractLowestPriceFromHTML(String input) {
        List<String> prices = parseStringUsingRegexMatchAllDigits(input, "price\":(.*?),");
        if (prices == null || prices.isEmpty()) {
            return null;
        }
        long lowestPrice = Long.MAX_VALUE;
        for (String price : prices) {
            long currentPrice = Long.parseLong(price);
            if (currentPrice < lowestPrice) {
                lowestPrice = currentPrice;
            }
        }
        return lowestPrice;
    }

    public static Long extractRAPFromHTML(String input) {
        String recentAveragePrice = parseStringUsingRegex(input, "recentAveragePrice\":(.*?),");
        if (recentAveragePrice == null || recentAveragePrice.equals("null")) {
            return null;
        }
        return Long.parseLong(recentAveragePrice);
    }

    public static String parseStringUsingRegex(String input, String regex) {
        if (input == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public static List<String> parseStringUsingRegexMatchAllDigits(String input, String regex) {
        if (input == null) {
            return null;
        }
        List<String> matches = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            if (!matcher.group(1).replaceAll("[^0-9]","").equals("")) {
                matches.add(matcher.group(1));
            }
        }
        return matches;
    }
}
