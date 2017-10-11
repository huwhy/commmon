package cn.huwhy.common.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TripleDES {
    private static final String ALGORITHM_3DES = "DESede";

    private static final String ENCODING       = "UTF-8";

    private static final String CIPHER_NAME    = "DESede/ECB/NoPadding";

    private String key;

    private final ThreadLocal<Cipher> encryptCipherThreadLocal = new ThreadLocal<Cipher>() {
        @Override
        protected Cipher initialValue() {
            try {
                SecretKey secretKey = new SecretKeySpec(key.getBytes(ENCODING), ALGORITHM_3DES);
                Cipher cipher = Cipher.getInstance(CIPHER_NAME);

                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                return cipher;
            } catch (Exception ignore) {
                return null;
            }
        }
    };

    private final ThreadLocal<Cipher> decryptCipherThreadLocal = new ThreadLocal<Cipher>() {
        @Override
        protected Cipher initialValue() {
            try {
                SecretKey secretKey = new SecretKeySpec(key.getBytes(ENCODING), ALGORITHM_3DES);
                Cipher cipher = Cipher.getInstance(CIPHER_NAME);

                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                return cipher;
            } catch (Exception ignore) {
                return null;
            }
        }
    };

    private byte[] encrypt(byte[] src) {
        try {
            return encryptCipherThreadLocal.get().doFinal(fill(src));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] decrypt(byte[] src) {
        try {
            return trim(decryptCipherThreadLocal.get().doFinal(src));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String encryptBytes(byte[] bytes) {
        return StringUtil.byte2hex(encrypt(bytes));
    }

    public String decryptBytes(byte[] bytes) {
        try {
            return new String(decrypt(bytes), ENCODING);
        } catch (Exception ex) {
            return null;
        }
    }

    public String encrypt(String src) {
        try {
            return StringUtil.byte2hex(encrypt(src.getBytes(ENCODING)));
        } catch (Exception ignore) {
            return null;
        }
    }

    public String decrypt(String src) {
        try {
            return new String(decrypt(StringUtil.hex2byte(src)), ENCODING);
        } catch (Exception ignore) {
            return null;
        }
    }

    private byte[] fill(byte[] bytes) {
        if (null == bytes)
            return null;

        int bytesLength = bytes.length;
        if (0 == bytesLength % 8)
            return bytes;

        int newBytesLength = (bytesLength / 8 + 1) * 8;
        byte[] newBytes = new byte[newBytesLength];

        for (int i = 0; i < bytesLength; i++)
            newBytes[i] = bytes[i];
        for (int k = bytesLength; k < newBytesLength; k++)
            newBytes[k] = 0;

        return newBytes;
    }

    private byte[] trim(byte[] bytes) {
        if (bytes == null)
            return null;

        int bytesLength = bytes.length;
        int i = bytesLength - 1;
        for (; i >= 0; i--) {
            if (bytes[i] != 0) {
                break;
            }
        }

        byte[] newBytes = new byte[i + 1];
        for (int k = 0; k <= i; k++) {
            newBytes[k] = bytes[k];
        }

        return newBytes;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
