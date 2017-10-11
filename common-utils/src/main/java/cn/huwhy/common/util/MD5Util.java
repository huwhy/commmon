package cn.huwhy.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public static final String encrypt(String value) {
        return encrypt(value, null, false);
    }

    public static final String encrypt(String value, Object salt, boolean base64) {
        try {
            String saltedPass = mergePasswordAndSalt(value, salt, false);

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            byte[] digest = messageDigest.digest(Utf8.encode(saltedPass));

            if (base64) {
                return Utf8.decode(Base64.encode(digest));
            } else {
                return new String(Hex.encode(digest));
            }

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String mergePasswordAndSalt(String password, Object salt, boolean strict) {
        if (password == null) {
            password = "";
        }

        if (strict && (salt != null)) {
            if ((salt.toString().lastIndexOf("{") != -1)
                    || (salt.toString().lastIndexOf("}") != -1)) {
                throw new IllegalArgumentException("Cannot use { or } in salt.toString()");
            }
        }

        if ((salt == null) || "".equals(salt)) {
            return password;
        } else {
            return password + "{" + salt.toString() + "}";
        }
    }

    public static void main(String[] args) {
        String a = "1234";
        System.out.println(encrypt(a));
    }
}
