package moe.shizuku.privileged.api;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import hidden.android.app.AppOpsManager;
import hidden.android.content.pm.UserInfo;
import hidden.android.net.wifi.WrappedWifiConfiguration;
import moe.shizuku.server.Protocol;
import android.support.annotation.CallSuper;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import moe.shizuku.server.io.ParcelInputStream;
import moe.shizuku.server.io.ParcelOutputStream;
import moe.shizuku.server.Actions;
abstract class AbstractPrivilegedAPIs {
private static final int TIMEOUT = 5000;
protected UUID token;
@CallSuper
public Protocol version() {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.version);
is.readException();
Protocol _result = is.readParcelable(Protocol.CREATOR);
return _result;
} catch (IOException ignored) {
}
return null;
}
@CallSuper
public Protocol authorize(long most, long least) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.authorize);
os.writeLong(most);
os.writeLong(least);
is.readException();
Protocol _result = is.readParcelable(Protocol.CREATOR);
return _result;
} catch (IOException ignored) {
}
return null;
}
@CallSuper
public List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, int flags) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.getTasks);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeInt(maxNum);
os.writeInt(flags);
is.readException();
List<ActivityManager.RunningTaskInfo> _result = is.readParcelableList(ActivityManager.RunningTaskInfo.CREATOR);
return _result;
} catch (IOException ignored) {
}
return new ArrayList<>();
}
@CallSuper
public int broadcastIntent(Intent intent, String requiredPermissions, int userId) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.broadcastIntent);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeParcelable(intent);
os.writeString(requiredPermissions);
os.writeInt(userId);
is.readException();
int _result = is.readInt();
return _result;
} catch (IOException ignored) {
}
return -1;
}
@CallSuper
public void forceStopPackage(String packageName, int userId) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.forceStopPackage);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeString(packageName);
os.writeInt(userId);
is.readException();
} catch (IOException ignored) {
}
}
@CallSuper
public int startActivity(Intent intent, int userId) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.startActivity);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeParcelable(intent);
os.writeInt(userId);
is.readException();
int _result = is.readInt();
return _result;
} catch (IOException ignored) {
}
return -1;
}
@CallSuper
public List<UserInfo> getUsers(boolean excludeDying) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.getUsers);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeBoolean(excludeDying);
is.readException();
List<UserInfo> _result = is.readParcelableList(UserInfo.CREATOR);
return _result;
} catch (IOException ignored) {
}
return new ArrayList<>();
}
@CallSuper
public Bitmap getUserIcon(int userHandle) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.getUserIcon);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeInt(userHandle);
is.readException();
Bitmap _result = is.readBitmap();
return _result;
} catch (IOException ignored) {
}
return null;
}
@CallSuper
public int getPackageUid(String packageName, int flags, int userId) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.getPackageUid);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeString(packageName);
os.writeInt(flags);
os.writeInt(userId);
is.readException();
int _result = is.readInt();
return _result;
} catch (IOException ignored) {
}
return -1;
}
@CallSuper
public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.getPackageInfo);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeString(packageName);
os.writeInt(flags);
os.writeInt(userId);
is.readException();
PackageInfo _result = is.readParcelable(PackageInfo.CREATOR);
return _result;
} catch (IOException ignored) {
}
return null;
}
@CallSuper
public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.getApplicationInfo);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeString(packageName);
os.writeInt(flags);
os.writeInt(userId);
is.readException();
ApplicationInfo _result = is.readParcelable(ApplicationInfo.CREATOR);
return _result;
} catch (IOException ignored) {
}
return null;
}
@CallSuper
public List<PackageInfo> getInstalledPackages(int flags, int userId) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.getInstalledPackages);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeInt(flags);
os.writeInt(userId);
is.readException();
List<PackageInfo> _result = is.readParcelableList(PackageInfo.CREATOR);
return _result;
} catch (IOException ignored) {
}
return new ArrayList<>();
}
@CallSuper
public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.getInstalledApplications);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeInt(flags);
os.writeInt(userId);
is.readException();
List<ApplicationInfo> _result = is.readParcelableList(ApplicationInfo.CREATOR);
return _result;
} catch (IOException ignored) {
}
return new ArrayList<>();
}
@CallSuper
public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.queryIntentActivities);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeParcelable(intent);
os.writeString(resolvedType);
os.writeInt(flags);
os.writeInt(userId);
is.readException();
List<ResolveInfo> _result = is.readParcelableList(ResolveInfo.CREATOR);
return _result;
} catch (IOException ignored) {
}
return new ArrayList<>();
}
@CallSuper
public int checkPermission(String permName, String pkgName, int userId) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.checkPermission);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeString(permName);
os.writeString(pkgName);
os.writeInt(userId);
is.readException();
int _result = is.readInt();
return _result;
} catch (IOException ignored) {
}
return -1;
}
@CallSuper
public int checkUidPermission(String permName, int uid) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.checkUidPermission);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeString(permName);
os.writeInt(uid);
is.readException();
int _result = is.readInt();
return _result;
} catch (IOException ignored) {
}
return -1;
}
@CallSuper
public int checkServerPermission(String permName) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.checkServerPermission);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeString(permName);
is.readException();
int _result = is.readInt();
return _result;
} catch (IOException ignored) {
}
return -1;
}
@CallSuper
public List<AppOpsManager.PackageOps> getOpsForPackage(int uid, String packageName, int[] ops) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.getOpsForPackage);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeInt(uid);
os.writeString(packageName);
if (ops == null) {
os.writeInt(-1);
} else {
os.writeInt(ops.length);
for (int arg : ops) {
os.writeInt(arg);
}
}
is.readException();
List<AppOpsManager.PackageOps> _result = is.readParcelableList(AppOpsManager.PackageOps.CREATOR);
return _result;
} catch (IOException ignored) {
}
return new ArrayList<>();
}
@CallSuper
public void setMode(int code, int uid, String packageName, int mode) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.setMode);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeInt(code);
os.writeInt(uid);
os.writeString(packageName);
os.writeInt(mode);
is.readException();
} catch (IOException ignored) {
}
}
@CallSuper
public void resetAllModes(int reqUserId, String reqPackageName) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.resetAllModes);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeInt(reqUserId);
os.writeString(reqPackageName);
is.readException();
} catch (IOException ignored) {
}
}
@CallSuper
public AppOpsManager.PackageOps getOpsForPackage2(int userId, String packageName, int[] ops) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.getOpsForPackage2);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeInt(userId);
os.writeString(packageName);
if (ops == null) {
os.writeInt(-1);
} else {
os.writeInt(ops.length);
for (int arg : ops) {
os.writeInt(arg);
}
}
is.readException();
AppOpsManager.PackageOps _result = is.readParcelable(AppOpsManager.PackageOps.CREATOR);
return _result;
} catch (IOException ignored) {
}
return null;
}
@CallSuper
public AppOpsManager.PackageOps setMode2(int[] code, int userId, String packageName, int[] mode) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.setMode2);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
if (code == null) {
os.writeInt(-1);
} else {
os.writeInt(code.length);
for (int arg : code) {
os.writeInt(arg);
}
}
os.writeInt(userId);
os.writeString(packageName);
if (mode == null) {
os.writeInt(-1);
} else {
os.writeInt(mode.length);
for (int arg : mode) {
os.writeInt(arg);
}
}
is.readException();
AppOpsManager.PackageOps _result = is.readParcelable(AppOpsManager.PackageOps.CREATOR);
return _result;
} catch (IOException ignored) {
}
return null;
}
@CallSuper
public AppOpsManager.PackageOps resetAllModes2(int reqUserId, String reqPackageName) {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.resetAllModes2);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
os.writeInt(reqUserId);
os.writeString(reqPackageName);
is.readException();
AppOpsManager.PackageOps _result = is.readParcelable(AppOpsManager.PackageOps.CREATOR);
return _result;
} catch (IOException ignored) {
}
return null;
}
@CallSuper
public List<WrappedWifiConfiguration> getPrivilegedConfiguredNetworks() {
try {
Socket client = new Socket(Protocol.HOST, Protocol.PORT);
client.setSoTimeout(TIMEOUT);
ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
ParcelInputStream is = new ParcelInputStream(client.getInputStream());
os.writeInt(Actions.getPrivilegedConfiguredNetworks);
os.writeLong(token.getMostSignificantBits());
os.writeLong(token.getLeastSignificantBits());
is.readException();
List<WrappedWifiConfiguration> _result = is.readParcelableList(WrappedWifiConfiguration.CREATOR);
return _result;
} catch (IOException ignored) {
}
return new ArrayList<>();
}
}
