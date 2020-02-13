package moe.shizuku.manager.viewmodel;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedViewModelProviders {

    private static final Map<ViewModel, AtomicInteger> sRefCount = new HashMap<>();
    private static final ViewModelStore sStore = new ViewModelStore();

    private static class SharedViewModelProvider extends ViewModelProvider {

        private ViewModelStore mComponentStore;

        private SharedViewModelProvider(@NonNull ViewModelStore componentStore, @NonNull Factory factory) {
            super(sStore, factory);

            mComponentStore = componentStore;
        }

        private void put(String key, ViewModel viewModel) {
            if (!(viewModel instanceof SharedViewModel))
                throw new IllegalStateException("Getting non SharedViewModel from SharedViewModelProvider");

            ViewModelStoreUtils.put(mComponentStore, key, viewModel);

            if (!sRefCount.containsKey(viewModel)) {
                sRefCount.put(viewModel, new AtomicInteger());
            }

            //noinspection ConstantConditions
            sRefCount.get(viewModel).incrementAndGet();
        }

        @NonNull
        @Override
        public <T extends ViewModel> T get(@NonNull String key, @NonNull Class<T> modelClass) {
            T viewModel = super.get(key, modelClass);
            put(key, viewModel);
            return viewModel;
        }
    }

    public static void clear(@NonNull SharedViewModel viewModel) {
        //noinspection ConstantConditions
        if (sRefCount.containsKey(viewModel)
                && sRefCount.get(viewModel).decrementAndGet() == 0) {
            sRefCount.remove(viewModel);
            viewModel.onFullyCleared();

            ViewModelStoreUtils.remove(sStore, viewModel);
        }
    }

    private static Application checkApplication(Activity activity) {
        Application application = activity.getApplication();
        if (application == null) {
            throw new IllegalStateException("Your activity/fragment is not yet attached to "
                    + "Application. You can't request ViewModel before onCreate call.");
        }
        return application;
    }

    private static Activity checkActivity(Fragment fragment) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            throw new IllegalStateException("Can't create ViewModelProvider for detached fragment");
        }
        return activity;
    }

    @NonNull
    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment) {
        return of(fragment, null);
    }

    @NonNull
    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity) {
        return of(activity, null);
    }

    @NonNull
    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment, @Nullable ViewModelProvider.Factory factory) {
        Application application = checkApplication(checkActivity(fragment));
        if (factory == null) {
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        }
        return new SharedViewModelProvider(fragment.getViewModelStore(), factory);
    }

    @NonNull
    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity,
                                       @Nullable ViewModelProvider.Factory factory) {
        Application application = checkApplication(activity);
        if (factory == null) {
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        }
        return new SharedViewModelProvider(activity.getViewModelStore(), factory);
    }

}
