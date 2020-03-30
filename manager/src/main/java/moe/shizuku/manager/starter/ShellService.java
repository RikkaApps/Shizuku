package moe.shizuku.manager.starter;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.topjohnwu.superuser.CallbackList;
import com.topjohnwu.superuser.Shell;

public class ShellService extends Service {

    public interface Listener {
        void onCommandResult(int exitCode);
        void onLine(String line);
        void onFailed();
    }

    public class ShellServiceBinder extends Binder {

        public void run(String[] command, Listener listener) {
            ShellService.this.run(command, listener);
        }
    }

    private ShellServiceBinder mBinder = new ShellServiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void run(String[] command, Listener listener) {
        openRootShell(command, listener);
    }

    private void openRootShell(final String[] command, final Listener listener) {
        if (!Shell.rootAccess()) {
            if (listener != null)
                listener.onFailed();

            return;
        }

        Shell.su(command).to(new CallbackList<String>() {

            @Override
            public void onAddElement(String s) {
                if (listener != null)
                    listener.onLine(s);
            }

        }).submit(out -> {
            if (listener != null)
                listener.onCommandResult(out.getCode());
        });
    }

    private void kill() {
        if (Shell.getCachedShell() != null) {
            try {
                Shell.getCachedShell().close();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        kill();
    }
}
