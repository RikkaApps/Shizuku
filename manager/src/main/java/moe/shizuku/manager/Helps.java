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
        ADB.put("zh-CN", "https://shizuku.rikka.app/zh-hans/guide/setup/");
        ADB.put("zh-TW", "https://shizuku.rikka.app/zh-hant/guide/setup/");
        ADB.put("en", "https://shizuku.rikka.app/guide/setup/");

        ADB_ANDROID11.put("zh-CN", "https://shizuku.rikka.app/zh-hans/guide/setup/");
        ADB_ANDROID11.put("zh-TW", "https://shizuku.rikka.app/zh-hant/guide/setup/");
        ADB_ANDROID11.put("en", "https://shizuku.rikka.app/guide/setup/");

        APPS.put("zh-CN", "https://shizuku.rikka.app/zh-hans/apps/");
        APPS.put("zh-TW", "https://shizuku.rikka.app/zh-hant/apps/");
        APPS.put("en", "https://shizuku.rikka.app/apps/");

        HOME.put("zh-CN", "https://shizuku.rikka.app/zh-hans/");
        HOME.put("zh-TW", "https://shizuku.rikka.app/zh-hant/");
        HOME.put("en", "https://shizuku.rikka.app/");

        DOWNLOAD.put("zh-CN", "https://shizuku.rikka.app/zh-hans/download/");
        DOWNLOAD.put("zh-TW", "https://shizuku.rikka.app/zh-hant/download/");
        DOWNLOAD.put("en", "https://shizuku.rikka.app/download/");

        SUI.put("en", "https://github.com/RikkaApps/Sui");
    }
}
