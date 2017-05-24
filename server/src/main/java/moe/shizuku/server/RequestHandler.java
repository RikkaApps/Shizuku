package moe.shizuku.server;
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
import android.os.RemoteException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import moe.shizuku.server.io.ParcelInputStream;
import moe.shizuku.server.io.ParcelOutputStream;
import moe.shizuku.server.util.ServerLog;
class RequestHandler {
private Impl impl;
RequestHandler(Impl impl) {
this.impl = impl;
}
interface Impl {
Protocol version() throws RemoteException;
Protocol authorize(long most, long least) throws RemoteException;
void sendTokenToManger(int uid) throws RemoteException;
List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, int flags) throws RemoteException;
int broadcastIntent(Intent intent, String requiredPermissions, int userId) throws RemoteException;
void forceStopPackage(String packageName, int userId) throws RemoteException;
int startActivity(Intent intent, int userId) throws RemoteException;
List<UserInfo> getUsers(boolean excludeDying) throws RemoteException;
Bitmap getUserIcon(int userHandle) throws RemoteException;
int getPackageUid(String packageName, int flags, int userId) throws RemoteException;
PackageInfo getPackageInfo(String packageName, int flags, int userId) throws RemoteException;
ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws RemoteException;
List<PackageInfo> getInstalledPackages(int flags, int userId) throws RemoteException;
List<ApplicationInfo> getInstalledApplications(int flags, int userId) throws RemoteException;
List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) throws RemoteException;
int checkPermission(String permName, String pkgName, int userId) throws RemoteException;
int checkUidPermission(String permName, int uid) throws RemoteException;
int checkServerPermission(String permName) throws RemoteException;
List<AppOpsManager.PackageOps> getOpsForPackage(int uid, String packageName, int[] ops) throws RemoteException;
void setMode(int code, int uid, String packageName, int mode) throws RemoteException;
void resetAllModes(int reqUserId, String reqPackageName) throws RemoteException;
AppOpsManager.PackageOps getOpsForPackage2(int userId, String packageName, int[] ops) throws RemoteException;
AppOpsManager.PackageOps setMode2(int[] code, int userId, String packageName, int[] mode) throws RemoteException;
AppOpsManager.PackageOps resetAllModes2(int reqUserId, String reqPackageName) throws RemoteException;
List<WrappedWifiConfiguration> getPrivilegedConfiguredNetworks() throws RemoteException;
}
void handle(Socket socket, UUID token) throws IOException, RemoteException {
ParcelInputStream is = new ParcelInputStream(socket.getInputStream());
ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());
int action = is.readInt();
if (action != Actions.authorize
&& action != Actions.version
&& action != Actions.sendTokenToManger) {
long most = is.readLong();
long least = is.readLong();
if (most != token.getMostSignificantBits()
&& least != token.getLeastSignificantBits()) {
os.writeException(new SecurityException("unauthorized"));
is.close();
os.flush();
os.close();
return;
}
}
switch (action) {
case Actions.version:
version(is, os);
break;
case Actions.authorize:
authorize(is, os);
break;
case Actions.sendTokenToManger:
sendTokenToManger(is, os);
break;
case Actions.getTasks:
getTasks(is, os);
break;
case Actions.broadcastIntent:
broadcastIntent(is, os);
break;
case Actions.forceStopPackage:
forceStopPackage(is, os);
break;
case Actions.startActivity:
startActivity(is, os);
break;
case Actions.getUsers:
getUsers(is, os);
break;
case Actions.getUserIcon:
getUserIcon(is, os);
break;
case Actions.getPackageUid:
getPackageUid(is, os);
break;
case Actions.getPackageInfo:
getPackageInfo(is, os);
break;
case Actions.getApplicationInfo:
getApplicationInfo(is, os);
break;
case Actions.getInstalledPackages:
getInstalledPackages(is, os);
break;
case Actions.getInstalledApplications:
getInstalledApplications(is, os);
break;
case Actions.queryIntentActivities:
queryIntentActivities(is, os);
break;
case Actions.checkPermission:
checkPermission(is, os);
break;
case Actions.checkUidPermission:
checkUidPermission(is, os);
break;
case Actions.checkServerPermission:
checkServerPermission(is, os);
break;
case Actions.getOpsForPackage:
getOpsForPackage(is, os);
break;
case Actions.setMode:
setMode(is, os);
break;
case Actions.resetAllModes:
resetAllModes(is, os);
break;
case Actions.getOpsForPackage2:
getOpsForPackage2(is, os);
break;
case Actions.setMode2:
setMode2(is, os);
break;
case Actions.resetAllModes2:
resetAllModes2(is, os);
break;
case Actions.getPrivilegedConfiguredNetworks:
getPrivilegedConfiguredNetworks(is, os);
break;
}
is.close();
os.flush();
os.close();
}
private void version(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
try {
Protocol result = impl.version();
os.writeNoException();
os.writeParcelable(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call version("  + ")", e);
}
}
}
private void authorize(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
long most = is.readLong();
long least = is.readLong();
try {
Protocol result = impl.authorize(most, least);
os.writeNoException();
os.writeParcelable(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call authorize(" + most + ", "+ least + ")", e);
}
}
}
private void sendTokenToManger(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
int uid = is.readInt();
try {
impl.sendTokenToManger(uid);
os.writeNoException();
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call sendTokenToManger(" + uid + ")", e);
}
}
}
private void getTasks(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
int maxNum = is.readInt();
int flags = is.readInt();
try {
List<ActivityManager.RunningTaskInfo> result = impl.getTasks(maxNum, flags);
os.writeNoException();
os.writeParcelableList(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call getTasks(" + maxNum + ", "+ flags + ")", e);
}
}
}
private void broadcastIntent(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
Intent intent = is.readParcelable(Intent.CREATOR);
String requiredPermissions = is.readString();
int userId = is.readInt();
try {
int result = impl.broadcastIntent(intent, requiredPermissions, userId);
os.writeNoException();
os.writeInt(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call broadcastIntent(" + intent + ", "+ requiredPermissions + ", "+ userId + ")", e);
}
}
}
private void forceStopPackage(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
String packageName = is.readString();
int userId = is.readInt();
try {
impl.forceStopPackage(packageName, userId);
os.writeNoException();
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call forceStopPackage(" + packageName + ", "+ userId + ")", e);
}
}
}
private void startActivity(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
Intent intent = is.readParcelable(Intent.CREATOR);
int userId = is.readInt();
try {
int result = impl.startActivity(intent, userId);
os.writeNoException();
os.writeInt(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call startActivity(" + intent + ", "+ userId + ")", e);
}
}
}
private void getUsers(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
boolean excludeDying = is.readBoolean();
try {
List<UserInfo> result = impl.getUsers(excludeDying);
os.writeNoException();
os.writeParcelableList(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call getUsers(" + excludeDying + ")", e);
}
}
}
private void getUserIcon(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
int userHandle = is.readInt();
try {
Bitmap result = impl.getUserIcon(userHandle);
os.writeNoException();
os.writeBitmap(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call getUserIcon(" + userHandle + ")", e);
}
}
}
private void getPackageUid(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
String packageName = is.readString();
int flags = is.readInt();
int userId = is.readInt();
try {
int result = impl.getPackageUid(packageName, flags, userId);
os.writeNoException();
os.writeInt(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call getPackageUid(" + packageName + ", "+ flags + ", "+ userId + ")", e);
}
}
}
private void getPackageInfo(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
String packageName = is.readString();
int flags = is.readInt();
int userId = is.readInt();
try {
PackageInfo result = impl.getPackageInfo(packageName, flags, userId);
os.writeNoException();
os.writeParcelable(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call getPackageInfo(" + packageName + ", "+ flags + ", "+ userId + ")", e);
}
}
}
private void getApplicationInfo(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
String packageName = is.readString();
int flags = is.readInt();
int userId = is.readInt();
try {
ApplicationInfo result = impl.getApplicationInfo(packageName, flags, userId);
os.writeNoException();
os.writeParcelable(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call getApplicationInfo(" + packageName + ", "+ flags + ", "+ userId + ")", e);
}
}
}
private void getInstalledPackages(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
int flags = is.readInt();
int userId = is.readInt();
try {
List<PackageInfo> result = impl.getInstalledPackages(flags, userId);
os.writeNoException();
os.writeParcelableList(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call getInstalledPackages(" + flags + ", "+ userId + ")", e);
}
}
}
private void getInstalledApplications(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
int flags = is.readInt();
int userId = is.readInt();
try {
List<ApplicationInfo> result = impl.getInstalledApplications(flags, userId);
os.writeNoException();
os.writeParcelableList(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call getInstalledApplications(" + flags + ", "+ userId + ")", e);
}
}
}
private void queryIntentActivities(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
Intent intent = is.readParcelable(Intent.CREATOR);
String resolvedType = is.readString();
int flags = is.readInt();
int userId = is.readInt();
try {
List<ResolveInfo> result = impl.queryIntentActivities(intent, resolvedType, flags, userId);
os.writeNoException();
os.writeParcelableList(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call queryIntentActivities(" + intent + ", "+ resolvedType + ", "+ flags + ", "+ userId + ")", e);
}
}
}
private void checkPermission(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
String permName = is.readString();
String pkgName = is.readString();
int userId = is.readInt();
try {
int result = impl.checkPermission(permName, pkgName, userId);
os.writeNoException();
os.writeInt(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call checkPermission(" + permName + ", "+ pkgName + ", "+ userId + ")", e);
}
}
}
private void checkUidPermission(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
String permName = is.readString();
int uid = is.readInt();
try {
int result = impl.checkUidPermission(permName, uid);
os.writeNoException();
os.writeInt(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call checkUidPermission(" + permName + ", "+ uid + ")", e);
}
}
}
private void checkServerPermission(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
String permName = is.readString();
try {
int result = impl.checkServerPermission(permName);
os.writeNoException();
os.writeInt(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call checkServerPermission(" + permName + ")", e);
}
}
}
private void getOpsForPackage(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
int uid = is.readInt();
String packageName = is.readString();
int opsLength = is.readInt();
int[] ops = null;
if (opsLength > 0) {
ops = new int[opsLength];
for (int i = 0; i < ops.length; i++) {
ops[i] = is.readInt();
}
}
try {
List<AppOpsManager.PackageOps> result = impl.getOpsForPackage(uid, packageName, ops);
os.writeNoException();
os.writeParcelableList(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call getOpsForPackage(" + uid + ", "+ packageName + ", "+ Arrays.toString(ops) + ")", e);
}
}
}
private void setMode(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
int code = is.readInt();
int uid = is.readInt();
String packageName = is.readString();
int mode = is.readInt();
try {
impl.setMode(code, uid, packageName, mode);
os.writeNoException();
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call setMode(" + code + ", "+ uid + ", "+ packageName + ", "+ mode + ")", e);
}
}
}
private void resetAllModes(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
int reqUserId = is.readInt();
String reqPackageName = is.readString();
try {
impl.resetAllModes(reqUserId, reqPackageName);
os.writeNoException();
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call resetAllModes(" + reqUserId + ", "+ reqPackageName + ")", e);
}
}
}
private void getOpsForPackage2(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
int userId = is.readInt();
String packageName = is.readString();
int opsLength = is.readInt();
int[] ops = null;
if (opsLength > 0) {
ops = new int[opsLength];
for (int i = 0; i < ops.length; i++) {
ops[i] = is.readInt();
}
}
try {
AppOpsManager.PackageOps result = impl.getOpsForPackage2(userId, packageName, ops);
os.writeNoException();
os.writeParcelable(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call getOpsForPackage2(" + userId + ", "+ packageName + ", "+ Arrays.toString(ops) + ")", e);
}
}
}
private void setMode2(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
int codeLength = is.readInt();
int[] code = null;
if (codeLength > 0) {
code = new int[codeLength];
for (int i = 0; i < code.length; i++) {
code[i] = is.readInt();
}
}
int userId = is.readInt();
String packageName = is.readString();
int modeLength = is.readInt();
int[] mode = null;
if (modeLength > 0) {
mode = new int[modeLength];
for (int i = 0; i < mode.length; i++) {
mode[i] = is.readInt();
}
}
try {
AppOpsManager.PackageOps result = impl.setMode2(code, userId, packageName, mode);
os.writeNoException();
os.writeParcelable(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call setMode2(" + Arrays.toString(code) + ", "+ userId + ", "+ packageName + ", "+ Arrays.toString(mode) + ")", e);
}
}
}
private void resetAllModes2(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
int reqUserId = is.readInt();
String reqPackageName = is.readString();
try {
AppOpsManager.PackageOps result = impl.resetAllModes2(reqUserId, reqPackageName);
os.writeNoException();
os.writeParcelable(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call resetAllModes2(" + reqUserId + ", "+ reqPackageName + ")", e);
}
}
}
private void getPrivilegedConfiguredNetworks(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
try {
List<WrappedWifiConfiguration> result = impl.getPrivilegedConfiguredNetworks();
os.writeNoException();
os.writeParcelableList(result);
} catch (Exception e) {
if (!(e instanceof IOException)) {
os.writeException(e);
ServerLog.eStack("error when call getPrivilegedConfiguredNetworks("  + ")", e);
}
}
}
}
