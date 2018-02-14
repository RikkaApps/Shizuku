/**
 * Copyright (c) 2007, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except compliance with the License.
 * You may obtaa copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to writing, software
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
   SearchableInfo getSearchableInfo(ComponentName launchActivity);
   List<SearchableInfo> getSearchablesInGlobalSearch();
   List<ResolveInfo> getGlobalSearchActivities();
   ComponentName getGlobalSearchActivity();
   ComponentName getWebSearchActivity();
   void launchAssist(Bundle args);
   boolean launchLegacyAssist(String hint, int userHandle, Bundle args);
}
