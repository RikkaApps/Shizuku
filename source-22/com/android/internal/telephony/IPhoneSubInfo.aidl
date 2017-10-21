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

/**
 * Interface used to retrieve various phone-related subscriber information.
 *
 */
interface IPhoneSubInfo {

    /**
     * Retrieves the unique device ID, e.g., IMEI for GSM phones.
     */
    String getDeviceId();

     /**
     * Retrieves the unique Network Access ID
     */
    String getNaiForSubscriber(int subId);

    /**
     * Retrieves the unique device ID of a phone for the device, e.g., IMEI
     * for GSM phones.
     */
    String getDeviceIdForPhone(int phoneId);

    /**
     * Retrieves the IMEI.
     */
    String getImeiForSubscriber(int subId);

    /**
     * Retrieves the software version number for the device, e.g., IMEI/SV
     * for GSM phones.
     */
    String getDeviceSvn();

    /**
     * Retrieves the software version number of a subId for the device, e.g., IMEI/SV
     * for GSM phones.
     */
    String getDeviceSvnUsingSubId(int subId);

    /**
     * Retrieves the unique sbuscriber ID, e.g., IMSI for GSM phones.
     */
    String getSubscriberId();

    /**
     * Retrieves the unique subscriber ID of a given subId, e.g., IMSI for GSM phones.
     */
    String getSubscriberIdForSubscriber(int subId);

    /**
     * Retrieves the Group Identifier Level1 for GSM phones.
     */
    String getGroupIdLevel1();

    /**
     * Retrieves the Group Identifier Level1 for GSM phones of a subId.
     */
    String getGroupIdLevel1ForSubscriber(int subId);

    /**
     * Retrieves the serial number of the ICC, if applicable.
     */
    String getIccSerialNumber();

    /**
     * Retrieves the serial number of a given subId.
     */
    String getIccSerialNumberForSubscriber(int subId);

    /**
     * Retrieves the phone number string for line 1.
     */
    String getLine1Number();

    /**
     * Retrieves the phone number string for line 1 of a subcription.
     */
    String getLine1NumberForSubscriber(int subId);


    /**
     * Retrieves the alpha identifier for line 1.
     */
    String getLine1AlphaTag();

    /**
     * Retrieves the alpha identifier for line 1 of a subId.
     */
    String getLine1AlphaTagForSubscriber(int subId);


    /**
     * Retrieves MSISDN Number.
     */
    String getMsisdn();

    /**
     * Retrieves the Msisdn of a subId.
     */
    String getMsisdnForSubscriber(int subId);

    /**
     * Retrieves the voice mail number.
     */
    String getVoiceMailNumber();

    /**
     * Retrieves the voice mail number of a given subId.
     */
    String getVoiceMailNumberForSubscriber(int subId);

    /**
     * Retrieves the complete voice mail number.
     */
    String getCompleteVoiceMailNumber();

    /**
     * Retrieves the complete voice mail number for particular subId
     */
    String getCompleteVoiceMailNumberForSubscriber(int subId);

    /**
     * Retrieves the alpha identifier associated with the voice mail number.
     */
    String getVoiceMailAlphaTag();

    /**
     * Retrieves the alpha identifier associated with the voice mail number
     * of a subId.
     */
    String getVoiceMailAlphaTagForSubscriber(int subId);

    /**
     * Returns the IMS private user identity (IMPI) that was loaded from the ISIM.
     * @return the IMPI, or null if not present or not loaded
     */
    String getIsimImpi();

    /**
     * Returns the IMS home network domain name that was loaded from the ISIM.
     * @return the IMS domain name, or null if not present or not loaded
     */
    String getIsimDomain();

    /**
     * Returns the IMS public user identities (IMPU) that were loaded from the ISIM.
     * @return an array of IMPU strings, with one IMPU per string, or null if
     *      not present or not loaded
     */
    String[] getIsimImpu();

    /**
     * Returns the IMS Service Table (IST) that was loaded from the ISIM.
     * @return IMS Service Table or null if not present or not loaded
     */
    String getIsimIst();

    /**
     * Returns the IMS Proxy Call Session Control Function(PCSCF) that were loaded from the ISIM.
     * @return an array of PCSCF strings with one PCSCF per string, or null if
     *      not present or not loaded
     */
    String[] getIsimPcscf();

    /**
     * TODO: Deprecate and remove this interface. Superceded by getIccsimChallengeResponse.
     * Returns the response of ISIM Authetification through RIL.
     * @return the response of ISIM Authetification, or null if
     *     the Authentification hasn't been successed or isn't present iphonesubinfo.
     */
    String getIsimChallengeResponse(String nonce);

    /**
     * Returns the response of the SIM application on the UICC to authentication
     * challenge/response algorithm. The data string and challenge response are
     * Base64 encoded Strings.
     * Can support EAP-SIM, EAP-AKA with results encoded per 3GPP TS 31.102.
     *
     * @param subId subscription ID to be queried
     * @param appType ICC application type (@see com.android.internal.telephony.PhoneConstants#APPTYPE_xxx)
     * @param data authentication challenge data
     * @return challenge response
     */
    String getIccSimChallengeResponse(int subId, int appType, String data);
}
