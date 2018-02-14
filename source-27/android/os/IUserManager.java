/*
**
** Copyright 2012, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except compliance with the License.
** You may obtaa copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package android.os;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserManager;
import android.content.pm.UserInfo;
import android.content.IntentSender;
import android.content.RestrictionEntry;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;

/**
 *  {@hide}
 */
interface IUserManager {

    /*
     * DO NOT MOVE - UserManager.h depends on the ordering of this function.
     */
    int getCredentialOwnerProfile(int userHandle);

    UserInfo createUser(String name, int flags);
    UserInfo createProfileForUser(String name, int flags, int userHandle,
            String[] disallowedPackages);
    UserInfo createRestrictedProfile(String name, int parentUserHandle);
    void setUserEnabled(int userHandle);
    void evictCredentialEncryptionKey(int userHandle);
    boolean removeUser(int userHandle);
    boolean removeUserEvenWhenDisallowed(int userHandle);
    void setUserName(int userHandle, String name);
    void setUserIcon(int userHandle, Bitmap icon);
    ParcelFileDescriptor getUserIcon(int userHandle);
    UserInfo getPrimaryUser();
    List<UserInfo> getUsers(boolean excludeDying);
    List<UserInfo> getProfiles(int userHandle, boolean enabledOnly);
    int[] getProfileIds(int userId, boolean enabledOnly);
    boolean canAddMoreManagedProfiles(int userHandle, boolean allowedToRemoveOne);
    UserInfo getProfileParent(int userHandle);
    boolean isSameProfileGroup(int userHandle, int otherUserHandle);
    UserInfo getUserInfo(int userHandle);
    String getUserAccount(int userHandle);
    void setUserAccount(int userHandle, String accountName);
    long getUserCreationTime(int userHandle);
    boolean isRestricted();
    boolean canHaveRestrictedProfile(int userHandle);
    int getUserSerialNumber(int userHandle);
    int getUserHandle(int userSerialNumber);
    int getUserRestrictionSource(String restrictionKey, int userHandle);
    List<UserManager.EnforcingUser> getUserRestrictionSources(String restrictionKey, int userHandle);
    Bundle getUserRestrictions(int userHandle);
    boolean hasBaseUserRestriction(String restrictionKey, int userHandle);
    boolean hasUserRestriction(String restrictionKey, int userHandle);
    void setUserRestriction(String key, boolean value, int userHandle);
    void setApplicationRestrictions(String packageName, Bundle restrictions,
            int userHandle);
    Bundle getApplicationRestrictions(String packageName);
    Bundle getApplicationRestrictionsForUser(String packageName, int userHandle);
    void setDefaultGuestRestrictions(Bundle restrictions);
    Bundle getDefaultGuestRestrictions();
    boolean markGuestForDeletion(int userHandle);
    void setQuietModeEnabled(int userHandle, boolean enableQuietMode);
    boolean isQuietModeEnabled(int userHandle);
    boolean trySetQuietModeDisabled(int userHandle, IntentSender target);
    void setSeedAccountData(int userHandle, String accountName,
            String accountType, PersistableBundle accountOptions, boolean persist);
    String getSeedAccountName();
    String getSeedAccountType();
    PersistableBundle getSeedAccountOptions();
    void clearSeedAccountData();
    boolean someUserHasSeedAccount(String accountName, String accountType);
    boolean isManagedProfile(int userId);
    boolean isDemoUser(int userId);
    UserInfo createProfileForUserEvenWhenDisallowed(String name, int flags, int userHandle,
            String[] disallowedPackages);
    boolean isUserUnlockingOrUnlocked(int userId);
    int getManagedProfileBadge(int userId);
    boolean isUserUnlocked(int userId);
    boolean isUserRunning(int userId);
    boolean isUserNameSet(int userHandle);
}
