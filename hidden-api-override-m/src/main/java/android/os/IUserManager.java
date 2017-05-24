package android.os;

import android.graphics.Bitmap;

/**
 * Created by Rikka on 2017/5/8.
 */

public interface IUserManager {
    Bitmap getUserIcon(int userHandle);
}
