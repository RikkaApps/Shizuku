package moe.shizuku.server.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by rikka on 2017/9/23.
 */

public class Utils {

    public static void setOut() throws IOException {
        File file = new File("/data/local/tmp/rikka_server2.log");
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }
        PrintStream os = new PrintStream(file);

        System.setOut(os);
        System.setErr(os);

        ServerLog.v("set output to /data/local/tmp/rikka_server2.log");
    }
}
