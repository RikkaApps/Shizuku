package moe.shizuku.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.UUID;

public class ShizukuClientHelperPre23 {

    private static final String KEY_TOKEN_MOST_SIG = "moe.shizuku.privilege.api.token_most";
    private static final String KEY_TOKEN_LEAST_SIG = "moe.shizuku.privilege.api.token_least";

    private static String sToken = "";

    public static boolean isPreM() {
        return Build.VERSION.SDK_INT < 23;
    }

    public static String setPre23Token(@NonNull Intent intent, @NonNull Context context) {
        long mostSig = intent.getLongExtra(ShizukuApiConstants.EXTRA_PRE_23_TOKEN_MOST_SIG, 0);
        long leastSig = intent.getLongExtra(ShizukuApiConstants.EXTRA_PRE_23_TOKEN_LEAST_SIG, 0);
        if (mostSig != 0 && leastSig != 0) {
            String token = new UUID(mostSig, leastSig).toString();
            setPre23Token(token, context);
            return token;
        }
        return null;
    }

    private static void setPre23Token(String token, Context context) {
        sToken = token;
        savePre23Token(context, UUID.fromString(token));
    }

    public static String loadPre23Token(@NonNull Context context) {
        SharedPreferences preferences = context.getSharedPreferences("moe.shizuku.privilege.api.token", Context.MODE_PRIVATE);
        long mostSig = preferences.getLong(KEY_TOKEN_MOST_SIG, 0);
        long leastSig = preferences.getLong(KEY_TOKEN_LEAST_SIG, 0);
        sToken = new UUID(mostSig, leastSig).toString();
        return sToken;
    }

    private static void savePre23Token(@NonNull Context context, @NonNull UUID token) {
        SharedPreferences preferences = context.getSharedPreferences("moe.shizuku.privilege.api.token", Context.MODE_PRIVATE);
        long mostSig = token.getLeastSignificantBits();
        long leastSig = token.getLeastSignificantBits();
        if (mostSig != 0 && leastSig != 0) {
            preferences.edit()
                    .putLong(KEY_TOKEN_MOST_SIG, mostSig)
                    .putLong(KEY_TOKEN_LEAST_SIG, leastSig)
                    .apply();
        }
    }

    public static Intent createPre23AuthorizationIntent(@NonNull Context context) {
        Intent intent = new Intent(ShizukuApiConstants.ACTION_PRE_23_REQUEST_AUTHORIZATION)
                .putExtra(ShizukuApiConstants.EXTRA_PRE_23_IS_V3, true)
                .setPackage(ShizukuApiConstants.MANAGER_APPLICATION_ID);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            return intent;
        }
        return null;
    }
}
