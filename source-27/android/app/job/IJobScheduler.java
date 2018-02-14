/**
 * Copyright (C) 2014 The Android Open Source Project
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

package android.app.job;

import android.app.job.JobInfo;
import android.app.job.JobWorkItem;

 /**
  * IPC interface that supports the app-facing {@link #JobScheduler} api.
  * {@hide}
  */
interface IJobScheduler {
    int schedule(JobInfo job);
    int enqueue(JobInfo job, JobWorkItem work);
    int scheduleAsPackage(JobInfo job, String packageName, int userId, String tag);
    void cancel(int jobId);
    void cancelAll();
    List<JobInfo> getAllPendingJobs();
    JobInfo getPendingJob(int jobId);
}
