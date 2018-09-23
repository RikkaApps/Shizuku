package androidx.fragment.app;

import android.content.Intent;

public class Fragment {

    final public FragmentActivity getActivity() {
        throw new RuntimeException("STUB");
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        throw new RuntimeException("STUB");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        throw new RuntimeException("STUB");
    }

    public final void requestPermissions(String[] permissions, int requestCode) {
        throw new RuntimeException("STUB");
    }
}
