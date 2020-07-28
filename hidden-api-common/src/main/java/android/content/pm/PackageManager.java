package android.content.pm;

public class PackageManager {

    public static class NameNotFoundException extends Exception {
        public NameNotFoundException() {
            throw new RuntimeException();
        }

        public NameNotFoundException(String name) {
            throw new RuntimeException();
        }
    }
}
