package cn.huwhy.common.cache;

interface Cache {

    void putString(String key, String value);

    String getString(String key);

}
