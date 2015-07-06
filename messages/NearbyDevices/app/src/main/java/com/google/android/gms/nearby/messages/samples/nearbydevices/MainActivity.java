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

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * The only activity in this sample. Sets up a retained fragment to which provides most of the
 * functionality for this sample.
 */
public class MainActivity extends AppCompatActivity {
    private static final String MAIN_FRAGMENT_TAG = "main_fragment_tag";

    private MainFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();
        mMainFragment = (MainFragment) fm.findFragmentByTag(MAIN_FRAGMENT_TAG);

        if (mMainFragment == null) {
            mMainFragment = new MainFragment();
            fm.beginTransaction().add(R.id.container, mMainFragment, MAIN_FRAGMENT_TAG).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMainFragment.finishedResolvingNearbyPermissionError();
        if (requestCode == Constants.REQUEST_RESOLVE_ERROR) {
            // User was presented with the Nearby opt-in dialog and pressed "Allow".
            if (resultCode == Activity.RESULT_OK) {
                // We track the pending subscription and publication tasks in MainFragment. Once
                // a user gives consent to use Nearby, we execute those tasks.
                mMainFragment.executePendingTasks();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User was presented with the Nearby opt-in dialog and pressed "Deny". We cannot
                // proceed with any pending subscription and publication tasks. Reset state.
                mMainFragment.resetToDefaultState();
            } else {
                Toast.makeText(this, "Failed to resolve error with code " + resultCode,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}