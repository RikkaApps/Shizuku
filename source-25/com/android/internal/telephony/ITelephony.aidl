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
import android.os.ResultReceiver;
import android.net.Uri;
import android.service.carrier.CarrierIdentifier;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.CellInfo;
import android.telephony.IccOpenLogicalChannelResponse;
import android.telephony.ModemActivityInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.RadioAccessFamily;
import android.telephony.ServiceState;
import android.telephony.TelephonyHistogram;
import android.telephony.VisualVoicemailSmsFilterSettings;
import com.android.internal.telephony.CellNetworkScanResult;
import com.android.internal.telephony.OperatorInfo;

import java.util.List;


/**
 * Interface used to interact with the phone.  Mostly this is used by the
 * TelephonyManager class.  A few places are still using this directly.
 * Please clean them up if possible and use TelephonyManager instead.
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
     * @param callingPackage The package making the call.
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
    boolean endCallForSubscriber(int subId);

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
     * Answer the currently-ringing call on particular subId .
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
    void answerRingingCallForSubscriber(int subId);

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
     * @param callingPackage the name of the package making the call.
     * @return true if the phone state is OFFHOOK.
     */
    boolean isOffhook(String callingPackage);

    /**
     * Check if a particular subId has an active or holding call
     *
     * @param subId user preferred subId.
     * @param callingPackage the name of the package making the call.
     * @return true if the phone state is OFFHOOK.
     */
    boolean isOffhookForSubscriber(int subId, String callingPackage);

    /**
     * Check if an incoming phone call is ringing or call waiting
     * on a particular subId.
     *
     * @param subId user preferred subId.
     * @param callingPackage the name of the package making the call.
     * @return true if the phone state is RINGING.
     */
    boolean isRingingForSubscriber(int subId, String callingPackage);

    /**
     * Check if an incoming phone call is ringing or call waiting.
     * @param callingPackage the name of the package making the call.
     * @return true if the phone state is RINGING.
     */
    boolean isRinging(String callingPackage);

    /**
     * Check if the phone is idle.
     * @param callingPackage the name of the package making the call.
     * @return true if the phone state is IDLE.
     */
    boolean isIdle(String callingPackage);

    /**
     * Check if the phone is idle on a particular subId.
     *
     * @param subId user preferred subId.
     * @param callingPackage the name of the package making the call.
     * @return true if the phone state is IDLE.
     */
    boolean isIdleForSubscriber(int subId, String callingPackage);

    /**
     * Check to see if the radio is on or not.
     * @param callingPackage the name of the package making the call.
     * @return returns true if the radio is on.
     */
    boolean isRadioOn(String callingPackage);

    /**
     * Check to see if the radio is on or not on particular subId.
     * @param subId user preferred subId.
     * @param callingPackage the name of the package making the call.
     * @return returns true if the radio is on.
     */
    boolean isRadioOnForSubscriber(int subId, String callingPackage);

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
    boolean supplyPinForSubscriber(int subId, String pin);

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
    boolean supplyPukForSubscriber(int subId, String puk, String pin);

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
    int[] supplyPinReportResultForSubscriber(int subId, String pin);

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
    int[] supplyPukReportResultForSubscriber(int subId, String puk, String pin);

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
    boolean handlePinMmiForSubscriber(int subId, String dialString);

    /**
     * Toggles the radio on or off.
     */
    void toggleRadioOnOff();

    /**
     * Toggles the radio on or off on particular subId.
     * @param subId user preferred subId.
     */
    void toggleRadioOnOffForSubscriber(int subId);

    /**
     * Set the radio to on or off
     */
    boolean setRadio(boolean turnOn);

    /**
     * Set the radio to on or off on particular subId.
     * @param subId user preferred subId.
     */
    boolean setRadioForSubscriber(int subId, boolean turnOn);

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
    void updateServiceLocationForSubscriber(int subId);

    /**
     * Enable location update notifications.
     */
    void enableLocationUpdates();

    /**
     * Enable location update notifications.
     * @param subId user preferred subId.
     */
    void enableLocationUpdatesForSubscriber(int subId);

    /**
     * Disable location update notifications.
     */
    void disableLocationUpdates();

    /**
     * Disable location update notifications.
     * @param subId user preferred subId.
     */
    void disableLocationUpdatesForSubscriber(int subId);

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

    Bundle getCellLocation(String callingPkg);

    /**
     * Returns the neighboring cell information of the device.
     */
    List<NeighboringCellInfo> getNeighboringCellInfo(String callingPkg);

     int getCallState();

    /**
     * Returns the call state for a slot.
     */
     int getCallStateForSlot(int slotId);

     int getDataActivity();
     int getDataState();

    /**
     * Returns the current active phone type as integer.
     * Returns TelephonyManager.PHONE_TYPE_CDMA if RILConstants.CDMA_PHONE
     * and TelephonyManager.PHONE_TYPE_GSM if RILConstants.GSM_PHONE
     */
    int getActivePhoneType();

    /**
     * Returns the current active phone type as integer for particular slot.
     * Returns TelephonyManager.PHONE_TYPE_CDMA if RILConstants.CDMA_PHONE
     * and TelephonyManager.PHONE_TYPE_GSM if RILConstants.GSM_PHONE
     * @param slotId - slot to query.
     */
    int getActivePhoneTypeForSlot(int slotId);

    /**
     * Returns the CDMA ERI icon index to display
     * @param callingPackage package making the call.
     */
    int getCdmaEriIconIndex(String callingPackage);

    /**
     * Returns the CDMA ERI icon index to display on particular subId.
     * @param subId user preferred subId.
     * @param callingPackage package making the call.
     */
    int getCdmaEriIconIndexForSubscriber(int subId, String callingPackage);

    /**
     * Returns the CDMA ERI icon mode,
     * 0 - ON
     * 1 - FLASHING
     * @param callingPackage package making the call.
     */
    int getCdmaEriIconMode(String callingPackage);

    /**
     * Returns the CDMA ERI icon mode on particular subId,
     * 0 - ON
     * 1 - FLASHING
     * @param subId user preferred subId.
     * @param callingPackage package making the call.
     */
    int getCdmaEriIconModeForSubscriber(int subId, String callingPackage);

    /**
     * Returns the CDMA ERI text,
     * @param callingPackage package making the call.
     */
    String getCdmaEriText(String callingPackage);

    /**
     * Returns the CDMA ERI text for particular subId,
     * @param subId user preferred subId.
     * @param callingPackage package making the call.
     */
    String getCdmaEriTextForSubscriber(int subId, String callingPackage);

    /**
     * Returns true if OTA service provisioning needs to run.
     * Only relevant on some technologies, others will always
     * return false.
     */
    boolean needsOtaServiceProvisioning();

    /**
     * Sets the voicemail number for a particular subscriber.
     */
    boolean setVoiceMailNumber(int subId, String alphaTag, String number);

    /**
      * Returns the unread count of voicemails
      */
    int getVoiceMessageCount();

    /**
     * Returns the unread count of voicemails for a subId.
     * @param subId user preferred subId.
     * Returns the unread count of voicemails
     */
    int getVoiceMessageCountForSubscriber(int subId);

    oneway void setVisualVoicemailEnabled(String callingPackage,
            in PhoneAccountHandle accountHandle, boolean enabled);

    boolean isVisualVoicemailEnabled(String callingPackage,
            in PhoneAccountHandle accountHandle);

    // Not oneway, caller needs to make sure the vaule is set before receiving a SMS
    void enableVisualVoicemailSmsFilter(String callingPackage, int subId,
            in VisualVoicemailSmsFilterSettings settings);

    oneway void disableVisualVoicemailSmsFilter(String callingPackage, int subId);

    // Get settings set by the calling package
    VisualVoicemailSmsFilterSettings getVisualVoicemailSmsFilterSettings(String callingPackage,
            int subId);

    // Get settings set by the package, requires READ_PRIVILEGED_PHONE_STATE permission
    VisualVoicemailSmsFilterSettings getSystemVisualVoicemailSmsFilterSettings(String packageName,
            int subId);

    /**
     * Returns the network type for data transmission
     * Legacy call, permission-free
     */
    int getNetworkType();

    /**
     * Returns the network type of a subId.
     * @param subId user preferred subId.
     * @param callingPackage package making the call.
     */
    int getNetworkTypeForSubscriber(int subId, String callingPackage);

    /**
     * Returns the network type for data transmission
     * @param callingPackage package making the call.
     */
    int getDataNetworkType(String callingPackage);

    /**
     * Returns the data network type of a subId
     * @param subId user preferred subId.
     * @param callingPackage package making the call.
     */
    int getDataNetworkTypeForSubscriber(int subId, String callingPackage);

    /**
      * Returns the voice network type of a subId
      * @param subId user preferred subId.
      * @param callingPackage package making the call.
      * Returns the network type
      */
    int getVoiceNetworkTypeForSubscriber(int subId, String callingPackage);

    /**
     * Return true if an ICC card is present
     */
    boolean hasIccCard();

    /**
     * Return true if an ICC card is present for a subId.
     * @param slotId user preferred slotId.
     * Return true if an ICC card is present
     */
    boolean hasIccCardUsingSlotId(int slotId);

    /**
     * Return if the current radio is LTE on CDMA. This
     * is a tri-state return value as for a period of time
     * the mode may be unknown.
     *
     * @param callingPackage the name of the calling package
     * @return {@link Phone#LTE_ON_CDMA_UNKNOWN}, {@link Phone#LTE_ON_CDMA_FALSE}
     * or {@link PHone#LTE_ON_CDMA_TRUE}
     */
    int getLteOnCdmaMode(String callingPackage);

    /**
     * Return if the current radio is LTE on CDMA. This
     * is a tri-state return value as for a period of time
     * the mode may be unknown.
     *
     * @param callingPackage the name of the calling package
     * @return {@link Phone#LTE_ON_CDMA_UNKNOWN}, {@link Phone#LTE_ON_CDMA_FALSE}
     * or {@link PHone#LTE_ON_CDMA_TRUE}
     */
    int getLteOnCdmaModeForSubscriber(int subId, String callingPackage);

    /**
     * Returns the all observed cell information of the device.
     */
    List<CellInfo> getAllCellInfo(String callingPkg);

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
     * @param subId The subscription to use.
     * @param AID Application id. See ETSI 102.221 and 101.220.
     * @return an IccOpenLogicalChannelResponse object.
     */
    IccOpenLogicalChannelResponse iccOpenLogicalChannel(int subId, String AID);

    /**
     * Closes a previously opened logical channel to the ICC card.
     *
     * Input parameters equivalent to TS 27.007 AT+CCHC command.
     *
     * @param subId The subscription to use.
     * @param channel is the channel id to be closed as retruned by a
     *            successful iccOpenLogicalChannel.
     * @return true if the channel was closed successfully.
     */
    boolean iccCloseLogicalChannel(int subId, int channel);

    /**
     * Transmit an APDU to the ICC card over a logical channel.
     *
     * Input parameters equivalent to TS 27.007 AT+CGLA command.
     *
     * @param subId The subscription to use.
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
    String iccTransmitApduLogicalChannel(int subId, int channel, int cla, int instruction,
            int p1, int p2, int p3, String data);

    /**
     * Transmit an APDU to the ICC card over the basic channel.
     *
     * Input parameters equivalent to TS 27.007 AT+CSIM command.
     *
     * @param subId The subscription to use.
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
    String iccTransmitApduBasicChannel(int subId, int cla, int instruction,
            int p1, int p2, int p3, String data);

    /**
     * Returns the response APDU for a command APDU sent through SIM_IO.
     *
     * @param subId The subscription to use.
     * @param fileID
     * @param command
     * @param p1 P1 value of the APDU command.
     * @param p2 P2 value of the APDU command.
     * @param p3 P3 value of the APDU command.
     * @param filePath
     * @return The APDU response.
     */
    byte[] iccExchangeSimIO(int subId, int fileID, int command, int p1, int p2, int p3,
            String filePath);

    /**
     * Send ENVELOPE to the SIM and returns the response.
     *
     * @param subId The subscription to use.
     * @param contents  String containing SAT/USAT response in hexadecimal
     *                  format starting with command tag. See TS 102 223 for
     *                  details.
     * @return The APDU response from the ICC card, with the last 4 bytes
     *         being the status word. If the command fails, returns an empty
     *         string.
     */
    String sendEnvelopeWithStatus(int subId, String content);

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
     * @param callingPackage The package making the call.
     *
     * @return the calculated preferred network type, defined in RILConstants.java.
     */
    int getCalculatedPreferredNetworkType(String callingPackage);

    /*
     * Get the preferred network type.
     * Used for device configuration by some CDMA operators.
     *
     * @param subId the id of the subscription to query.
     * @return the preferred network type, defined in RILConstants.java.
     */
    int getPreferredNetworkType(int subId);

    /**
     * Check TETHER_DUN_REQUIRED and TETHER_DUN_APN settings, net.tethering.noprovisioning
     * SystemProperty, and config_tether_apndata to decide whether DUN APN is required for
     * tethering.
     *
     * @return 0: Not required. 1: required. 2: Not set.
     */
    int getTetherApnRequired();

    /**
     * Set the network selection mode to automatic.
     *
     * @param subId the id of the subscription to update.
     */
    void setNetworkSelectionModeAutomatic(int subId);

    /**
     * Perform a radio scan and return the list of avialble networks.
     *
     * @param subId the id of the subscription.
     * @return CellNetworkScanResult containing status of scan and networks.
     */
    CellNetworkScanResult getCellNetworkScanResults(int subId);

    /**
     * Ask the radio to connect to the input network and change selection mode to manual.
     *
     * @param subId the id of the subscription.
     * @param operatorInfo the operator to attach to.
     * @param persistSelection should the selection persist till reboot or its
     *        turned off? Will also result in notification being not shown to
     *        the user if the signal is lost.
     * @return true if the request suceeded.
     */
    boolean setNetworkSelectionModeManual(int subId, in OperatorInfo operator,
            boolean persistSelection);

    /**
     * Set the preferred network type.
     * Used for device configuration by some CDMA operators.
     *
     * @param subId the id of the subscription to update.
     * @param networkType the preferred network type, defined in RILConstants.java.
     * @return true on success; false on any failure.
     */
    boolean setPreferredNetworkType(int subId, int networkType);

    /**
     * User enable/disable Mobile Data.
     *
     * @param enable true to turn on, else false
     */
    void setDataEnabled(int subId, boolean enable);

    /**
     * Get the user enabled state of Mobile Data.
     *
     * @return true on enabled
     */
    boolean getDataEnabled(int subId);

    /**
     * Get P-CSCF address from PCO after data connection is established or modified.
     * @param apnType the apnType, "ims" for IMS APN, "emergency" for EMERGENCY APN
     * @param callingPackage The package making the call.
     */
    String[] getPcscfAddress(String apnType, String callingPackage);

    /**
     * Set IMS registration state
     */
    void setImsRegistrationState(boolean registered);

    /**
     * Return MDN string for CDMA phone.
     * @param subId user preferred subId.
     */
    String getCdmaMdn(int subId);

    /**
     * Return MIN string for CDMA phone.
     * @param subId user preferred subId.
     */
    String getCdmaMin(int subId);

    /**
     * Has the calling application been granted special privileges by the carrier.
     *
     * If any of the packages in the calling UID has carrier privileges, the
     * call will return true. This access is granted by the owner of the UICC
     * card and does not depend on the registered carrier.
     *
     * TODO: Add a link to documentation.
     *
     * @param subId The subscription to use.
     * @return carrier privilege status defined in TelephonyManager.
     */
    int getCarrierPrivilegeStatus(int subId);

    /**
     * Similar to above, but check for the package whose name is pkgName.
     */
    int checkCarrierPrivilegesForPackage(String pkgName);

    /**
     * Similar to above, but check across all phones.
     */
    int checkCarrierPrivilegesForPackageAnyPhone(String pkgName);

    /**
     * Returns list of the package names of the carrier apps that should handle the input intent
     * and have carrier privileges for the given phoneId.
     *
     * @param intent Intent that will be sent.
     * @param phoneId The phoneId on which the carrier app has carrier privileges.
     * @return list of carrier app package names that can handle the intent on phoneId.
     *         Returns null if there is an error and an empty list if there
     *         are no matching packages.
     */
    List<String> getCarrierPackageNamesForIntentAndPhone(in Intent intent, int phoneId);

    /**
     * Set the line 1 phone number string and its alphatag for the current ICCID
     * for display purpose only, for example, displayed in Phone Status. It won't
     * change the actual MSISDN/MDN. To unset alphatag or number, pass in a null
     * value.
     *
     * @param subId the subscriber that the alphatag and dialing number belongs to.
     * @param alphaTag alpha-tagging of the dailing nubmer
     * @param number The dialing number
     * @return true if the operation was executed correctly.
     */
    boolean setLine1NumberForDisplayForSubscriber(int subId, String alphaTag, String number);

    /**
     * Returns the displayed dialing number string if it was set previously via
     * {@link #setLine1NumberForDisplay}. Otherwise returns null.
     *
     * @param subId whose dialing number for line 1 is returned.
     * @param callingPackage The package making the call.
     * @return the displayed dialing number if set, or null if not set.
     */
    String getLine1NumberForDisplay(int subId, String callingPackage);

    /**
     * Returns the displayed alphatag of the dialing number if it was set
     * previously via {@link #setLine1NumberForDisplay}. Otherwise returns null.
     *
     * @param subId whose alphatag associated with line 1 is returned.
     * @param callingPackage The package making the call.
     * @return the displayed alphatag of the dialing number if set, or null if
     *         not set.
     */
    String getLine1AlphaTagForDisplay(int subId, String callingPackage);

    String[] getMergedSubscriberIds(String callingPackage);

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
     * @param subId The subscription to use.
     * @param brand The brand name to display/set.
     * @return true if the operation was executed correctly.
     */
    boolean setOperatorBrandOverride(int subId, String brand);

    /**
     * Override the roaming indicator for the current ICCID.
     *
     * Using this call, the carrier app (see #hasCarrierPrivileges) can override
     * the platform's notion of a network operator being considered roaming or not.
     * The change only affects the ICCID that was active when this call was made.
     *
     * If null is passed as any of the input, the corresponding value is deleted.
     *
     * <p>Requires that the caller have carrier privilege. See #hasCarrierPrivileges.
     *
     * @param subId for which the roaming overrides apply.
     * @param gsmRoamingList - List of MCCMNCs to be considered roaming for 3GPP RATs.
     * @param gsmNonRoamingList - List of MCCMNCs to be considered not roaming for 3GPP RATs.
     * @param cdmaRoamingList - List of SIDs to be considered roaming for 3GPP2 RATs.
     * @param cdmaNonRoamingList - List of SIDs to be considered not roaming for 3GPP2 RATs.
     * @return true if the operation was executed correctly.
     */
    boolean setRoamingOverride(int subId, in List<String> gsmRoamingList,
            in List<String> gsmNonRoamingList, in List<String> cdmaRoamingList,
            in List<String> cdmaNonRoamingList);

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

    /**
     * Set phone radio type and access technology.
     *
     * @param rafs an RadioAccessFamily array to indicate all phone's
     *        new radio access family. The length of RadioAccessFamily
     *        must equ]]al to phone count.
     */
    void setRadioCapability(in RadioAccessFamily[] rafs);

    /**
     * Get phone radio type and access technology.
     *
     * @param phoneId which phone you want to get
     * @param callingPackage the name of the package making the call
     * @return phone radio type and access technology
     */
    int getRadioAccessFamily(in int phoneId, String callingPackage);

    /**
     * Enables or disables video calling.
     *
     * @param enable Whether to enable video calling.
     */
    void enableVideoCalling(boolean enable);

    /**
     * Whether video calling has been enabled by the user.
     *
     * @param callingPackage The package making the call.
     * @return {@code true} if the user has enabled video calling, {@code false} otherwise.
     */
    boolean isVideoCallingEnabled(String callingPackage);

    /**
     * Whether the DTMF tone length can be changed.
     *
     * @return {@code true} if the DTMF tone length can be changed.
     */
    boolean canChangeDtmfToneLength();

    /**
     * Whether the device is a world phone.
     *
     * @return {@code true} if the devices is a world phone.
     */
    boolean isWorldPhone();

    /**
     * Whether the phone supports TTY mode.
     *
     * @return {@code true} if the device supports TTY mode.
     */
    boolean isTtyModeSupported();

    /**
     * Whether the phone supports hearing aid compatibility.
     *
     * @return {@code true} if the device supports hearing aid compatibility.
     */
    boolean isHearingAidCompatibilitySupported();

    /**
     * Get IMS Registration Status
     */
    boolean isImsRegistered();

    /**
     * Returns the Status of Wi-Fi Calling
     */
    boolean isWifiCallingAvailable();

    /**
     * Returns the Status of Volte
     */
    boolean isVolteAvailable();

     /**
     * Returns the Status of VT (video telephony)
     */
    boolean isVideoTelephonyAvailable();

    /**
      * Returns the unique device ID of phone, for example, the IMEI for
      * GSM and the MEID for CDMA phones. Return null if device ID is not available.
      *
      * @param callingPackage The package making the call.
      * <p>Requires Permission:
      *   {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
      */
    String getDeviceId(String callingPackage);

    /**
     * Returns the IMEI for the given slot.
     *
     * @param slotId - device slot.
     * @param callingPackage The package making the call.
     * <p>Requires Permission:
     *   {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     */
    String getImeiForSlot(int slotId, String callingPackage);

    /**
     * Returns the device software version.
     *
     * @param slotId - device slot.
     * @param callingPackage The package making the call.
     * <p>Requires Permission:
     *   {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     */
    String getDeviceSoftwareVersionForSlot(int slotId, String callingPackage);

    /**
     * Returns the subscription ID associated with the specified PhoneAccount.
     */
    int getSubIdForPhoneAccount(in PhoneAccount phoneAccount);

    void factoryReset(int subId);

    /**
     * An estimate of the users's current locale based on the default SIM.
     *
     * The returned string will be a well formed BCP-47 language tag, or {@code null}
     * if no locale could be derived.
     */
    String getLocaleFromDefaultSim();

    /**
     * Requests the modem activity info asynchronously.
     * The implementor is expected to reply with the
     * {@link android.telephony.ModemActivityInfo} object placed into the Bundle with the key
     * {@link android.telephony.TelephonyManager#MODEM_ACTIVITY_RESULT_KEY}.
     * The result code is ignored.
     */
    oneway void requestModemActivityInfo(in ResultReceiver result);

    /**
     * Get the service state on specified subscription
     * @param subId Subscription id
     * @param callingPackage The package making the call
     * @return Service state on specified subscription.
     */
    ServiceState getServiceStateForSubscriber(int subId, String callingPackage);

    /**
     * Returns the URI for the per-account voicemail ringtone set in Phone settings.
     *
     * @param accountHandle The handle for the {@link PhoneAccount} for which to retrieve the
     * voicemail ringtone.
     * @return The URI for the ringtone to play when receiving a voicemail from a specific
     * PhoneAccount.
     */
    Uri getVoicemailRingtoneUri(in PhoneAccountHandle accountHandle);

    /**
     * Returns whether vibration is set for voicemail notification in Phone settings.
     *
     * @param accountHandle The handle for the {@link PhoneAccount} for which to retrieve the
     * voicemail vibration setting.
     * @return {@code true} if the vibration is set for this PhoneAccount, {@code false} otherwise.
     */
    boolean isVoicemailVibrationEnabled(in PhoneAccountHandle accountHandle);

    /**
     * Returns a list of packages that have carrier privileges.
     */
    List<String> getPackagesWithCarrierPrivileges();

    /**
     * Return the application ID for the app type.
     *
     * @param subId the subscription ID that this request applies to.
     * @param appType the uicc app type,
     * @return Application ID for specificied app type or null if no uicc or error.
     */
    String getAidForAppType(int subId, int appType);

    /**
    * Return the Electronic Serial Number.
    *
    * Requires that the calling app has READ_PRIVILEGED_PHONE_STATE permission
    *
    * @param subId the subscription ID that this request applies to.
    * @return ESN or null if error.
    * @hide
    */
    String getEsn(int subId);

    /**
    * Return the Preferred Roaming List Version
    *
    * Requires that the calling app has READ_PRIVILEGED_PHONE_STATE permission
    * @param subId the subscription ID that this request applies to.
    * @return PRLVersion or null if error.
    * @hide
    */
    String getCdmaPrlVersion(int subId);

    /**
     * Get snapshot of Telephony histograms
     * @return List of Telephony histograms
     * Requires Permission:
     *   {@link android.Manifest.permission#MODIFY_PHONE_STATE MODIFY_PHONE_STATE}
     * Or the calling app has carrier privileges.
     */
    List<TelephonyHistogram> getTelephonyHistograms();

    /**
     * Set the allowed carrier list for slotId
     * Require system privileges. In the future we may add this to carrier APIs.
     *
     * @return The number of carriers set successfully. Should match length of
     * carriers on success.
     */
    int setAllowedCarriers(int slotId, in List<CarrierIdentifier> carriers);

    /**
     * Get the allowed carrier list for slotId.
     * Require system privileges. In the future we may add this to carrier APIs.
     *
     * @return List of {@link android.service.carrier.CarrierIdentifier}; empty list
     * means all carriers are allowed.
     */
    List<CarrierIdentifier> getAllowedCarriers(int slotId);

    /**
     * Action set from carrier signalling broadcast receivers to enable/disable metered apns
     * Permissions android.Manifest.permission.MODIFY_PHONE_STATE is required
     * @param subId the subscription ID that this action applies to.
     * @param enabled control enable or disable metered apns.
     * @hide
     */
    void carrierActionSetMeteredApnsEnabled(int subId, boolean visible);

    /**
     * Action set from carrier signalling broadcast receivers to enable/disable radio
     * Permissions android.Manifest.permission.MODIFY_PHONE_STATE is required
     * @param subId the subscription ID that this action applies to.
     * @param enabled control enable or disable radio.
     * @hide
     */
    void carrierActionSetRadioEnabled(int subId, boolean enabled);

    /**
     * Get aggregated video call data usage since boot.
     * Permissions android.Manifest.permission.READ_NETWORK_USAGE_HISTORY is required.
     * @return total data usage in bytes
     * @hide
     */
    long getVtDataUsage();

    /**
     * Policy control of data connection. Usually used when data limit is passed.
     * @param enabled True if enabling the data, otherwise disabling.
     * @param subId Subscription index
     * @hide
     */
    void setPolicyDataEnabled(boolean enabled, int subId);
}
