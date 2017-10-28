package moe.shizuku.manager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.UUID;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.api.ShizukuClient;

public abstract class AbstractAuthorizationActivity extends Activity {

    private static final String ACTION_AUTHORIZATION = BuildConfig.APPLICATION_ID + ".intent.action.AUTHORIZATION_RESULT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setResult(boolean granted, String packageName) {
        if (granted) {
            UUID token = ShizukuManagerSettings.getToken(this);
            Intent intent = new Intent(ACTION_AUTHORIZATION)
                    .setPackage(packageName)
                    .putExtra(ShizukuConstants.EXTRA_TOKEN_MOST_SIG, token.getMostSignificantBits())
                    .putExtra(ShizukuConstants.EXTRA_TOKEN_LEAST_SIG, token.getLeastSignificantBits());

            setResult(ShizukuClient.AUTH_RESULT_OK, intent);
        } else {
            setResult(ShizukuClient.AUTH_RESULT_USER_DENIED);
        }
    }
}
