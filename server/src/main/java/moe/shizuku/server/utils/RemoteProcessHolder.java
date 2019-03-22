package moe.shizuku.server.utils;

import android.os.ParcelFileDescriptor;

import java.io.IOException;

import moe.shizuku.server.IRemoteProcess;

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
        process.exitValue();
    }
}
