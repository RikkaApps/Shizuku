package moe.shizuku.manager;

import moe.shizuku.manager.utils.MultiLocaleEntity;

public class Helps {

    public static final MultiLocaleEntity ADB = new MultiLocaleEntity();
    public static final MultiLocaleEntity APPS = new MultiLocaleEntity();

    static {
        ADB.put("zh-CN", "https://rikka.app/shizuku/docs/zh-CN/?doc=adb&title=使用%20adb%20启动");
        ADB.put("zh-TW", "https://rikka.app/shizuku/docs/zh-TW/?doc=adb&title=使用%20adb%20启动");
        ADB.put("en", "https://rikka.app/shizuku/docs/en/?doc=adb&title=Start%20with%20adb");

        APPS.put("zh-CN", "https://rikka.app/shizuku/docs/zh-CN/?doc=apps&title=使用%20Shizuku%20的应用");
        APPS.put("zh-TW", "https://rikka.app/shizuku/docs/zh-TW/?doc=apps&title=使用%20Shizuku%20的應用程式");
        APPS.put("en", "https://rikka.app/shizuku/docs/en/?doc=apps&title=Apps%20using%20Shizuku");
    }
}
