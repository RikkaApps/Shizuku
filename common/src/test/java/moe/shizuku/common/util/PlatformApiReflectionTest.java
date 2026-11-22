package moe.shizuku.common.util;

import java.lang.reflect.Method;
import java.util.List;

/** Run with javac/java; no Android runtime or test framework is required. */
public final class PlatformApiReflectionTest {

    public static class Slice {
    }

    public static final class PackageInfoList extends Slice {
    }

    public interface OldPackageManager {
        Slice getInstalledPackages(long flags, int userId) throws Exception;
    }

    public interface NewPackageManager {
        PackageInfoList getInstalledPackages(long flags, int userId);
    }

    public interface LegacyPackageManager {
        Slice getInstalledPackages(int flags, int userId);
    }

    public interface SingleBooleanUsers {
        List<Integer> getUsers(boolean excludeDying);
    }

    public interface ThreeBooleanUsers {
        List<Integer> getUsers(boolean excludePartial, boolean excludeDying, boolean excludePreCreated);
    }

    public static void main(String[] args) throws Throwable {
        packageReturnTypesAndFlags();
        userSignatures();
        failuresRemainFailures();
        System.out.println("Discovery reflection regression tests passed (return types, flags, user signatures, failures).");
    }

    private static void packageReturnTypesAndFlags() throws Throwable {
        Slice oldSlice = new Slice();
        long highFlags = 1L << 40;
        OldPackageManager old = (flags, user) -> {
            check(flags == highFlags && user == 150, "long flags or user changed");
            return oldSlice;
        };
        Method oldMethod = PlatformApiReflection.installedPackagesMethod(OldPackageManager.class, true);
        check(PlatformApiReflection.installedPackages(oldMethod, old, highFlags, 150) == oldSlice,
                "old slice was not retained");

        PackageInfoList newSlice = new PackageInfoList();
        NewPackageManager current = (flags, user) -> newSlice;
        Method newMethod = PlatformApiReflection.installedPackagesMethod(NewPackageManager.class, true);
        check(newMethod.getReturnType() == PackageInfoList.class, "new return type was not resolved");
        check(PlatformApiReflection.installedPackages(newMethod, current, 0, 0) == newSlice,
                "Android 17-style return type failed");

        LegacyPackageManager legacy = (flags, user) -> {
            check(flags == -1 && user == 0, "legacy bitmask or user changed");
            return oldSlice;
        };
        Method legacyMethod = PlatformApiReflection.installedPackagesMethod(LegacyPackageManager.class, false);
        check(PlatformApiReflection.installedPackages(legacyMethod, legacy, 0xffff_ffffL, 0) == oldSlice,
                "int flags were not boxed as Integer");
        check(PlatformApiReflection.installedPackages(legacyMethod, legacy, -1, 0) == oldSlice,
                "signed legacy bitmask changed");
        try {
            PlatformApiReflection.installedPackages(legacyMethod, legacy, highFlags, 0);
            throw new AssertionError("out-of-range flags were silently truncated");
        } catch (IllegalArgumentException expected) {
            // The old API cannot represent flags above 32 bits.
        }
    }

    private static void userSignatures() throws Throwable {
        SingleBooleanUsers current = excludeDying -> {
            check(!excludeDying, "single boolean changed");
            return List.of(0, 150);
        };
        Method single = PlatformApiReflection.usersMethod(SingleBooleanUsers.class);
        check(PlatformApiReflection.users(single, current, false).equals(List.of(0, 150)),
                "single-boolean users failed");

        ThreeBooleanUsers older = (excludePartial, excludeDying, excludePreCreated) -> {
            check(excludePartial && !excludeDying && excludePreCreated, "three booleans changed");
            return List.of(0);
        };
        Method triple = PlatformApiReflection.usersMethod(ThreeBooleanUsers.class);
        check(PlatformApiReflection.users(triple, older, false).equals(List.of(0)),
                "three-boolean users failed");
    }

    private static void failuresRemainFailures() throws Throwable {
        Method method = PlatformApiReflection.installedPackagesMethod(OldPackageManager.class, true);
        Throwable[] failures = {
                new SecurityException("profile access denied"),
                new Exception("remote call failed"),
                new NoSuchMethodError("platform API changed")
        };
        for (Throwable failure : failures) {
            OldPackageManager broken = (flags, user) -> {
                if (failure instanceof Error) throw (Error) failure;
                throw (Exception) failure;
            };
            try {
                PlatformApiReflection.installedPackages(method, broken, 0, 150);
                throw new AssertionError("failure was reported as a result");
            } catch (Throwable actual) {
                check(actual == failure, "original cause was wrapped or replaced");
            }
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
