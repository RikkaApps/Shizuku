package android.content.pm;

import androidx.annotation.RequiresApi;

public class UserInfo {

    public static final int FLAG_MANAGED_PROFILE = 0x00000020;

    public int id;
    public String name;
    public int flags;

    @RequiresApi(30)
    public String userType;
}
