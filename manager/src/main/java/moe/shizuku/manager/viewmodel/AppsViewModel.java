package moe.shizuku.manager.viewmodel;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.manager.BuildConfig;
import moe.shizuku.manager.authorization.AuthorizationManager;
import moe.shizuku.manager.utils.AppNameComparator;

public class AppsViewModel extends SharedViewModel {

    private List<PackageInfo> mData;
    private final MutableLiveData<Object> mLiveData = new MutableLiveData<>();
    private Disposable mLoadDisposable;

    public void observe(LifecycleOwner lifecycleOwner, Observer<Object> observer) {
        mLiveData.observe(lifecycleOwner, observer);
    }

    public List<PackageInfo> getData() {
        return mData;
    }

    @Override
    protected void onFullyCleared() {
        if (mLoadDisposable != null)
            mLoadDisposable.dispose();
    }

    public void load(Context context) {
        mLoadDisposable = Single
                .fromCallable(() -> {
                    List<PackageInfo> list = new ArrayList<>();
                    for (String packageName : AuthorizationManager.getPackages()) {
                        if (BuildConfig.APPLICATION_ID.equals(packageName)) {
                            continue;
                        }

                        try {
                            list.add(context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA));
                        } catch (PackageManager.NameNotFoundException ignored) {
                        }
                    }
                    Collections.sort(list, new AppNameComparator(context).getPackageInfoComparator());
                    return list;
                })
                .subscribeOn(Schedulers.io())
                .subscribe(list -> {
                    mData = list;
                    mLiveData.postValue(list);
                }, mLiveData::postValue);
    }
}
