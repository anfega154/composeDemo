package com.mantum.core.util;

import androidx.annotation.NonNull;
import android.webkit.MimeTypeMap;

public abstract class Tool {

    private final static String NON_THIN = "[^iIl1.,']";

    private static int textWidth(String str) {
        str = Assert.isNull(str) ? "" : str;
        return str.length() - str.replaceAll(NON_THIN, "").length() / 2;
    }

    public static String mime(@NonNull String name) {
        name = name.replace(" ", "%20");
        String extension = MimeTypeMap.getFileExtensionFromUrl(name);
        if (extension != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return null;
    }

    public static String formData(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String) {
            return (String) value;
        }

        return String.valueOf(value);
    }

    /**
     * http://stackoverflow.com/questions/3597550/ideal-method-to-truncate-a-string-with-ellipsis
     */
    public static String ellipsize(String text, int max) {
        if (text == null || text.isEmpty())
            return "";

        if (textWidth(text) <= max)
            return text;

        // Start by chopping off at the word before max
        // This is an over-approximation due to thin-characters...
        int end = text.lastIndexOf(' ', max - 3);

        // Just one long word. Chop it off.
        if (end == -1)
            return text.substring(0, max-3) + "...";

        // Step forward as long as textWidth allows.
        int newEnd = end;
        do {
            end = newEnd;
            newEnd = text.indexOf(' ', end + 1);

            // No more spaces.
            if (newEnd == -1)
                newEnd = text.length();

        } while (textWidth(text.substring(0, newEnd) + "...") < max);

        return text.substring(0, end) + "...";
    }
}