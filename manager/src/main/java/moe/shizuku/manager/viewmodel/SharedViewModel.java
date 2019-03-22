package moe.shizuku.manager.viewmodel;

import androidx.annotation.CallSuper;
import androidx.lifecycle.ViewModel;

public abstract class SharedViewModel extends ViewModel {

    @CallSuper
    @Override
    protected void onCleared() {
        SharedViewModelProviders.clear(this);
    }

    protected abstract void onFullyCleared();
}
