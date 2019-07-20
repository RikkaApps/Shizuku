package moe.shizuku.manager;

import moe.shizuku.manager.utils.MultiLocaleEntity;

public class Helps {

    public static final MultiLocaleEntity ADB = new MultiLocaleEntity();
    public static final MultiLocaleEntity APPS = new MultiLocaleEntity();

    static {
        ADB.put("zh-CN", "https://docs.rikka.app/shizuku/zh-hans/setup.html#%E8%AE%BE%E5%A4%87%E6%9C%AA-root");
        ADB.put("zh-TW", "https://docs.rikka.app/shizuku/zh-hant/setup.html#%E8%A3%9D%E7%BD%AE%E6%9C%AA-root");
        ADB.put("en", "https://docs.rikka.app/shizuku/en/setup.html#device-is-not-rooted");

        APPS.put("zh-CN", "https://docs.rikka.app/shizuku/zh-hans/apps.html");
        APPS.put("zh-TW", "https://docs.rikka.app/shizuku/zh-hant/apps.html");
        APPS.put("en", "https://docs.rikka.app/shizuku/en/apps.html");
    }
}
