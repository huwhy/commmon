package cn.huwhy.common.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    private static final String EMPTY = "";

    private static final ThreadLocal<Pattern> mobilePatternThreadLocal = new ThreadLocal<Pattern>() {
        @Override
        protected Pattern initialValue() {
            return Pattern.compile("^(1)\\d{10}$");
        }
    };

    private static final ThreadLocal<Pattern> emojiPatternThreadLocal = new ThreadLocal<Pattern>() {
        @Override
        protected Pattern initialValue() {
            return Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                    Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        }
    };

    public static boolean isEmpty(String value) {
        int strLen;
        if ((null == value) || (0 == (strLen = value.length())))
            return true;

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(value.charAt(i)))
                return false;
        }
        return true;
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }

        if (end < 0) {
            end = str.length() + end;
        }
        if (start < 0) {
            start = str.length() + start;
        }

        if (end > str.length()) {
            end = str.length();
        }

        if (start > end) {
            return EMPTY;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    public static boolean equals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }

    public static String byte2hex(byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buffer) {
            String stmp = Integer.toHexString(b & 0xFF);
            if (stmp.length() == 1)
                sb.append("0").append(stmp);
            else
                sb.append(stmp);
        }

        return sb.toString().toUpperCase();
    }

    public static byte[] hex2byte(String hex) {
        if (0 != hex.length() % 2)
            throw new IllegalArgumentException();

        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];

        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }

        return b;
    }

    public static boolean isValidMobile(String mobile) {
        return isEmpty(mobile) ? false : mobilePatternThreadLocal.get().matcher(mobile).matches();
    }

    public static String trim(String src) {
        return src == null ? "" : src.trim();
    }

    public static String toUpper(String src) {
        return src == null ? null : src.toUpperCase();
    }

    public static String toLower(String src) {
        return src == null ? null : src.toLowerCase();
    }

    public static boolean lenBetween(String str, int min, int max) {
        return str != null && str.length() >= min && str.length() <= max;
    }

    public static String filterEmoji(String src) {
        try {
            System.out.println(Arrays.toString(src.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
        }
        Matcher emojiMatcher = emojiPatternThreadLocal.get().matcher(src);
        if (emojiMatcher.find()) {
            return emojiMatcher.replaceAll("");
        }
        return src;
    }
}
