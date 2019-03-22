/*
 * Copyright (C) 2015 The Android Open Source Project
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

package moe.shizuku.manager.utils;

/**
 * Created by rikka on 2017/10/27.
 */

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.text.Collator;
import java.util.Comparator;

/**
 * Class to manage access to an app name comparator.
 * <p>
 * Used to sort application name in all apps view and widget tray view.
 */
public class AppNameComparator {
    private final Collator mCollator;
    private final PackageManager mPackageManager;
    private final Comparator<PackageInfo> mPackageInfoComparator;
    private final Comparator<String> mSectionNameComparator;

    public AppNameComparator(final Context context) {
        mPackageManager = context.getPackageManager();
        mCollator = Collator.getInstance();
        mPackageInfoComparator = (a, b) -> {
            // Order by the title in the current locale
            int result = compareTitles(a.applicationInfo.loadLabel(mPackageManager).toString(), b.applicationInfo.loadLabel(mPackageManager).toString());
            if (result == 0) {
                // If two apps have the same title, then order by the component name
                result = a.packageName.compareTo(b.packageName);
            }
            return result;
        };
        mSectionNameComparator = this::compareTitles;
    }

    /**
     * Returns a locale-aware comparator that will alphabetically order a list of applications.
     */
    public Comparator<PackageInfo> getPackageInfoComparator() {
        return mPackageInfoComparator;
    }

    /**
     * Returns a locale-aware comparator that will alphabetically order a list of section names.
     */
    public Comparator<String> getSectionNameComparator() {
        return mSectionNameComparator;
    }

    /**
     * Compares two titles with the same return value semantics as Comparator.
     */
    private int compareTitles(String titleA, String titleB) {
        // Ensure that we de-prioritize any titles that don't start with a linguistic letter or digit
        boolean aStartsWithLetter = (titleA.length() > 0) &&
                Character.isLetterOrDigit(titleA.codePointAt(0));
        boolean bStartsWithLetter = (titleB.length() > 0) &&
                Character.isLetterOrDigit(titleB.codePointAt(0));
        if (aStartsWithLetter && !bStartsWithLetter) {
            return -1;
        } else if (!aStartsWithLetter && bStartsWithLetter) {
            return 1;
        }

        // Order by the title in the current locale
        return mCollator.compare(titleA, titleB);
    }
}
