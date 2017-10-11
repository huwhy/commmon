package cn.huwhy.common.util;

import java.util.regex.Pattern;

/**
 * 正则帮助类
 */
public class RegexUtil {

    private static final Pattern DIGIT_PATTERN = Pattern.compile("^\\d+$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");

    public static boolean isDigit(String s) {
        return StringUtil.isNotEmpty(s) && DIGIT_PATTERN.matcher(s).matches();
    }

    public static boolean isPhone(String s) {
        return StringUtil.isNotEmpty(s) && PHONE_PATTERN.matcher(s).matches();
    }

    public static void main(String[] args) {
        System.out.println(isPhone("13242342341"));
    }
}
