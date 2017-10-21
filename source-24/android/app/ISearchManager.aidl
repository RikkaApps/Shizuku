/**
 * Copyright (c) 2007, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app;

import android.app.SearchableInfo;
import android.app.ISearchManagerCallback;
import android.content.ComponentName;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;

/** @hide */
interface ISearchManager {
   SearchableInfo getSearchableInfo(in ComponentName launchActivity);
   List<SearchableInfo> getSearchablesInGlobalSearch();
   List<ResolveInfo> getGlobalSearchActivities();
   ComponentName getGlobalSearchActivity();
   ComponentName getWebSearchActivity();
   void launchAssist(in Bundle args);
   boolean launchLegacyAssist(String hint, int userHandle, in Bundle args);
}
