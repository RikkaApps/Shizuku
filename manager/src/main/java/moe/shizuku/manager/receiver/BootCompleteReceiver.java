package moe.shizuku.manager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import moe.shizuku.manager.Constants;
import moe.shizuku.manager.ServerLauncher;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.ShizukuManagerSettings.LaunchMethod;
import moe.shizuku.manager.ShizukuManagerSettings.RootLaunchMethod;
import moe.shizuku.manager.service.ShellService;
import moe.shizuku.manager.utils.BindServiceHelper;

/**
 * Created by Rikka on 2017/5/24.
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        if (ShizukuManagerSettings.getLastLaunchMode() == LaunchMethod.ROOT) {
            Log.i(Constants.TAG, "start on boot");

            final BindServiceHelper helper = new BindServiceHelper(context, ShellService.class);
            helper.bind(new BindServiceHelper.OnServiceConnectedListener() {
                @Override
                public void onServiceConnected(IBinder binder) {
                    ShellService.ShellServiceBinder service = (ShellService.ShellServiceBinder) binder;

                    ShellService.Listener listener = new ShellService.Listener() {
                        @Override
                        public void onFailed() {
                            helper.unbind();
                        }

                        @Override
                        public void onCommandResult(int commandCode, int exitCode) {
                            helper.unbind();
                        }

                        @Override
                        public void onLine(String line) {

                        }
                    };

                    if (ShizukuManagerSettings.getRootLaunchMethod() == RootLaunchMethod.ALTERNATIVE) {
                        service.run(ServerLauncher.COMMAND_ROOT_OLD, 0, listener);
                    } else {
                        service.run(ServerLauncher.COMMAND_ROOT, 0, listener);
                    }
                }
            });
        }
    }
}
