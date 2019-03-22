package moe.shizuku.manager.viewmodel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelStore;

public class ViewModelStoreUtils {

    private static Field mMapField;
    private static Method putMethod;

    static {
        try {
            mMapField = ViewModelStore.class.getDeclaredField("mMap");
            mMapField.setAccessible(true);
            putMethod = ViewModelStore.class.getDeclaredMethod("put", String.class, ViewModel.class);
            putMethod.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, ViewModel> getMap(ViewModelStore store) {
        try {
            //noinspection unchecked
            return (Map<String, ViewModel>) mMapField.get(store);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void put(ViewModelStore store, String key, ViewModel viewModel) {
        try {
            putMethod.invoke(store, key, viewModel);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void remove(ViewModelStore store, ViewModel viewModel) {
        Map<String, ViewModel> map = getMap(store);
        String keyToRemove = null;
        for (Map.Entry<String, ViewModel> entry : map.entrySet()) {
            if (entry.getValue() == viewModel) {
                keyToRemove = entry.getKey();
                break;
            }
        }
        if (keyToRemove != null)
            map.remove(keyToRemove);
    }
}