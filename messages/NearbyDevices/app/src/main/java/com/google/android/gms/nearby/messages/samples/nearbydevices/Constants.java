/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.nearby.messages.samples.nearbydevices;

class Constants {
    /**
     * Request code to use when launching the resolution activity.
     */
    static final int REQUEST_RESOLVE_ERROR = 1001;

    // The time-to-live when subscribing or publishing in this sample. Three minutes.
    static final int TTL_IN_SECONDS = 3 * 60;

    // Keys to get and set the current subscription and publication tasks using SharedPreferences.
    static final String KEY_SUBSCRIPTION_TASK = "subscription_task";
    static final String KEY_PUBLICATION_TASK = "publication_task";

    // Tasks constants.
    static final String TASK_SUBSCRIBE = "task_subscribe";
    static final String TASK_UNSUBSCRIBE = "task_unsubscribe";
    static final String TASK_PUBLISH = "task_publish";
    static final String TASK_UNPUBLISH = "task_unpublish";
    static final String TASK_NONE = "task_none";
}