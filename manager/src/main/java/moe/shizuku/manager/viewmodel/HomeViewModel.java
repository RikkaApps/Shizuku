package moe.shizuku.manager.viewmodel;

import java.util.UUID;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.api.ShizukuService;
import moe.shizuku.manager.legacy.ShizukuLegacy;
import moe.shizuku.manager.model.ServiceStatus;
import moe.shizuku.server.IShizukuService;

import static moe.shizuku.manager.utils.Logger.LOGGER;

public class HomeViewModel extends ViewModel {

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
        ShizukuLegacy.ShizukuState v2Status;
        serviceStatus.setV2Status((v2Status = ShizukuLegacy.ShizukuClient.getState()));

        if (ShizukuService.getBinder() != null) {
            if (ShizukuService.pingBinder()) {
                try {
                    serviceStatus.setUid(ShizukuService.getUid());
                    serviceStatus.setVersion(ShizukuService.getVersion());
                    if (serviceStatus.getVersion() >= 6) {
                        try {
                            serviceStatus.setSEContext(ShizukuService.getSELinuxContext());
                        } catch (Throwable tr) {
                            LOGGER.w(tr, "getSELinuxContext");
                        }
                    }

                    if (v2Status.getCode() == ShizukuLegacy.ShizukuState.STATUS_UNAUTHORIZED) {
                        String token = IShizukuService.Stub.asInterface(ShizukuService.getBinder()).getToken();
                        ShizukuLegacy.putToken(UUID.fromString(token));

                        serviceStatus.setV2Status(ShizukuLegacy.ShizukuClient.getState());
                    }
                } catch (Throwable tr) {
                    LOGGER.w(tr, "");
                }
            } else {
                ShizukuService.setBinder(null);
            }
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
