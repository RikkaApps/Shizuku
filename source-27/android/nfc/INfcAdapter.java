/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except compliance with the License.
 * You may obtaa copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.nfc;

import android.app.PendingIntent;
import android.content.IntentFilter;
import android.nfc.BeamShareData;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.TechListParcel;
import android.nfc.IAppCallback;
import android.nfc.INfcAdapterExtras;
import android.nfc.INfcTag;
import android.nfc.INfcCardEmulation;
import android.nfc.INfcFCardEmulation;
import android.nfc.INfcUnlockHandler;
import android.nfc.ITagRemovedCallback;
import android.nfc.INfcDta;
import android.os.Bundle;

/**
 * @hide
 */
interface INfcAdapter
{
    INfcTag getNfcTagInterface();
    INfcCardEmulation getNfcCardEmulationInterface();
    INfcFCardEmulation getNfcFCardEmulationInterface();
    INfcAdapterExtras getNfcAdapterExtrasInterface(String pkg);
    INfcDta getNfcDtaInterface(String pkg);
    int getState();
    boolean disable(boolean saveState);
    boolean enable();
    boolean enableNdefPush();
    boolean disableNdefPush();
    boolean isNdefPushEnabled();
    void pausePolling(int timeoutInMs);
    void resumePolling();

    void setForegroundDispatch(PendingIntent intent,
            IntentFilter[] filters, TechListParcel techLists);
    void setAppCallback(IAppCallback callback);
    void invokeBeam();
    void invokeBeamInternal(BeamShareData shareData);

    boolean ignore(int nativeHandle, int debounceMs, ITagRemovedCallback callback);

    void dispatch(Tag tag);

    void setReaderMode (IBinder b, IAppCallback callback, int flags, Bundle extras);
    void setP2pModes(int initatorModes, int targetModes);

    void addNfcUnlockHandler(INfcUnlockHandler unlockHandler, int[] techList);
    void removeNfcUnlockHandler(INfcUnlockHandler unlockHandler);

    void verifyNfcPermission();
}
