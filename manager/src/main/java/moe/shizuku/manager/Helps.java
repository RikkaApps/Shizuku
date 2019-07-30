package moe.shizuku.manager;

import moe.shizuku.manager.utils.MultiLocaleEntity;

public class Helps {

    public static final MultiLocaleEntity ADB = new MultiLocaleEntity();
    public static final MultiLocaleEntity APPS = new MultiLocaleEntity();
    public static final MultiLocaleEntity HOME = new MultiLocaleEntity();

    static {
        ADB.put("zh-CN", "https://shizuku.rikka.app/zh-hans/guide/setup.html#%E8%AE%BE%E5%A4%87%E6%9C%AA-root");
        ADB.put("zh-TW", "https://shizuku.rikka.app/zh-hant/guide/setup.html#%E8%A3%9D%E7%BD%AE%E6%9C%AA-root");
        ADB.put("en", "https://shizuku.rikka.app/guide/setup.html#device-is-not-rooted");

        APPS.put("zh-CN", "https://shizuku.rikka.app/zh-hans/apps.html");
        APPS.put("zh-TW", "https://shizuku.rikka.app/zh-hant/apps.html");
        APPS.put("en", "https://shizuku.rikka.app/apps.html");

        HOME.put("zh-CN", "https://shizuku.rikka.app/zh-hans/");
        HOME.put("zh-TW", "https://shizuku.rikka.app/zh-hant/");
        HOME.put("en", "https://shizuku.rikka.app/");
    }
}
