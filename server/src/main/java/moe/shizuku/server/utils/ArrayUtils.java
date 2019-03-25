package moe.shizuku.server.utils;

public class ArrayUtils {

    public static boolean contains(String[] array, String item) {
        for (String i : array) {
            if (item.equals(i))
                return true;
        }
        return false;
    }
}
