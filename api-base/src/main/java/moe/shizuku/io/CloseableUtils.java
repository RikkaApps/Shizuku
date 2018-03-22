package moe.shizuku.io;

import java.io.Closeable;
import java.io.IOException;

public class CloseableUtils {

    public static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }
}
