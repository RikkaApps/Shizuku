package moe.shizuku.manager.viewmodel;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.UUID;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuService;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.manager.AppConstants;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.model.ServiceStatus;
import moe.shizuku.server.IShizukuService;
import moe.shizuku.server.ServerConstants;

import static moe.shizuku.manager.utils.Logger.LOGGER;

public class HomeViewModel extends ViewModel {

    private static void requestBinder() {
        try (LocalSocket socket = new LocalSocket(LocalSocket.SOCKET_STREAM)) {
            socket.connect(new LocalSocketAddress(ShizukuApiConstants.SOCKET_NAME, LocalSocketAddress.Namespace.ABSTRACT));
            socket.setSoTimeout(ShizukuApiConstants.SOCKET_TIMEOUT);
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            os.writeInt(ShizukuApiConstants.SOCKET_VERSION_CODE);
            os.writeInt(ServerConstants.SOCKET_ACTION_MANAGER_REQUEST_BINDER);
            is.readInt();
        } catch (Exception e) {
            Log.w(AppConstants.TAG, "can't connect to server: " + e.getMessage(), e);
        }
    }

    private final ServiceStatus serviceStatus = new ServiceStatus();

    private final MutableLiveData<Object> mLiveData = new MutableLiveData<>();
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public void observe(LifecycleOwner lifecycleOwner, Observer<Object> observer) {
        mLiveData.observe(lifecycleOwner, observer);
    }

    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    private void loadInternal() {
        ShizukuState v2Status;
        serviceStatus.setV2Status((v2Status = ShizukuClient.getState()));

        if (ShizukuService.getBinder() != null) {
            if (ShizukuService.pingBinder()) {
                try {
                    serviceStatus.setUid(ShizukuService.getUid());
                    serviceStatus.setVersion(ShizukuService.getVersion());

                    if (v2Status.getCode() == ShizukuState.STATUS_UNAUTHORIZED) {
                        String token = IShizukuService.Stub.asInterface(ShizukuService.getBinder()).getToken();
                        ShizukuManagerSettings.putToken(UUID.fromString(token));

                        serviceStatus.setV2Status(ShizukuClient.getState());
                    }
                } catch (Throwable tr) {
                    LOGGER.w(tr, "");
                }
            } else {
                ShizukuService.setBinder(null);
            }
        } else {
            requestBinder();
        }
    }

    public void load() {
        mCompositeDisposable.add(Single
                .fromCallable(() -> {
                    loadInternal();
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .subscribe(mLiveData::postValue, mLiveData::postValue));
    }

    @Override
    protected void onCleared() {
        mCompositeDisposable.dispose();
    }
}
