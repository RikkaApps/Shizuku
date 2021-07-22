package moe.shizuku.server.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class UNIXProcess {

    private static Constructor<?> constructor;
    private static Field pidField;

    static {
        try {
            Class<?> cls = Class.forName("java.lang.UNIXProcess");

            /*
             * UNIXProcess(final byte[] prog,
             *             final byte[] argBlock, final int argc,
             *             final byte[] envBlock, final int envc,
             *             final byte[] dir,
             *             final int[] fds,
             *             final boolean redirectErrorStream)
             */
            constructor = cls.getDeclaredConstructor(
                    byte[].class,
                    byte[].class, int.class,
                    byte[].class, int.class,
                    byte[].class,
                    int[].class,
                    boolean.class);

            pidField = cls.getDeclaredField("pid");
            pidField.setAccessible(true);

            constructor.setAccessible(true);
        } catch (ReflectiveOperationException e) {
        }
    }

    private static byte[] toCString(String s) {
        if (s == null)
            return null;
        byte[] bytes = s.getBytes();
        byte[] result = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0,
                result, 0,
                bytes.length);
        result[result.length - 1] = (byte) 0;
        return result;
    }

    public static UNIXProcess create(String[] cmdarray, String dir, int[] std_fds) {
        byte[][] args = new byte[cmdarray.length - 1][];
        int size = args.length; // For added NUL bytes
        for (int i = 0; i < args.length; i++) {
            args[i] = cmdarray[i + 1].getBytes();
            size += args[i].length;
        }
        byte[] argBlock = new byte[size];
        int i = 0;
        for (byte[] arg : args) {
            System.arraycopy(arg, 0, argBlock, i, arg.length);
            i += arg.length + 1;
            // No need to write NUL bytes explicitly
        }

        int[] envc = new int[1];
        byte[] envBlock = new byte[0]/*ProcessEnvironment.toEnvironmentBlock(environment, envc)*/;

        try {
            Process process = (Process) constructor.newInstance(toCString(cmdarray[0]),
                    argBlock, args.length,
                    envBlock, envc[0],
                    toCString(dir),
                    std_fds,
                    false);
            return new UNIXProcess(process, pidField.getInt(process));
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private final Process process;
    private final int pid;

    private UNIXProcess(Process process, int pid) {
        this.process = process;
        this.pid = pid;
    }

    public Process getProcess() {
        return process;
    }

    public int getPid() {
        return pid;
    }
}
