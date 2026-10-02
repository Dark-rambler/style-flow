package com.styloflow.shared.domain.model;

public final class TextUtils {

    private TextUtils() {}

    /** Trims the text; empty or blank values become {@code null}. */
    public static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
