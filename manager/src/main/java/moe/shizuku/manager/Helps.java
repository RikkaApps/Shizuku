package moe.shizuku.manager;

import moe.shizuku.manager.utils.MultiLocaleEntity;

public class Helps {

    public static final MultiLocaleEntity ADB = new MultiLocaleEntity();
    public static final MultiLocaleEntity ADB_ANDROID11 = new MultiLocaleEntity();
    public static final MultiLocaleEntity APPS = new MultiLocaleEntity();
    public static final MultiLocaleEntity HOME = new MultiLocaleEntity();
    public static final MultiLocaleEntity DOWNLOAD = new MultiLocaleEntity();
    public static final MultiLocaleEntity SUI = new MultiLocaleEntity();

    static {
        ADB.put("zh-CN", "https://shizuku.rikka.app/zh-hans/guide/setup.html");
        ADB.put("zh-TW", "https://shizuku.rikka.app/zh-hant/guide/setup.html");
        ADB.put("en", "https://shizuku.rikka.app/guide/setup.html");

        ADB_ANDROID11.put("zh-CN", "https://shizuku.rikka.app/zh-hans/guide/setup.html");
        ADB_ANDROID11.put("zh-TW", "https://shizuku.rikka.app/zh-hant/guide/setup.html");
        ADB_ANDROID11.put("en", "https://shizuku.rikka.app/guide/setup.html");

        APPS.put("zh-CN", "https://shizuku.rikka.app/zh-hans/apps.html");
        APPS.put("zh-TW", "https://shizuku.rikka.app/zh-hant/apps.html");
        APPS.put("en", "https://shizuku.rikka.app/apps.html");

        HOME.put("zh-CN", "https://shizuku.rikka.app/zh-hans/");
        HOME.put("zh-TW", "https://shizuku.rikka.app/zh-hant/");
        HOME.put("en", "https://shizuku.rikka.app/");

        DOWNLOAD.put("zh-CN", "https://shizuku.rikka.app/zh-hans/download.html");
        DOWNLOAD.put("zh-TW", "https://shizuku.rikka.app/zh-hant/download.html");
        DOWNLOAD.put("en", "https://shizuku.rikka.app/download.html");

        SUI.put("en", "https://github.com/RikkaApps/Sui");
    }
}
