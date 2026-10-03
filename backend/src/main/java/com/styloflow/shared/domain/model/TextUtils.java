package com.styloflow.shared.domain.model;

public final class TextUtils {

    private TextUtils() {}

    public static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
