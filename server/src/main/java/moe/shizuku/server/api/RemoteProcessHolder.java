package moe.shizuku.server.api;

import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import moe.shizuku.server.IRemoteProcess;
import moe.shizuku.server.utils.ParcelFileDescriptorUtil;

public class RemoteProcessHolder extends IRemoteProcess.Stub {

    private Process process;

    public RemoteProcessHolder(Process process) {
        this.process = process;
    }

    @Override
    public ParcelFileDescriptor getOutputStream() {
        try {
            return ParcelFileDescriptorUtil.pipeTo(process.getOutputStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public ParcelFileDescriptor getInputStream() {
        try {
            return ParcelFileDescriptorUtil.pipeFrom(process.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public ParcelFileDescriptor getErrorStream() {
        try {
            return ParcelFileDescriptorUtil.pipeFrom(process.getErrorStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int waitFor() {
        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int exitValue() {
        return process.exitValue();
    }

    @Override
    public void destroy() {
        process.destroy();
    }

    @Override
    public boolean alive() throws RemoteException {
        try {
            this.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    @Override
    public boolean waitForTimeout(long timeout, String unitName) throws RemoteException {
        TimeUnit unit = TimeUnit.valueOf(unitName);
        long startTime = System.nanoTime();
        long rem = unit.toNanos(timeout);

        do {
            try {
                exitValue();
                return true;
            } catch(IllegalThreadStateException ex) {
                if (rem > 0) {
                    try {
                        Thread.sleep(
                                Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
                    } catch (InterruptedException e) {
                        throw new IllegalStateException();
                    }
                }
            }
            rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        } while (rem > 0);
        return false;
    }
}
