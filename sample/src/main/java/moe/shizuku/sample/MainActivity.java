package moe.shizuku.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;

public class MainActivity extends Activity {

    static {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
