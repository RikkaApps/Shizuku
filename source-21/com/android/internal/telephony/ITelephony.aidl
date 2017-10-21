/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.internal.telephony;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.IccOpenLogicalChannelResponse;
import android.telephony.NeighboringCellInfo;
import java.util.List;


/**
 * Interface used to interact with the phone.  Mostly this is used by the
 * TelephonyManager class.  A few places are still using this directly.
 * Please clean them up if possible and use TelephonyManager insteadl.
 *
 * {@hide}
 */
interface ITelephony {

    /**
     * Dial a number. This doesn't place the call. It displays
     * the Dialer screen.
     * @param number the number to be dialed. If null, this
     * would display the Dialer screen with no number pre-filled.
     */
    void dial(String number);

    /**
     * Place a call to the specified number.
     * @param number the number to be called.
     */
    void call(String callingPackage, String number);

    /**
     * End call if there is a call in progress, otherwise does nothing.
     *
     * @return whether it hung up
     */
    boolean endCall();

    /**
     * End call on particular subId or go to the Home screen
     * @param subId user preferred subId.
     * @return whether it hung up
     */
    boolean endCallForSubscriber(long subId);

    /**
     * Answer the currently-ringing call.
     *
     * If there's already a current active call, that call will be
     * automatically put on hold.  If both lines are currently in use, the
     * current active call will be ended.
     *
     * TODO: provide a flag to let the caller specify what policy to use
     * if both lines are in use.  (The current behavior is hardwired to
     * "answer incoming, end ongoing", which is how the CALL button
     * is specced to behave.)
     *
     * TODO: this should be a oneway call (especially since it's called
     * directly from the key queue thread).
     */
    void answerRingingCall();

    /**
     * Silence the ringer if an incoming call is currently ringing.
     * (If vibrating, stop the vibrator also.)
     *
     * It's safe to call this if the ringer has already been silenced, or
     * even if there's no incoming call.  (If so, this method will do nothing.)
     *
     * TODO: this should be a oneway call too (see above).
     *       (Actually *all* the methods here that return void can
     *       probably be oneway.)
     */
    void silenceRinger();

    /**
     * Check if we are in either an active or holding call
     * @return true if the phone state is OFFHOOK.
     */
    boolean isOffhook();

    /**
     * Check if a particular subId has an active or holding call
     *
     * @param subId user preferred subId.
     * @return true if the phone state is OFFHOOK.
     */
    boolean isOffhookForSubscriber(long subId);

    /**
     * Check if an incoming phone call is ringing or call waiting
     * on a particular subId.
     *
     * @param subId user preferred subId.
     * @return true if the phone state is RINGING.
     */
    boolean isRingingForSubscriber(long subId);

    /**
     * Check if an incoming phone call is ringing or call waiting.
     * @return true if the phone state is RINGING.
     */
    boolean isRinging();

    /**
     * Check if the phone is idle.
     * @return true if the phone state is IDLE.
     */
    boolean isIdle();

    /**
     * Check if the phone is idle on a particular subId.
     *
     * @param subId user preferred subId.
     * @return true if the phone state is IDLE.
     */
    boolean isIdleForSubscriber(long subId);

    /**
     * Check to see if the radio is on or not.
     * @return returns true if the radio is on.
     */
    boolean isRadioOn();

    /**
     * Check to see if the radio is on or not on particular subId.
     * @param subId user preferred subId.
     * @return returns true if the radio is on.
     */
    boolean isRadioOnForSubscriber(long subId);

    /**
     * Check if the SIM pin lock is enabled.
     * @return true if the SIM pin lock is enabled.
     */
    boolean isSimPinEnabled();

    /**
     * Supply a pin to unlock the SIM.  Blocks until a result is determined.
     * @param pin The pin to check.
     * @return whether the operation was a success.
     */
    boolean supplyPin(String pin);

    /**
     * Supply a pin to unlock the SIM for particular subId.
     * Blocks until a result is determined.
     * @param pin The pin to check.
     * @param subId user preferred subId.
     * @return whether the operation was a success.
     */
    boolean supplyPinForSubscriber(long subId, String pin);

    /**
     * Supply puk to unlock the SIM and set SIM pin to new pin.
     *  Blocks until a result is determined.
     * @param puk The puk to check.
     *        pin The new pin to be set in SIM
     * @return whether the operation was a success.
     */
    boolean supplyPuk(String puk, String pin);

    /**
     * Supply puk to unlock the SIM and set SIM pin to new pin.
     *  Blocks until a result is determined.
     * @param puk The puk to check.
     *        pin The new pin to be set in SIM
     * @param subId user preferred subId.
     * @return whether the operation was a success.
     */
    boolean supplyPukForSubscriber(long subId, String puk, String pin);

    /**
     * Supply a pin to unlock the SIM.  Blocks until a result is determined.
     * Returns a specific success/error code.
     * @param pin The pin to check.
     * @return retValue[0] = Phone.PIN_RESULT_SUCCESS on success. Otherwise error code
     *         retValue[1] = number of attempts remaining if known otherwise -1
     */
    int[] supplyPinReportResult(String pin);

    /**
     * Supply a pin to unlock the SIM.  Blocks until a result is determined.
     * Returns a specific success/error code.
     * @param pin The pin to check.
     * @return retValue[0] = Phone.PIN_RESULT_SUCCESS on success. Otherwise error code
     *         retValue[1] = number of attempts remaining if known otherwise -1
     */
    int[] supplyPinReportResultForSubscriber(long subId, String pin);

    /**
     * Supply puk to unlock the SIM and set SIM pin to new pin.
     * Blocks until a result is determined.
     * Returns a specific success/error code
     * @param puk The puk to check
     *        pin The pin to check.
     * @return retValue[0] = Phone.PIN_RESULT_SUCCESS on success. Otherwise error code
     *         retValue[1] = number of attempts remaining if known otherwise -1
     */
    int[] supplyPukReportResult(String puk, String pin);

    /**
     * Supply puk to unlock the SIM and set SIM pin to new pin.
     * Blocks until a result is determined.
     * Returns a specific success/error code
     * @param puk The puk to check
     *        pin The pin to check.
     * @return retValue[0] = Phone.PIN_RESULT_SUCCESS on success. Otherwise error code
     *         retValue[1] = number of attempts remaining if known otherwise -1
     */
    int[] supplyPukReportResultForSubscriber(long subId, String puk, String pin);

    /**
     * Handles PIN MMI commands (PIN/PIN2/PUK/PUK2), which are initiated
     * without SEND (so <code>dial</code> is not appropriate).
     *
     * @param dialString the MMI command to be executed.
     * @return true if MMI command is executed.
     */
    boolean handlePinMmi(String dialString);

    /**
     * Handles PIN MMI commands (PIN/PIN2/PUK/PUK2), which are initiated
     * without SEND (so <code>dial</code> is not appropriate) for
     * a particular subId.
     * @param dialString the MMI command to be executed.
     * @param subId user preferred subId.
     * @return true if MMI command is executed.
     */
    boolean handlePinMmiForSubscriber(long subId, String dialString);

    /**
     * Toggles the radio on or off.
     */
    void toggleRadioOnOff();

    /**
     * Toggles the radio on or off on particular subId.
     * @param subId user preferred subId.
     */
    void toggleRadioOnOffForSubscriber(long subId);

    /**
     * Set the radio to on or off
     */
    boolean setRadio(boolean turnOn);

    /**
     * Set the radio to on or off on particular subId.
     * @param subId user preferred subId.
     */
    boolean setRadioForSubscriber(long subId, boolean turnOn);

    /**
     * Set the radio to on or off unconditionally
     */
    boolean setRadioPower(boolean turnOn);

    /**
     * Request to update location information in service state
     */
    void updateServiceLocation();

    /**
     * Request to update location information for a subscrition in service state
     * @param subId user preferred subId.
     */
    void updateServiceLocationForSubscriber(long subId);

    /**
     * Enable location update notifications.
     */
    void enableLocationUpdates();

    /**
     * Enable location update notifications.
     * @param subId user preferred subId.
     */
    void enableLocationUpdatesForSubscriber(long subId);

    /**
     * Disable location update notifications.
     */
    void disableLocationUpdates();

    /**
     * Disable location update notifications.
     * @param subId user preferred subId.
     */
    void disableLocationUpdatesForSubscriber(long subId);

    /**
     * Allow mobile data connections.
     */
    boolean enableDataConnectivity();

    /**
     * Disallow mobile data connections.
     */
    boolean disableDataConnectivity();

    /**
     * Report whether data connectivity is possible.
     */
    boolean isDataConnectivityPossible();

    Bundle getCellLocation();

    /**
     * Returns the neighboring cell information of the device.
     */
    List<NeighboringCellInfo> getNeighboringCellInfo(String callingPkg);

     int getCallState();

    /**
     * Returns the call state for a subId.
     */
     int getCallStateForSubscriber(long subId);

     int getDataActivity();
     int getDataState();

    /**
     * Returns the current active phone type as integer.
     * Returns TelephonyManager.PHONE_TYPE_CDMA if RILConstants.CDMA_PHONE
     * and TelephonyManager.PHONE_TYPE_GSM if RILConstants.GSM_PHONE
     */
    int getActivePhoneType();

    /**
     * Returns the current active phone type as integer for particular subId.
     * Returns TelephonyManager.PHONE_TYPE_CDMA if RILConstants.CDMA_PHONE
     * and TelephonyManager.PHONE_TYPE_GSM if RILConstants.GSM_PHONE
     * @param subId user preferred subId.
     */
    int getActivePhoneTypeForSubscriber(long subId);

    /**
     * Returns the CDMA ERI icon index to display
     */
    int getCdmaEriIconIndex();

    /**
     * Returns the CDMA ERI icon index to display on particular subId.
     * @param subId user preferred subId.
     */
    int getCdmaEriIconIndexForSubscriber(long subId);

    /**
     * Returns the CDMA ERI icon mode,
     * 0 - ON
     * 1 - FLASHING
     */
    int getCdmaEriIconMode();

    /**
     * Returns the CDMA ERI icon mode on particular subId,
     * 0 - ON
     * 1 - FLASHING
     * @param subId user preferred subId.
     */
    int getCdmaEriIconModeForSubscriber(long subId);

    /**
     * Returns the CDMA ERI text,
     */
    String getCdmaEriText();

    /**
     * Returns the CDMA ERI text for particular subId,
     * @param subId user preferred subId.
     */
    String getCdmaEriTextForSubscriber(long subId);

    /**
     * Returns true if OTA service provisioning needs to run.
     * Only relevant on some technologies, others will always
     * return false.
     */
    boolean needsOtaServiceProvisioning();

    /**
      * Returns the unread count of voicemails
      */
    int getVoiceMessageCount();

    /**
     * Returns the unread count of voicemails for a subId.
     * @param subId user preferred subId.
     * Returns the unread count of voicemails
     */
    int getVoiceMessageCountForSubscriber(long subId);

    /**
      * Returns the network type for data transmission
      */
    int getNetworkType();

    /**
     * Returns the network type of a subId.
     * @param subId user preferred subId.
     * Returns the network type
     */
    int getNetworkTypeForSubscriber(long subId);

    /**
      * Returns the network type for data transmission
      */
    int getDataNetworkType();

    /**
      * Returns the data network type of a subId
      * @param subId user preferred subId.
      * Returns the network type
      */
    int getDataNetworkTypeForSubscriber(long subId);

    /**
      * Returns the network type for voice
      */
    int getVoiceNetworkType();

    /**
      * Returns the voice network type of a subId
      * @param subId user preferred subId.
      * Returns the network type
      */
    int getVoiceNetworkTypeForSubscriber(long subId);

    /**
     * Return true if an ICC card is present
     */
    boolean hasIccCard();

    /**
     * Return true if an ICC card is present for a subId.
     * @param slotId user preferred slotId.
     * Return true if an ICC card is present
     */
    boolean hasIccCardUsingSlotId(long slotId);

    /**
     * Return if the current radio is LTE on CDMA. This
     * is a tri-state return value as for a period of time
     * the mode may be unknown.
     *
     * @return {@link Phone#LTE_ON_CDMA_UNKNOWN}, {@link Phone#LTE_ON_CDMA_FALSE}
     * or {@link PHone#LTE_ON_CDMA_TRUE}
     */
    int getLteOnCdmaMode();

    /**
     * Return if the current radio is LTE on CDMA. This
     * is a tri-state return value as for a period of time
     * the mode may be unknown.
     *
     * @return {@link Phone#LTE_ON_CDMA_UNKNOWN}, {@link Phone#LTE_ON_CDMA_FALSE}
     * or {@link PHone#LTE_ON_CDMA_TRUE}
     */
    int getLteOnCdmaModeForSubscriber(long subId);

    /**
     * Returns the all observed cell information of the device.
     */
    List<CellInfo> getAllCellInfo();

    /**
     * Sets minimum time in milli-seconds between onCellInfoChanged
     */
    void setCellInfoListRate(int rateInMillis);

    /**
     * get default sim
     * @return sim id
     */
    int getDefaultSim();

    /**
     * Opens a logical channel to the ICC card.
     *
     * Input parameters equivalent to TS 27.007 AT+CCHO command.
     *
     * @param AID Application id. See ETSI 102.221 and 101.220.
     * @return an IccOpenLogicalChannelResponse object.
     */
    IccOpenLogicalChannelResponse iccOpenLogicalChannel(String AID);

    /**
     * Closes a previously opened logical channel to the ICC card.
     *
     * Input parameters equivalent to TS 27.007 AT+CCHC command.
     *
     * @param channel is the channel id to be closed as retruned by a
     *            successful iccOpenLogicalChannel.
     * @return true if the channel was closed successfully.
     */
    boolean iccCloseLogicalChannel(int channel);

    /**
     * Transmit an APDU to the ICC card over a logical channel.
     *
     * Input parameters equivalent to TS 27.007 AT+CGLA command.
     *
     * @param channel is the channel id to be closed as retruned by a
     *            successful iccOpenLogicalChannel.
     * @param cla Class of the APDU command.
     * @param instruction Instruction of the APDU command.
     * @param p1 P1 value of the APDU command.
     * @param p2 P2 value of the APDU command.
     * @param p3 P3 value of the APDU command. If p3 is negative a 4 byte APDU
     *            is sent to the SIM.
     * @param data Data to be sent with the APDU.
     * @return The APDU response from the ICC card with the status appended at
     *            the end.
     */
    String iccTransmitApduLogicalChannel(int channel, int cla, int instruction,
            int p1, int p2, int p3, String data);

    /**
     * Transmit an APDU to the ICC card over the basic channel.
     *
     * Input parameters equivalent to TS 27.007 AT+CSIM command.
     *
     * @param cla Class of the APDU command.
     * @param instruction Instruction of the APDU command.
     * @param p1 P1 value of the APDU command.
     * @param p2 P2 value of the APDU command.
     * @param p3 P3 value of the APDU command. If p3 is negative a 4 byte APDU
     *            is sent to the SIM.
     * @param data Data to be sent with the APDU.
     * @return The APDU response from the ICC card with the status appended at
     *            the end.
     */
    String iccTransmitApduBasicChannel(int cla, int instruction,
            int p1, int p2, int p3, String data);

    /**
     * Returns the response APDU for a command APDU sent through SIM_IO.
     *
     * @param fileID
     * @param command
     * @param p1 P1 value of the APDU command.
     * @param p2 P2 value of the APDU command.
     * @param p3 P3 value of the APDU command.
     * @param filePath
     * @return The APDU response.
     */
    byte[] iccExchangeSimIO(int fileID, int command, int p1, int p2, int p3,
            String filePath);

    /**
     * Send ENVELOPE to the SIM and returns the response.
     *
     * @param contents  String containing SAT/USAT response in hexadecimal
     *                  format starting with command tag. See TS 102 223 for
     *                  details.
     * @return The APDU response from the ICC card, with the last 4 bytes
     *         being the status word. If the command fails, returns an empty
     *         string.
     */
    String sendEnvelopeWithStatus(String content);

    /**
     * Read one of the NV items defined in {@link RadioNVItems} / {@code ril_nv_items.h}.
     * Used for device configuration by some CDMA operators.
     *
     * @param itemID the ID of the item to read.
     * @return the NV item as a String, or null on any failure.
     */
    String nvReadItem(int itemID);

    /**
     * Write one of the NV items defined in {@link RadioNVItems} / {@code ril_nv_items.h}.
     * Used for device configuration by some CDMA operators.
     *
     * @param itemID the ID of the item to read.
     * @param itemValue the value to write, as a String.
     * @return true on success; false on any failure.
     */
    boolean nvWriteItem(int itemID, String itemValue);

    /**
     * Update the CDMA Preferred Roaming List (PRL) in the radio NV storage.
     * Used for device configuration by some CDMA operators.
     *
     * @param preferredRoamingList byte array containing the new PRL.
     * @return true on success; false on any failure.
     */
    boolean nvWriteCdmaPrl(in byte[] preferredRoamingList);

    /**
     * Perform the specified type of NV config reset. The radio will be taken offline
     * and the device must be rebooted after the operation. Used for device
     * configuration by some CDMA operators.
     *
     * @param resetType the type of reset to perform (1 == factory reset; 2 == NV-only reset).
     * @return true on success; false on any failure.
     */
    boolean nvResetConfig(int resetType);

    /*
     * Get the calculated preferred network type.
     * Used for device configuration by some CDMA operators.
     *
     * @return the calculated preferred network type, defined in RILConstants.java.
     */
    int getCalculatedPreferredNetworkType();

    /*
     * Get the preferred network type.
     * Used for device configuration by some CDMA operators.
     *
     * @return the preferred network type, defined in RILConstants.java.
     */
    int getPreferredNetworkType();

    /**
     * Set the preferred network type.
     * Used for device configuration by some CDMA operators.
     *
     * @param networkType the preferred network type, defined in RILConstants.java.
     * @return true on success; false on any failure.
     */
    boolean setPreferredNetworkType(int networkType);

    /**
     * User enable/disable Mobile Data.
     *
     * @param enable true to turn on, else false
     */
    void setDataEnabled(boolean enable);

    /**
     * Get the user enabled state of Mobile Data.
     *
     * @return true on enabled
     */
    boolean getDataEnabled();

    /**
     * Get P-CSCF address from PCO after data connection is established or modified.
     * @param apnType the apnType, "ims" for IMS APN, "emergency" for EMERGENCY APN
     */
    String[] getPcscfAddress(String apnType);

    /**
     * Set IMS registration state
     */
    void setImsRegistrationState(boolean registered);

    /**
     * Return MDN string for CDMA phone.
     * @param subId user preferred subId.
     */
    String getCdmaMdn(long subId);

    /**
     * Return MIN string for CDMA phone.
     * @param subId user preferred subId.
     */
    String getCdmaMin(long subId);

    /**
     * Has the calling application been granted special privileges by the carrier.
     *
     * If any of the packages in the calling UID has carrier privileges, the
     * call will return true. This access is granted by the owner of the UICC
     * card and does not depend on the registered carrier.
     *
     * TODO: Add a link to documentation.
     *
     * @return carrier privilege status defined in TelephonyManager.
     */
    int hasCarrierPrivileges();

    /**
     * Similar to above, but check for pkg whose name is pkgname.
     */
    int checkCarrierPrivilegesForPackage(String pkgname);

    /**
     * Returns the package name of the carrier apps that should handle the input intent.
     *
     * @param packageManager PackageManager for getting receivers.
     * @param intent Intent that will be sent.
     * @return list of carrier app package names that can handle the intent.
     *         Returns null if there is an error and an empty list if there
     *         are no matching packages.
     */
    List<String> getCarrierPackageNamesForIntent(in Intent intent);

    /**
     * Set whether Android should display a simplified Mobile Network Settings UI
     * for the current ICCID.
     *
     * @param subId for which the simplified UI should be enabled or disabled.
     * @param enable true means enabling the simplified UI.
     */
    void enableSimplifiedNetworkSettingsForSubscriber(long subId, boolean enable);

    /**
     * Get whether a simplified Mobile Network Settings UI is enabled for the
     * current ICCID.
     *
     * @param subId for which the simplified UI should be enabled or disabled.
     * @return true if the simplified UI is enabled.
     */
    boolean getSimplifiedNetworkSettingsEnabledForSubscriber(long subId);

    /**
     * Set the line 1 phone number string and its alphatag for the current ICCID
     * for display purpose only, for example, displayed in Phone Status. It won't
     * change the actual MSISDN/MDN. To unset alphatag or number, pass in a null
     * value.
     *
     * @param subId the subscriber that the alphatag and dialing number belongs to.
     * @param alphaTag alpha-tagging of the dailing nubmer
     * @param number The dialing number
     */
    void setLine1NumberForDisplayForSubscriber(long subId, String alphaTag, String number);

    /**
     * Returns the displayed dialing number string if it was set previously via
     * {@link #setLine1NumberForDisplay}. Otherwise returns null.
     *
     * @param subId whose dialing number for line 1 is returned.
     * @return the displayed dialing number if set, or null if not set.
     */
    String getLine1NumberForDisplay(long subId);

    /**
     * Returns the displayed alphatag of the dialing number if it was set
     * previously via {@link #setLine1NumberForDisplay}. Otherwise returns null.
     *
     * @param subId whose alphatag associated with line 1 is returned.
     * @return the displayed alphatag of the dialing number if set, or null if
     *         not set.
     */
    String getLine1AlphaTagForDisplay(long subId);

    /**
     * Override the operator branding for the current ICCID.
     *
     * Once set, whenever the SIM is present in the device, the service
     * provider name (SPN) and the operator name will both be replaced by the
     * brand value input. To unset the value, the same function should be
     * called with a null brand value.
     *
     * <p>Requires Permission:
     *   {@link android.Manifest.permission#MODIFY_PHONE_STATE MODIFY_PHONE_STATE}
     *  or has to be carrier app - see #hasCarrierPrivileges.
     *
     * @param brand The brand name to display/set.
     * @return true if the operation was executed correctly.
     */
    boolean setOperatorBrandOverride(String brand);

    /**
     * Returns the result and response from RIL for oem request
     *
     * @param oemReq the data is sent to ril.
     * @param oemResp the respose data from RIL.
     * @return negative value request was not handled or get error
     *         0 request was handled succesfully, but no response data
     *         positive value success, data length of response
     */
    int invokeOemRilRequestRaw(in byte[] oemReq, out byte[] oemResp);

    /**
     * Check if any mobile Radios need to be shutdown.
     *
     * @return true is any mobile radio needs to be shutdown
     */
    boolean needMobileRadioShutdown();

    /**
     * Shutdown Mobile Radios
     */
    void shutdownMobileRadios();
}
