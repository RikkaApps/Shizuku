/*
 * Copyright (c) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ims.internal;

import android.app.PendingIntent;

import com.android.ims.ImsCallProfile;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsUt;

import android.os.Message;

/**
 * {@hide}
 */
interface IImsService {
    int open(int phoneId, int serviceClass, in PendingIntent incomingCallIntent,
            in IImsRegistrationListener listener);
    void close(int serviceId);
    boolean isConnected(int serviceId, int serviceType, int callType);
    boolean isOpened(int serviceId);

    /**
     * Replace existing registration listener
     *
     */
    void setRegistrationListener(int serviceId, in IImsRegistrationListener listener);

    /**
     * Add new registration listener
     */
    void addRegistrationListener(int phoneId, int serviceClass,
            in IImsRegistrationListener listener);

    ImsCallProfile createCallProfile(int serviceId, int serviceType, int callType);

    IImsCallSession createCallSession(int serviceId, in ImsCallProfile profile,
            in IImsCallSessionListener listener);
    IImsCallSession getPendingCallSession(int serviceId, String callId);

    /**
     * Ut interface for the supplementary service configuration.
     */
    IImsUt getUtInterface(int serviceId);

    /**
     * Config interface to get/set IMS service/capability parameters.
     */
    IImsConfig getConfigInterface(int phoneId);

    /**
     * Used for turning on IMS when its in OFF state.
     */
    void turnOnIms(int phoneId);

    /**
     * Used for turning off IMS when its in ON state.
     * When IMS is OFF, device will behave as CSFB'ed.
     */
    void turnOffIms(int phoneId);

    /**
     * ECBM interface for Emergency Callback mode mechanism.
     */
    IImsEcbm getEcbmInterface(int serviceId);

   /**
     * Used to set current TTY Mode.
     */
    void setUiTTYMode(int serviceId, int uiTtyMode, in Message onComplete);

    /**
     * MultiEndpoint interface for DEP.
     */
    IImsMultiEndpoint getMultiEndpointInterface(int serviceId);
}
