package moe.shizuku.sample;

import android.os.Build;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

@SuppressWarnings("unused")
public class FileUtils {

    private static final long COPY_CHECKPOINT_BYTES = 524288;

    public interface ProgressListener {
        void onProgress(long progress);
    }

    /**
     * Copy the contents of one file to another, replacing any existing content.
     * <p>
     * Attempts to use several optimization strategies to copy the data in the
     * kernel before falling back to a userspace copy as a last resort.
     *
     * @return number of bytes copied.
     */
    public static long copy(@NonNull File from, @NonNull File to) throws IOException {
        return copy(from, to, null, null, null);
    }

    /**
     * Copy the contents of one file to another, replacing any existing content.
     * <p>
     * Attempts to use several optimization strategies to copy the data in the
     * kernel before falling back to a userspace copy as a last resort.
     *
     * @param signal   to signal if the copy should be cancelled early.
     * @param executor that listener events should be delivered via.
     * @param listener to be periodically notified as the copy progresses.
     * @return number of bytes copied.
     */
    public static long copy(@NonNull File from, @NonNull File to,
                            @Nullable CancellationSignal signal, @Nullable Executor executor,
                            @Nullable ProgressListener listener) throws IOException {
        try (FileInputStream in = new FileInputStream(from);
             FileOutputStream out = new FileOutputStream(to)) {
            return copy(in, out, signal, executor, listener);
        }
    }

    /**
     * Copy the contents of one stream to another.
     * <p>
     * Attempts to use several optimization strategies to copy the data in the
     * kernel before falling back to a userspace copy as a last resort.
     *
     * @return number of bytes copied.
     */
    public static long copy(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        return copy(in, out, null, null, null);
    }

    /**
     * Copy the contents of one stream to another.
     * <p>
     * Attempts to use several optimization strategies to copy the data in the
     * kernel before falling back to a userspace copy as a last resort.
     *
     * @param signal   to signal if the copy should be cancelled early.
     * @param executor that listener events should be delivered via.
     * @param listener to be periodically notified as the copy progresses.
     * @return number of bytes copied.
     */
    public static long copy(@NonNull InputStream in, @NonNull OutputStream out,
                            @Nullable CancellationSignal signal, @Nullable Executor executor,
                            @Nullable ProgressListener listener) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && in instanceof FileInputStream
                && out instanceof FileOutputStream) {
            return android.os.FileUtils.copy(((FileInputStream) in).getFD(), ((FileOutputStream) out).getFD(),
                    signal, executor, listener != null ? (android.os.FileUtils.ProgressListener) listener::onProgress : null);
        } else {
            return copyInternalUserspace(in, out, signal, executor, listener);
        }
    }

    private static long copyInternalUserspace(InputStream in, OutputStream out,
                                              CancellationSignal signal, Executor executor, ProgressListener listener)
            throws IOException {
        long progress = 0;
        long checkpoint = 0;
        byte[] buffer = new byte[8192];

        int t;
        while ((t = in.read(buffer)) != -1) {
            out.write(buffer, 0, t);

            progress += t;
            checkpoint += t;

            if (checkpoint >= COPY_CHECKPOINT_BYTES) {
                if (signal != null) {
                    signal.throwIfCanceled();
                }
                if (executor != null && listener != null) {
                    final long progressSnapshot = progress;
                    executor.execute(() -> {
                        listener.onProgress(progressSnapshot);
                    });
                }
                checkpoint = 0;
            }
        }
        if (executor != null && listener != null) {
            final long progressSnapshot = progress;
            executor.execute(() -> {
                listener.onProgress(progressSnapshot);
            });
        }
        return progress;
    }
}
